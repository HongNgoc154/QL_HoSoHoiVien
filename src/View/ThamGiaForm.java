package View;

import Util.*;
import dao.ArchiveDAO;
import dao.NhatKyDAO;
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
//        JButton btnExport = UITheme.outlineButton("Xuất");
        btnSearch.setPreferredSize(new Dimension(90, 34));
        btnReset .setPreferredSize(new Dimension(96, 34));
//        btnExport.setPreferredSize(new Dimension(90, 34));

        btnSearch.addActionListener(e -> search());
        btnReset .addActionListener(e -> { txtSearch.setText(""); cbTrangThai.setSelectedIndex(0); loadTable(); });
//        btnExport.addActionListener(e -> ExcelExporter.exportToCSV(table, "ThamGia", this));

        bar.add(sw); bar.add(cbTrangThai);
        bar.add(btnSearch); bar.add(btnReset); //bar.add(btnExport);
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
        JButton btnExport = UITheme.outlineButton("📥 Xuất Excel");
        JButton btnEdit = UITheme.outlineButton("Chỉnh sửa");
        JButton btnDel  = UITheme.dangerButton("Xóa");
        btnExport.addActionListener(e -> ExcelExporter.exportToCSV(table, "ThamGia", this));
        btnEdit.addActionListener(e -> editSelected());
        btnDel .addActionListener(e -> deleteSelected());
        acts.add(btnExport); acts.add(btnEdit); acts.add(btnDel);
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
        String tt = String.valueOf(model.getValueAt(row, 4));
        if (!("Đã từ chối".equalsIgnoreCase(tt) || "Đã hủy".equalsIgnoreCase(tt))) {
            msg("Chỉ được xóa khi đăng ký bị từ chối/hủy hoặc hoạt động đã kết thúc.");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
            "Xác nhận xóa đăng ký tham gia này?\n"
          + "Hội viên: " + model.getValueAt(row, 2) + "\n"
          + "Hoạt động: " + model.getValueAt(row, 3),
            "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            try (Connection c = DatabaseHelper.getConnection();
                 PreparedStatement ps = c.prepareStatement("DELETE FROM ThamGia WHERE id=?")) {
                ArchiveDAO.archiveByQuery("Tham gia", id, "ThamGia", "id", Session.getCurrentUserId(), "XÓA");
                ps.setInt(1, id); ps.executeUpdate(); loadTable();
                NhatKyDAO.log(Session.getCurrentUserId(), "XÓA", "Kho lưu trữ", "Xóa tham gia ID: " + id);
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
        dlg.setSize(isEdit ? 500 : 700, isEdit ? 300 : 500);
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
            // ── 1. HEADER ──────────────────────────────────────────────────
            JPanel body = new JPanel(new GridLayout(1, 2, 0, 0));
            body.setBackground(Color.WHITE);
 
            // ── 2. PANEL TRÁI: Thông tin hội viên ─────────────────────────
            JPanel leftPanel = new JPanel(new BorderLayout());
            leftPanel.setBackground(Color.WHITE);
            leftPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.BORDER_COLOR));
            leftPanel.add(buildSectionTitle("👤  Thông tin hội viên", UITheme.PRIMARY), BorderLayout.NORTH);
 
            JPanel leftFields = new JPanel(new GridBagLayout());
            leftFields.setBackground(Color.WHITE);
            leftFields.setBorder(new EmptyBorder(18, 20, 18, 20));
            GridBagConstraints lgc = new GridBagConstraints();
            lgc.insets = new Insets(7, 4, 7, 4);
            lgc.fill = GridBagConstraints.HORIZONTAL;
            lgc.anchor = GridBagConstraints.WEST;
 
            JTextField txtMaHV  = buildDlgField("Nhập mã hội viên (VD: HV001)");
            JTextField txtTenHV = buildDlgField("");
            txtTenHV.setEditable(false);
            txtTenHV.setBackground(new Color(245, 247, 250));
            txtTenHV.setForeground(UITheme.TEXT_SECONDARY);
 
            JComboBox<String> cbTT2 = new JComboBox<>(new String[]{
                "Đã đăng ký", "Đăng ký", "Chờ duyệt"
            });
            UITheme.styleCombo(cbTT2);
            cbTT2.setPreferredSize(new Dimension(250, 36));
 
            // Info box hiển thị hội viên tìm thấy
            JPanel infoBox = new JPanel(new GridLayout(2, 1, 0, 2)) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(UITheme.PRIMARY_LIGHT);
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                    g2.setColor(UITheme.BORDER_COLOR);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            infoBox.setOpaque(false);
            infoBox.setBorder(new EmptyBorder(7, 12, 7, 12));
            infoBox.setVisible(false);
            JLabel lblFoundName   = new JLabel("—");
            JLabel lblFoundStatus = new JLabel("");
            lblFoundName.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblFoundName.setForeground(UITheme.PRIMARY);
            lblFoundStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblFoundStatus.setForeground(UITheme.TEXT_SECONDARY);
            infoBox.add(lblFoundName);
            infoBox.add(lblFoundStatus);
 
            // Auto-fill khi nhập mã
            txtMaHV.addKeyListener(new KeyAdapter() {
                public void keyReleased(KeyEvent e) {
                    String ma = txtMaHV.getText().trim();
                    if (ma.isEmpty()) { txtTenHV.setText(""); infoBox.setVisible(false); return; }
                    try (Connection conn = DatabaseHelper.getConnection();
                         PreparedStatement ps = conn.prepareStatement(
                             "SELECT tenHoiVien, trangThai FROM HoiVien WHERE maHoiVien=?")) {
                        ps.setString(1, ma);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) {
                            String ten = rs.getString("tenHoiVien");
                            String tt  = rs.getString("trangThai");
                            txtTenHV.setText(ten);
                            lblFoundName.setText(ten);
                            lblFoundStatus.setText(ma + "  ·  " + (tt != null ? tt : ""));
                            boolean daRoi = "Đã rời".equalsIgnoreCase(tt);
                            lblFoundName.setForeground(daRoi ? UITheme.DANGER : UITheme.PRIMARY);
                            infoBox.setVisible(true);
                        } else {
                            txtTenHV.setText(""); infoBox.setVisible(false);
                        }
                    } catch (Exception ex) { /* ignore */ }
                    leftFields.revalidate(); leftFields.repaint();
                }
            });
 
            addFRow(leftFields, lgc, 0, "Mã hội viên *", txtMaHV);
            lgc.gridx = 0; lgc.gridy = 1; lgc.gridwidth = 2; lgc.weightx = 1;
            leftFields.add(infoBox, lgc);
            lgc.gridwidth = 1;
            addFRow(leftFields, lgc, 2, "Họ và tên",    txtTenHV);
            addFRow(leftFields, lgc, 3, "Trạng thái *", cbTT2);
 
            // Filler đẩy nội dung lên trên
            lgc.gridx = 0; lgc.gridy = 4; lgc.gridwidth = 2;
            lgc.weighty = 1; lgc.fill = GridBagConstraints.BOTH;
            leftFields.add(new JPanel() {{ setOpaque(false); }}, lgc);
 
            leftPanel.add(leftFields, BorderLayout.CENTER);
 
            // ── 3. PANEL PHẢI: Chọn hoạt động ─────────────────────────────
            JPanel rightPanel = new JPanel(new BorderLayout());
            rightPanel.setBackground(Color.WHITE);
            rightPanel.add(buildSectionTitle("📅  Chọn hoạt động", new Color(14, 118, 168)), BorderLayout.NORTH);
 
            JPanel rightContent = new JPanel(new BorderLayout(0, 8));
            rightContent.setBackground(Color.WHITE);
            rightContent.setBorder(new EmptyBorder(14, 20, 14, 20));
 
            // Ô tìm kiếm hoạt động
            JTextField txtActSearch = new JTextField(14);
            JPanel actSearchWrap = UITheme.searchField(txtActSearch, "Tìm hoạt động...");
            rightContent.add(actSearchWrap, BorderLayout.NORTH);
 
            // JList hoạt động với custom renderer
            DefaultListModel<ActivityItem> actListModel = new DefaultListModel<>();
            JList<ActivityItem> actJList = new JList<>(actListModel);
            actJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            actJList.setBackground(Color.WHITE);
            actJList.setFixedCellHeight(58);
            actJList.setBorder(BorderFactory.createEmptyBorder());
            actJList.setCellRenderer((list, value, idx, isSel, cf) -> {
                JPanel cell = new JPanel(new BorderLayout(10, 0));
                cell.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)));
                cell.setOpaque(true);
                cell.setBackground(isSel ? UITheme.PRIMARY_LIGHT : Color.WHITE);
 
                // Left dot
                JPanel dot = new JPanel() {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(isSel ? UITheme.PRIMARY : UITheme.ACCENT);
                        g2.fillOval(2, 2, 9, 9);
                        g2.dispose();
                    }
                };
                dot.setOpaque(false);
                dot.setPreferredSize(new Dimension(13, 13));
 
                // Text area
                JPanel textArea = new JPanel(new GridLayout(2, 1, 0, 2));
                textArea.setOpaque(false);
                JLabel nameLbl = new JLabel(value.name);
                nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                nameLbl.setForeground(isSel ? UITheme.PRIMARY : UITheme.TEXT_PRIMARY);
                JLabel metaLbl = new JLabel("📍 " + value.location + "   📅 " + value.date);
                metaLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                metaLbl.setForeground(UITheme.TEXT_SECONDARY);
                textArea.add(nameLbl);
                textArea.add(metaLbl);
 
                // Status badge
                JLabel badge = new JLabel("  " + value.status + "  ");
                badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
                badge.setOpaque(true);
                if ("Đang diễn ra".equals(value.status)) {
                    badge.setBackground(UITheme.SUCCESS_BG);
                    badge.setForeground(Color.decode("#065f46"));
                } else {
                    badge.setBackground(UITheme.INFO_BG);
                    badge.setForeground(Color.decode("#1e40af"));
                }
 
                cell.add(dot, BorderLayout.WEST);
                cell.add(textArea, BorderLayout.CENTER);
                cell.add(badge, BorderLayout.EAST);
                return cell;
            });
 
            // Load dữ liệu hoạt động từ DB
            java.util.List<ActivityItem> allActs = new java.util.ArrayList<>();
            String sqlAct = "SELECT id, tenHoatDong, "
                + "ISNULL(diaDiem,'—') diaDiem, "
                + "CONVERT(varchar,thoiGianBatDau,103) tgBD, "
                + "CASE WHEN GETDATE() BETWEEN thoiGianBatDau AND thoiGianKetThuc "
                + "THEN N'Đang diễn ra' ELSE N'Sắp diễn ra' END trangThai "
                + "FROM HoatDong "
                + "WHERE trangThai IN (N'Sắp diễn ra',N'Đang diễn ra',N'Hoạt động') "
                + "OR thoiGianKetThuc >= GETDATE() ORDER BY thoiGianBatDau";
            try (Connection cAct = DatabaseHelper.getConnection();
                 ResultSet rsAct = cAct.createStatement().executeQuery(sqlAct)) {
                while (rsAct.next()) {
                    ActivityItem ai = new ActivityItem(
                        rsAct.getInt("id"), rsAct.getString("tenHoatDong"));
                    ai.location = rsAct.getString("diaDiem");
                    ai.date     = rsAct.getString("tgBD") != null ? rsAct.getString("tgBD") : "—";
                    ai.status   = rsAct.getString("trangThai");
                    allActs.add(ai);
                    actListModel.addElement(ai);
                }
            } catch (Exception exAct) {
                try (Connection cAct = DatabaseHelper.getConnection();
                     ResultSet rsAct = cAct.createStatement().executeQuery(
                         "SELECT id, tenHoatDong FROM HoatDong ORDER BY id DESC")) {
                    while (rsAct.next()) {
                        ActivityItem ai = new ActivityItem(rsAct.getInt("id"), rsAct.getString("tenHoatDong"));
                        ai.location = "—"; ai.date = "—"; ai.status = "Sắp diễn ra";
                        allActs.add(ai); actListModel.addElement(ai);
                    }
                } catch (Exception ignored) {}
            }
            if (!actListModel.isEmpty()) actJList.setSelectedIndex(0);
 
            // Filter khi gõ tìm kiếm
            txtActSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                void filter() {
                    String kw = txtActSearch.getText().trim().toLowerCase();
                    actListModel.clear();
                    for (ActivityItem ai : allActs)
                        if (kw.isEmpty() || ai.name.toLowerCase().contains(kw))
                            actListModel.addElement(ai);
                    if (!actListModel.isEmpty()) actJList.setSelectedIndex(0);
                }
                public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            });
 
            JScrollPane actScroll = UITheme.styledScrollPane(actJList);
            actScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));
            rightContent.add(actScroll, BorderLayout.CENTER);
 
            // Đếm
            JLabel cntLbl = new JLabel(actListModel.size() + " hoạt động");
            cntLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            cntLbl.setForeground(UITheme.TEXT_MUTED);
            rightContent.add(cntLbl, BorderLayout.SOUTH);
 
            rightPanel.add(rightContent, BorderLayout.CENTER);
 
            body.add(leftPanel);
            body.add(rightPanel);
            dlg.add(body, BorderLayout.CENTER);
 
            // ── 4. FOOTER ─────────────────────────────────────────────────
            JPanel footer = new JPanel(new BorderLayout()) {
                @Override protected void paintComponent(Graphics g) {
                    g.setColor(UITheme.BG_MAIN);
                    g.fillRect(0, 0, getWidth(), getHeight());
                    g.setColor(UITheme.BORDER_COLOR);
                    g.drawLine(0, 0, getWidth(), 0);
                    super.paintComponent(g);
                }
            };
            footer.setOpaque(false);
            footer.setBorder(new EmptyBorder(10, 20, 10, 20));
            footer.setPreferredSize(new Dimension(0, 52));
 
            JLabel hint = new JLabel("⚠  Hội viên \"Đã rời\" không thể đăng ký");
            hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            hint.setForeground(UITheme.TEXT_MUTED);
 
            JPanel fBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            fBtns.setOpaque(false);
            JButton btnC2 = UITheme.outlineButton("Hủy");
            JButton btnS2 = UITheme.primaryButton("  ✔  Thêm mới  ");
            btnC2.setPreferredSize(new Dimension(90, 34));
            btnS2.setPreferredSize(new Dimension(130, 34));
            fBtns.add(btnC2); fBtns.add(btnS2);
            footer.add(hint, BorderLayout.WEST);
            footer.add(fBtns, BorderLayout.EAST);
            dlg.add(footer, BorderLayout.SOUTH);
 
            // ── 5. EVENTS ─────────────────────────────────────────────────
            btnC2.addActionListener(e -> dlg.dispose());
 
            btnS2.addActionListener(e -> {
                String maHV  = txtMaHV.getText().trim();
                String tenHV = txtTenHV.getText().trim();
                ActivityItem sel = actJList.getSelectedValue();
 
                if (maHV.isEmpty()) { msg(dlg, "Vui lòng nhập mã hội viên."); return; }
                if (tenHV.isEmpty()) { msg(dlg, "Mã hội viên không tìm thấy trong hệ thống."); return; }
                if (sel == null) { msg(dlg, "Vui lòng chọn hoạt động."); return; }
 
                try (Connection c = DatabaseHelper.getConnection()) {
                    PreparedStatement psHV = c.prepareStatement(
                        "SELECT id, trangThai FROM HoiVien WHERE maHoiVien=?");
                    psHV.setString(1, maHV);
                    ResultSet rsHV = psHV.executeQuery();
                    if (!rsHV.next()) { msg(dlg, "Hội viên không tồn tại."); return; }
                    if ("Đã rời".equalsIgnoreCase(rsHV.getString("trangThai"))) {
                        msg(dlg, "Hội viên đã rời hội và không thể tham gia hoạt động."); return;
                    }
                    int idHV = rsHV.getInt("id");
                    int idHD = sel.id;
 
                    PreparedStatement chk = c.prepareStatement(
                        "SELECT 1 FROM ThamGia WHERE idHoiVien=? AND idHoatDong=?");
                    chk.setInt(1, idHV); chk.setInt(2, idHD);
                    if (chk.executeQuery().next()) {
                        msg(dlg, "Hội viên này đã đăng ký hoạt động được chọn rồi."); return;
                    }
 
                    PreparedStatement ins = c.prepareStatement(
                        "INSERT INTO ThamGia(idHoiVien,idHoatDong,trangThai,ngayDangKy) VALUES(?,?,?,GETDATE())");
                    ins.setInt(1, idHV);
                    ins.setInt(2, idHD);
                    ins.setString(3, (String) cbTT2.getSelectedItem());
                    ins.executeUpdate();
 
                    loadTable();
                    dlg.dispose();
                    JOptionPane.showMessageDialog(ThamGiaForm.this,
                        "✅ Đăng ký tham gia thành công!\n"
                      + "Hội viên: " + tenHV + "\nHoạt động: " + sel.name,
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) { msg(dlg, "Lỗi: " + ex.getMessage()); }
            });
 
            dlg.setVisible(true);
            return;
        }
