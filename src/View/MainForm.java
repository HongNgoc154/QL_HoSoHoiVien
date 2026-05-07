package View;

import Util.UITheme;
import Util.Session;
import Util.EmailSender;
import model.TaiKhoan;
import controller.AuthController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import database.DatabaseHelper;

public class MainForm extends JFrame {

    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JButton activeBtn = null;
    private JPanel sidebar;
    private JButton btnBell;
    private Timer notificationTimer;

    // ─── Sidebar dimensions ─────────────────────────────────────────────────
    private static final int SIDEBAR_W      = 240;
    private static final int BTN_H          = 48;
    private static final Color SB_BG        = Color.decode("#1A3E5C");
    private static final Color SB_HOVER     = new Color(255, 255, 255, 22);
    private static final Color SB_ACTIVE_BG = Color.decode("#2872A1");
    private static final Color SB_ACTIVE_IND= Color.WHITE;
    private static final Color SB_TXT_ON    = Color.WHITE;
    private static final Color SB_TXT_OFF   = new Color(255, 255, 255, 175);
    private static final Color SB_SECTION   = new Color(255, 255, 255, 100);

    private boolean isAdmin() {
        return Session.isAdmin();
    }

    public MainForm() {
        setTitle("Hệ thống Quản lý Hồ sơ Hội viên");
        setSize(1300, 760);
        setMinimumSize(new Dimension(1100, 640));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(createNavbar(),  BorderLayout.NORTH);

        sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        cardLayout   = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UITheme.BG_MAIN);

        contentPanel.add(new DashboardPanel(), "dashboard");
        contentPanel.add(new HoiVienForm(),    "hoivien");
        contentPanel.add(new HoatDongForm(),   "hoatdong");
        contentPanel.add(new ThamGiaForm(),    "thamgia");

        if (isAdmin()) {
            contentPanel.add(new NhanVienForm(), "nhanvien");
            contentPanel.add(new NhatKyForm(),   "nhatky");
        }

