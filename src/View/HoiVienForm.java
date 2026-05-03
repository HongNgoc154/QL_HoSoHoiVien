package View;

import Util.UITheme;
import Util.StyledTable;
import Util.ExcelExporter;
import database.DatabaseHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class HoiVienForm extends JPanel {

    private StyledTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private JComboBox<String> cbThang, cbNam, cbTrangThai;

    public HoiVienForm() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        // ===== HEADER =====
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));

        JLabel title = new JLabel("Quản lý Hội viên");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);

        JButton btnAdd = UITheme.primaryButton("+ Thêm mới");
        header.add(title, BorderLayout.WEST);
        header.add(btnAdd, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ===== FILTER BAR =====
        JPanel filterCard = createCard();
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 8));

        txtSearch = new JTextField(18);
        txtSearch.setFont(UITheme.FONT_LABEL);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        txtSearch.putClientProperty("Placeholder", "Tìm theo tên, mã hội viên...");

        cbThang = new JComboBox<>(new String[]{"Tháng","T1","T2","T3","T4","T5","T6","T7","T8","T9","T10","T11","T12"});
        cbNam = new JComboBox<>(new String[]{"Năm","2023","2024","2025","2026"});
        cbTrangThai = new JComboBox<>(new String[]{"Trạng thái","Hoạt động","Tạm dừng","Đã rời"});
        styleCombo(cbThang); styleCombo(cbNam); styleCombo(cbTrangThai);

        JButton btnSearch = UITheme.primaryButton("🔍 Tìm kiếm");
        JButton btnReset = UITheme.outlineButton("↺ Đặt lại");
        JButton btnExport = UITheme.outlineButton("📥 Xuất Excel");

        filterCard.add(new JLabel("Tìm kiếm:"));
        filterCard.add(txtSearch);
        filterCard.add(cbThang);
        filterCard.add(cbNam);
        filterCard.add(cbTrangThai);
        filterCard.add(btnSearch);
        filterCard.add(btnReset);
        filterCard.add(Box.createHorizontalStrut(10));
        filterCard.add(btnExport);

        // ===== TABLE =====
        model = new DefaultTableModel(new String[]{
            "ID","Mã HV","Họ tên","Ngày sinh","Giới tính","SĐT","Email","Trạng thái","Ngày tham gia"
        }, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new StyledTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(70);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(7).setPreferredWidth(90);

        // Hover tooltip + detail
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    int id = (int) model.getValueAt(row, 0);
                    showDetail(id, row);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        scrollPane.getViewport().setBackground(Color.WHITE);

        // ===== ACTION BAR below table =====
        JPanel tableCard = createCard();
        tableCard.setLayout(new BorderLayout());

        // Table header with action buttons
        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        tableHeader.setBorder(new EmptyBorder(0, 0, 12, 0));
        JLabel tblTitle = new JLabel("Danh sách hội viên");
        tblTitle.setFont(UITheme.FONT_HEADING);
        tblTitle.setForeground(UITheme.TEXT_PRIMARY);

        JPanel tblActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tblActions.setOpaque(false);
        JButton btnEdit = UITheme.outlineButton("✏ Sửa");
        JButton btnDel = UITheme.dangerButton("🗑 Xóa");
        tblActions.add(btnEdit);
        tblActions.add(btnDel);

        tableHeader.add(tblTitle, BorderLayout.WEST);
        tableHeader.add(tblActions, BorderLayout.EAST);

        tableCard.add(tableHeader, BorderLayout.NORTH);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Combine center content
        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(filterCard, BorderLayout.NORTH);
        center.add(tableCard, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        // Events
        loadTable();

        btnSearch.addActionListener(e -> search());
        btnReset.addActionListener(e -> { txtSearch.setText(""); loadTable(); });
        btnAdd.addActionListener(e -> openForm(null));
        btnEdit.addActionListener(e -> editSelected());
        btnDel.addActionListener(e -> deleteSelected());
        btnExport.addActionListener(e -> ExcelExporter.exportToCSV(table, "HoiVien", this));
        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) { if (e.getKeyCode() == KeyEvent.VK_ENTER) search(); }
        });
    }

    private JPanel createCard() {
        JPanel p = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.setColor(UITheme.BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(14, 16, 14, 16));
        return p;
    }

    private void styleCombo(JComboBox<String> cb) {
        cb.setFont(UITheme.FONT_LABEL);
        cb.setBackground(Color.WHITE);
        cb.setPreferredSize(new Dimension(110, 32));
    }

    void loadTable() {
        model.setRowCount(0);
        try (Connection conn = DatabaseHelper.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM HoiVien ORDER BY id DESC")) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"), rs.getString("maHoiVien"),
                    rs.getString("tenHoiVien"), rs.getDate("ngaySinh"),
                    rs.getString("gioiTinh"), rs.getString("sdt"),
                    rs.getString("email"), rs.getString("trangThai"),
                    rs.getDate("ngayThamGia")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void search() {
        model.setRowCount(0);
        String kw = txtSearch.getText().trim();
        String thang = (String) cbThang.getSelectedItem();
        String nam = (String) cbNam.getSelectedItem();
        String tt = (String) cbTrangThai.getSelectedItem();

        StringBuilder sql = new StringBuilder(
            "SELECT * FROM HoiVien WHERE (tenHoiVien LIKE ? OR maHoiVien LIKE ?)"
        );
        if (!"Tháng".equals(thang)) sql.append(" AND MONTH(ngayThamGia)=").append(
            thang.replace("T",""));
        if (!"Năm".equals(nam)) sql.append(" AND YEAR(ngayThamGia)=").append(nam);
        if (!"Trạng thái".equals(tt)) sql.append(" AND trangThai=N'").append(tt).append("'");
        sql.append(" ORDER BY id DESC");

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, "%" + kw + "%");
            ps.setString(2, "%" + kw + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"), rs.getString("maHoiVien"),
                    rs.getString("tenHoiVien"), rs.getDate("ngaySinh"),
                    rs.getString("gioiTinh"), rs.getString("sdt"),
                    rs.getString("email"), rs.getString("trangThai"),
                    rs.getDate("ngayThamGia")
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
        String name = (String) model.getValueAt(row, 2);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn xóa hội viên\n\"" + name + "\" không?",
            "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM HoiVien WHERE id=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
                loadTable();
                JOptionPane.showMessageDialog(this, "Xóa thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa: " + e.getMessage());
            }
        }
    }

    private void showDetail(int id, int row) {
        // Only show detail when NOT clicking action area
        String ten = (String) model.getValueAt(row, 2);
        String ma = (String) model.getValueAt(row, 1);
        Object ngaySinh = model.getValueAt(row, 3);
        String gt = (String) model.getValueAt(row, 4);
        String sdt = (String) model.getValueAt(row, 5);
        String email = (String) model.getValueAt(row, 6);
        String tt = (String) model.getValueAt(row, 7);

        JDialog dlg = createDetailDialog("Chi tiết hội viên");
        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(20, 24, 20, 24));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 4, 6, 4);
        gc.anchor = GridBagConstraints.WEST;

        // Avatar circle
        JPanel avatarPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.PRIMARY_LIGHT);
                g2.fillOval(0, 0, 70, 70);
                g2.setColor(UITheme.PRIMARY);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 28));
                String t = ten != null && !ten.isEmpty() ? String.valueOf(ten.charAt(0)) : "?";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(t, (70-fm.stringWidth(t))/2, (70-fm.getHeight())/2+fm.getAscent());
            }
        };
        avatarPanel.setPreferredSize(new Dimension(70, 70));
        avatarPanel.setOpaque(false);

        gc.gridx=0; gc.gridy=0; gc.gridwidth=2; gc.anchor=GridBagConstraints.CENTER;
        content.add(avatarPanel, gc);
        gc.gridwidth=1; gc.anchor=GridBagConstraints.WEST;

        addDetailRow(content, gc, 1, "Mã hội viên:", ma);
        addDetailRow(content, gc, 2, "Họ và tên:", ten);
        addDetailRow(content, gc, 3, "Ngày sinh:", ngaySinh != null ? ngaySinh.toString() : "");
        addDetailRow(content, gc, 4, "Giới tính:", gt);
        addDetailRow(content, gc, 5, "Số điện thoại:", sdt);
        addDetailRow(content, gc, 6, "Email:", email);
        addDetailRow(content, gc, 7, "Trạng thái:", tt);

        JButton btnClose = UITheme.primaryButton("Đóng");
        JButton btnEdit = UITheme.outlineButton("Chỉnh sửa");

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.add(btnEdit);
        btnRow.add(btnClose);

        gc.gridx=0; gc.gridy=9; gc.gridwidth=2;
        gc.anchor=GridBagConstraints.EAST;
        content.add(btnRow, gc);

        dlg.add(content);
        btnClose.addActionListener(e -> dlg.dispose());
        btnEdit.addActionListener(e -> { dlg.dispose(); openForm(row); });
        dlg.setVisible(true);
    }

    private void addDetailRow(JPanel p, GridBagConstraints gc, int y, String label, String value) {
        gc.gridx=0; gc.gridy=y; gc.gridwidth=1;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_SECONDARY);
        lbl.setPreferredSize(new Dimension(130, 26));
        p.add(lbl, gc);

        gc.gridx=1;
        JLabel val = new JLabel(value != null ? value : "—");
        val.setFont(UITheme.FONT_LABEL);
        val.setForeground(UITheme.TEXT_PRIMARY);
        p.add(val, gc);
    }

    private JDialog createDetailDialog(String title) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dlg.setSize(420, 460);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);
        dlg.getContentPane().setBackground(Color.WHITE);
        return dlg;
    }

    private void openForm(Integer row) {
        boolean isEdit = row != null;
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            isEdit ? "Chỉnh sửa hội viên" : "Thêm hội viên mới", true);
        dlg.setSize(500, 500);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);

        // Form header
        JPanel formHeader = new JPanel(new BorderLayout());
        formHeader.setBackground(UITheme.PRIMARY);
        formHeader.setBorder(new EmptyBorder(16, 20, 16, 20));
        JLabel hdrTitle = new JLabel(isEdit ? "✏  Chỉnh sửa hội viên" : "➕  Thêm hội viên mới");
        hdrTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        hdrTitle.setForeground(Color.WHITE);
        formHeader.add(hdrTitle);

        // Fields
        JPanel fields = new JPanel(new GridBagLayout());
        fields.setBackground(Color.WHITE);
        fields.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 4, 6, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtMa = createField();
        JTextField txtTen = createField();
        JTextField txtNgaySinh = createField();
        JTextField txtGT = createField();
        JTextField txtSdt = createField();
        JTextField txtEmail = createField();
        String[] ttOptions = {"Hoạt động", "Tạm dừng", "Đã rời"};
        JComboBox<String> cbTT = new JComboBox<>(ttOptions);
        cbTT.setFont(UITheme.FONT_LABEL);

        if (isEdit) {
            txtMa.setText(str(model.getValueAt(row, 1)));
            txtTen.setText(str(model.getValueAt(row, 2)));
            txtNgaySinh.setText(str(model.getValueAt(row, 3)));
            txtGT.setText(str(model.getValueAt(row, 4)));
            txtSdt.setText(str(model.getValueAt(row, 5)));
            txtEmail.setText(str(model.getValueAt(row, 6)));
            cbTT.setSelectedItem(str(model.getValueAt(row, 7)));
            txtMa.setEditable(false);
            txtMa.setBackground(UITheme.BG_MAIN);
        }

        addFormRow(fields, gc, 0, "Mã hội viên *", txtMa);
        addFormRow(fields, gc, 1, "Họ và tên *", txtTen);
        addFormRow(fields, gc, 2, "Ngày sinh (yyyy-mm-dd)", txtNgaySinh);
        addFormRow(fields, gc, 3, "Giới tính", txtGT);
        addFormRow(fields, gc, 4, "Số điện thoại", txtSdt);
        addFormRow(fields, gc, 5, "Email", txtEmail);
        addFormRow(fields, gc, 6, "Trạng thái", cbTT);

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        btnRow.setBackground(Color.decode("#F8FAFC"));
        btnRow.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));
        JButton btnCancel = UITheme.outlineButton("Hủy");
        JButton btnSave = UITheme.primaryButton(isEdit ? "Lưu thay đổi" : "Thêm mới");
        btnRow.add(btnCancel);
        btnRow.add(btnSave);

        content.add(formHeader, BorderLayout.NORTH);
        content.add(fields, BorderLayout.CENTER);
        content.add(btnRow, BorderLayout.SOUTH);
        dlg.add(content);

        btnCancel.addActionListener(e -> dlg.dispose());
        btnSave.addActionListener(e -> {
            if (txtMa.getText().trim().isEmpty() || txtTen.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Mã và Tên hội viên không được để trống!");
                return;
            }
            try (Connection conn = DatabaseHelper.getConnection()) {
                if (!isEdit) {
                    PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO HoiVien(maHoiVien,tenHoiVien,ngaySinh,gioiTinh,sdt,email,trangThai) VALUES(?,?,?,?,?,?,?)");
                    ps.setString(1, txtMa.getText().trim());
                    ps.setString(2, txtTen.getText().trim());
                    setDateOrNull(ps, 3, txtNgaySinh.getText().trim());
                    ps.setString(4, txtGT.getText().trim());
                    ps.setString(5, txtSdt.getText().trim());
                    ps.setString(6, txtEmail.getText().trim());
                    ps.setString(7, (String) cbTT.getSelectedItem());
                    ps.executeUpdate();
                } else {
                    int id = (int) model.getValueAt(row, 0);
                    PreparedStatement ps = conn.prepareStatement(
                        "UPDATE HoiVien SET tenHoiVien=?,ngaySinh=?,gioiTinh=?,sdt=?,email=?,trangThai=? WHERE id=?");
                    ps.setString(1, txtTen.getText().trim());
                    setDateOrNull(ps, 2, txtNgaySinh.getText().trim());
                    ps.setString(3, txtGT.getText().trim());
                    ps.setString(4, txtSdt.getText().trim());
                    ps.setString(5, txtEmail.getText().trim());
                    ps.setString(6, (String) cbTT.getSelectedItem());
                    ps.setInt(7, id);
                    ps.executeUpdate();
                }
                loadTable();
                dlg.dispose();
                JOptionPane.showMessageDialog(this, isEdit ? "Cập nhật thành công!" : "Thêm mới thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Lỗi: " + ex.getMessage());
            }
        });

        dlg.setVisible(true);
    }

    private JTextField createField() {
        JTextField f = new JTextField();
        f.setFont(UITheme.FONT_LABEL);
        f.setPreferredSize(new Dimension(280, 34));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return f;
    }

    private void addFormRow(JPanel p, GridBagConstraints gc, int y, String label, JComponent field) {
        gc.gridx=0; gc.gridy=y; gc.weightx=0; gc.gridwidth=1;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_SECONDARY);
        lbl.setPreferredSize(new Dimension(170, 26));
        p.add(lbl, gc);
        gc.gridx=1; gc.weightx=1;
        p.add(field, gc);
    }

    private String str(Object o) { return o == null ? "" : o.toString(); }

    private void setDateOrNull(PreparedStatement ps, int idx, String val) throws SQLException {
        if (val.isEmpty()) ps.setNull(idx, Types.DATE);
        else ps.setDate(idx, Date.valueOf(val));
    }
}