// ────────── KẾT THÚC KHỐI ELSE ──────────

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
    
    // ══════════════════════════════════════════════════════════════════
//  THÊM 2 HELPER METHODS vào class ThamGiaForm
//  (đặt cùng chỗ với styledFld và addFRow)
// ══════════════════════════════════════════════════════════════════
 
    /** Tạo tiêu đề section với accent stripe bên trái */
    private JPanel buildSectionTitle(String text, Color accent) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0,
                    new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 22),
                    getWidth(), 0, Color.WHITE);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(UITheme.BORDER_COLOR);
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2.setColor(accent);
                g2.fillRect(0, 0, 3, getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(9, 15, 9, 15));
        p.setPreferredSize(new Dimension(0, 40));
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(accent);
        p.add(lbl, BorderLayout.WEST);
        return p;
    }
 
    /** Tạo JTextField có style chuẩn dùng trong dialog */
    private JTextField buildDlgField(String tooltip) {
        JTextField tf = new JTextField();
        tf.setFont(UITheme.FONT_LABEL);
        tf.setPreferredSize(new Dimension(250, 36));
        if (!tooltip.isEmpty()) tf.setToolTipText(tooltip);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UITheme.PRIMARY, 1, true),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            }
        });
        return tf;
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
        String location = "—";
        String date     = "—";
        String status   = "Sắp diễn ra";
 
        ActivityItem(int id, String name) { this.id = id; this.name = name; }
 
        @Override public String toString() { return name; }
    }
}