        add(contentPanel, BorderLayout.CENTER);
        switchPanel("dashboard");
        startNotificationAutoRefresh();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  NAVBAR
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel createNavbar() {
        JPanel nav = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.WHITE);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(UITheme.BORDER_COLOR);
                g2.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
            }
        };
        nav.setPreferredSize(new Dimension(0, 56));
        nav.setBorder(new EmptyBorder(0, 20, 0, 20));

        // ── Left: app name (centered vertically) ──────────────────────────
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);

        // Multi-line label để vừa với navbar
        JLabel appName = new JLabel("<html><center>Quản Lý Hồ Sơ<br>Hội Viên</center></html>");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        appName.setForeground(UITheme.PRIMARY);
        left.add(appName);
        nav.add(left, BorderLayout.WEST);

        // ── Right: notifications + user ──────────────────────────────────
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        TaiKhoan user     = Session.getUser();
        String   username = user != null ? user.getUsername() : "Người dùng";
        String   role     = user != null ? user.getRole()     : "";

        JLabel roleLabel = new JLabel(role);
        roleLabel.setFont(UITheme.FONT_SMALL);
        roleLabel.setForeground(UITheme.PRIMARY);
        roleLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.PRIMARY_LIGHT),
            BorderFactory.createEmptyBorder(2, 8, 2, 8)));

        // Avatar circle
        JLabel avatar = new JLabel(String.valueOf(username.charAt(0)).toUpperCase()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.PRIMARY);
                g2.fillOval(0, 0, 34, 34);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
                FontMetrics fm = g2.getFontMetrics();
                String t = getText();
                g2.drawString(t, (34 - fm.stringWidth(t))/2, (34 - fm.getHeight())/2 + fm.getAscent());
            }
        };
        avatar.setPreferredSize(new Dimension(34, 34));
        avatar.setOpaque(false);

        // User dropdown
        JButton userBtn = new JButton(username + "  ▾");
        userBtn.setFont(UITheme.FONT_BOLD);
        userBtn.setForeground(UITheme.TEXT_PRIMARY);
        userBtn.setBackground(Color.WHITE);
        userBtn.setBorderPainted(false);
        userBtn.setFocusPainted(false);
        userBtn.setOpaque(true);
        userBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        JMenuItem itemInfo    = new JMenuItem("👤  " + username + "  (" + role + ")");
        itemInfo.setFont(UITheme.FONT_BOLD); itemInfo.setEnabled(false);
        JMenuItem itemProfile = new JMenuItem("⚙   Thông tin tài khoản");
        itemProfile.setFont(UITheme.FONT_LABEL);
        itemProfile.addActionListener(e -> showProfileDialog());
        JMenuItem itemLogout  = new JMenuItem("⏻   Đăng xuất");
        itemLogout.setFont(UITheme.FONT_LABEL);
        itemLogout.setForeground(UITheme.DANGER);
        itemLogout.addActionListener(e -> logout());
        popup.add(itemInfo); popup.add(new JSeparator());
        popup.add(itemProfile); popup.add(new JSeparator());
        popup.add(itemLogout);
        userBtn.addActionListener(e -> popup.show(userBtn, 0, userBtn.getHeight()));

        btnBell = UITheme.outlineButton("🔔 " + getUnreadNotificationCount());
        btnBell.addActionListener(e -> showNotificationDialog(btnBell));

        right.add(btnBell);
        right.add(roleLabel);
        right.add(Box.createHorizontalStrut(6));
        right.add(avatar);
        right.add(userBtn);
        nav.add(right, BorderLayout.EAST);
        return nav;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  SIDEBAR
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel createSidebar() {

    JPanel sb = new JPanel() {

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g;

            g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            );

            // Gradient background
            GradientPaint gp = new GradientPaint(
                0, 0,
                SB_BG,
                0, getHeight(),
                Color.decode("#0F2940")
            );

            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    };

    sb.setPreferredSize(new Dimension(SIDEBAR_W, 0));
    sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
    sb.setBorder(new EmptyBorder(0, 0, 0, 0));

    // =========================================================
    // BRANDING
    // =========================================================
    JPanel brandPanel = new JPanel() {

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g;

            g2.setColor(new Color(255,255,255,18));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    };

    brandPanel.setOpaque(false);
    brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));

    brandPanel.setBorder(new EmptyBorder(22, 0, 20, 0));

    brandPanel.setMaximumSize(new Dimension(SIDEBAR_W, 110));
    brandPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

    // ---------- LOGO ----------
    JLabel logo = new JLabel("◆");
    logo.setAlignmentX(Component.CENTER_ALIGNMENT);

    logo.setFont(new Font("Segoe UI", Font.BOLD, 24));
    logo.setForeground(Color.WHITE);

    // ---------- TITLE ----------
    JLabel title1 = new JLabel("QUẢN LÝ HỒ SƠ");
    title1.setAlignmentX(Component.CENTER_ALIGNMENT);

    title1.setFont(new Font("Segoe UI", Font.BOLD, 13));
    title1.setForeground(Color.WHITE);

    JLabel title2 = new JLabel("HỘI VIÊN");
    title2.setAlignmentX(Component.CENTER_ALIGNMENT);

    title2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    title2.setForeground(new Color(220,220,220));

    brandPanel.add(logo);
    brandPanel.add(Box.createVerticalStrut(8));
    brandPanel.add(title1);
    brandPanel.add(Box.createVerticalStrut(2));
    brandPanel.add(title2);

    sb.add(brandPanel);

    // =========================================================
    // DIVIDER
    // =========================================================
    JPanel divider = new JPanel() {

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g;

            g2.setColor(new Color(255,255,255,30));

            g2.drawLine(18, 0, getWidth()-18, 0);
        }
    };

    divider.setOpaque(false);

    divider.setMaximumSize(new Dimension(SIDEBAR_W, 1));
    divider.setPreferredSize(new Dimension(SIDEBAR_W, 1));

    sb.add(divider);

    sb.add(Box.createVerticalStrut(10));

    // =========================================================
    // MENU
    // =========================================================
    String[][] commonMenus = {

        {"🏠", "Trang chủ", "dashboard"},

        {"👥", "Quản lý hồ sơ hội viên", "hoivien"},

        {"📅", "Hoạt động", "hoatdong"},

        {"✅", "Tham gia", "thamgia"}
    };

    for (String[] item : commonMenus) {

        JButton btn = createSidebarBtn(
            item[0],
            item[1],
            item[2]
        );

        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        sb.add(btn);

        sb.add(Box.createVerticalStrut(4));
    }

    // =========================================================
    // ADMIN SECTION
    // =========================================================
    if (isAdmin()) {

        sb.add(Box.createVerticalStrut(14));

        JLabel adminLbl = new JLabel("QUẢN TRỊ");

        adminLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        adminLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));

        adminLbl.setForeground(new Color(255,255,255,120));

        sb.add(adminLbl);

        sb.add(Box.createVerticalStrut(8));

        String[][] adminMenus = {

            {"👔", "Nhân viên", "nhanvien"},

            {"📋", "Nhật ký", "nhatky"}
        };

        for (String[] item : adminMenus) {

            JButton btn = createSidebarBtn(
                item[0],
                item[1],
                item[2]
            );

            btn.setAlignmentX(Component.LEFT_ALIGNMENT);

            sb.add(btn);

            sb.add(Box.createVerticalStrut(4));
        }
    }

    // =========================================================
    // PUSH FOOTER DOWN
    // =========================================================
    sb.add(Box.createVerticalGlue());

    // =========================================================
    // FOOTER
    // =========================================================
    JPanel footer = new JPanel(
        new FlowLayout(FlowLayout.CENTER, 0, 0)
    );

    footer.setOpaque(false);

    footer.setMaximumSize(
        new Dimension(SIDEBAR_W, 40)
    );

    JLabel version = new JLabel("v1.0.0");

    version.setFont(
        new Font("Segoe UI", Font.PLAIN, 10)
    );

    version.setForeground(
        new Color(255,255,255,80)
    );

    footer.add(version);

    sb.add(footer);

    sb.add(Box.createVerticalStrut(12));

    return sb;
}

    // ── Brand label helper ──────────────────────────────────────────────
    private JLabel makeBrandLabel(String text, int size, boolean bold, Color color) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size));
        lbl.setForeground(color);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setMaximumSize(new Dimension(SIDEBAR_W, 22));
        return lbl;
    }

    // ── Sidebar divider ─────────────────────────────────────────────────
    private JPanel sidebarDivider() {
        JPanel div = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(new Color(255, 255, 255, 35));
                g.drawLine(16, 0, getWidth()-16, 0);
            }
        };
        div.setOpaque(false);
        div.setMaximumSize(new Dimension(SIDEBAR_W, 1));
        div.setPreferredSize(new Dimension(SIDEBAR_W, 1));
        return div;
    }

    // ── Section label (e.g. "QUẢN TRỊ") ────────────────────────────────
    private JPanel sidebarSectionLabel(String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(SIDEBAR_W, 22));
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(SB_SECTION);
        p.add(lbl);
        return p;
    }

    // ── Sidebar button ──────────────────────────────────────────────────
    private JButton createSidebarBtn(String icon, String label, String panel) {
        JButton btn = new JButton() {
            boolean hovered = false;

            {
                setOpaque(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setMaximumSize(new Dimension(SIDEBAR_W, BTN_H));
                setPreferredSize(new Dimension(SIDEBAR_W, BTN_H));
                setAlignmentX(Component.LEFT_ALIGNMENT);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                    public void mouseExited (MouseEvent e) { hovered = false; repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g;

                g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
                );

                boolean isActive = (activeBtn == this);

                // =========================
                // BACKGROUND
                // =========================
                if (isActive) {

                    g2.setColor(SB_ACTIVE_BG);

                    g2.fillRoundRect(
                        6,
                        4,
                        getWidth() - 12,
                        BTN_H - 8,
                        10,
                        10
                    );

                    // active line
                    g2.setColor(SB_ACTIVE_IND);

                    g2.fillRoundRect(
                        0,
                        (BTN_H - 24) / 2,
                        4,
                        24,
                        4,
                        4
                    );

                } else if (hovered) {

                    g2.setColor(SB_HOVER);

                    g2.fillRoundRect(
                        6,
                        4,
                        getWidth() - 12,
                        BTN_H - 8,
                        10,
                        10
                    );
                }

                // =========================
                // ICON
                // =========================
                g2.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 17));

                g2.setColor(isActive ? SB_TXT_ON : SB_TXT_OFF);

                FontMetrics iconFm = g2.getFontMetrics();

                int iconX = 18;

                int iconY =
                        (BTN_H - iconFm.getHeight()) / 2
                        + iconFm.getAscent();

                g2.drawString(icon, iconX, iconY);

                // =========================
                // TEXT
                // =========================
                g2.setFont(
                    isActive
                        ? new Font("Segoe UI", Font.BOLD, 13)
                        : new Font("Segoe UI", Font.PLAIN, 13)
                );

                FontMetrics textFm = g2.getFontMetrics();

                String displayLabel = label;

                int maxWidth = getWidth() - 70;

                if (textFm.stringWidth(displayLabel) > maxWidth) {

                    while (
                        textFm.stringWidth(displayLabel + "…") > maxWidth
                        && displayLabel.length() > 1
                    ) {

                        displayLabel = displayLabel.substring(
                            0,
                            displayLabel.length() - 1
                        );
                    }

                    displayLabel += "…";
                }

                int textX = 50;

                int textY =
                        (BTN_H - textFm.getHeight()) / 2
                        + textFm.getAscent();

                g2.drawString(displayLabel, textX, textY);
            }
        };

        btn.addActionListener(e -> {
            activeBtn = btn;
            switchPanel(panel);
            sidebar.repaint();
        });

        btn.setToolTipText(label); // tooltip khi text bị cắt
        return btn;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  NOTIFICATIONS
    // ══════════════════════════════════════════════════════════════════════════
    private int getUnreadNotificationCount() {
        try (Connection conn = DatabaseHelper.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(
                 "SELECT COUNT(*) FROM ThongBao WHERE daDoc=0")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) { return 0; }
    }

    private void showNotificationDialog(JButton source) {
        refreshNotificationBadge();
        JDialog dlg = new JDialog(this, "Thông báo", true);
        dlg.setSize(560, 400);
        dlg.setLocationRelativeTo(this);

        DefaultListModel<NotificationItem> m = new DefaultListModel<>();
        java.util.List<Integer> ids = new java.util.ArrayList<>();
        try (Connection conn = DatabaseHelper.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(
                 "SELECT id,noiDung,daDoc,thoiGian FROM ThongBao ORDER BY id DESC")) {
            while (rs.next()) {
                ids.add(rs.getInt("id"));
                m.addElement(new NotificationItem(
                    rs.getString("noiDung") + " | " + rs.getString("thoiGian"),
                    rs.getBoolean("daDoc")));
            }
        } catch (Exception e) {}

        JList<NotificationItem> list = new JList<>(m);
        list.setCellRenderer((jl, value, index, isSelected, cellHasFocus) -> {
            JLabel lbl = new JLabel((value.isRead ? "✓ " : "• ") + value.text);
            lbl.setOpaque(true);
            lbl.setBorder(new EmptyBorder(8, 10, 8, 10));
            if (isSelected) {
                lbl.setBackground(new Color(230, 240, 255));
                lbl.setForeground(value.isRead ? new Color(55, 65, 81) : new Color(185, 28, 28));
            } else {
                lbl.setBackground(Color.WHITE);
                lbl.setForeground(value.isRead ? new Color(75, 85, 99) : new Color(220, 38, 38));
            }
            lbl.setFont(new Font("Segoe UI", value.isRead ? Font.PLAIN : Font.BOLD, 13));
            return lbl;
        });
        
        JButton btnDetail = UITheme.primaryButton("Xem chi tiết");
        btnDetail.addActionListener(e -> {
            int idx = list.getSelectedIndex();
            if (idx >= 0) showNotificationDetail(ids.get(idx), dlg, source);
        });
        dlg.add(new JScrollPane(list), BorderLayout.CENTER);
        dlg.add(btnDetail, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }
    
    
    private static class NotificationItem {
        private final String text;
        private final boolean isRead;

        private NotificationItem(String text, boolean isRead) {
            this.text = text;
            this.isRead = isRead;
        }
    }

    private void showNotificationDetail(int idThongBao, JDialog parent, JButton btnBell) {
        try (Connection conn = DatabaseHelper.getConnection()) {
            ensureLeaveRequestNotificationColumn(conn);
            PreparedStatement ps = conn.prepareStatement(
                "SELECT tb.id, tb.noiDung, tb.idYeuCauRoiHoi, dkt.id idDangKyTam, dkt.idHoiVien, dkt.idHoatDong, "
              + "dkt.thoiGianDangKy, hv.tenHoiVien, hv.maHoiVien, hv.trangThai trangThaiHoiVien, hd.tenHoatDong "
              +"FROM ThongBao tb "
              + "LEFT JOIN DangKyTam dkt ON tb.idDangKyTam=dkt.id "
              + "LEFT JOIN HoiVien hv ON dkt.idHoiVien=hv.id "
              + "LEFT JOIN HoatDong hd ON dkt.idHoatDong=hd.id WHERE tb.id=?");
            ps.setInt(1, idThongBao);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return;
            conn.createStatement().executeUpdate(
                "UPDATE ThongBao SET daDoc=1 WHERE id=" + idThongBao);
            int idYeuCau = rs.getInt("idYeuCauRoiHoi");
            if (idYeuCau > 0) {
                showLeaveRequestDetail(conn, idYeuCau, parent, btnBell);
                return;
            }

            JDialog d = new JDialog(this, "Duyệt đăng ký", true);
            d.setSize(520, 320); d.setLocationRelativeTo(this);
            JTextArea ta = new JTextArea(
                "👤 THÔNG TIN HỘI VIÊN\n"
                + "Mã hội viên: " + safeValue(rs.getString("maHoiVien")) + "\n"
                + "Họ tên: " + safeValue(rs.getString("tenHoiVien")) + "\n"
                + "Ngày sinh: " + safeValue(String.valueOf(rs.getDate("ngaySinh"))) + "\n"
                + "Giới tính: " + safeValue(rs.getString("gioiTinh")) + "\n"
                + "Số điện thoại: " + safeValue(rs.getString("sdt")) + "\n"
                + "Email: " + safeValue(rs.getString("email")) + "\n"
                + "Địa chỉ: " + safeValue(rs.getString("diaChi")) + "\n"
                + "Ảnh hội viên: " + safeValue(rs.getString("hinhAnh")) + "\n"
                + "Trạng thái hiện tại: " + safeValue(rs.getString("trangThaiHoiVien")) + "\n\n"
                + "📋 THÔNG TIN YÊU CẦU RỜI HỘI\n"
                + "📅 Ngày yêu cầu: " + safeValue(String.valueOf(rs.getTimestamp("thoiGianTao"))) + "\n"
                + "📝 Lý do rời hội: " + safeValue(rs.getString("lyDo")) + "\n"
                + "📌 Nguồn yêu cầu: " + safeValue(rs.getString("nguonYeuCau")) + "\n"
                + "⏱ Trạng thái yêu cầu: " + safeValue(rs.getString("trangThai")) + "\n"
                + "✅ Thời gian xác nhận: " + safeValue(String.valueOf(rs.getTimestamp("thoiGianXacNhan"))));
            ta.setEditable(false); ta.setLineWrap(true);
            
            JLabel lbStatus = new JLabel("Trạng thái hội viên: " + rs.getString("trangThaiHoiVien"));
            lbStatus.setBorder(new EmptyBorder(8, 8, 8, 8));
            String stHv = rs.getString("trangThaiHoiVien");
            if (stHv != null && stHv.trim().equalsIgnoreCase("Đã rời")) {
                lbStatus.setForeground(Color.RED);
            }

            JButton ok = UITheme.primaryButton("✔ Xác nhận");
            JButton no = UITheme.dangerButton("❌ Từ chối");
            JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            p.add(no); p.add(ok);

            int idDkt = rs.getInt("idDangKyTam");
            int idHv  = rs.getInt("idHoiVien");
            int idHd  = rs.getInt("idHoatDong");
            RegisterValidationResult vr = validateRegisterData(conn, idHv, idHd);
            if (!vr.valid) {
                ok.setEnabled(false);
                ok.setToolTipText(vr.message);
            }
            ok.addActionListener(ev -> approveRegister(idDkt, idHv, idHd, d));
            no.addActionListener(ev -> rejectRegister(idDkt, d));

            JPanel center = new JPanel(new BorderLayout());
            center.add(lbStatus, BorderLayout.NORTH);
            center.add(new JScrollPane(ta), BorderLayout.CENTER);
            d.add(center, BorderLayout.CENTER);
            d.add(p, BorderLayout.SOUTH);
            d.setVisible(true);

            btnBell.setText("🔔 " + getUnreadNotificationCount());
            parent.dispose();
        } catch (Exception ignored) {}
    }
    
    
    private void showLeaveRequestDetail(Connection conn, int idYeuCau, JDialog parent, JButton btnBell) {
        JDialog loading = new JDialog(this, "Đang tải", true);
        loading.setSize(260, 90);
        loading.setLocationRelativeTo(this);
        loading.setLayout(new BorderLayout());
        loading.add(new JLabel("Đang tải chi tiết yêu cầu rời hội...", SwingConstants.CENTER), BorderLayout.CENTER);

        SwingUtilities.invokeLater(() -> loading.setVisible(true));
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT y.id, y.lyDo, y.nguonYeuCau, y.trangThai, y.thoiGianTao, y.thoiGianXacNhan, "
                    + "h.id idHoiVien, h.maHoiVien, h.tenHoiVien, h.ngaySinh, h.gioiTinh, h.sdt, h.email, h.diaChi, h.hinhAnh, h.trangThai trangThaiHoiVien "
                    + "FROM YeuCauRoiHoi y JOIN HoiVien h ON y.idHoiVien=h.id WHERE y.id=?");
            ps.setInt(1, idYeuCau);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy dữ liệu yêu cầu rời hội.");
                return;
            }

            JDialog d = new JDialog(this, "Chi tiết yêu cầu rời hội", true);
            d.setSize(760, 560);
            d.setLocationRelativeTo(this);
            JPanel root = new JPanel(new BorderLayout(12, 12));
            root.setBorder(new EmptyBorder(16, 16, 16, 16));
            root.setBackground(new Color(245, 247, 251));

            JPanel card = new JPanel(new BorderLayout(10, 10));
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 235, 242)),
                new EmptyBorder(14, 14, 14, 14)
            ));
            card.setBackground(Color.WHITE);

            JLabel header = new JLabel("TỜ HỒ SƠ YÊU CẦU RỜI HỘI");
            header.setOpaque(true);
            header.setBackground(new Color(22, 119, 255));
            header.setForeground(Color.WHITE);
            header.setBorder(new EmptyBorder(10, 12, 10, 12));
            header.setFont(new Font("Segoe UI", Font.BOLD, 16));

            JTextArea ta = new JTextArea(
                "👤 THÔNG TIN HỘI VIÊN\n"
                + "Mã hội viên: " + safeValue(rs.getString("maHoiVien")) + "\n"
                + "Họ tên: " + safeValue(rs.getString("tenHoiVien")) + "\n"
                + "Ngày sinh: " + safeValue(String.valueOf(rs.getDate("ngaySinh"))) + "\n"
                + "Giới tính: " + safeValue(rs.getString("gioiTinh")) + "\n"
                + "Số điện thoại: " + safeValue(rs.getString("sdt")) + "\n"
                + "Email: " + safeValue(rs.getString("email")) + "\n"
                + "Địa chỉ: " + safeValue(rs.getString("diaChi")) + "\n"
                + "Ảnh hội viên: " + safeValue(rs.getString("hinhAnh")) + "\n"
                + "Trạng thái hiện tại: " + safeValue(rs.getString("trangThaiHoiVien")) + "\n\n"
                + "📋 THÔNG TIN YÊU CẦU RỜI HỘI\n"
                + "📅 Ngày yêu cầu: " + safeValue(String.valueOf(rs.getTimestamp("thoiGianTao"))) + "\n"
                + "📝 Lý do rời hội: " + safeValue(rs.getString("lyDo")) + "\n"
                + "📌 Nguồn yêu cầu: " + safeValue(rs.getString("nguonYeuCau")) + "\n"
                + "⏱ Trạng thái yêu cầu: " + safeValue(rs.getString("trangThai")) + "\n"
                + "✅ Thời gian xác nhận: " + safeValue(String.valueOf(rs.getTimestamp("thoiGianXacNhan"))));
            ta.setEditable(false);
            ta.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            ta.setLineWrap(true);
            ta.setWrapStyleWord(true);
            ta.setBackground(Color.WHITE);

            String trangThaiHoiVien = safeValue(rs.getString("trangThaiHoiVien"));
            JLabel status = new JLabel("Trạng thái hội viên: " + trangThaiHoiVien);
            status.setBorder(new EmptyBorder(4, 2, 8, 2));
            status.setFont(new Font("Segoe UI", Font.BOLD, 13));
            if ("Đã rời".equalsIgnoreCase(trangThaiHoiVien)) status.setForeground(Color.RED);

            JButton approve = UITheme.primaryButton("✔ Duyệt yêu cầu");
            JButton cancel = UITheme.dangerButton("❌ Hủy yêu cầu");
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            actions.setOpaque(false);
            actions.add(cancel);
            actions.add(approve);
            
            String yeuCauStatus = safeValue(rs.getString("trangThai"));
            boolean processed = "Đã duyệt".equalsIgnoreCase(yeuCauStatus) || "Đã hủy".equalsIgnoreCase(yeuCauStatus);
            if (processed) {
                approve.setEnabled(false);
                cancel.setEnabled(false);
                approve.setToolTipText("Yêu cầu đã được xử lý trước đó.");
            }

            int idHoiVien = rs.getInt("idHoiVien");
            String email = rs.getString("email");
            String tenHV = rs.getString("tenHoiVien");
            approve.addActionListener(ev -> approveLeaveRequest(idYeuCau, idHoiVien, tenHV, email, d));
            cancel.addActionListener(ev -> cancelLeaveRequest(idYeuCau, d));

            JPanel center = new JPanel(new BorderLayout());
            center.setOpaque(false);
            center.add(status, BorderLayout.NORTH);
            center.add(new JScrollPane(ta), BorderLayout.CENTER);

            card.add(header, BorderLayout.NORTH);
            card.add(center, BorderLayout.CENTER);
            card.add(actions, BorderLayout.SOUTH);
            root.add(card, BorderLayout.CENTER);
            d.add(root);
            d.setVisible(true);
            btnBell.setText("🔔 " + getUnreadNotificationCount());
            parent.dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải chi tiết yêu cầu rời hội: " + ex.getMessage());
            } finally {
            loading.dispose();
        } 
    }
    
    private String safeValue(String value) {
        return value == null || "null".equalsIgnoreCase(value) ? "" : value;
    }

    private void approveLeaveRequest(int idYeuCau, int idHoiVien, String tenHV, String email, JDialog d) {
        try (Connection conn = DatabaseHelper.getConnection()) {
            PreparedStatement check = conn.prepareStatement("SELECT trangThai FROM YeuCauRoiHoi WHERE id=?");
            check.setInt(1, idYeuCau);
            ResultSet cr = check.executeQuery();
            if (!cr.next()) return;
            String st = cr.getString(1);
            if (!"Chờ xác nhận".equalsIgnoreCase(st) && !"Đã xác nhận".equalsIgnoreCase(st)) {
                JOptionPane.showMessageDialog(d, "Yêu cầu đã được xử lý trước đó.");
                return;
            }
            PreparedStatement upReq = conn.prepareStatement("UPDATE YeuCauRoiHoi SET trangThai=N'Đã duyệt', thoiGianDuyet=GETDATE() WHERE id=?");
            upReq.setInt(1, idYeuCau);
            upReq.executeUpdate();
            PreparedStatement upMem = conn.prepareStatement("UPDATE HoiVien SET trangThai=N'Đã rời', ngayRoi=GETDATE() WHERE id=?");
            upMem.setInt(1, idHoiVien);
            upMem.executeUpdate();
            if (email != null && !email.trim().isEmpty()) {
                EmailSender.send(email, "Phản hồi yêu cầu rời hội",
                    "Chào bạn,\n\nYêu cầu rời hội của bạn đã được duyệt thành công.\n\nKể từ thời điểm này, bạn sẽ không còn nhận được bất kỳ thông báo nào liên quan đến các hoạt động sắp tới.\n\nCảm ơn bạn đã đồng hành cùng chúng tôi trong suốt thời gian qua.\n\nTrân trọng.");
            }
            JOptionPane.showMessageDialog(d, "Đã duyệt yêu cầu rời hội cho " + tenHV + ".");
            d.dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(d, "Lỗi duyệt yêu cầu: " + ex.getMessage());
        }
    }

    private void cancelLeaveRequest(int idYeuCau, JDialog d) {
        try (Connection conn = DatabaseHelper.getConnection()) {
            PreparedStatement check = conn.prepareStatement("SELECT trangThai FROM YeuCauRoiHoi WHERE id=?");
            check.setInt(1, idYeuCau);
            ResultSet cr = check.executeQuery();
            if (!cr.next()) return;
            String st = cr.getString(1);
            if (!"Chờ xác nhận".equalsIgnoreCase(st) && !"Đã xác nhận".equalsIgnoreCase(st)) {
                JOptionPane.showMessageDialog(d, "Yêu cầu đã được xử lý trước đó.");
                return;
            }
            PreparedStatement upReq = conn.prepareStatement("UPDATE YeuCauRoiHoi SET trangThai=N'Đã hủy' WHERE id=?");
            upReq.setInt(1, idYeuCau);
            upReq.executeUpdate();
            JOptionPane.showMessageDialog(d, "Đã hủy yêu cầu rời hội.");
            d.dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(d, "Lỗi hủy yêu cầu: " + ex.getMessage());
        }
    }

    private void ensureLeaveRequestNotificationColumn(Connection conn) {
        try {
            conn.createStatement().executeUpdate(
                "IF COL_LENGTH('ThongBao','idYeuCauRoiHoi') IS NULL "
                    + "ALTER TABLE ThongBao ADD idYeuCauRoiHoi INT NULL "
                    + "CONSTRAINT FK_ThongBao_YeuCauRoiHoi FOREIGN KEY REFERENCES YeuCauRoiHoi(id)");
        } catch (Exception ignored) {
        }
    }
    
    
    private static class RegisterValidationResult {
        private boolean valid;
        private boolean memberLeft;
        private String memberStatus;
        private String message;

        RegisterValidationResult(boolean valid, boolean memberLeft, String memberStatus, String message) {
            this.valid = valid;
            this.memberLeft = memberLeft;
            this.memberStatus = memberStatus;
            this.message = message;
        }
    }

    private RegisterValidationResult validateRegisterData(Connection conn, int idHv, int idHd) throws SQLException {
        if (idHv <= 0) return new RegisterValidationResult(false, false, null, "Hội viên không hợp lệ.");
        if (idHd <= 0) return new RegisterValidationResult(false, false, null, "Hoạt động không hợp lệ.");

        String memberStatus = null;
        PreparedStatement hvCheck = conn.prepareStatement("SELECT trangThai FROM HoiVien WHERE id=?");
        hvCheck.setInt(1, idHv);
        ResultSet hvRs = hvCheck.executeQuery();
        if (!hvRs.next()) return new RegisterValidationResult(false, false, null, "Hội viên không tồn tại.");
        memberStatus = hvRs.getString("trangThai");

        PreparedStatement hdCheck = conn.prepareStatement("SELECT 1 FROM HoatDong WHERE id=?");
        hdCheck.setInt(1, idHd);
        if (!hdCheck.executeQuery().next()) return new RegisterValidationResult(false, false, memberStatus, "Hoạt động không tồn tại.");

        if (memberStatus != null && memberStatus.trim().equalsIgnoreCase("Đã rời")) {
            return new RegisterValidationResult(false, true, memberStatus, "Hội viên đã rời hội và không thể tham gia hoạt động.");
        }

        PreparedStatement joinedCheck = conn.prepareStatement("SELECT 1 FROM ThamGia WHERE idHoiVien=? AND idHoatDong=?");
        joinedCheck.setInt(1, idHv);
        joinedCheck.setInt(2, idHd);
        if (joinedCheck.executeQuery().next()) {
            return new RegisterValidationResult(false, false, memberStatus, "Hội viên đã đăng ký hoạt động này.");
        }

        return new RegisterValidationResult(true, false, memberStatus, null);
    }

    private void approveRegister(int idDkt, int idHv, int idHd, JDialog d) {
        try (Connection conn = DatabaseHelper.getConnection()) {
            if (idDkt <= 0) {
                JOptionPane.showMessageDialog(d, "Đăng ký tạm không hợp lệ.");
                return;
            }
            RegisterValidationResult vr = validateRegisterData(conn, idHv, idHd);
            if (!vr.valid) {
                JOptionPane.showMessageDialog(d, vr.message);
                return;
            }

            PreparedStatement ins = conn.prepareStatement(
                "INSERT INTO ThamGia(idHoiVien,idHoatDong,trangThai,ngayDangKy) VALUES(?,?,N'Đã đăng ký',GETDATE())");
            ins.setInt(1, idHv);
            ins.setInt(2, idHd);
            ins.executeUpdate();

            PreparedStatement up = conn.prepareStatement("UPDATE DangKyTam SET trangThai=N'Đã duyệt' WHERE id=?");
            up.setInt(1, idDkt);
            up.executeUpdate();
            JOptionPane.showMessageDialog(d, "Đã duyệt đăng ký.");
            d.dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(d, "Lỗi duyệt: " + ex.getMessage());
        }
    }

    private void rejectRegister(int idDkt, JDialog d) {
        try (Connection conn = DatabaseHelper.getConnection()) {
            PreparedStatement up = conn.prepareStatement("UPDATE DangKyTam SET trangThai=N'Từ chối' WHERE id=?");
            up.setInt(1, idDkt);
            up.executeUpdate();
            JOptionPane.showMessageDialog(d, "Đã từ chối đăng ký.");
            d.dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(d, "Lỗi từ chối: " + ex.getMessage());
        }
    }

    private void startNotificationAutoRefresh() {
        notificationTimer = new Timer(10_000, e -> refreshNotificationBadge());
        notificationTimer.setRepeats(true);
        notificationTimer.start();
    }

    private void refreshNotificationBadge() {
        if (btnBell != null)
            btnBell.setText("🔔 " + getUnreadNotificationCount());
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PROFILE DIALOG
    // ══════════════════════════════════════════════════════════════════════════
    private void showProfileDialog() {
        TaiKhoan user = Session.getUser();
        if (user == null) return;

        JDialog dlg = new JDialog(this, "Thông tin tài khoản", true);
        dlg.setSize(420, 380); dlg.setLocationRelativeTo(this); dlg.setResizable(false);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UITheme.PRIMARY);
        topBar.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel topContent = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        topContent.setOpaque(false);

        JLabel avatarBig = new JLabel(String.valueOf(user.getUsername().charAt(0)).toUpperCase()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillOval(0, 0, 52, 52);
                g2.setColor(UITheme.PRIMARY_DARK);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 24));
                FontMetrics fm = g2.getFontMetrics();
                String t = getText();
                g2.drawString(t,(52-fm.stringWidth(t))/2,(52-fm.getHeight())/2+fm.getAscent());
            }
        };
        avatarBig.setPreferredSize(new Dimension(52, 52));
        avatarBig.setOpaque(false);

        JPanel nameInfo = new JPanel(new GridLayout(2,1,0,2));
        nameInfo.setOpaque(false);
        JLabel lblUname = new JLabel(user.getUsername());
        lblUname.setFont(new Font("Segoe UI", Font.BOLD, 16)); lblUname.setForeground(Color.WHITE);
        JLabel lblRole  = new JLabel(user.getRole());
        lblRole.setFont(UITheme.FONT_SMALL); lblRole.setForeground(new Color(255,255,255,200));
        nameInfo.add(lblUname); nameInfo.add(lblRole);

        topContent.add(avatarBig); topContent.add(nameInfo);
        topBar.add(topContent, BorderLayout.CENTER);

        JPanel info = new JPanel(new GridBagLayout());
        info.setBackground(Color.WHITE);
        info.setBorder(new EmptyBorder(20, 28, 20, 28));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 4, 8, 4); gc.anchor = GridBagConstraints.WEST;

        addInfoRow(info, gc, 0, "Tên đăng nhập:", user.getUsername());
        addInfoRow(info, gc, 1, "Phân quyền:",    user.getRole());
        addInfoRow(info, gc, 2, "ID nhân viên:",  String.valueOf(user.getIdNhanVien()));

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT tenNhanVien, email, sdt FROM NhanVien WHERE id=?")) {
            ps.setInt(1, user.getIdNhanVien());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                addInfoRow(info, gc, 3, "Tên nhân viên:", rs.getString("tenNhanVien"));
                addInfoRow(info, gc, 4, "Email:",         rs.getString("email"));
                addInfoRow(info, gc, 5, "Số điện thoại:", rs.getString("sdt"));
            }
        } catch (Exception ex) {}

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        btns.setBackground(Color.decode("#F8FAFC"));
        btns.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));
        JButton btnClose = UITheme.primaryButton("Đóng");
        btns.add(btnClose);

        content.add(topBar,       BorderLayout.NORTH);
        content.add(info,         BorderLayout.CENTER);
        content.add(btns,         BorderLayout.SOUTH);
        dlg.add(content);
        btnClose.addActionListener(e -> dlg.dispose());
        dlg.setVisible(true);
    }

    private void addInfoRow(JPanel p, GridBagConstraints gc, int y, String lbl, String val) {
        gc.gridx = 0; gc.gridy = y;
        JLabel l = new JLabel(lbl);
        l.setFont(UITheme.FONT_BOLD); l.setForeground(UITheme.TEXT_SECONDARY);
        l.setPreferredSize(new Dimension(150, 24));
        p.add(l, gc);
        gc.gridx = 1;
        JLabel v = new JLabel(val != null ? val : "—");
        v.setFont(UITheme.FONT_LABEL); v.setForeground(UITheme.TEXT_PRIMARY);
        p.add(v, gc);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UTILS
    // ══════════════════════════════════════════════════════════════════════════
    private void switchPanel(String name) {
        cardLayout.show(contentPanel, name);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn đăng xuất?",
            "Đăng xuất", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            new AuthController().logout();
            dispose();
            SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
        }
    }
}