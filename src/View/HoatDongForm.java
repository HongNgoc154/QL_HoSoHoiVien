package View;

import Util.*;
import dao.NhatKyDAO;
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
        JPanel searchWrap = UITheme.searchField(txtSearch, "Tìm theo tên hoạt động...");

        cbThang    = makeCombo("Tháng","T1","T2","T3","T4","T5","T6","T7","T8","T9","T10","T11","T12");
        cbNam      = makeCombo("Năm","2023","2024","2025","2026");
        cbLoai     = new JComboBox<>(); UITheme.styleCombo(cbLoai);
        cbTrangThai= makeCombo("Trạng thái","Sắp diễn ra","Đang diễn ra","Đã kết thúc");

        refreshFilterLoai(); // nạp danh sách loại từ DB vào cbLoai

        JButton btnSearch = UITheme.primaryButton("Tìm");
        JButton btnReset  = UITheme.outlineButton("Đặt lại");
        JButton btnExport = UITheme.outlineButton("Xuất file");
        setSize2(btnSearch, 90, 34);
        setSize2(btnReset,  96, 34);
        setSize2(btnExport, 90, 34);

        btnSearch.addActionListener(e -> search());
        btnReset .addActionListener(e -> { txtSearch.setText(""); loadTable(); });
        btnExport.addActionListener(e -> ExcelExporter.exportToCSV(table, "HoatDong", this));

        bar.add(searchWrap); bar.add(cbThang); bar.add(cbNam);
        bar.add(cbLoai); bar.add(cbTrangThai);
        bar.add(btnSearch); bar.add(btnReset); bar.add(btnExport);
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
        JButton btnEdit = UITheme.outlineButton("Chỉnh sửa");
        JButton btnDel  = UITheme.dangerButton("Xóa");
        btnMail.addActionListener(e -> sendMailForSelectedActivity());
        btnEdit.addActionListener(e -> editSelected());
        btnDel .addActionListener(e -> deleteSelected());
        acts.add(btnMail); acts.add(btnEdit); acts.add(btnDel);
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
        String loai      = String.valueOf(cbLoai.getSelectedItem());
        String trangThai = String.valueOf(cbTrangThai.getSelectedItem());

        StringBuilder sql = new StringBuilder("SELECT * FROM HoatDong WHERE 1=1");
        if (!kw.isEmpty())           sql.append(" AND tenHoatDong LIKE ?");
        if (!"Tháng".equals(thang))  sql.append(" AND MONTH(thoiGianBatDau)=").append(thang.replace("T",""));
        if (!"Năm".equals(nam))      sql.append(" AND YEAR(thoiGianBatDau)=").append(nam);
        if (!"Loại".equals(loai))    sql.append(" AND loaiHoatDong=?");
        sql.append(" ORDER BY id DESC");

        try (Connection c  = DatabaseHelper.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int idx = 1;
            if (!kw.isEmpty())        ps.setString(idx++, "%" + kw + "%");
            if (!"Loại".equals(loai)) ps.setString(idx++, loai);

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
        if (JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa hoạt động này?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)
                == JOptionPane.YES_OPTION) {
            try (Connection c = DatabaseHelper.getConnection();
                 PreparedStatement ps = c.prepareStatement("DELETE FROM HoatDong WHERE id=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
                loadTable();
                refreshFilterLoai();
                NhatKyDAO.log(Session.getCurrentUserId(), "Xóa", "HoatDong", "Xóa hoạt động ID=" + id);
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
        JDialog dlg = FormPanel.createDialog(
            this, isEdit ? "Chỉnh sửa hoạt động" : "Thêm hoạt động mới", 680, 600);
        dlg.add(FormPanel.createHeader(
            isEdit ? "Chỉnh sửa hoạt động" : "Thêm hoạt động mới"), BorderLayout.NORTH);

        // ── Fields ──────────────────────────────────────────────────────────
        JPanel fields = new JPanel(new GridBagLayout());
        fields.setBackground(Color.WHITE);
        GridBagConstraints gc = FormPanel.defaultGBC();

        // Tên hoạt động
        PlaceholderTextField txtTen = new PlaceholderTextField("Nhập tên hoạt động...");
        styleFld(txtTen);

        // ── LOẠI HOẠT ĐỘNG: Combo editable + nút thêm loại mới ─────────────
        JPanel loaiPanel = buildLoaiPanel(dlg);
        JComboBox<String> cbLoaiF = getLoaiCombo(loaiPanel); // tham chiếu nội bộ

        // Các trường còn lại
        JTextField txtDia          = new PlaceholderTextField("Nhập địa điểm tổ chức...");   styleFld(txtDia);
        JTextField txtNguoiToChuc  = new PlaceholderTextField("Nhập tên đơn vị hoặc người tổ chức..."); styleFld(txtNguoiToChuc);
        JTextArea  txtMoTa         = new JTextArea(3, 30);
        txtMoTa.setFont(UITheme.FONT_LABEL);
        txtMoTa.setLineWrap(true); txtMoTa.setWrapStyleWord(true);
        txtMoTa.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR,1,true),
            BorderFactory.createEmptyBorder(6,10,6,10)));

        JSpinner spBD  = makeDateTimeSpinner();
        JSpinner spKT  = makeDateTimeSpinner();
        JSpinner spHan = makeDateTimeSpinner();

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

        FormPanel.addRow(fields, gc, 0, "Tên hoạt động *",    txtTen);
        FormPanel.addRow(fields, gc, 1, "Loại hoạt động *",   loaiPanel);
        FormPanel.addRow(fields, gc, 2, "Thời gian bắt đầu",  spBD);
        FormPanel.addRow(fields, gc, 3, "Thời gian kết thúc", spKT);
        FormPanel.addRow(fields, gc, 4, "Hạn đăng ký",        spHan);
        FormPanel.addRow(fields, gc, 5, "Người tổ chức *",    txtNguoiToChuc);
        FormPanel.addRow(fields, gc, 6, "Địa điểm *",         txtDia);
        FormPanel.addRow(fields, gc, 7, "Mô tả",              new JScrollPane(txtMoTa));

        // ── Footer buttons ───────────────────────────────────────────────────
        JButton btnSave   = UITheme.primaryButton("Lưu");
        JButton btnCancel = UITheme.outlineButton("Hủy");
        btnCancel.addActionListener(e -> dlg.dispose());

        btnSave.addActionListener(e -> {
            String ten          = txtTen.getText().trim();
            String loai         = getLoaiValue(cbLoaiF);
            String dia          = txtDia.getText().trim();
            String nguoiToChuc  = txtNguoiToChuc.getText().trim();
            String moTa         = txtMoTa.getText().trim();
            Date   bd           = (Date) spBD.getValue();
            Date   kt           = (Date) spKT.getValue();
            Date   han          = (Date) spHan.getValue();
            Date   now          = new Date();

            // Validation
            if (ten.isEmpty() || loai.isEmpty() || dia.isEmpty() || nguoiToChuc.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng nhập đầy đủ các trường bắt buộc (*).");
                return;
            }
            if (!bd.after(now)) {
                JOptionPane.showMessageDialog(dlg, "Thời gian bắt đầu phải lớn hơn thời điểm hiện tại.");
                return;
            }
            if (!kt.after(now)) {
                JOptionPane.showMessageDialog(dlg, "Thời gian kết thúc phải lớn hơn thời điểm hiện tại.");
                return;
            }
            if (kt.before(bd)) {
                JOptionPane.showMessageDialog(dlg, "Thời gian kết thúc phải sau thời gian bắt đầu.");
                return;
            }
            if (!han.before(bd)) {
                JOptionPane.showMessageDialog(dlg,
                    "Hạn đăng ký phải trước thời gian bắt đầu hoạt động\n(đề xuất: trước ít nhất 1 ngày).");
                return;
            }

            // Chuẩn hoá loại: Capitalize first letter
            String loaiNorm = loai.substring(0,1).toUpperCase() + loai.substring(1);

            try (Connection c = DatabaseHelper.getConnection()) {
                if (isEdit) {
                    PreparedStatement ps = c.prepareStatement(
                        "UPDATE HoatDong SET tenHoatDong=?,loaiHoatDong=?,thoiGianBatDau=?," +
                        "thoiGianKetThuc=?,hanDangKy=?,nguoiToChuc=?,diaDiem=?,moTa=? WHERE id=?");
                    ps.setString(1, ten);
                    ps.setString(2, loaiNorm);
                    ps.setTimestamp(3, new Timestamp(bd.getTime()));
                    ps.setTimestamp(4, new Timestamp(kt.getTime()));
                    ps.setTimestamp(5, new Timestamp(han.getTime()));
                    ps.setString(6, nguoiToChuc);
                    ps.setString(7, dia);
                    ps.setString(8, moTa);
                    ps.setInt(9, (int) model.getValueAt(row, 0));
                    ps.executeUpdate();
                    NhatKyDAO.log(Session.getCurrentUserId(), "Sửa", "HoatDong",
                        "Sửa hoạt động ID=" + model.getValueAt(row, 0));
                } else {
                    PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO HoatDong(tenHoatDong,loaiHoatDong,thoiGianBatDau," +
                        "thoiGianKetThuc,hanDangKy,nguoiToChuc,diaDiem,moTa,trangThai) " +
                        "VALUES(?,?,?,?,?,?,?,?,?)");
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
                refreshFilterLoai();   // cập nhật cbLoai ở filter
                dlg.dispose();
                JOptionPane.showMessageDialog(this, "Lưu hoạt động thành công.");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Lỗi: " + ex.getMessage());
            }
        });

        dlg.add(FormPanel.createBody(fields),         BorderLayout.CENTER);
        dlg.add(FormPanel.createFooter(btnCancel, btnSave), BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  LOẠI HOẠT ĐỘNG PANEL
    //  Gồm: JComboBox editable (chọn hoặc tự gõ) + nút "＋ Loại mới" (dialog)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Tạo panel chứa combo loại (editable) và nút thêm loại mới.
     * Trả về JPanel; dùng getLoaiCombo() để lấy lại combo reference.
     */
    private JPanel buildLoaiPanel(JDialog parentDlg) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panel.setOpaque(false);

        // ── Combo editable ──────────────────────────────────────────────────
        JComboBox<String> cb = new JComboBox<>();
        cb.setEditable(true);
        UITheme.styleCombo(cb);
        cb.setPreferredSize(new Dimension(220, 34));

        // Style vùng text nhập
        Component editorComp = cb.getEditor().getEditorComponent();
        if (editorComp instanceof JTextField tf) {
            tf.setFont(UITheme.FONT_LABEL);
            tf.setForeground(UITheme.TEXT_PRIMARY);
            tf.putClientProperty("JTextField.placeholderText", "Chọn hoặc nhập loại mới...");
        }

        // Nạp danh sách loại từ DB
        loadLoaiIntoCombo(cb);

        // ── Nút "＋ Loại mới" ──────────────────────────────────────────────
        JButton btnNewLoai = new JButton("＋ Loại mới");
        btnNewLoai.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnNewLoai.setForeground(Color.decode("#1359B9"));
        btnNewLoai.setBackground(Color.decode("#EEF4FF"));
        btnNewLoai.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#9FE4FB"), 1, true),
            BorderFactory.createEmptyBorder(5, 12, 5, 12)));
        btnNewLoai.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnNewLoai.setFocusPainted(false);
        btnNewLoai.setPreferredSize(new Dimension(120, 34));

        // Hover effect
        btnNewLoai.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnNewLoai.setBackground(Color.decode("#D6EAFF"));
            }
            public void mouseExited(MouseEvent e) {
                btnNewLoai.setBackground(Color.decode("#EEF4FF"));
            }
        });

        btnNewLoai.addActionListener(e -> showAddLoaiDialog(parentDlg, cb));

        panel.add(cb);
        panel.add(btnNewLoai);

        // Lưu combo vào clientProperty để getLoaiCombo() lấy lại
        panel.putClientProperty("loaiCombo", cb);
        return panel;
    }

    /** Lấy lại JComboBox từ panel đã tạo bởi buildLoaiPanel(). */
    @SuppressWarnings("unchecked")
    private JComboBox<String> getLoaiCombo(JPanel panel) {
        return (JComboBox<String>) panel.getClientProperty("loaiCombo");
    }

    /**
     * Dialog nhỏ để nhập tên loại hoạt động mới.
     * Sau khi xác nhận, loại được thêm vào combo của form và không ghi DB
     * (chỉ lưu vào DB khi lưu hoạt động). Nếu loại đã tồn tại → chỉ chọn.
     */
    private void showAddLoaiDialog(JDialog parentDlg, JComboBox<String> targetCombo) {
        JDialog addDlg = new JDialog(parentDlg, "Thêm loại hoạt động mới", true);
        addDlg.setSize(420, 200);
        addDlg.setLocationRelativeTo(parentDlg);
        addDlg.setLayout(new BorderLayout());
        addDlg.getContentPane().setBackground(Color.WHITE);
        addDlg.setResizable(false);

        // Header nhỏ
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(Color.decode("#1359B9"));
        hdr.setBorder(new EmptyBorder(12, 18, 12, 18));
        JLabel lblTitle = new JLabel("Thêm loại hoạt động mới");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(Color.WHITE);
        hdr.add(lblTitle, BorderLayout.CENTER);
        addDlg.add(hdr, BorderLayout.NORTH);

        // Body
        JPanel body = new JPanel(new BorderLayout(10, 0));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(18, 22, 10, 22));

        JLabel lbl = new JLabel("Tên loại:");
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_SECONDARY);
        lbl.setPreferredSize(new Dimension(80, 34));

        PlaceholderTextField txtLoai = new PlaceholderTextField("VD: Hội thảo, Workshop, Cuộc thi...");
        txtLoai.setFont(UITheme.FONT_LABEL);
        txtLoai.setPreferredSize(new Dimension(240, 34));
        txtLoai.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));

        body.add(lbl,     BorderLayout.WEST);
        body.add(txtLoai, BorderLayout.CENTER);
        addDlg.add(body, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footer.setBackground(Color.decode("#F8FAFC"));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));

        JButton btnHuy   = UITheme.outlineButton("Hủy");
        JButton btnThem  = UITheme.primaryButton("Thêm loại");
        setSize2(btnHuy,  90, 34);
        setSize2(btnThem, 110, 34);

        btnHuy .addActionListener(ev -> addDlg.dispose());
        btnThem.addActionListener(ev -> {
            String ten = txtLoai.getText().trim();
            if (ten.isEmpty()) {
                JOptionPane.showMessageDialog(addDlg, "Vui lòng nhập tên loại hoạt động.");
                return;
            }
            // Capitalize first letter
            String tenNorm = ten.substring(0,1).toUpperCase() + ten.substring(1);

            // Kiểm tra đã tồn tại chưa (so sánh không phân biệt hoa/thường)
            boolean exists = false;
            for (int i = 0; i < targetCombo.getItemCount(); i++) {
                if (tenNorm.equalsIgnoreCase(targetCombo.getItemAt(i))) {
                    targetCombo.setSelectedIndex(i);
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                targetCombo.addItem(tenNorm);
                targetCombo.setSelectedItem(tenNorm);
            } else {
                JOptionPane.showMessageDialog(addDlg,
                    "Loại \"" + tenNorm + "\" đã tồn tại.\nĐã tự động chọn loại đó cho bạn.");
            }
            addDlg.dispose();
        });

        // Cho phép nhấn Enter để thêm
        txtLoai.addActionListener(ev -> btnThem.doClick());

        footer.add(btnHuy); footer.add(btnThem);
        addDlg.add(footer, BorderLayout.SOUTH);
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
            + "Đăng ký tham gia: /tham-gia?hoatDongId=" + model.getValueAt(row, 0)
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
        SpinnerDateModel md = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.MINUTE);
        JSpinner sp = new JSpinner(md);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(sp, "dd/MM/yyyy HH:mm");
        sp.setEditor(editor);
        editor.getTextField().setEditable(false);
        sp.setPreferredSize(new Dimension(260, 34));
        return sp;
    }

    private void styleFld(JTextField f) { f.setPreferredSize(new Dimension(260, 34)); }

    private JComboBox<String> makeCombo(String... items) {
        JComboBox<String> cb = new JComboBox<>(items);
        UITheme.styleCombo(cb);
        return cb;
    }

    private void setSize2(JButton btn, int w, int h) {
        btn.setPreferredSize(new Dimension(w, h));
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