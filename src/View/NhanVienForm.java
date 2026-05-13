package View;

import Util.*;
import dao.NhatKyDAO;
import dao.ArchiveDAO;
import database.DatabaseHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

/**
 * NhanVienForm – Quản lý nhân viên và tài khoản.
 * Giao diện đồng nhất với card layout, gradient header, styled table.
 */
public class NhanVienForm extends JPanel {

    private StyledTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private JComboBox<String> cbVaiTro;

    public NhanVienForm() {
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
        header.add(UITheme.pageTitlePanel("Quản lý Nhân viên",
                "Tài khoản và phân quyền người dùng hệ thống"), BorderLayout.WEST);
        JButton btnAdd = UITheme.primaryButton("Thêm nhân viên");
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

    private JPanel buildFilterCard() {
        JPanel card = UITheme.cardPanel(new BorderLayout());
        card.setBorder(new EmptyBorder(10, 14, 10, 14));

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bar.setOpaque(false);

        txtSearch = new JTextField(16);
        JPanel sw = UITheme.searchField(txtSearch, "Tìm tên, mã nhân viên...");

        cbVaiTro = new JComboBox<>(new String[]{"Vai trò","Admin","Nhân viên"});
        UITheme.styleCombo(cbVaiTro);

        JButton btnSearch = UITheme.primaryButton("Tìm");
        JButton btnReset  = UITheme.outlineButton("Đặt lại");
//        JButton btnExport = UITheme.outlineButton("Xuất");
        btnSearch.setPreferredSize(new Dimension(90, 34));
        btnReset .setPreferredSize(new Dimension(96, 34));
//        btnExport.setPreferredSize(new Dimension(90, 34));
        btnSearch.addActionListener(e -> search());
        btnReset .addActionListener(e -> { txtSearch.setText(""); cbVaiTro.setSelectedIndex(0); loadTable(); });
//        btnExport.addActionListener(e -> ExcelExporter.exportToCSV(table, "NhanVien", this));

        bar.add(sw); bar.add(cbVaiTro);
        bar.add(btnSearch); bar.add(btnReset); //bar.add(btnExport);
        card.add(bar, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTableCard() {
        model = new DefaultTableModel(
            new String[]{"ID","Mã NV","Họ tên","Username","Vai trò","Giới tính","SĐT","Email","Trạng thái"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new StyledTable(model);
        int[] w = {50, 80, 180, 110, 90, 70, 110, 170, 90};
        for (int i = 0; i < w.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);

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
        JLabel lTitle = new JLabel("Danh sách nhân viên");
        lTitle.setFont(UITheme.FONT_BOLD); lTitle.setForeground(UITheme.TEXT_PRIMARY);
        tblHead.add(lTitle, BorderLayout.WEST);

        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        acts.setOpaque(false);
        JButton btnExport = UITheme.outlineButton("Xuất");
        JButton btnEdit = UITheme.outlineButton("Chỉnh sửa");
        JButton btnDel  = UITheme.dangerButton("Xóa");
        btnExport.addActionListener(e -> ExcelExporter.exportToCSV(table, "NhanVien", this));
        btnEdit.addActionListener(e -> editSelected());
        btnDel .addActionListener(e -> deleteSelected());
        acts.add(btnExport); acts.add(btnEdit); acts.add(btnDel);
        tblHead.add(acts, BorderLayout.EAST);

        card.add(tblHead, BorderLayout.NORTH);
        card.add(UITheme.styledScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    public void loadTable() {
        model.setRowCount(0);
        String sql = "SELECT nv.id, nv.maNhanVien, nv.tenNhanVien, tk.username, tk.role, "
                   + "nv.gioiTinh, nv.sdt, nv.email, "
                   + "ISNULL(nv.trangThai, N'Đang làm') as trangThai "
                   + "FROM NhanVien nv LEFT JOIN TaiKhoan tk ON tk.idNhanVien=nv.id "
                   + "ORDER BY nv.id DESC";
        try (Connection c = DatabaseHelper.getConnection();
             ResultSet rs = c.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"), rs.getString("maNhanVien"), rs.getString("tenNhanVien"),
                    rs.getString("username"), rs.getString("role"), rs.getString("gioiTinh"),
                    rs.getString("sdt"), rs.getString("email"), rs.getString("trangThai")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void search() {
        model.setRowCount(0);
        String kw = txtSearch.getText().trim();
        String vt = (String) cbVaiTro.getSelectedItem();
        StringBuilder sql = new StringBuilder(
            "SELECT nv.id, nv.maNhanVien, nv.tenNhanVien, tk.username, tk.role, "
          + "nv.gioiTinh, nv.sdt, nv.email, ISNULL(nv.trangThai,N'Đang làm') as trangThai "
          + "FROM NhanVien nv LEFT JOIN TaiKhoan tk ON tk.idNhanVien=nv.id "
          + "WHERE (nv.tenNhanVien LIKE ? OR nv.maNhanVien LIKE ?)");
        if (!"Vai trò".equals(vt)) sql.append(" AND tk.role=N'").append(vt).append("'");
        sql.append(" ORDER BY nv.id DESC");
        try (Connection c = DatabaseHelper.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            ps.setString(1, "%" + kw + "%"); ps.setString(2, "%" + kw + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"), rs.getString("maNhanVien"), rs.getString("tenNhanVien"),
                    rs.getString("username"), rs.getString("role"), rs.getString("gioiTinh"),
                    rs.getString("sdt"), rs.getString("email"), rs.getString("trangThai")
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
        String trangThai = str(model.getValueAt(row, 8));
        if (id == Session.getCurrentUserId()) {
            JOptionPane.showMessageDialog(this, "Không thể xóa tài khoản đang đăng nhập!");
            return;
        }
        if ("Đang làm".equalsIgnoreCase(trangThai)) {
            JOptionPane.showMessageDialog(this, "Không cho phép xóa nhân viên đang ở trạng thái \"Đang làm\".");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this, "Xác nhận xóa nhân viên này?",
            "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            try (Connection c = DatabaseHelper.getConnection();
                 PreparedStatement ps = c.prepareStatement("DELETE FROM NhanVien WHERE id=?")) {
                ArchiveDAO.archiveByQuery("Nhân viên", id, "NhanVien", "id", Session.getCurrentUserId(), "XÓA");
                ps.setInt(1, id); ps.executeUpdate();
                NhatKyDAO.log(Session.getCurrentUserId(), "XÓA", "Nhân viên", "Xóa nhân viên ID: " + id);
                loadTable();
                JOptionPane.showMessageDialog(this, "Nhân viên đã được xóa thành công");
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage()); }
        }
    }

    private void openForm(Integer row) {
        boolean isEdit = row != null;
        JDialog dlg = FormPanel.createDialog(this,
            isEdit ? "Sửa nhân viên" : "Thêm nhân viên", 560, 460);
        dlg.setLayout(new BorderLayout());
        dlg.add(FormPanel.createHeader(isEdit ? "Chỉnh sửa nhân viên" : "Thêm nhân viên mới"),
                BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setBackground(Color.WHITE);
        GridBagConstraints gc = FormPanel.defaultGBC();

        JTextField txtMa   = FormPanel.styledFieldReadonly(260);
        JTextField txtTen  = FormPanel.styledField(260);
        JTextField txtSdt  = FormPanel.styledField(260);
        JTextField txtEmail= FormPanel.styledField(260);
        JTextField txtUser = FormPanel.styledField(260);
        JComboBox<String> cbGT   = new JComboBox<>(new String[]{"Nam","Nữ"});
        JComboBox<String> cbRole = new JComboBox<>(new String[]{"Nhân viên","Admin"});
        UITheme.styleCombo(cbGT); UITheme.styleCombo(cbRole);
        cbGT.setPreferredSize(new Dimension(260, 34));
        cbRole.setPreferredSize(new Dimension(260, 34));

        JSpinner spNgay = new JSpinner(new SpinnerDateModel());
        spNgay.setEditor(new JSpinner.DateEditor(spNgay, "dd/MM/yyyy"));
        spNgay.setFont(UITheme.FONT_LABEL);
        spNgay.setPreferredSize(new Dimension(260, 34));

        if (isEdit) {
            txtMa  .setText(str(model.getValueAt(row, 1)));
            txtTen .setText(str(model.getValueAt(row, 2)));
            txtUser.setText(str(model.getValueAt(row, 3)));
            cbRole .setSelectedItem(str(model.getValueAt(row, 4)));
            cbGT   .setSelectedItem(str(model.getValueAt(row, 5)));
            txtSdt .setText(str(model.getValueAt(row, 6)));
            txtEmail.setText(str(model.getValueAt(row, 7)));
        } else {
            txtMa.setText(nextCode());
        }

        FormPanel.addRow(fields, gc, 0, "Mã nhân viên",  txtMa);
        FormPanel.addRow(fields, gc, 1, "Họ tên *",      txtTen);
        FormPanel.addRow(fields, gc, 2, "Ngày sinh",     spNgay);
        FormPanel.addRow(fields, gc, 3, "Giới tính",     cbGT);
        FormPanel.addRow(fields, gc, 4, "SĐT *",         txtSdt);
        FormPanel.addRow(fields, gc, 5, "Email *",       txtEmail);
        FormPanel.addRow(fields, gc, 6, "Username *",    txtUser);
        FormPanel.addRow(fields, gc, 7, "Vai trò *",     cbRole);

        dlg.add(FormPanel.createBody(fields), BorderLayout.CENTER);

        JButton btnSave   = UITheme.primaryButton("Lưu");
        JButton btnCancel = UITheme.outlineButton("Hủy");
        btnCancel.addActionListener(e -> dlg.dispose());
        btnSave.addActionListener(e -> {
            String ten   = txtTen.getText().trim();
            String sdt   = txtSdt.getText().trim();
            String email = txtEmail.getText().trim();
            String user2 = txtUser.getText().trim();
            if (ten.isEmpty() || sdt.isEmpty() || email.isEmpty() || user2.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng nhập đầy đủ thông tin."); return;
            }
            if (!ValidationHelper.isValidSdt(sdt)) { JOptionPane.showMessageDialog(dlg, "SĐT không hợp lệ."); return; }
            if (!ValidationHelper.isValidEmail(email)) { JOptionPane.showMessageDialog(dlg, "Email không hợp lệ."); return; }
            try (Connection c = DatabaseHelper.getConnection()) {
                LocalDate ns = new java.sql.Date(((java.util.Date)spNgay.getValue()).getTime()).toLocalDate();
                if (isEdit) {
                    PreparedStatement ps = c.prepareStatement(
                        "UPDATE NhanVien SET tenNhanVien=?,ngaySinh=?,gioiTinh=?,sdt=?,email=? WHERE id=?");
                    ps.setString(1, ten); ps.setDate(2, java.sql.Date.valueOf(ns));
                    ps.setString(3, (String)cbGT.getSelectedItem());
                    ps.setString(4, sdt); ps.setString(5, email);
                    ps.setInt(6, (int)model.getValueAt(row, 0)); ps.executeUpdate();
                    NhatKyDAO.log(Session.getCurrentUserId(), "SỬA", "Nhân viên", "Sửa NV: " + ten);
                } else {
                    c.setAutoCommit(false);
                    PreparedStatement ps1 = c.prepareStatement(
                        "INSERT INTO NhanVien(maNhanVien,tenNhanVien,ngaySinh,gioiTinh,sdt,email) VALUES(?,?,?,?,?,?)",
                        java.sql.Statement.RETURN_GENERATED_KEYS);
                    ps1.setString(1, txtMa.getText()); ps1.setString(2, ten);
                    ps1.setDate(3, java.sql.Date.valueOf(ns));
                    ps1.setString(4, (String)cbGT.getSelectedItem());
                    ps1.setString(5, sdt); ps1.setString(6, email);
                    ps1.executeUpdate();
                    ResultSet keys = ps1.getGeneratedKeys();
                    if (keys.next()) {
                        int nvId = keys.getInt(1);
                        PreparedStatement ps2 = c.prepareStatement(
                            "INSERT INTO TaiKhoan(username,password,role,idNhanVien) VALUES(?,?,?,?)");
                        ps2.setString(1, user2); ps2.setString(2, "123456");
                        ps2.setString(3, (String)cbRole.getSelectedItem()); ps2.setInt(4, nvId);
                        ps2.executeUpdate();
                    }
                    c.commit();
                    NhatKyDAO.log(Session.getCurrentUserId(), "THÊM", "Nhân viên", "Thêm NV: " + ten);
                }
                loadTable(); dlg.dispose();
            } catch (Exception ex) { JOptionPane.showMessageDialog(dlg, "Lỗi: " + ex.getMessage()); }
        });
        dlg.add(FormPanel.createFooter(btnCancel, btnSave), BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private String nextCode() {
        try (Connection c = DatabaseHelper.getConnection();
             ResultSet rs = c.createStatement().executeQuery(
                "SELECT MAX(CAST(SUBSTRING(maNhanVien,3,10) AS INT)) AS mx FROM NhanVien")) {
            if (rs.next()) return String.format("NV%03d", rs.getInt("mx") + 1);
        } catch (Exception e) { e.printStackTrace(); }
        return "NV001";
    }

    private String str(Object o) { return o == null ? "" : o.toString(); }
}