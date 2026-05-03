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

public class NhanVienForm extends JPanel {

    private StyledTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private JComboBox<String> cbTrangThai;

    public NhanVienForm() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));
        JLabel title = new JLabel("Quản lý Nhân viên");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);
        JButton btnAdd = UITheme.primaryButton("+ Thêm nhân viên");
        header.add(title, BorderLayout.WEST);
        header.add(btnAdd, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JPanel filterCard = createCard();
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 8));
        txtSearch = new JTextField(18);
        txtSearch.setFont(UITheme.FONT_LABEL);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        cbTrangThai = new JComboBox<>(new String[]{"Trạng thái","Đang làm","Đã nghỉ"});
        cbTrangThai.setFont(UITheme.FONT_LABEL);
        cbTrangThai.setBackground(Color.WHITE);
        cbTrangThai.setPreferredSize(new Dimension(130, 32));
        JButton btnSearch = UITheme.primaryButton("🔍 Tìm");
        JButton btnReset = UITheme.outlineButton("↺ Đặt lại");
        JButton btnExport = UITheme.outlineButton("📥 Xuất Excel");
        filterCard.add(new JLabel("Tìm:"));
        filterCard.add(txtSearch);
        filterCard.add(cbTrangThai);
        filterCard.add(btnSearch);
        filterCard.add(btnReset);
        filterCard.add(Box.createHorizontalStrut(10));
        filterCard.add(btnExport);

        model = new DefaultTableModel(new String[]{
            "ID","Mã NV","Họ tên","Ngày sinh","Giới tính","SĐT","Email","Trạng thái"
        }, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new StyledTable(model);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(2).setPreferredWidth(180);

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) showDetail(row);
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel tableCard = createCard();
        tableCard.setLayout(new BorderLayout());
        JPanel tblHeader = new JPanel(new BorderLayout());
        tblHeader.setOpaque(false);
        tblHeader.setBorder(new EmptyBorder(0, 0, 12, 0));
        JLabel tblTitle = new JLabel("Danh sách nhân viên");
        tblTitle.setFont(UITheme.FONT_HEADING);
        tblTitle.setForeground(UITheme.TEXT_PRIMARY);
        JPanel actBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actBtns.setOpaque(false);
        JButton btnEdit = UITheme.outlineButton("✏ Sửa");
        JButton btnDel = UITheme.dangerButton("🗑 Xóa");
        actBtns.add(btnEdit); actBtns.add(btnDel);
        tblHeader.add(tblTitle, BorderLayout.WEST);
        tblHeader.add(actBtns, BorderLayout.EAST);
        tableCard.add(tblHeader, BorderLayout.NORTH);
        tableCard.add(scroll, BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(filterCard, BorderLayout.NORTH);
        center.add(tableCard, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        loadTable();
        btnSearch.addActionListener(e -> search());
        btnReset.addActionListener(e -> { txtSearch.setText(""); loadTable(); });
        btnAdd.addActionListener(e -> openForm(null));
        btnEdit.addActionListener(e -> editSelected());
        btnDel.addActionListener(e -> deleteSelected());
        btnExport.addActionListener(e -> ExcelExporter.exportToCSV(table, "NhanVien", this));
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

    void loadTable() {
        model.setRowCount(0);
        try (Connection conn = DatabaseHelper.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM NhanVien ORDER BY id DESC")) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"), rs.getString("maNhanVien"),
                    rs.getString("tenNhanVien"), rs.getDate("ngaySinh"),
                    rs.getString("gioiTinh"), rs.getString("sdt"),
                    rs.getString("email"), rs.getString("trangThai")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void search() {
        model.setRowCount(0);
        String kw = txtSearch.getText().trim();
        String tt = (String) cbTrangThai.getSelectedItem();
        StringBuilder sql = new StringBuilder(
            "SELECT * FROM NhanVien WHERE (tenNhanVien LIKE ? OR maNhanVien LIKE ?)");
        if (!"Trạng thái".equals(tt)) sql.append(" AND trangThai=N'").append(tt).append("'");
        sql.append(" ORDER BY id DESC");
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, "%" + kw + "%"); ps.setString(2, "%" + kw + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"), rs.getString("maNhanVien"),
                    rs.getString("tenNhanVien"), rs.getDate("ngaySinh"),
                    rs.getString("gioiTinh"), rs.getString("sdt"),
                    rs.getString("email"), rs.getString("trangThai")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showDetail(int row) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chi tiết nhân viên", true);
        dlg.setSize(420, 380);
        dlg.setLocationRelativeTo(this);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);
        JPanel topBar = new JPanel();
        topBar.setBackground(UITheme.PRIMARY);
        topBar.setBorder(new EmptyBorder(14, 20, 14, 20));
        JLabel ttl = new JLabel("👔  " + str(model.getValueAt(row, 2)));
        ttl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        ttl.setForeground(Color.WHITE);
        topBar.add(ttl);

        JPanel info = new JPanel(new GridBagLayout());
        info.setBackground(Color.WHITE);
        info.setBorder(new EmptyBorder(18, 24, 10, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 4, 6, 4);
        gc.anchor = GridBagConstraints.WEST;

        addRow(info, gc, 0, "Mã nhân viên:", str(model.getValueAt(row, 1)));
        addRow(info, gc, 1, "Họ và tên:", str(model.getValueAt(row, 2)));
        addRow(info, gc, 2, "Ngày sinh:", str(model.getValueAt(row, 3)));
        addRow(info, gc, 3, "Giới tính:", str(model.getValueAt(row, 4)));
        addRow(info, gc, 4, "Số điện thoại:", str(model.getValueAt(row, 5)));
        addRow(info, gc, 5, "Email:", str(model.getValueAt(row, 6)));
        addRow(info, gc, 6, "Trạng thái:", str(model.getValueAt(row, 7)));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        btns.setBackground(Color.decode("#F8FAFC"));
        btns.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));
        JButton btnEdit = UITheme.outlineButton("Chỉnh sửa");
        JButton btnClose = UITheme.primaryButton("Đóng");
        btns.add(btnEdit); btns.add(btnClose);
        content.add(topBar, BorderLayout.NORTH);
        content.add(info, BorderLayout.CENTER);
        content.add(btns, BorderLayout.SOUTH);
        dlg.add(content);
        btnClose.addActionListener(e -> dlg.dispose());
        btnEdit.addActionListener(e -> { dlg.dispose(); openForm(row); });
        dlg.setVisible(true);
    }

    private void addRow(JPanel p, GridBagConstraints gc, int y, String lbl, String val) {
        gc.gridx=0; gc.gridy=y;
        JLabel l = new JLabel(lbl); l.setFont(UITheme.FONT_BOLD); l.setForeground(UITheme.TEXT_SECONDARY);
        l.setPreferredSize(new Dimension(140, 26)); p.add(l, gc);
        gc.gridx=1;
        JLabel v = new JLabel(val); v.setFont(UITheme.FONT_LABEL); v.setForeground(UITheme.TEXT_PRIMARY); p.add(v, gc);
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Chọn dòng cần sửa!"); return; }
        openForm(row);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Chọn dòng cần xóa!"); return; }
        int id = (int) model.getValueAt(row, 0);
        String name = str(model.getValueAt(row, 2));
        int c = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn xóa nhân viên\n\"" + name + "\" không?",
            "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM NhanVien WHERE id=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
                loadTable();
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage()); }
        }
    }

    private void openForm(Integer row) {
        boolean isEdit = row != null;
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            isEdit ? "Sửa nhân viên" : "Thêm nhân viên mới", true);
        dlg.setSize(480, 460);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);

        JPanel fh = new JPanel(new BorderLayout());
        fh.setBackground(UITheme.PRIMARY);
        fh.setBorder(new EmptyBorder(14, 20, 14, 20));
        JLabel hl = new JLabel(isEdit ? "✏  Sửa thông tin nhân viên" : "➕  Thêm nhân viên mới");
        hl.setFont(new Font("Segoe UI", Font.BOLD, 15)); hl.setForeground(Color.WHITE);
        fh.add(hl);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setBackground(Color.WHITE);
        fields.setBorder(new EmptyBorder(18, 24, 10, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 4, 6, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtMa = fld(), txtTen = fld(), txtNgaySinh = fld();
        JTextField txtGT = fld(), txtSdt = fld(), txtEmail = fld();
        JComboBox<String> cbTT = new JComboBox<>(new String[]{"Đang làm","Đã nghỉ"});
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

        addFRow(fields, gc, 0, "Mã nhân viên *", txtMa);
        addFRow(fields, gc, 1, "Họ và tên *", txtTen);
        addFRow(fields, gc, 2, "Ngày sinh (yyyy-mm-dd)", txtNgaySinh);
        addFRow(fields, gc, 3, "Giới tính", txtGT);
        addFRow(fields, gc, 4, "Số điện thoại", txtSdt);
        addFRow(fields, gc, 5, "Email", txtEmail);
        addFRow(fields, gc, 6, "Trạng thái", cbTT);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        btns.setBackground(Color.decode("#F8FAFC"));
        btns.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));
        JButton btnC = UITheme.outlineButton("Hủy"), btnS = UITheme.primaryButton(isEdit ? "Lưu" : "Thêm mới");
        btns.add(btnC); btns.add(btnS);

        content.add(fh, BorderLayout.NORTH);
        content.add(fields, BorderLayout.CENTER);
        content.add(btns, BorderLayout.SOUTH);
        dlg.add(content);
        btnC.addActionListener(e -> dlg.dispose());
        btnS.addActionListener(e -> {
            if (txtMa.getText().trim().isEmpty() || txtTen.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Mã và Tên nhân viên không được để trống!"); return;
            }
            try (Connection conn = DatabaseHelper.getConnection()) {
                if (!isEdit) {
                    PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO NhanVien(maNhanVien,tenNhanVien,ngaySinh,gioiTinh,sdt,email,trangThai) VALUES(?,?,?,?,?,?,?)");
                    ps.setString(1, txtMa.getText().trim());
                    ps.setString(2, txtTen.getText().trim());
                    setDate(ps, 3, txtNgaySinh.getText().trim());
                    ps.setString(4, txtGT.getText().trim());
                    ps.setString(5, txtSdt.getText().trim());
                    ps.setString(6, txtEmail.getText().trim());
                    ps.setString(7, (String) cbTT.getSelectedItem());
                    ps.executeUpdate();
                } else {
                    int id = (int) model.getValueAt(row, 0);
                    PreparedStatement ps = conn.prepareStatement(
                        "UPDATE NhanVien SET tenNhanVien=?,ngaySinh=?,gioiTinh=?,sdt=?,email=?,trangThai=? WHERE id=?");
                    ps.setString(1, txtTen.getText().trim());
                    setDate(ps, 2, txtNgaySinh.getText().trim());
                    ps.setString(3, txtGT.getText().trim());
                    ps.setString(4, txtSdt.getText().trim());
                    ps.setString(5, txtEmail.getText().trim());
                    ps.setString(6, (String) cbTT.getSelectedItem());
                    ps.setInt(7, id);
                    ps.executeUpdate();
                }
                loadTable(); dlg.dispose();
            } catch (Exception ex) { JOptionPane.showMessageDialog(dlg, "Lỗi: " + ex.getMessage()); }
        });
        dlg.setVisible(true);
    }

    private JTextField fld() {
        JTextField f = new JTextField();
        f.setFont(UITheme.FONT_LABEL);
        f.setPreferredSize(new Dimension(260, 32));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        return f;
    }

    private void addFRow(JPanel p, GridBagConstraints gc, int y, String lbl, JComponent field) {
        gc.gridx=0; gc.gridy=y; gc.weightx=0;
        JLabel l = new JLabel(lbl); l.setFont(UITheme.FONT_BOLD); l.setForeground(UITheme.TEXT_SECONDARY);
        l.setPreferredSize(new Dimension(190, 26)); p.add(l, gc);
        gc.gridx=1; gc.weightx=1; p.add(field, gc);
    }

    private void setDate(PreparedStatement ps, int idx, String val) throws SQLException {
        if (val.isEmpty()) ps.setNull(idx, Types.DATE);
        else ps.setDate(idx, Date.valueOf(val));
    }

    private String str(Object o) { return o == null ? "" : o.toString(); }
}