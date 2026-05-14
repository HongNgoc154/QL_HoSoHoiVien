package View;

import Util.*;
import dao.NhatKyDAO;
import dao.ArchiveDAO;
import database.DatabaseHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Date;
import java.util.LinkedHashSet;

/**
 * HoatDongForm – Quản lý hoạt động hội.
 * Giao diện đồng nhất: card layout, gradient header, styled table.
 *
 * Tính năng nổi bật:
 *  - Loại hoạt động: JComboBox editable, cho phép chọn từ DB hoặc nhập mới tức thì
 *  - Sau khi lưu, loại mới tự động xuất hiện trong combo filter
 */
public class HoatDongForm extends JPanel {

    private StyledTable        table;
    private DefaultTableModel  model;
    private JTextField         txtSearch;
    private JComboBox<String>  cbThang, cbNam, cbLoai, cbTrangThai;

    // ─────────────────────────────────────────────────────────────────────────
    public HoatDongForm() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(22, 26, 22, 26));
        buildUI();
        loadTable();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  BUILD UI
    // ══════════════════════════════════════════════════════════════════════════
    private void buildUI() {
        // ── HEADER ────────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 14, 0));
        header.add(
            UITheme.pageTitlePanel("Quản lý Hoạt động",
                "Tổ chức và theo dõi hoạt động hội viên"),
            BorderLayout.WEST
        );
        JButton btnAdd = UITheme.primaryButton("  ＋  Thêm mới");
        btnAdd.setPreferredSize(new Dimension(170, 36));
        btnAdd.addActionListener(e -> openForm(null));
        header.add(btnAdd, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── CENTER ────────────────────────────────────────────────────────────
        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        center.add(buildFilterCard(), BorderLayout.NORTH);
        center.add(buildTableCard(),  BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  FILTER CARD
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildFilterCard() {
        JPanel card = UITheme.cardPanel(new BorderLayout());
        card.setBorder(new EmptyBorder(10, 14, 10, 14));

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bar.setOpaque(false);

        txtSearch = new JTextField(16);
        JPanel searchWrap = UITheme.searchField(
            txtSearch,
            "Tìm theo tên hoặc loại hoạt động..."
        );

        cbThang    = makeCombo("Tháng","T1","T2","T3","T4","T5","T6","T7","T8","T9","T10","T11","T12");
        cbNam      = makeCombo("Năm","2023","2024","2025","2026");
//        cbLoai     = new JComboBox<>(); UITheme.styleCombo(cbLoai);
        cbTrangThai= makeCombo("Trạng thái","Sắp diễn ra","Đang diễn ra","Đã kết thúc");

//        refreshFilterLoai(); // nạp danh sách loại từ DB vào cbLoai

        JButton btnSearch = UITheme.primaryButton("Tìm");
        JButton btnReset  = UITheme.outlineButton("Đặt lại");
//        JButton btnExport = UITheme.outlineButton("Xuất file");
        setSize2(btnSearch, 90, 34);
        setSize2(btnReset,  96, 34);
//        setSize2(btnExport, 90, 34);

        btnSearch.addActionListener(e -> search());
        btnReset.addActionListener(e -> {

            txtSearch.setText("");

            cbThang.setSelectedIndex(0);

            cbNam.setSelectedIndex(0);

//            cbLoai.setSelectedIndex(0);

            cbTrangThai.setSelectedIndex(0);

            loadTable();
        });
//        btnExport.addActionListener(e -> ExcelExporter.exportToCSV(table, "HoatDong", this));

        bar.add(searchWrap); bar.add(cbThang); bar.add(cbNam);
//        bar.add(cbLoai); 
        bar.add(cbTrangThai);
        bar.add(btnSearch); bar.add(btnReset); //bar.add(btnExport);
        card.add(bar, BorderLayout.CENTER);
        return card;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  TABLE CARD
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildTableCard() {
        model = new DefaultTableModel(
            new String[]{"ID","Tên hoạt động","Loại","Bắt đầu","Kết thúc",
                         "Hạn đăng ký","Người tổ chức","Địa điểm","Mô tả","Trạng thái"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new StyledTable(model);
        int[] widths = {50,180,100,120,120,120,150,130,160,110};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        table.getColumnModel().getColumn(9).setCellRenderer(new StatusRenderer());

        JPanel card = UITheme.cardPanel(new BorderLayout());

        // Table header toolbar
        JPanel tblHead = new JPanel(new BorderLayout());
        tblHead.setOpaque(false);
        tblHead.setBorder(new EmptyBorder(9, 14, 9, 14));
        JLabel tblTitle = new JLabel("Danh sách hoạt động");
        tblTitle.setFont(UITheme.FONT_BOLD);
        tblTitle.setForeground(UITheme.TEXT_PRIMARY);
        tblHead.add(tblTitle, BorderLayout.WEST);

        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        acts.setOpaque(false);
        JButton btnMail = UITheme.primaryButton("Gửi mail");
        JButton btnExport = UITheme.outlineButton("Xuất Excel");
        JButton btnEdit = UITheme.outlineButton("Chỉnh sửa");
        JButton btnDel  = UITheme.dangerButton("Xóa");
        btnMail.addActionListener(e -> sendMailForSelectedActivity());
        btnExport.addActionListener(e -> ExcelExporter.exportToCSV(table, "HoatDong", this));
        btnEdit.addActionListener(e -> editSelected());
        btnDel .addActionListener(e -> deleteSelected());
        acts.add(btnMail); acts.add(btnExport); acts.add(btnEdit); acts.add(btnDel);
        tblHead.add(acts, BorderLayout.EAST);

        card.add(tblHead, BorderLayout.NORTH);
        card.add(UITheme.styledScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  LOAD / SEARCH
    // ══════════════════════════════════════════════════════════════════════════
    public void loadTable() {
        model.setRowCount(0);
        try (Connection c = DatabaseHelper.getConnection();
             ResultSet rs = c.createStatement().executeQuery(
                 "SELECT * FROM HoatDong ORDER BY id DESC")) {
            while (rs.next()) addRow(rs);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void addRow(ResultSet rs) throws Exception {
        model.addRow(new Object[]{
            rs.getInt("id"),
            rs.getString("tenHoatDong"),
            rs.getString("loaiHoatDong"),
            rs.getTimestamp("thoiGianBatDau"),
            rs.getTimestamp("thoiGianKetThuc"),
            rs.getTimestamp("hanDangKy"),
            rs.getString("nguoiToChuc"),
            rs.getString("diaDiem"),
            rs.getString("moTa"),
            computeStatus(rs.getTimestamp("thoiGianBatDau"), rs.getTimestamp("thoiGianKetThuc"))
        });
    }

    private String computeStatus(Timestamp start, Timestamp end) {
        long now = System.currentTimeMillis();
        if (start == null) return "—";
        if (now < start.getTime()) return "Sắp diễn ra";
        if (end == null || now <= end.getTime()) return "Đang diễn ra";
        return "Đã kết thúc";
    }

    private void search() {
        model.setRowCount(0);
        String kw        = txtSearch.getText().trim();
        String thang     = String.valueOf(cbThang.getSelectedItem());
        String nam       = String.valueOf(cbNam.getSelectedItem());
//        String loai      = String.valueOf(cbLoai.getSelectedItem());
        String trangThai = String.valueOf(cbTrangThai.getSelectedItem());

        StringBuilder sql = new StringBuilder("SELECT * FROM HoatDong WHERE 1=1");
        if (!kw.isEmpty()) {
            sql.append(
                " AND (tenHoatDong LIKE ? OR loaiHoatDong LIKE ?)"
            );
}
        if (!"Tháng".equals(thang))  sql.append(" AND MONTH(thoiGianBatDau)=").append(thang.replace("T",""));
        if (!"Năm".equals(nam))      sql.append(" AND YEAR(thoiGianBatDau)=").append(nam);
//        if (!"Loại".equals(loai))    sql.append(" AND loaiHoatDong=?");
        sql.append(" ORDER BY id DESC");

        try (Connection c  = DatabaseHelper.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int idx = 1;
            if (!kw.isEmpty()) {

                ps.setString(idx++, "%" + kw + "%");

                ps.setString(idx++, "%" + kw + "%");
            }
//            if (!"Loại".equals(loai)) ps.setString(idx++, loai);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String status = computeStatus(rs.getTimestamp("thoiGianBatDau"), rs.getTimestamp("thoiGianKetThuc"));
                if (!"Trạng thái".equals(trangThai) && !status.equals(trangThai)) continue;
                addRow(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tìm kiếm: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  CRUD ACTIONS
    // ══════════════════════════════════════════════════════════════════════════
    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng cần sửa!"); return; }
        openForm(row);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng cần xóa!"); return; }
        int id = (int) model.getValueAt(row, 0);
        String status = String.valueOf(model.getValueAt(row, 9));
        if (!"Đã kết thúc".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "Chỉ được xóa hoạt động đã kết thúc.");
            return;
        }
        try (Connection c = DatabaseHelper.getConnection();
             PreparedStatement p1 = c.prepareStatement("SELECT COUNT(*) FROM ThamGia WHERE idHoatDong=?")) {
            p1.setInt(1, id);
            ResultSet rs = p1.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Hoạt động đã có hội viên tham gia, không thể xóa.");
                return;
            }
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Lỗi kiểm tra: " + ex.getMessage()); return; }
        if (JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa hoạt động này?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)
                == JOptionPane.YES_OPTION) {
            try (Connection c = DatabaseHelper.getConnection();
                 PreparedStatement ps = c.prepareStatement("DELETE FROM HoatDong WHERE id=?")) {
                ArchiveDAO.archiveByQuery("Hoạt động", id, "HoatDong", "id", Session.getCurrentUserId(), "XÓA");
                ps.setInt(1, id);
                ps.executeUpdate();
                loadTable();
                refreshFilterLoai();
                NhatKyDAO.log(Session.getCurrentUserId(), "Xóa", "HoatDong", "Xóa hoạt động ID=" + id);
                JOptionPane.showMessageDialog(this, "Xóa hoạt động thành công.");
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage()); }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  OPEN FORM (Thêm / Sửa)
    //  ★ Điểm chính: cbLoaiF là JComboBox editable, kết hợp nút "＋" mở dialog
    //    nhập loại mới hoàn toàn, tránh nhầm lẫn với các loại hiện có.
    // ══════════════════════════════════════════════════════════════════════════
    private void openForm(Integer row) {
        boolean isEdit = (row != null);
        String title = isEdit ? "Chỉnh sửa hoạt động" : "Thêm hoạt động mới";
 
        JDialog dlg = FormPanel.createDialog(this, title, 880, 590);
        dlg.setLayout(new BorderLayout());
        dlg.add(FormPanel.createHeader(title), BorderLayout.NORTH);
 
        // ── BODY: 2 cột ────────────────────────────────────────────────────
        JPanel body = new JPanel(new GridLayout(1, 2, 16, 0));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(18, 22, 10, 22));
 
        // ── CỘT TRÁI: Thông tin hoạt động ───────────────────────────────────
        JPanel leftCol = new JPanel(new GridBagLayout());
        leftCol.setOpaque(false);
        GridBagConstraints lg = FormPanel.defaultGBC();
 
        // Section title trái
        lg.gridx = 0; lg.gridy = 0; lg.gridwidth = 2; lg.weightx = 1;
        leftCol.add(buildSectionTitle("📋  Thông tin hoạt động"), lg);
        lg.gridwidth = 1;
 
        // Tên hoạt động
        PlaceholderTextField txtTen = new PlaceholderTextField("Nhập tên hoạt động...");
        styleFld(txtTen);
 
        // Spinner thời gian
        JSpinner spBD  = makeDateTimeSpinner();
        JSpinner spKT  = makeDateTimeSpinner();
        JSpinner spHan = makeDateTimeSpinner();
        styleSpinner(spBD); styleSpinner(spKT); styleSpinner(spHan);
 
        // Người tổ chức & địa điểm
        JTextField txtNguoiToChuc = new PlaceholderTextField("Tên đơn vị hoặc người tổ chức...");
        styleFld(txtNguoiToChuc);
        JTextField txtDia = new PlaceholderTextField("Địa điểm tổ chức...");
        styleFld(txtDia);
 
        // Mô tả
        JTextArea txtMoTa = new JTextArea(3, 20);
        txtMoTa.setFont(UITheme.FONT_LABEL);
        txtMoTa.setLineWrap(true);
        txtMoTa.setWrapStyleWord(true);
        txtMoTa.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        JScrollPane moTaScroll = new JScrollPane(txtMoTa);
        moTaScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));
        moTaScroll.setPreferredSize(new Dimension(260, 72));
 
        addLeftRow(leftCol, lg, 1, "Tên hoạt động *", txtTen);
        addLeftRow(leftCol, lg, 2, "Thời gian bắt đầu", spBD);
        addLeftRow(leftCol, lg, 3, "Thời gian kết thúc", spKT);
        addLeftRow(leftCol, lg, 4, "Hạn đăng ký", spHan);
        addLeftRow(leftCol, lg, 5, "Người tổ chức *", txtNguoiToChuc);
        addLeftRow(leftCol, lg, 6, "Địa điểm *", txtDia);
        addLeftRow(leftCol, lg, 7, "Mô tả", moTaScroll);
 
        // Filler đẩy nội dung lên trên
        lg.gridx = 0; lg.gridy = 8; lg.gridwidth = 2;
        lg.weighty = 1; lg.fill = GridBagConstraints.BOTH;
        leftCol.add(new JLabel(), lg);
 
        // ── CỘT PHẢI: Loại hoạt động ─────────────────────────────────────
        JPanel rightCol = new JPanel(new GridBagLayout());
        rightCol.setBackground(new Color(248, 251, 255));
        rightCol.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(219, 234, 254), 1, true),
            BorderFactory.createEmptyBorder(0, 14, 14, 14)));
        GridBagConstraints rg = new GridBagConstraints();
        rg.fill = GridBagConstraints.HORIZONTAL;
        rg.insets = new Insets(6, 4, 4, 4);
        rg.weightx = 1;
 
        // Section title phải
        rg.gridx = 0; rg.gridy = 0; rg.gridwidth = 1;
        rightCol.add(buildSectionTitle("🏷️  Loại hoạt động"), rg);
 
        // Hướng dẫn
        rg.gridy = 1;
        JLabel hint = new JLabel("<html><span style='color:#6B7280;font-size:11px'>"
            + "Chọn loại có sẵn hoặc nhập tên loại mới vào ô bên dưới.</span></html>");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        rightCol.add(hint, rg);
 
        // Label
        rg.gridy = 2;
        JLabel lblLoai = new JLabel("Loại hoạt động *");
        lblLoai.setFont(UITheme.FONT_BOLD);
        lblLoai.setForeground(UITheme.TEXT_SECONDARY);
        rightCol.add(lblLoai, rg);
 
        // Combo editable
        JComboBox<String> cbLoaiF = new JComboBox<>();
        cbLoaiF.setEditable(true);
        UITheme.styleCombo(cbLoaiF);
        cbLoaiF.setPreferredSize(new Dimension(240, 36));
        Component edComp = cbLoaiF.getEditor().getEditorComponent();
        if (edComp instanceof JTextField tf) {
            tf.setFont(UITheme.FONT_LABEL);
            tf.setForeground(UITheme.TEXT_PRIMARY);
            tf.putClientProperty("JTextField.placeholderText", "Chọn hoặc gõ tên loại mới...");
        }
        loadLoaiIntoCombo(cbLoaiF);
 
        rg.gridy = 3;
        rightCol.add(cbLoaiF, rg);
 
        // Nút "+ Thêm loại mới"
        rg.gridy = 4;
        JButton btnNewLoai = buildAddLoaiButton();
        btnNewLoai.addActionListener(e -> showAddLoaiDialog(dlg, cbLoaiF));
        rightCol.add(btnNewLoai, rg);
 
        // Divider
        rg.gridy = 5;
        rightCol.add(buildDivider(), rg);
 
        // Label danh sách loại
        rg.gridy = 6;
        JLabel lblExisting = new JLabel("Các loại hoạt động hiện có:");
        lblExisting.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblExisting.setForeground(UITheme.TEXT_SECONDARY);
        rightCol.add(lblExisting, rg);
 
        // Panel chip badges hiện các loại từ DB
        rg.gridy = 7;
        JPanel chipPanel = buildChipPanel(cbLoaiF);
        rightCol.add(chipPanel, rg);
 
        // Ghi chú
        rg.gridy = 8;
        JLabel note = new JLabel("<html><span style='color:#9CA3AF;font-size:10px'>"
            + "💡 Nhấn vào badge để chọn nhanh loại hoạt động.</span></html>");
        note.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        rightCol.add(note, rg);
 
        // Filler
        rg.gridy = 9; rg.weighty = 1; rg.fill = GridBagConstraints.BOTH;
        rightCol.add(new JLabel(), rg);
 
        // ── Đổ dữ liệu nếu Edit ─────────────────────────────────────────────
        if (isEdit) {
            txtTen        .setText(str(model.getValueAt(row, 1)));
            cbLoaiF       .setSelectedItem(str(model.getValueAt(row, 2)));
            spBD  .setValue(model.getValueAt(row, 3) != null ? model.getValueAt(row, 3) : new Date());
            spKT  .setValue(model.getValueAt(row, 4) != null ? model.getValueAt(row, 4) : new Date());
            spHan .setValue(model.getValueAt(row, 5) != null ? model.getValueAt(row, 5) : new Date());
            txtNguoiToChuc.setText(str(model.getValueAt(row, 6)));
            txtDia        .setText(str(model.getValueAt(row, 7)));
            txtMoTa       .setText(str(model.getValueAt(row, 8)));
        }
 
        // ── Ghép 2 cột vào body ──────────────────────────────────────────────
        body.add(leftCol);
        body.add(rightCol);
 
        JScrollPane bodyScroll = new JScrollPane(body);
        bodyScroll.setBorder(BorderFactory.createEmptyBorder());
        bodyScroll.getViewport().setBackground(Color.WHITE);
        dlg.add(bodyScroll, BorderLayout.CENTER);
 
        // ── Footer ───────────────────────────────────────────────────────────
        JButton btnCancel = UITheme.outlineButton("Hủy");
        JButton btnSave   = UITheme.primaryButton(isEdit ? "  ✔  Cập nhật  " : "  ✔  Lưu mới  ");
        setSize2(btnCancel, 100, 36);
        setSize2(btnSave,   140, 36);
        btnCancel.addActionListener(e -> dlg.dispose());
 
        btnSave.addActionListener(e -> {
            String ten    = txtTen.getText().trim();
            String loaiRaw = cbLoaiF.getEditor().getItem() != null
                             ? cbLoaiF.getEditor().getItem().toString().trim()
                             : "";
            String loaiNorm = loaiRaw.isEmpty() ? loaiRaw
                              : loaiRaw.substring(0, 1).toUpperCase() + loaiRaw.substring(1);
            Date   bd  = (Date) spBD.getValue();
            Date   kt  = (Date) spKT.getValue();
            Date   han = (Date) spHan.getValue();
            String nguoiToChuc = txtNguoiToChuc.getText().trim();
            String dia  = txtDia.getText().trim();
            String moTa = txtMoTa.getText().trim();
 
            if (ten.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng nhập tên hoạt động.");
                return;
            }
            if (loaiNorm.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng chọn hoặc nhập loại hoạt động.");
                return;
            }
            if (nguoiToChuc.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng nhập người/đơn vị tổ chức.");
                return;
            }
            if (dia.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng nhập địa điểm tổ chức.");
                return;
            }
 
            try (Connection c = DatabaseHelper.getConnection()) {
                if (isEdit) {
                    int id = (int) model.getValueAt(row, 0);
                    PreparedStatement ps = c.prepareStatement(
                        "UPDATE HoatDong SET tenHoatDong=?, loaiHoatDong=?, thoiGianBatDau=?,"
                      + " thoiGianKetThuc=?, hanDangKy=?, nguoiToChuc=?, diaDiem=?, moTa=?"
                      + " WHERE id=?");
                    ps.setString(1, ten);
                    ps.setString(2, loaiNorm);
                    ps.setTimestamp(3, new Timestamp(bd.getTime()));
                    ps.setTimestamp(4, new Timestamp(kt.getTime()));
                    ps.setTimestamp(5, new Timestamp(han.getTime()));
                    ps.setString(6, nguoiToChuc);
                    ps.setString(7, dia);
                    ps.setString(8, moTa);
                    ps.setInt(9, id);
                    ps.executeUpdate();
                    NhatKyDAO.log(Session.getCurrentUserId(), "Sửa", "HoatDong",
                        "Sửa hoạt động ID=" + id);
                } else {
                    PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO HoatDong(tenHoatDong, loaiHoatDong, thoiGianBatDau,"
                      + " thoiGianKetThuc, hanDangKy, nguoiToChuc, diaDiem, moTa, trangThai)"
                      + " VALUES(?,?,?,?,?,?,?,?,?)");
                    ps.setString(1, ten);
                    ps.setString(2, loaiNorm);
                    ps.setTimestamp(3, new Timestamp(bd.getTime()));
                    ps.setTimestamp(4, new Timestamp(kt.getTime()));
                    ps.setTimestamp(5, new Timestamp(han.getTime()));
                    ps.setString(6, nguoiToChuc);
                    ps.setString(7, dia);
                    ps.setString(8, moTa);
                    ps.setString(9, computeStatus(new Timestamp(bd.getTime()), new Timestamp(kt.getTime())));
                    ps.executeUpdate();
                    NhatKyDAO.log(Session.getCurrentUserId(), "Thêm", "HoatDong",
                        "Thêm hoạt động: " + ten);
                }
                loadTable();
                refreshFilterLoai();
                dlg.dispose();
                JOptionPane.showMessageDialog(this, "Lưu hoạt động thành công.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Lỗi: " + ex.getMessage());
            }
        });
 
        dlg.add(FormPanel.createFooter(btnCancel, btnSave), BorderLayout.SOUTH);
        dlg.setVisible(true);
    }
 
    // ── Helper: addLeftRow (gắn label + field vào cột trái) ─────────────────
    private void addLeftRow(JPanel panel, GridBagConstraints gc,
                            int y, String label, JComponent field) {
        gc.gridx = 0; gc.gridy = y; gc.weightx = 0; gc.gridwidth = 1;
        gc.weighty = 0; gc.fill = GridBagConstraints.HORIZONTAL;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_SECONDARY);
        lbl.setPreferredSize(new Dimension(148, 28));
        panel.add(lbl, gc);
        gc.gridx = 1; gc.weightx = 1;
        panel.add(field, gc);
    }
 
    // ── Helper: tiêu đề section ─────────────────────────────────────────────
    private JLabel buildSectionTitle(String text) {
        JLabel lbl = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.decode("#1359B9"));
                g2.setStroke(new BasicStroke(2f));
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2.dispose();
            }
        };
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(Color.decode("#1359B9"));
        lbl.setBorder(new EmptyBorder(2, 0, 8, 0));
        lbl.setPreferredSize(new Dimension(0, 32));
        return lbl;
    }
 
    // ── Helper: style spinner ────────────────────────────────────────────────
    private void styleSpinner(JSpinner sp) {
        sp.setFont(UITheme.FONT_LABEL);
        sp.setPreferredSize(new Dimension(260, 34));
        sp.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));
    }
 
    // ── Helper: nút thêm loại mới ────────────────────────────────────────────
    private JButton buildAddLoaiButton() {
        JButton btn = new JButton("＋  Thêm loại mới");
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.decode("#1359B9"));
        btn.setBackground(Color.decode("#EEF4FF"));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#9FE4FB"), 1, true),
            BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(0, 36));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(Color.decode("#DBEAFE")); }
            public void mouseExited (MouseEvent e) { btn.setBackground(Color.decode("#EEF4FF")); }
        });
        return btn;
    }
 
    // ── Helper: divider ngang ────────────────────────────────────────────────
    private JPanel buildDivider() {
        JPanel div = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(new Color(219, 234, 254));
                g.fillRect(0, getHeight() / 2 - 1, getWidth(), 1);
            }
        };
        div.setOpaque(false);
        div.setPreferredSize(new Dimension(0, 14));
        return div;
    }
 
    // ── Helper: chip badges loại hoạt động hiện có ───────────────────────────
    private JPanel buildChipPanel(JComboBox<String> targetCombo) {
        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        wrap.setOpaque(false);
        wrap.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(219, 234, 254), 1, true),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)));
 
        try (Connection c = DatabaseHelper.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT DISTINCT LTRIM(RTRIM(loaiHoatDong)) loai FROM HoatDong "
               + "WHERE loaiHoatDong IS NOT NULL AND LTRIM(RTRIM(loaiHoatDong))<>'' "
               + "ORDER BY loai");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String loai = rs.getString("loai");
                if (loai == null || loai.isEmpty()) continue;
                JButton chip = buildChip(loai, targetCombo);
                wrap.add(chip);
            }
        } catch (Exception ignored) {}
 
        if (wrap.getComponentCount() == 0) {
            JLabel empty = new JLabel("Chưa có loại nào. Thêm loại mới bên trên.");
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            empty.setForeground(new Color(156, 163, 175));
            wrap.add(empty);
        }
 
        wrap.setPreferredSize(new Dimension(0, 82));
        return wrap;
    }
 
    // ── Helper: 1 chip badge ─────────────────────────────────────────────────
    private JButton buildChip(String label, JComboBox<String> targetCombo) {
    JButton chip = new JButton(label) {
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Object sel = targetCombo.getEditor().getItem();
            boolean selected = label.equals(sel != null ? sel.toString().trim() : "");
            if (selected) {
                g2.setColor(Color.decode("#1359B9"));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            } else {
                g2.setColor(Color.decode("#EEF4FF"));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(Color.decode("#9FE4FB"));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            }
            g2.dispose();
            // ★ Đổi màu chữ TRƯỚC khi super vẽ text
            setForeground(selected ? Color.WHITE : Color.decode("#1359B9"));
            super.paintComponent(g);
        }
    };
    chip.setFont(new Font("Segoe UI", Font.BOLD, 11));
    chip.setContentAreaFilled(false);
    chip.setBorderPainted(false);
    chip.setFocusPainted(false);
    chip.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    chip.setBorder(new EmptyBorder(4, 12, 4, 12));
    chip.setForeground(Color.decode("#1359B9")); // mặc định ban đầu

    chip.addActionListener(e -> {
        targetCombo.setSelectedItem(label);
        targetCombo.getEditor().setItem(label);
        chip.repaint();
    });

    targetCombo.addItemListener(evt -> chip.repaint());
    Component edComp = targetCombo.getEditor().getEditorComponent();
    if (edComp instanceof JTextField tf) {
        tf.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) { chip.repaint(); }
        });
    }
    return chip;
}

    // ══════════════════════════════════════════════════════════════════════════
    //  LOẠI HOẠT ĐỘNG PANEL
    //  Gồm: JComboBox editable (chọn hoặc tự gõ) + nút "＋ Loại mới" (dialog)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Tạo panel chứa combo loại (editable) và nút thêm loại mới.
     * Trả về JPanel; dùng getLoaiCombo() để lấy lại combo reference.
     */
