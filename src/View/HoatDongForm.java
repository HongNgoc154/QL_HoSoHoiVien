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
import java.text.SimpleDateFormat;

public class HoatDongForm extends JPanel {

    private StyledTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private JComboBox<String> cbThang, cbNam, cbLoai, cbTrangThai;

    public HoatDongForm() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));
        JLabel title = new JLabel("Quản lý Hoạt động");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);
        JButton btnAdd = UITheme.primaryButton("+ Thêm mới");
        header.add(title, BorderLayout.WEST);
        header.add(btnAdd, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Filter card
        JPanel filterCard = createCard();
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 8));

        txtSearch = createSearchField("Tìm theo tên hoạt động...");
        cbThang = makeCombo(new String[]{"Tháng","T1","T2","T3","T4","T5","T6","T7","T8","T9","T10","T11","T12"});
        cbNam = makeCombo(new String[]{"Năm","2023","2024","2025","2026"});
        cbLoai = makeCombo(new String[]{"Loại","Hội thảo","Workshop","Cuộc thi","Khác"});
        cbTrangThai = makeCombo(new String[]{"Trạng thái","Sắp diễn ra","Đang diễn ra","Đã kết thúc"});

        JButton btnSearch = UITheme.primaryButton("🔍 Tìm kiếm");
        JButton btnReset = UITheme.outlineButton("↺ Đặt lại");
        JButton btnExport = UITheme.outlineButton("📥 Xuất Excel");

        filterCard.add(new JLabel("Tìm:"));
        filterCard.add(txtSearch);
        filterCard.add(cbThang);
        filterCard.add(cbNam);
        filterCard.add(cbLoai);
        filterCard.add(cbTrangThai);
        filterCard.add(btnSearch);
        filterCard.add(btnReset);
        filterCard.add(Box.createHorizontalStrut(10));
        filterCard.add(btnExport);

        // Table card
        model = new DefaultTableModel(new String[]{
            "ID","Tên hoạt động","Loại","Thời gian bắt đầu","Thời gian kết thúc","Địa điểm","Trạng thái"
        }, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new StyledTable(model);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(220);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);
        table.getColumnModel().getColumn(4).setPreferredWidth(150);

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
        JLabel tblTitle = new JLabel("Danh sách hoạt động");
        tblTitle.setFont(UITheme.FONT_HEADING);
        tblTitle.setForeground(UITheme.TEXT_PRIMARY);

        JPanel actBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actBtns.setOpaque(false);
        JButton btnEdit = UITheme.outlineButton("✏ Sửa");
        JButton btnDel = UITheme.dangerButton("🗑 Xóa");
        actBtns.add(btnEdit);
        actBtns.add(btnDel);

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
        btnExport.addActionListener(e -> ExcelExporter.exportToCSV(table, "HoatDong", this));
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

    private JTextField createSearchField(String hint) {
        JTextField f = new JTextField(18);
        f.setFont(UITheme.FONT_LABEL);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return f;
    }

    private JComboBox<String> makeCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(UITheme.FONT_LABEL);
        cb.setBackground(Color.WHITE);
        cb.setPreferredSize(new Dimension(120, 32));
        return cb;
    }

    void loadTable() {
        model.setRowCount(0);
        try (Connection conn = DatabaseHelper.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(
                 "SELECT * FROM HoatDong ORDER BY thoiGianBatDau DESC")) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"), rs.getString("tenHoatDong"),
                    rs.getString("loaiHoatDong"), rs.getTimestamp("thoiGianBatDau"),
                    rs.getTimestamp("thoiGianKetThuc"), rs.getString("diaDiem"),
                    rs.getString("trangThai")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void search() {
        model.setRowCount(0);
        String kw = txtSearch.getText().trim();
        String thang = (String) cbThang.getSelectedItem();
        String nam = (String) cbNam.getSelectedItem();
        String loai = (String) cbLoai.getSelectedItem();
        String tt = (String) cbTrangThai.getSelectedItem();

        StringBuilder sql = new StringBuilder("SELECT * FROM HoatDong WHERE tenHoatDong LIKE ?");
        if (!"Tháng".equals(thang)) sql.append(" AND MONTH(thoiGianBatDau)=").append(thang.replace("T",""));
        if (!"Năm".equals(nam)) sql.append(" AND YEAR(thoiGianBatDau)=").append(nam);
        if (!"Loại".equals(loai)) sql.append(" AND loaiHoatDong=N'").append(loai).append("'");
        if (!"Trạng thái".equals(tt)) sql.append(" AND trangThai=N'").append(tt).append("'");
        sql.append(" ORDER BY thoiGianBatDau DESC");

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, "%" + kw + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"), rs.getString("tenHoatDong"),
                    rs.getString("loaiHoatDong"), rs.getTimestamp("thoiGianBatDau"),
                    rs.getTimestamp("thoiGianKetThuc"), rs.getString("diaDiem"),
                    rs.getString("trangThai")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showDetail(int row) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chi tiết hoạt động", true);
        dlg.setSize(460, 380);
        dlg.setLocationRelativeTo(this);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UITheme.PRIMARY);
        topBar.setBorder(new EmptyBorder(14, 20, 14, 20));
        JLabel ttl = new JLabel("📅  " + str(model.getValueAt(row, 1)));
        ttl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        ttl.setForeground(Color.WHITE);
        topBar.add(ttl);

        JPanel info = new JPanel(new GridBagLayout());
        info.setBackground(Color.WHITE);
        info.setBorder(new EmptyBorder(18, 24, 10, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 4, 5, 4);
        gc.anchor = GridBagConstraints.WEST;

        addRow(info, gc, 0, "Loại hoạt động:", str(model.getValueAt(row, 2)));
        addRow(info, gc, 1, "Thời gian bắt đầu:", str(model.getValueAt(row, 3)));
        addRow(info, gc, 2, "Thời gian kết thúc:", str(model.getValueAt(row, 4)));
        addRow(info, gc, 3, "Địa điểm:", str(model.getValueAt(row, 5)));
        addRow(info, gc, 4, "Trạng thái:", str(model.getValueAt(row, 6)));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        btns.setBackground(Color.decode("#F8FAFC"));
        btns.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));
        JButton btnEdit = UITheme.outlineButton("Chỉnh sửa");
        JButton btnClose = UITheme.primaryButton("Đóng");
        btns.add(btnEdit);
        btns.add(btnClose);

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
        l.setPreferredSize(new Dimension(160, 26));
        p.add(l, gc);
        gc.gridx=1;
        JLabel v = new JLabel(val); v.setFont(UITheme.FONT_LABEL); v.setForeground(UITheme.TEXT_PRIMARY);
        p.add(v, gc);
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
        String name = str(model.getValueAt(row, 1));
        int c = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn xóa hoạt động\n\"" + name + "\" không?",
            "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM HoatDong WHERE id=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
                loadTable();
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage()); }
        }
    }

    private void openForm(Integer row) {
        boolean isEdit = row != null;
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            isEdit ? "Sửa hoạt động" : "Thêm hoạt động mới", true);
        dlg.setSize(500, 480);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);

        JPanel fh = new JPanel(new BorderLayout());
        fh.setBackground(UITheme.PRIMARY);
        fh.setBorder(new EmptyBorder(14, 20, 14, 20));
        JLabel hl = new JLabel(isEdit ? "✏  Sửa hoạt động" : "➕  Thêm hoạt động mới");
        hl.setFont(new Font("Segoe UI", Font.BOLD, 15)); hl.setForeground(Color.WHITE);
        fh.add(hl);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setBackground(Color.WHITE);
        fields.setBorder(new EmptyBorder(18, 24, 10, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 4, 6, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtTen = fld(), txtLoai = fld(), txtDiaDiem = fld();
        JTextField txtStart = fld(), txtEnd = fld(), txtMoTa = fld();
        String[] ttOpts = {"Sắp diễn ra","Đang diễn ra","Đã kết thúc"};
        JComboBox<String> cbTT = new JComboBox<>(ttOpts);
        cbTT.setFont(UITheme.FONT_LABEL);

        if (isEdit) {
            txtTen.setText(str(model.getValueAt(row, 1)));
            txtLoai.setText(str(model.getValueAt(row, 2)));
            txtStart.setText(str(model.getValueAt(row, 3)));
            txtEnd.setText(str(model.getValueAt(row, 4)));
            txtDiaDiem.setText(str(model.getValueAt(row, 5)));
            cbTT.setSelectedItem(str(model.getValueAt(row, 6)));
        }

        addFRow(fields, gc, 0, "Tên hoạt động *", txtTen);
        addFRow(fields, gc, 1, "Loại hoạt động", txtLoai);
        addFRow(fields, gc, 2, "Bắt đầu (yyyy-MM-dd HH:mm)", txtStart);
        addFRow(fields, gc, 3, "Kết thúc (yyyy-MM-dd HH:mm)", txtEnd);
        addFRow(fields, gc, 4, "Địa điểm", txtDiaDiem);
        addFRow(fields, gc, 5, "Trạng thái", cbTT);

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
            if (txtTen.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Tên hoạt động không được để trống!"); return;
            }
            try (Connection conn = DatabaseHelper.getConnection()) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                if (!isEdit) {
                    PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO HoatDong(tenHoatDong,loaiHoatDong,thoiGianBatDau,thoiGianKetThuc,diaDiem,trangThai) VALUES(?,?,?,?,?,?)");
                    ps.setString(1, txtTen.getText().trim());
                    ps.setString(2, txtLoai.getText().trim());
                    setTs(ps, 3, txtStart.getText().trim(), sdf);
                    setTs(ps, 4, txtEnd.getText().trim(), sdf);
                    ps.setString(5, txtDiaDiem.getText().trim());
                    ps.setString(6, (String) cbTT.getSelectedItem());
                    ps.executeUpdate();
                } else {
                    int id = (int) model.getValueAt(row, 0);
                    PreparedStatement ps = conn.prepareStatement(
                        "UPDATE HoatDong SET tenHoatDong=?,loaiHoatDong=?,thoiGianBatDau=?,thoiGianKetThuc=?,diaDiem=?,trangThai=? WHERE id=?");
                    ps.setString(1, txtTen.getText().trim());
                    ps.setString(2, txtLoai.getText().trim());
                    setTs(ps, 3, txtStart.getText().trim(), sdf);
                    setTs(ps, 4, txtEnd.getText().trim(), sdf);
                    ps.setString(5, txtDiaDiem.getText().trim());
                    ps.setString(6, (String) cbTT.getSelectedItem());
                    ps.setInt(7, id);
                    ps.executeUpdate();
                }
                loadTable();
                dlg.dispose();
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
        l.setPreferredSize(new Dimension(200, 26));
        p.add(l, gc); gc.gridx=1; gc.weightx=1; p.add(field, gc);
    }

    private void setTs(PreparedStatement ps, int i, String val, SimpleDateFormat sdf) throws Exception {
        if (val.isEmpty()) ps.setNull(i, Types.TIMESTAMP);
        else ps.setTimestamp(i, new Timestamp(sdf.parse(val).getTime()));
    }

    private String str(Object o) { return o == null ? "" : o.toString(); }
}