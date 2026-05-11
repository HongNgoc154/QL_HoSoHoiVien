package View;

import Util.*;
import database.DatabaseHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.*;

/**
 * ThamGiaForm – Quản lý tham gia hoạt động.
 * ✅ Thêm đăng ký tham gia: nhập mã hội viên (tự điền tên), chọn hoạt động, chọn trạng thái
 * ✅ Kiểm tra trùng đăng ký, kiểm tra hội viên "Đã rời"
 * ✅ Sửa trạng thái tham gia
 * ✅ Xóa đăng ký
 */
public class ThamGiaForm extends JPanel {

    private StyledTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private JComboBox<String> cbTrangThai;

    public ThamGiaForm() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(22, 26, 22, 26));
        buildUI();
        loadTable();
    }

    // ══════════════════════════════════════════════════════════════════
    //  BUILD UI
    // ══════════════════════════════════════════════════════════════════
    private void buildUI() {
        // ── HEADER ──────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 14, 0));
        header.add(UITheme.pageTitlePanel("Quản lý Tham gia",
                "Theo dõi đăng ký và tham dự hoạt động"), BorderLayout.WEST);
        JButton btnAdd = UITheme.primaryButton("  ＋  Thêm đăng ký");
        btnAdd.setPreferredSize(new Dimension(170, 36));
        btnAdd.addActionListener(e -> openForm(null));
        header.add(btnAdd, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── CENTER ──────────────────────────────────────────────────
        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        center.add(buildFilterCard(), BorderLayout.NORTH);
        center.add(buildTableCard(),  BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    // ── FILTER CARD ────────────────────────────────────────────────
    private JPanel buildFilterCard() {
        JPanel card = UITheme.cardPanel(new BorderLayout());
        card.setBorder(new EmptyBorder(10, 14, 10, 14));

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bar.setOpaque(false);

        txtSearch = new JTextField(16);
        JPanel sw = UITheme.searchField(txtSearch, "Tìm tên hội viên, mã HV...");

        cbTrangThai = new JComboBox<>(new String[]{"Trạng thái","Đã tham gia","Vắng","Đăng ký","Đã đăng ký","Chờ duyệt"});
        UITheme.styleCombo(cbTrangThai);

        JButton btnSearch = UITheme.primaryButton("Tìm");
        JButton btnReset  = UITheme.outlineButton("Đặt lại");
        JButton btnExport = UITheme.outlineButton("Xuất");
        btnSearch.setPreferredSize(new Dimension(90, 34));
        btnReset .setPreferredSize(new Dimension(96, 34));
        btnExport.setPreferredSize(new Dimension(90, 34));

        btnSearch.addActionListener(e -> search());
        btnReset .addActionListener(e -> { txtSearch.setText(""); cbTrangThai.setSelectedIndex(0); loadTable(); });
        btnExport.addActionListener(e -> ExcelExporter.exportToCSV(table, "ThamGia", this));

        bar.add(sw); bar.add(cbTrangThai);
        bar.add(btnSearch); bar.add(btnReset); bar.add(btnExport);
        card.add(bar, BorderLayout.CENTER);
        return card;
    }

    // ── TABLE CARD ─────────────────────────────────────────────────
    private JPanel buildTableCard() {
        model = new DefaultTableModel(
            new String[]{"ID","Mã HV","Họ tên hội viên","Hoạt động","Trạng thái","Thời gian đăng ký"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new StyledTable(model);
        int[] w = {50, 80, 180, 220, 110, 160};
        for (int i = 0; i < w.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);

        JPanel card = UITheme.cardPanel(new BorderLayout());
        card.add(buildCardHeader(), BorderLayout.NORTH);
        card.add(UITheme.styledScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    private JPanel buildCardHeader() {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, UITheme.PRIMARY_LIGHT, getWidth(), 0, Color.WHITE));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(UITheme.BORDER_COLOR);
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2.dispose(); super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(9, 14, 9, 14));
        JLabel lbl = new JLabel("Danh sách tham gia");
        lbl.setFont(UITheme.FONT_BOLD); lbl.setForeground(UITheme.TEXT_PRIMARY);
        p.add(lbl, BorderLayout.WEST);

        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        acts.setOpaque(false);
        JButton btnEdit = UITheme.outlineButton("Chỉnh sửa");
        JButton btnDel  = UITheme.dangerButton("Xóa");
        btnEdit.addActionListener(e -> editSelected());
        btnDel .addActionListener(e -> deleteSelected());
        acts.add(btnEdit); acts.add(btnDel);
        p.add(acts, BorderLayout.EAST);
        return p;
    }

    // ══════════════════════════════════════════════════════════════════
    //  LOAD / SEARCH
    // ══════════════════════════════════════════════════════════════════
    public void loadTable() {
        model.setRowCount(0);
        String sql = "SELECT tg.id, hv.maHoiVien, hv.tenHoiVien, hd.tenHoatDong, "
                   + "tg.trangThai, tg.ngayDangKy "
                   + "FROM ThamGia tg "
                   + "JOIN HoiVien hv ON tg.idHoiVien=hv.id "
                   + "JOIN HoatDong hd ON tg.idHoatDong=hd.id "
                   + "ORDER BY tg.id DESC";
        try (Connection c = DatabaseHelper.getConnection();
             ResultSet rs = c.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"), rs.getString("maHoiVien"), rs.getString("tenHoiVien"),
                    rs.getString("tenHoatDong"), rs.getString("trangThai"), rs.getTimestamp("ngayDangKy")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void search() {
        model.setRowCount(0);
        String kw = txtSearch.getText().trim();
        String tt = (String) cbTrangThai.getSelectedItem();
        StringBuilder sql = new StringBuilder(
            "SELECT tg.id, hv.maHoiVien, hv.tenHoiVien, hd.tenHoatDong, tg.trangThai, tg.ngayDangKy "
          + "FROM ThamGia tg JOIN HoiVien hv ON tg.idHoiVien=hv.id "
          + "JOIN HoatDong hd ON tg.idHoatDong=hd.id "
          + "WHERE (hv.tenHoiVien LIKE ? OR hv.maHoiVien LIKE ?)");
        if (!"Trạng thái".equals(tt)) sql.append(" AND tg.trangThai=N'").append(tt).append("'");
        sql.append(" ORDER BY tg.id DESC");
        try (Connection c = DatabaseHelper.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            ps.setString(1, "%" + kw + "%"); ps.setString(2, "%" + kw + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"), rs.getString("maHoiVien"), rs.getString("tenHoiVien"),
                    rs.getString("tenHoatDong"), rs.getString("trangThai"), rs.getTimestamp("ngayDangKy")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ══════════════════════════════════════════════════════════════════
    //  CRUD ACTIONS
    // ══════════════════════════════════════════════════════════════════
    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { msg("Vui lòng chọn dòng cần sửa!"); return; }
        openForm(row);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { msg("Vui lòng chọn dòng cần xóa!"); return; }
        int id = (int) model.getValueAt(row, 0);
        int ok = JOptionPane.showConfirmDialog(this,
            "Xác nhận xóa đăng ký tham gia này?\n"
          + "Hội viên: " + model.getValueAt(row, 2) + "\n"
          + "Hoạt động: " + model.getValueAt(row, 3),
            "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            try (Connection c = DatabaseHelper.getConnection();
                 PreparedStatement ps = c.prepareStatement("DELETE FROM ThamGia WHERE id=?")) {
                ps.setInt(1, id); ps.executeUpdate(); loadTable();
            } catch (Exception e) { msg("Lỗi: " + e.getMessage()); }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  OPEN FORM – THÊM / SỬA ĐẦY ĐỦ
    // ══════════════════════════════════════════════════════════════════
    private void openForm(Integer row) {
        boolean isEdit = row != null;
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            isEdit ? "✏  Cập nhật trạng thái tham gia" : "➕  Thêm đăng ký tham gia", true);
        dlg.setSize(500, isEdit ? 300 : 400);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);
        dlg.setLayout(new BorderLayout());

        // ── Dialog header ──
        JPanel fh = new JPanel(new BorderLayout());
        fh.setBackground(UITheme.PRIMARY);
        fh.setBorder(new EmptyBorder(14, 20, 14, 20));
        JLabel hl = new JLabel(isEdit ? "✏  Cập nhật trạng thái tham gia" : "➕  Thêm đăng ký tham gia");
        hl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        hl.setForeground(Color.WHITE);
        fh.add(hl);
        dlg.add(fh, BorderLayout.NORTH);

        // ── Fields ──
        JPanel fields = new JPanel(new GridBagLayout());
        fields.setBackground(Color.WHITE);
        fields.setBorder(new EmptyBorder(18, 24, 10, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 4, 8, 4);
        gc.fill   = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.WEST;

        JComboBox<String> cbTT = new JComboBox<>(new String[]{"Đăng ký","Đã đăng ký","Đã tham gia","Vắng","Chờ duyệt"});
        UITheme.styleCombo(cbTT);
        cbTT.setPreferredSize(new Dimension(280, 34));

        if (isEdit) {
            // ── CHẾ ĐỘ SỬA: chỉ đổi trạng thái ──
            cbTT.setSelectedItem(str(model.getValueAt(row, 4)));

            addFRow(fields, gc, 0, "Hội viên:",  new JLabel(str(model.getValueAt(row, 2))));
            addFRow(fields, gc, 1, "Hoạt động:", new JLabel(str(model.getValueAt(row, 3))));
            addFRow(fields, gc, 2, "Trạng thái *", cbTT);

        } else {
            // ── CHẾ ĐỘ THÊM MỚI ──
            JTextField txtMaHV  = styledFld("Nhập mã hội viên (VD: HV001)");
            JTextField txtTenHV = styledFld("Tên hội viên (tự điền)");
            txtTenHV.setEditable(false);
            txtTenHV.setBackground(new Color(245, 247, 250));

            // Auto-fill tên khi nhập mã
            txtMaHV.addKeyListener(new KeyAdapter() {
                public void keyReleased(KeyEvent e) {
                    fillMemberName(txtMaHV.getText().trim(), txtTenHV);
                }
            });

            // ComboBox danh sách hoạt động
            JComboBox<ActivityItem> cbHoatDong = new JComboBox<>();
            cbHoatDong.setPreferredSize(new Dimension(280, 34));
            cbHoatDong.setFont(UITheme.FONT_LABEL);
            loadActiveActivities(cbHoatDong);

            addFRow(fields, gc, 0, "Mã hội viên *",  txtMaHV);
            addFRow(fields, gc, 1, "Tên hội viên",   txtTenHV);
            addFRow(fields, gc, 2, "Hoạt động *",    cbHoatDong);
            addFRow(fields, gc, 3, "Trạng thái *",   cbTT);

            // Ghi đè nút Lưu cho thêm mới
            JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
            btns.setBackground(Color.decode("#F8FAFC"));
            btns.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));
            JButton btnC = UITheme.outlineButton("Hủy");
            JButton btnS = UITheme.primaryButton("Thêm mới");
            btns.add(btnC); btns.add(btnS);
            dlg.add(fields, BorderLayout.CENTER);
            dlg.add(btns,   BorderLayout.SOUTH);

            btnC.addActionListener(e -> dlg.dispose());
            btnS.addActionListener(e -> {
                String maHV = txtMaHV.getText().trim();
                String tenHV = txtTenHV.getText().trim();
                if (maHV.isEmpty()) { msg(dlg, "Vui lòng nhập mã hội viên."); return; }
                if (tenHV.isEmpty()) { msg(dlg, "Mã hội viên không tìm thấy trong hệ thống."); return; }
                if (cbHoatDong.getItemCount() == 0) { msg(dlg, "Không có hoạt động nào khả dụng."); return; }
                ActivityItem activity = (ActivityItem) cbHoatDong.getSelectedItem();
                String trangThai = (String) cbTT.getSelectedItem();

                try (Connection c = DatabaseHelper.getConnection()) {
                    // Lấy idHoiVien từ mã
                    PreparedStatement psHV = c.prepareStatement(
                        "SELECT id, trangThai FROM HoiVien WHERE maHoiVien=?");
                    psHV.setString(1, maHV);
                    ResultSet rsHV = psHV.executeQuery();
                    if (!rsHV.next()) { msg(dlg, "Hội viên không tồn tại."); return; }
                    if ("Đã rời".equalsIgnoreCase(rsHV.getString("trangThai"))) {
                        msg(dlg, "Hội viên đã rời hội và không thể tham gia hoạt động."); return;
                    }
                    int idHV = rsHV.getInt("id");
                    int idHD = activity.id;

                    // Kiểm tra trùng
                    PreparedStatement chk = c.prepareStatement(
                        "SELECT 1 FROM ThamGia WHERE idHoiVien=? AND idHoatDong=?");
                    chk.setInt(1, idHV); chk.setInt(2, idHD);
                    if (chk.executeQuery().next()) {
                        msg(dlg, "Hội viên này đã đăng ký hoạt động được chọn rồi."); return;
                    }

                    // Insert ThamGia
                    PreparedStatement ins = c.prepareStatement(
                        "INSERT INTO ThamGia(idHoiVien,idHoatDong,trangThai,ngayDangKy) VALUES(?,?,?,GETDATE())");
                    ins.setInt(1, idHV);
                    ins.setInt(2, idHD);
                    ins.setString(3, trangThai);
                    ins.executeUpdate();

                    loadTable();
                    dlg.dispose();
                    JOptionPane.showMessageDialog(this,
                        "✅ Đã đăng ký tham gia thành công!\n"
                      + "Hội viên: " + tenHV + "\nHoạt động: " + activity.name);
                } catch (Exception ex) { msg(dlg, "Lỗi: " + ex.getMessage()); }
            });
            dlg.setVisible(true);
            return; // Trả về sớm (đã add components rồi)
        }

        // ── CHẾ ĐỘ SỬA: layout đơn giản ──
        dlg.add(fields, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        btns.setBackground(Color.decode("#F8FAFC"));
        btns.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));
        JButton btnC = UITheme.outlineButton("Hủy");
        JButton btnS = UITheme.primaryButton("Lưu");
        btns.add(btnC); btns.add(btnS);
        dlg.add(btns, BorderLayout.SOUTH);

        btnC.addActionListener(e -> dlg.dispose());
        final JComboBox<String> finalCbTT = cbTT;
        btnS.addActionListener(e -> {
            int id = (int) model.getValueAt(row, 0);
            try (Connection c = DatabaseHelper.getConnection();
                 PreparedStatement ps = c.prepareStatement("UPDATE ThamGia SET trangThai=? WHERE id=?")) {
                ps.setString(1, (String) finalCbTT.getSelectedItem());
                ps.setInt(2, id);
                ps.executeUpdate();
                loadTable();
                dlg.dispose();
            } catch (Exception ex) { msg(dlg, "Lỗi: " + ex.getMessage()); }
        });

        dlg.setVisible(true);
    }

    // ══════════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════════

    /** Điền tên hội viên khi nhập mã */
    private void fillMemberName(String ma, JTextField txtTen) {
        if (ma.isEmpty()) { txtTen.setText(""); return; }
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "SELECT tenHoiVien FROM HoiVien WHERE maHoiVien=?")) {
            ps.setString(1, ma);
            ResultSet rs = ps.executeQuery();
            txtTen.setText(rs.next() ? rs.getString("tenHoiVien") : "");
        } catch (Exception ignored) {}
    }

    /** Load danh sách hoạt động vào ComboBox (lọc Sắp diễn ra + Đang diễn ra) */
    private void loadActiveActivities(JComboBox<ActivityItem> cb) {
        cb.removeAllItems();
        String sql = "SELECT id, tenHoatDong FROM HoatDong "
                   + "WHERE trangThai IN (N'Sắp diễn ra', N'Đang diễn ra', N'Hoạt động') "
                   + "OR thoiGianKetThuc >= GETDATE() "
                   + "ORDER BY thoiGianBatDau";
        try (Connection c = DatabaseHelper.getConnection();
             ResultSet rs = c.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                cb.addItem(new ActivityItem(rs.getInt("id"), rs.getString("tenHoatDong")));
            }
        } catch (Exception e) {
            // Fallback: lấy tất cả nếu không lọc được
            try (Connection c = DatabaseHelper.getConnection();
                 ResultSet rs = c.createStatement().executeQuery(
                     "SELECT id, tenHoatDong FROM HoatDong ORDER BY id DESC")) {
                while (rs.next()) cb.addItem(new ActivityItem(rs.getInt("id"), rs.getString("tenHoatDong")));
            } catch (Exception ignored) {}
        }
    }

    private JTextField styledFld(String toolTip) {
        JTextField tf = new JTextField();
        tf.setFont(UITheme.FONT_LABEL);
        tf.setPreferredSize(new Dimension(280, 34));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        tf.setToolTipText(toolTip);
        return tf;
    }

    private void addFRow(JPanel p, GridBagConstraints gc, int y, String label, Component comp) {
        gc.gridx = 0; gc.gridy = y; gc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BOLD); lbl.setForeground(UITheme.TEXT_SECONDARY);
        lbl.setPreferredSize(new Dimension(150, 28));
        p.add(lbl, gc);
        gc.gridx = 1; gc.weightx = 1;
        p.add(comp, gc);
    }

    private void msg(String s) { JOptionPane.showMessageDialog(this, s); }
    private void msg(Component parent, String s) { JOptionPane.showMessageDialog(parent, s); }
    private String str(Object o) { return o == null ? "" : o.toString(); }

    // ══════════════════════════════════════════════════════════════════
    //  INNER CLASS: Item cho ComboBox hoạt động
    // ══════════════════════════════════════════════════════════════════
    private static class ActivityItem {
        final int    id;
        final String name;
        ActivityItem(int id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }
}