//    private JPanel buildLoaiPanel(JDialog parentDlg) {
//        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
//        panel.setOpaque(false);
//
//        // ── Combo editable ──────────────────────────────────────────────────
//        JComboBox<String> cb = new JComboBox<>();
//        cb.setEditable(true);
//        UITheme.styleCombo(cb);
//        cb.setPreferredSize(new Dimension(220, 34));
//
//        // Style vùng text nhập
//        Component editorComp = cb.getEditor().getEditorComponent();
//        if (editorComp instanceof JTextField tf) {
//            tf.setFont(UITheme.FONT_LABEL);
//            tf.setForeground(UITheme.TEXT_PRIMARY);
//            tf.putClientProperty("JTextField.placeholderText", "Chọn hoặc nhập loại mới...");
//        }
//
//        // Nạp danh sách loại từ DB
//        loadLoaiIntoCombo(cb);
//
//        // ── Nút "＋ Loại mới" ──────────────────────────────────────────────
//        JButton btnNewLoai = new JButton("＋ Loại mới");
//        btnNewLoai.setFont(new Font("Segoe UI", Font.BOLD, 12));
//        btnNewLoai.setForeground(Color.decode("#1359B9"));
//        btnNewLoai.setBackground(Color.decode("#EEF4FF"));
//        btnNewLoai.setBorder(BorderFactory.createCompoundBorder(
//            BorderFactory.createLineBorder(Color.decode("#9FE4FB"), 1, true),
//            BorderFactory.createEmptyBorder(5, 12, 5, 12)));
//        btnNewLoai.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//        btnNewLoai.setFocusPainted(false);
//        btnNewLoai.setPreferredSize(new Dimension(120, 34));
//
//        // Hover effect
//        btnNewLoai.addMouseListener(new MouseAdapter() {
//            public void mouseEntered(MouseEvent e) {
//                btnNewLoai.setBackground(Color.decode("#D6EAFF"));
//            }
//            public void mouseExited(MouseEvent e) {
//                btnNewLoai.setBackground(Color.decode("#EEF4FF"));
//            }
//        });
//
//        btnNewLoai.addActionListener(e -> showAddLoaiDialog(parentDlg, cb));
//
//        panel.add(cb);
//        panel.add(btnNewLoai);
//
//        // Lưu combo vào clientProperty để getLoaiCombo() lấy lại
//        panel.putClientProperty("loaiCombo", cb);
//        return panel;
//    }

    /** Lấy lại JComboBox từ panel đã tạo bởi buildLoaiPanel(). */
    @SuppressWarnings("unchecked")
    private JComboBox<String> getLoaiCombo(JPanel panel) {
        return (JComboBox<String>) panel.getClientProperty("loaiCombo");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  DIALOG THÊM LOẠI HOẠT ĐỘNG MỚI
    // ══════════════════════════════════════════════════════════════════════════
    private void showAddLoaiDialog(JDialog parentDlg, JComboBox<String> targetCombo) {
        JDialog addDlg = new JDialog(parentDlg, "Thêm loại hoạt động mới", true);
        addDlg.setSize(440, 220);
        addDlg.setLocationRelativeTo(parentDlg);
        addDlg.setResizable(false);
        addDlg.setLayout(new BorderLayout());
        addDlg.getContentPane().setBackground(Color.WHITE);
 
        // Header
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(Color.decode("#1359B9"));
        hdr.setBorder(new EmptyBorder(13, 20, 13, 20));
        JLabel lblTitle = new JLabel("🏷️  Thêm loại hoạt động mới");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(Color.WHITE);
        hdr.add(lblTitle);
        addDlg.add(hdr, BorderLayout.NORTH);
 
        // Body
        JPanel body2 = new JPanel(new GridBagLayout());
        body2.setBackground(Color.WHITE);
        body2.setBorder(new EmptyBorder(20, 24, 8, 24));
        GridBagConstraints bg = new GridBagConstraints();
        bg.fill = GridBagConstraints.HORIZONTAL;
        bg.insets = new Insets(4, 4, 4, 4);
 
        // Label + hint
        bg.gridx = 0; bg.gridy = 0; bg.gridwidth = 2; bg.weightx = 1;
        JLabel hintLbl = new JLabel("<html><span style='color:#6B7280'>Tên loại sẽ xuất hiện trong combo khi lưu hoạt động.</span></html>");
        hintLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        body2.add(hintLbl, bg);
 
        bg.gridy = 1; bg.gridwidth = 1; bg.weightx = 0;
        JLabel lbl2 = new JLabel("Tên loại *");
        lbl2.setFont(UITheme.FONT_BOLD);
        lbl2.setForeground(UITheme.TEXT_SECONDARY);
        lbl2.setPreferredSize(new Dimension(90, 34));
        body2.add(lbl2, bg);
 
        bg.gridx = 1; bg.weightx = 1;
        PlaceholderTextField txtLoai = new PlaceholderTextField("VD: Hội thảo, Workshop, Cuộc thi...");
        txtLoai.setFont(UITheme.FONT_LABEL);
        txtLoai.setPreferredSize(new Dimension(260, 34));
        txtLoai.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        // Focus border
        txtLoai.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                txtLoai.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.decode("#1359B9"), 1, true),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                txtLoai.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            }
        });
        body2.add(txtLoai, bg);
        addDlg.add(body2, BorderLayout.CENTER);
 
        // Footer
        JPanel foot2 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        foot2.setBackground(new Color(248, 251, 255));
        foot2.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));
 
        JButton btnHuy  = UITheme.outlineButton("Hủy");
        JButton btnThem = UITheme.primaryButton("＋  Thêm loại");
        setSize2(btnHuy, 90, 34);
        setSize2(btnThem, 120, 34);
 
        btnHuy.addActionListener(ev -> addDlg.dispose());
 
        Runnable doAdd = () -> {
            String ten = txtLoai.getText().trim();
            if (ten.isEmpty()) {
                JOptionPane.showMessageDialog(addDlg, "Vui lòng nhập tên loại hoạt động.");
                return;
            }
            String tenNorm = ten.substring(0, 1).toUpperCase() + ten.substring(1);
 
            // Kiểm tra đã tồn tại
            boolean exists = false;
            for (int i = 0; i < targetCombo.getItemCount(); i++) {
                if (tenNorm.equalsIgnoreCase(targetCombo.getItemAt(i))) {
                    targetCombo.setSelectedIndex(i);
                    targetCombo.getEditor().setItem(targetCombo.getItemAt(i));
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                targetCombo.addItem(tenNorm);
                targetCombo.setSelectedItem(tenNorm);
                targetCombo.getEditor().setItem(tenNorm);
            } else {
                JOptionPane.showMessageDialog(addDlg,
                    "Loại \"" + tenNorm + "\" đã tồn tại.\nĐã tự động chọn loại đó.");
            }
            addDlg.dispose();
        };
 
        btnThem.addActionListener(ev -> doAdd.run());
        txtLoai.addActionListener(ev -> doAdd.run()); // nhấn Enter
 
        foot2.add(btnHuy);
        foot2.add(btnThem);
        addDlg.add(foot2, BorderLayout.SOUTH);
        addDlg.setVisible(true);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  SEND MAIL
    // ══════════════════════════════════════════════════════════════════════════
    private void sendMailForSelectedActivity() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hoạt động cần gửi mail.");
            return;
        }
        int id = (int) model.getValueAt(row, 0);
        Timestamp han = (Timestamp) model.getValueAt(row, 5);
        if (han != null && han.before(new Timestamp(System.currentTimeMillis()))) {
            JOptionPane.showMessageDialog(this, "Hoạt động đã hết hạn đăng ký.");
            return;
        }

        try (Connection c = DatabaseHelper.getConnection()) {
            PreparedStatement chk = c.prepareStatement(
                "SELECT COUNT(*) FROM NhatKyHeThong WHERE doiTuong=N'HoatDong' AND hanhDong=N'Gửi mail' AND moTa LIKE ?");
            chk.setString(1, "%MAIL_HD_" + id + "%");
            ResultSet rsChk = chk.executeQuery(); rsChk.next();
            if (rsChk.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Hoạt động này đã gửi mail trước đó (chống gửi trùng).");
                return;
            }

            JDialog loading = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Đang gửi mail", true);
            loading.add(new JLabel("Đang gửi mail thông báo...", SwingConstants.CENTER));
            loading.setSize(280, 90); loading.setLocationRelativeTo(this);

            SwingWorker<Integer, Void> worker = new SwingWorker<>() {
                protected Integer doInBackground() throws Exception {
                    int sent = 0;
                    String sql = "SELECT email, tenHoiVien FROM HoiVien WHERE trangThai=N'Hoạt động' AND email IS NOT NULL AND LTRIM(RTRIM(email))<>''";
                    try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            EmailSender.send(rs.getString("email"), "Thông báo hoạt động mới",
                                buildMailBody(rs.getString("tenHoiVien"), row));
                            sent++;
                        }
                    }
                    return sent;
                }
                protected void done() {
                    loading.dispose();
                    try {
                        int sent = get();
                        NhatKyDAO.log(Session.getCurrentUserId(), "Gửi mail", "HoatDong",
                            "MAIL_HD_" + id + " - Gửi mail hoạt động cho " + sent + " hội viên hoạt động");
                        JOptionPane.showMessageDialog(HoatDongForm.this,
                            "Gửi mail thành công cho " + sent + " hội viên.");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(HoatDongForm.this, "Gửi mail thất bại: " + ex.getMessage());
                    }
                }
            };
            worker.execute();
            loading.setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi gửi mail: " + ex.getMessage());
        }
    }

    private String buildMailBody(String tenHoiVien, int row) {
        return "Kính gửi " + tenHoiVien + ",\n\n"
            + "Bạn có hoạt động mới:\n"
            + "- Tên hoạt động : " + str(model.getValueAt(row, 1)) + "\n"
            + "- Loại          : " + str(model.getValueAt(row, 2)) + "\n"
            + "- Bắt đầu       : " + str(model.getValueAt(row, 3)) + "\n"
            + "- Kết thúc      : " + str(model.getValueAt(row, 4)) + "\n"
            + "- Hạn đăng ký   : " + str(model.getValueAt(row, 5)) + "\n"
            + "- Người tổ chức : " + str(model.getValueAt(row, 6)) + "\n"
            + "- Địa điểm      : " + str(model.getValueAt(row, 7)) + "\n"
            + "- Mô tả         : " + str(model.getValueAt(row, 8)) + "\n\n"
            + "Đăng ký tham gia tại:\n"
            + "http://localhost:8080/tham-gia?hoatDongId="
            + model.getValueAt(row, 0)
            + "\n\nTrân trọng.";
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Nạp các loại duy nhất từ DB vào combo (dùng cho cả filter và form).
     */
    private void loadLoaiIntoCombo(JComboBox<String> combo) {
        LinkedHashSet<String> types = new LinkedHashSet<>();
        try (Connection c = DatabaseHelper.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT DISTINCT LTRIM(RTRIM(loaiHoatDong)) loai FROM HoatDong " +
                 "WHERE loaiHoatDong IS NOT NULL AND LTRIM(RTRIM(loaiHoatDong))<>'' ORDER BY loai");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) types.add(rs.getString("loai"));
        } catch (Exception ignored) {}

        combo.removeAllItems();
        for (String t : types) combo.addItem(t);
    }

    /**
     * Refresh cbLoai (filter panel) – giữ lại lựa chọn hiện tại nếu có.
     */
    private void refreshFilterLoai() {
        Object selected = cbLoai.getSelectedItem();
        cbLoai.removeAllItems();
        cbLoai.addItem("Loại");
        LinkedHashSet<String> types = new LinkedHashSet<>();
        try (Connection c = DatabaseHelper.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT DISTINCT LTRIM(RTRIM(loaiHoatDong)) loai FROM HoatDong " +
                 "WHERE loaiHoatDong IS NOT NULL AND LTRIM(RTRIM(loaiHoatDong))<>'' ORDER BY loai");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) types.add(rs.getString("loai"));
        } catch (Exception ignored) {}
        for (String t : types) cbLoai.addItem(t);
        if (selected != null) cbLoai.setSelectedItem(selected);
    }

    /**
     * Lấy giá trị từ JComboBox editable (ưu tiên text đang nhập trong editor).
     */
    private String getLoaiValue(JComboBox<String> cb) {
        Object item = cb.getEditor().getItem();
        return item == null ? "" : item.toString().trim();
    }

    private JSpinner makeDateTimeSpinner() {

    SpinnerDateModel md =
        new SpinnerDateModel(new Date(), null, null, java.util.Calendar.MINUTE);

    JSpinner sp = new JSpinner(md);

    JSpinner.DateEditor editor =
        new JSpinner.DateEditor(sp, "dd/MM/yyyy HH:mm");

    sp.setEditor(editor);

    // cho phép nhập trực tiếp
    JFormattedTextField txt = editor.getTextField();

    txt.setEditable(true);

    // căn giữa đẹp hơn
    txt.setHorizontalAlignment(JTextField.CENTER);

    // cho phép focus
    txt.setFocusable(true);

    sp.setPreferredSize(new Dimension(260, 34));

    return sp;
}

    private void styleFld(JTextField f) { f.setPreferredSize(new Dimension(260, 34)); }

    private JComboBox<String> makeCombo(String... items) {
        JComboBox<String> cb = new JComboBox<>(items);
        UITheme.styleCombo(cb);
        return cb;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  HELPER GIỮ LẠI: setSize2 (đã có trong class gốc)
    // ══════════════════════════════════════════════════════════════════════════
    // Nếu class gốc chưa có setSize2, thêm vào:
    private void setSize2(JButton btn, int w, int h) {
        btn.setPreferredSize(new Dimension(w, h));
        btn.setMinimumSize(new Dimension(w, h));
        btn.setMaximumSize(new Dimension(w, h));
    }

    private String str(Object o) { return o == null ? "" : o.toString(); }

    // ══════════════════════════════════════════════════════════════════════════
    //  STATUS RENDERER
    // ══════════════════════════════════════════════════════════════════════════
    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                String st = value == null ? "" : value.toString();
                switch (st) {
                    case "Sắp diễn ra"  -> lbl.setForeground(new Color(25, 118, 210));
                    case "Đang diễn ra" -> lbl.setForeground(new Color(46, 125, 50));
                    default             -> lbl.setForeground(new Color(120, 120, 120));
                }
            }
            return lbl;
        }
    }
}