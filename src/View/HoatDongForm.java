package View;

import Util.*;
import database.DatabaseHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/**
 * HoatDongForm – Quản lý hoạt động hội.
 * Giao diện đồng nhất: card layout, gradient header, styled table.
 */
public class HoatDongForm extends JPanel {

    private StyledTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private JComboBox<String> cbThang, cbNam, cbLoai, cbTrangThai;

    public HoatDongForm() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(22, 26, 22, 26));
        buildUI();
        loadTable();
    }

    private void buildUI() {
        // ── HEADER ────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 14, 0));
        header.add(UITheme.pageTitlePanel("Quản lý Hoạt động",
                "Tổ chức và theo dõi hoạt động hội viên"), BorderLayout.WEST);
        JButton btnAdd = UITheme.primaryButton("  ＋  Thêm mới");
        btnAdd.setPreferredSize(new Dimension(170, 36));
        btnAdd.addActionListener(e -> openForm(null));
        header.add(btnAdd, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── CENTER ────────────────────────────────────────────────────────
        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        center.add(buildFilterCard(), BorderLayout.NORTH);
        center.add(buildTableCard(), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    // ── FILTER CARD ───────────────────────────────────────────────────────
    private JPanel buildFilterCard() {
        JPanel card = UITheme.cardPanel(new BorderLayout());
        card.setBorder(new EmptyBorder(10, 14, 10, 14));

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bar.setOpaque(false);

        txtSearch = new JTextField(16);
        JPanel searchWrap = UITheme.searchField(txtSearch, "Tìm theo tên hoạt động...");

        cbThang   = makeCombo("Tháng","T1","T2","T3","T4","T5","T6","T7","T8","T9","T10","T11","T12");
        cbNam     = makeCombo("Năm","2023","2024","2025","2026");
        cbLoai    = makeCombo("Loại","Hội thảo","Workshop","Cuộc thi","Khác");
        cbTrangThai = makeCombo("Trạng thái","Sắp diễn ra","Đang diễn ra","Đã kết thúc");

        JButton btnSearch = UITheme.primaryButton("Tìm");
        JButton btnReset  = UITheme.outlineButton("Đặt lại");
        JButton btnExport = UITheme.outlineButton("Xuất file");
        setButtonSize(btnSearch, 90, 34);
        setButtonSize(btnReset,  96, 34);
        setButtonSize(btnExport, 90, 34);

        btnSearch.addActionListener(e -> search());
        btnReset .addActionListener(e -> { txtSearch.setText(""); loadTable(); });
        btnExport.addActionListener(e -> ExcelExporter.exportToCSV(table, "HoatDong", this));

        bar.add(searchWrap); bar.add(cbThang); bar.add(cbNam);
        bar.add(cbLoai); bar.add(cbTrangThai);
        bar.add(btnSearch); bar.add(btnReset); bar.add(btnExport);
        card.add(bar, BorderLayout.CENTER);
        return card;
    }

    // ── TABLE CARD ────────────────────────────────────────────────────────
    private JPanel buildTableCard() {
        // Model
        model = new DefaultTableModel(new String[]{
            "ID","Tên hoạt động","Loại","Bắt đầu","Kết thúc","Địa điểm","Mô tả","Trạng thái"
        }, 0) { public boolean isCellEditable(int r, int c) { return false; } };

        table = new StyledTable(model);
        int[] widths = {50, 200, 90, 120, 120, 110, 160, 110};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Card
        JPanel card = UITheme.cardPanel(new BorderLayout());

        // Card header
        JPanel tblHead = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                GradientPaint gp=new GradientPaint(0,0,UITheme.PRIMARY_LIGHT,getWidth(),0,Color.WHITE);
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(UITheme.BORDER_COLOR); g2.drawLine(0,getHeight()-1,getWidth(),getHeight()-1);
                g2.dispose(); super.paintComponent(g);
            }
        };
        tblHead.setOpaque(false);
        tblHead.setBorder(new EmptyBorder(9, 14, 9, 14));

        JLabel tblTitle = new JLabel("Danh sách hoạt động");
        tblTitle.setFont(UITheme.FONT_BOLD);
        tblTitle.setForeground(UITheme.TEXT_PRIMARY);
        tblHead.add(tblTitle, BorderLayout.WEST);

        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        acts.setOpaque(false);
        JButton btnEdit = UITheme.outlineButton("Chỉnh sửa");
        JButton btnDel  = UITheme.dangerButton("Xóa");
        btnEdit.addActionListener(e -> editSelected());
        btnDel .addActionListener(e -> deleteSelected());
        acts.add(btnEdit); acts.add(btnDel);
        tblHead.add(acts, BorderLayout.EAST);

        card.add(tblHead, BorderLayout.NORTH);
        card.add(UITheme.styledScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    // ── LOAD / SEARCH ─────────────────────────────────────────────────────
    public void loadTable() {
        model.setRowCount(0);
        String sql = "SELECT * FROM HoatDong ORDER BY id DESC";
        try (Connection c = DatabaseHelper.getConnection();
             ResultSet rs = c.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("tenHoatDong"),
                    rs.getString("loaiHoatDong"),
                    rs.getTimestamp("thoiGianBatDau"),
                    rs.getTimestamp("thoiGianKetThuc"),
                    rs.getString("diaDiem"),
                    rs.getString("moTa"),
                    computeStatus(rs.getTimestamp("thoiGianBatDau"), rs.getTimestamp("thoiGianKetThuc"))
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
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
        String kw = txtSearch.getText().trim();
        String loai = (String) cbLoai.getSelectedItem();
        StringBuilder sql = new StringBuilder(
            "SELECT * FROM HoatDong WHERE tenHoatDong LIKE ?");
        if (!"Loại".equals(loai)) sql.append(" AND loaiHoatDong=N'").append(loai).append("'");
        sql.append(" ORDER BY id DESC");
        try (Connection c = DatabaseHelper.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            ps.setString(1, "%" + kw + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"), rs.getString("tenHoatDong"), rs.getString("loaiHoatDong"),
                    rs.getTimestamp("thoiGianBatDau"), rs.getTimestamp("thoiGianKetThuc"),
                    rs.getString("diaDiem"), rs.getString("moTa"),
                    computeStatus(rs.getTimestamp("thoiGianBatDau"), rs.getTimestamp("thoiGianKetThuc"))
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng cần sửa!"); return; }
        openForm(row);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng cần xóa!"); return; }
        int id = (int) model.getValueAt(row, 0);
        int ok = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn xóa hoạt động này?",
            "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            try (Connection c = DatabaseHelper.getConnection();
                 PreparedStatement ps = c.prepareStatement("DELETE FROM HoatDong WHERE id=?")) {
                ps.setInt(1, id); ps.executeUpdate(); loadTable();
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage()); }
        }
    }

    // ── OPEN FORM ─────────────────────────────────────────────────────────
    private void openForm(Integer row) {
        boolean isEdit = row != null;
        JDialog dlg = FormPanel.createDialog(this,
            isEdit ? "Chỉnh sửa hoạt động" : "Thêm hoạt động mới", 560, 440);
        dlg.setLayout(new BorderLayout());
        dlg.add(FormPanel.createHeader(isEdit ? "Chỉnh sửa hoạt động" : "Thêm hoạt động mới"),
                BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setBackground(Color.WHITE);
        GridBagConstraints gc = FormPanel.defaultGBC();

        JTextField txtTen   = FormPanel.styledField(300);
        JComboBox<String> cbLoaiF = new JComboBox<>(new String[]{"Hội thảo","Workshop","Cuộc thi","Khác"});
        UITheme.styleCombo(cbLoaiF);
        JTextField txtBD   = FormPanel.styledField(200);
        JTextField txtKT   = FormPanel.styledField(200);
        JTextField txtDia  = FormPanel.styledField(300);
        JTextArea  txtMoTa = new JTextArea(3, 30);
        txtMoTa.setFont(UITheme.FONT_LABEL);
        txtMoTa.setLineWrap(true);
        txtMoTa.setWrapStyleWord(true);
        txtMoTa.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        JScrollPane spMoTa = new JScrollPane(txtMoTa);
        spMoTa.setPreferredSize(new Dimension(300, 70));
        spMoTa.setBorder(BorderFactory.createEmptyBorder());

        if (isEdit) {
            txtTen .setText(str(model.getValueAt(row, 1)));
            cbLoaiF.setSelectedItem(str(model.getValueAt(row, 2)));
            Object bd = model.getValueAt(row, 3);
            Object kt = model.getValueAt(row, 4);
            if (bd != null) txtBD.setText(bd.toString());
            if (kt != null) txtKT.setText(kt.toString());
            txtDia .setText(str(model.getValueAt(row, 5)));
            txtMoTa.setText(str(model.getValueAt(row, 6)));
        }

        FormPanel.addRow(fields, gc, 0, "Tên hoạt động *", txtTen);
        FormPanel.addRow(fields, gc, 1, "Loại hoạt động *", cbLoaiF);
        FormPanel.addRow(fields, gc, 2, "Thời gian bắt đầu", txtBD);
        FormPanel.addRow(fields, gc, 3, "Thời gian kết thúc", txtKT);
        FormPanel.addRow(fields, gc, 4, "Địa điểm", txtDia);
        FormPanel.addRow(fields, gc, 5, "Mô tả", spMoTa);

        dlg.add(FormPanel.createBody(fields), BorderLayout.CENTER);

        JButton btnSave   = UITheme.primaryButton("Lưu");
        JButton btnCancel = UITheme.outlineButton("Hủy");
        btnCancel.addActionListener(e -> dlg.dispose());
        btnSave.addActionListener(e -> {
            String ten = txtTen.getText().trim();
            if (ten.isEmpty()) { JOptionPane.showMessageDialog(dlg, "Vui lòng nhập tên hoạt động."); return; }
            try (Connection c = DatabaseHelper.getConnection()) {
                if (isEdit) {
                    PreparedStatement ps = c.prepareStatement(
                        "UPDATE HoatDong SET tenHoatDong=?,loaiHoatDong=?,diaDiem=?,moTa=? WHERE id=?");
                    ps.setString(1, ten); ps.setString(2, (String)cbLoaiF.getSelectedItem());
                    ps.setString(3, txtDia.getText().trim()); ps.setString(4, txtMoTa.getText().trim());
                    ps.setInt(5, (int)model.getValueAt(row, 0)); ps.executeUpdate();
                } else {
                    PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO HoatDong(tenHoatDong,loaiHoatDong,diaDiem,moTa) VALUES(?,?,?,?)");
                    ps.setString(1, ten); ps.setString(2, (String)cbLoaiF.getSelectedItem());
                    ps.setString(3, txtDia.getText().trim()); ps.setString(4, txtMoTa.getText().trim());
                    ps.executeUpdate();
                }
                loadTable(); dlg.dispose();
            } catch (Exception ex) { JOptionPane.showMessageDialog(dlg, "Lỗi: " + ex.getMessage()); }
        });
        dlg.add(FormPanel.createFooter(btnCancel, btnSave), BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ── HELPERS ───────────────────────────────────────────────────────────
    private JComboBox<String> makeCombo(String... items) {
        JComboBox<String> cb = new JComboBox<>(items);
        UITheme.styleCombo(cb);
        return cb;
    }

    private void setButtonSize(JButton btn, int w, int h) {
        btn.setPreferredSize(new Dimension(w, h));
    }

    private String str(Object o) { return o == null ? "" : o.toString(); }
}