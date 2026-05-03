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

public class ThamGiaForm extends JPanel {

    private StyledTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private JComboBox<String> cbTrangThai, cbNam;

    public ThamGiaForm() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));
        JLabel title = new JLabel("Quản lý Tham gia");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);
        JButton btnAdd = UITheme.primaryButton("+ Đăng ký mới");
        header.add(title, BorderLayout.WEST);
        header.add(btnAdd, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Filter
        JPanel filterCard = createCard();
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 8));
        txtSearch = new JTextField(18);
        txtSearch.setFont(UITheme.FONT_LABEL);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        cbTrangThai = makeCombo(new String[]{"Trạng thái","Đăng ký","Đã tham gia","Vắng"});
        cbNam = makeCombo(new String[]{"Năm","2024","2025","2026"});
        JButton btnSearch = UITheme.primaryButton("🔍 Tìm");
        JButton btnReset = UITheme.outlineButton("↺ Đặt lại");
        JButton btnExport = UITheme.outlineButton("📥 Xuất Excel");
        filterCard.add(new JLabel("Tìm hội viên:"));
        filterCard.add(txtSearch);
        filterCard.add(cbTrangThai);
        filterCard.add(cbNam);
        filterCard.add(btnSearch);
        filterCard.add(btnReset);
        filterCard.add(Box.createHorizontalStrut(10));
        filterCard.add(btnExport);

        // Table
        model = new DefaultTableModel(new String[]{
            "ID","Hội viên","Hoạt động","Ngày đăng ký","Trạng thái","Ghi chú"
        }, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new StyledTable(model);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);

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
        JLabel tblTitle = new JLabel("Danh sách đăng ký tham gia");
        tblTitle.setFont(UITheme.FONT_HEADING);
        tblTitle.setForeground(UITheme.TEXT_PRIMARY);

        JPanel actBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actBtns.setOpaque(false);
        JButton btnEdit = UITheme.outlineButton("✏ Sửa TT");
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
        btnExport.addActionListener(e -> ExcelExporter.exportToCSV(table, "ThamGia", this));
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

    private JComboBox<String> makeCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(UITheme.FONT_LABEL);
        cb.setBackground(Color.WHITE);
        cb.setPreferredSize(new Dimension(130, 32));
        return cb;
    }

    void loadTable() {
        model.setRowCount(0);
        try (Connection conn = DatabaseHelper.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(
                 "SELECT tg.id, hv.tenHoiVien, hd.tenHoatDong, tg.ngayDangKy, tg.trangThai, tg.ghiChu " +
                 "FROM ThamGia tg JOIN HoiVien hv ON tg.idHoiVien=hv.id " +
                 "JOIN HoatDong hd ON tg.idHoatDong=hd.id ORDER BY tg.id DESC")) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"), rs.getString("tenHoiVien"),
                    rs.getString("tenHoatDong"), rs.getTimestamp("ngayDangKy"),
                    rs.getString("trangThai"), rs.getString("ghiChu")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void search() {
        model.setRowCount(0);
        String kw = txtSearch.getText().trim();
        String tt = (String) cbTrangThai.getSelectedItem();
        String nam = (String) cbNam.getSelectedItem();

        StringBuilder sql = new StringBuilder(
            "SELECT tg.id, hv.tenHoiVien, hd.tenHoatDong, tg.ngayDangKy, tg.trangThai, tg.ghiChu " +
            "FROM ThamGia tg JOIN HoiVien hv ON tg.idHoiVien=hv.id " +
            "JOIN HoatDong hd ON tg.idHoatDong=hd.id WHERE hv.tenHoiVien LIKE ?");
        if (!"Trạng thái".equals(tt)) sql.append(" AND tg.trangThai=N'").append(tt).append("'");
        if (!"Năm".equals(nam)) sql.append(" AND YEAR(tg.ngayDangKy)=").append(nam);
        sql.append(" ORDER BY tg.id DESC");

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, "%" + kw + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"), rs.getString("tenHoiVien"),
                    rs.getString("tenHoatDong"), rs.getTimestamp("ngayDangKy"),
                    rs.getString("trangThai"), rs.getString("ghiChu")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showDetail(int row) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chi tiết đăng ký", true);
        dlg.setSize(420, 320);
        dlg.setLocationRelativeTo(this);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);

        JPanel topBar = new JPanel();
        topBar.setBackground(UITheme.PRIMARY);
        topBar.setBorder(new EmptyBorder(14, 20, 14, 20));
        JLabel ttl = new JLabel("✅  Chi tiết đăng ký tham gia");
        ttl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        ttl.setForeground(Color.WHITE);
        topBar.add(ttl);

        JPanel info = new JPanel(new GridBagLayout());
        info.setBackground(Color.WHITE);
        info.setBorder(new EmptyBorder(18, 24, 10, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 4, 6, 4);
        gc.anchor = GridBagConstraints.WEST;

        addRow(info, gc, 0, "Hội viên:", str(model.getValueAt(row, 1)));
        addRow(info, gc, 1, "Hoạt động:", str(model.getValueAt(row, 2)));
        addRow(info, gc, 2, "Ngày đăng ký:", str(model.getValueAt(row, 3)));
        addRow(info, gc, 3, "Trạng thái:", str(model.getValueAt(row, 4)));
        addRow(info, gc, 4, "Ghi chú:", str(model.getValueAt(row, 5)));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        btns.setBackground(Color.decode("#F8FAFC"));
        btns.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));
        JButton btnClose = UITheme.primaryButton("Đóng");
        btns.add(btnClose);
        content.add(topBar, BorderLayout.NORTH);
        content.add(info, BorderLayout.CENTER);
        content.add(btns, BorderLayout.SOUTH);
        dlg.add(content);
        btnClose.addActionListener(e -> dlg.dispose());
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
        int c = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn xóa đăng ký tham gia này không?",
            "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM ThamGia WHERE id=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
                loadTable();
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage()); }
        }
    }

    private void openForm(Integer row) {
        boolean isEdit = row != null;
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            isEdit ? "Cập nhật trạng thái" : "Đăng ký tham gia", true);
        dlg.setSize(460, 400);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);

        JPanel fh = new JPanel(new BorderLayout());
        fh.setBackground(UITheme.PRIMARY);
        fh.setBorder(new EmptyBorder(14, 20, 14, 20));
        JLabel hl = new JLabel(isEdit ? "✏  Cập nhật trạng thái" : "➕  Đăng ký tham gia mới");
        hl.setFont(new Font("Segoe UI", Font.BOLD, 15)); hl.setForeground(Color.WHITE);
        fh.add(hl);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setBackground(Color.WHITE);
        fields.setBorder(new EmptyBorder(18, 24, 10, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 4, 6, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;

        // Load combo data
        JComboBox<ComboItem> cbHV = new JComboBox<>();
        JComboBox<ComboItem> cbHD = new JComboBox<>();
        cbHV.setFont(UITheme.FONT_LABEL);
        cbHD.setFont(UITheme.FONT_LABEL);
        String[] ttItems = {"Đăng ký", "Đã tham gia", "Vắng"};
        JComboBox<String> cbTT = new JComboBox<>(ttItems);
        cbTT.setFont(UITheme.FONT_LABEL);
        JTextField txtGhiChu = new JTextField();
        txtGhiChu.setFont(UITheme.FONT_LABEL);
        txtGhiChu.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));

        try (Connection conn = DatabaseHelper.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT id, tenHoiVien FROM HoiVien");
            while (rs.next()) cbHV.addItem(new ComboItem(rs.getInt("id"), rs.getString("tenHoiVien")));
            rs = conn.createStatement().executeQuery("SELECT id, tenHoatDong FROM HoatDong");
            while (rs.next()) cbHD.addItem(new ComboItem(rs.getInt("id"), rs.getString("tenHoatDong")));
        } catch (Exception e) {}

        gc.gridx=0; gc.gridy=0; gc.weightx=0;
        JLabel lHV = new JLabel("Hội viên *"); lHV.setFont(UITheme.FONT_BOLD); lHV.setForeground(UITheme.TEXT_SECONDARY);
        lHV.setPreferredSize(new Dimension(160, 26)); fields.add(lHV, gc);
        gc.gridx=1; gc.weightx=1; fields.add(cbHV, gc);

        gc.gridx=0; gc.gridy=1; gc.weightx=0;
        JLabel lHD = new JLabel("Hoạt động *"); lHD.setFont(UITheme.FONT_BOLD); lHD.setForeground(UITheme.TEXT_SECONDARY);
        lHD.setPreferredSize(new Dimension(160, 26)); fields.add(lHD, gc);
        gc.gridx=1; gc.weightx=1; fields.add(cbHD, gc);

        gc.gridx=0; gc.gridy=2; gc.weightx=0;
        JLabel lTT = new JLabel("Trạng thái"); lTT.setFont(UITheme.FONT_BOLD); lTT.setForeground(UITheme.TEXT_SECONDARY);
        lTT.setPreferredSize(new Dimension(160, 26)); fields.add(lTT, gc);
        gc.gridx=1; gc.weightx=1; fields.add(cbTT, gc);

        gc.gridx=0; gc.gridy=3; gc.weightx=0;
        JLabel lGC = new JLabel("Ghi chú"); lGC.setFont(UITheme.FONT_BOLD); lGC.setForeground(UITheme.TEXT_SECONDARY);
        lGC.setPreferredSize(new Dimension(160, 26)); fields.add(lGC, gc);
        gc.gridx=1; gc.weightx=1; fields.add(txtGhiChu, gc);

        if (isEdit) {
            cbHV.setEnabled(false);
            cbHD.setEnabled(false);
            cbTT.setSelectedItem(str(model.getValueAt(row, 4)));
            txtGhiChu.setText(str(model.getValueAt(row, 5)));
        }

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        btns.setBackground(Color.decode("#F8FAFC"));
        btns.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));
        JButton btnC = UITheme.outlineButton("Hủy"), btnS = UITheme.primaryButton(isEdit ? "Lưu" : "Đăng ký");
        btns.add(btnC); btns.add(btnS);

        content.add(fh, BorderLayout.NORTH);
        content.add(fields, BorderLayout.CENTER);
        content.add(btns, BorderLayout.SOUTH);
        dlg.add(content);
        btnC.addActionListener(e -> dlg.dispose());
        btnS.addActionListener(e -> {
            try (Connection conn = DatabaseHelper.getConnection()) {
                if (!isEdit) {
                    ComboItem hv = (ComboItem) cbHV.getSelectedItem();
                    ComboItem hd = (ComboItem) cbHD.getSelectedItem();
                    if (hv == null || hd == null) { JOptionPane.showMessageDialog(dlg, "Chọn hội viên và hoạt động!"); return; }
                    PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO ThamGia(idHoiVien,idHoatDong,trangThai,ghiChu) VALUES(?,?,?,?)");
                    ps.setInt(1, hv.id); ps.setInt(2, hd.id);
                    ps.setString(3, (String) cbTT.getSelectedItem());
                    ps.setString(4, txtGhiChu.getText().trim());
                    ps.executeUpdate();
                } else {
                    int id = (int) model.getValueAt(row, 0);
                    PreparedStatement ps = conn.prepareStatement("UPDATE ThamGia SET trangThai=?,ghiChu=? WHERE id=?");
                    ps.setString(1, (String) cbTT.getSelectedItem());
                    ps.setString(2, txtGhiChu.getText().trim());
                    ps.setInt(3, id);
                    ps.executeUpdate();
                }
                loadTable(); dlg.dispose();
            } catch (Exception ex) { JOptionPane.showMessageDialog(dlg, "Lỗi: " + ex.getMessage()); }
        });
        dlg.setVisible(true);
    }

    private String str(Object o) { return o == null ? "" : o.toString(); }

    static class ComboItem {
        int id; String name;
        ComboItem(int id, String name) { this.id=id; this.name=name; }
        public String toString() { return name; }
    }
}