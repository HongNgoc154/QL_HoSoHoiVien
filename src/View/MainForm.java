package View;

import Util.UITheme;
import Util.Session;
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

    // Kiểm tra quyền Admin
    private boolean isAdmin() {
        return Session.isAdmin();
    }

    public MainForm() {
        setTitle("Hệ thống Quản lý Hội viên");
        setSize(1300, 760);
        setMinimumSize(new Dimension(1100, 640));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ===== TOP NAVBAR =====
        add(createNavbar(), BorderLayout.NORTH);

        // ===== SIDEBAR =====
        sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // ===== CONTENT =====
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UITheme.BG_MAIN);

        contentPanel.add(new DashboardPanel(), "dashboard");
        contentPanel.add(new HoiVienForm(), "hoivien");
        contentPanel.add(new HoatDongForm(), "hoatdong");
        contentPanel.add(new ThamGiaForm(), "thamgia");

        // Chỉ admin mới thấy các trang này
        if (isAdmin()) {
            contentPanel.add(new NhanVienForm(), "nhanvien");
            contentPanel.add(new NhatKyForm(), "nhatky");
        }

        add(contentPanel, BorderLayout.CENTER);
        switchPanel("dashboard");
    }

    // ===== NAVBAR =====
    private JPanel createNavbar() {
        JPanel nav = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.WHITE);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(UITheme.BORDER_COLOR);
                g2.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
            }
        };
        nav.setPreferredSize(new Dimension(0, 56));
        nav.setBorder(new EmptyBorder(0, 20, 0, 20));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        JLabel appName = new JLabel("  Quản Lý Hội Viên");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 17));
        appName.setForeground(UITheme.PRIMARY);
        left.add(appName);
        nav.add(left, BorderLayout.WEST);

        // Right: user info
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        TaiKhoan user = Session.getUser();
        String username = user != null ? user.getUsername() : "Người dùng";
        String role     = user != null ? user.getRole() : "";

        // Role badge
        JLabel roleLabel = new JLabel(role);
        roleLabel.setFont(UITheme.FONT_SMALL);
        roleLabel.setForeground(UITheme.PRIMARY);
        roleLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.PRIMARY_LIGHT),
            BorderFactory.createEmptyBorder(2, 8, 2, 8)));

        // Avatar circle
        JLabel avatar = new JLabel(String.valueOf(username.charAt(0)).toUpperCase()) {
            protected void paintComponent(Graphics g) {
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

        // User dropdown button
        JButton userBtn = new JButton(username + "  ▾");
        userBtn.setFont(UITheme.FONT_BOLD);
        userBtn.setForeground(UITheme.TEXT_PRIMARY);
        userBtn.setBackground(Color.WHITE);
        userBtn.setBorderPainted(false);
        userBtn.setFocusPainted(false);
        userBtn.setOpaque(true);
        userBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Popup menu
        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));

        JMenuItem itemInfo = new JMenuItem("👤  " + username + "  (" + role + ")");
        itemInfo.setFont(UITheme.FONT_BOLD);
        itemInfo.setEnabled(false);

        JMenuItem itemProfile = new JMenuItem("⚙   Thông tin tài khoản");
        itemProfile.setFont(UITheme.FONT_LABEL);
        itemProfile.addActionListener(e -> showProfileDialog());

        JMenuItem itemLogout = new JMenuItem("⏻   Đăng xuất");
        itemLogout.setFont(UITheme.FONT_LABEL);
        itemLogout.setForeground(UITheme.DANGER);
        itemLogout.addActionListener(e -> logout());

        popup.add(itemInfo);
        popup.add(new JSeparator());
        popup.add(itemProfile);
        popup.add(new JSeparator());
        popup.add(itemLogout);

        userBtn.addActionListener(e -> popup.show(userBtn, 0, userBtn.getHeight()));

        JButton btnBell = UITheme.outlineButton("🔔 " + getUnreadNotificationCount());
        btnBell.addActionListener(e -> showNotificationDialog(btnBell));
        right.add(btnBell);
        right.add(roleLabel);
        right.add(Box.createHorizontalStrut(6));
        right.add(avatar);
        right.add(userBtn);
        nav.add(right, BorderLayout.EAST);
        return nav;
    }

    
    private int getUnreadNotificationCount() {
        try (Connection conn = DatabaseHelper.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM ThongBao WHERE daDoc=0")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) { return 0; }
    }

    private void showNotificationDialog(JButton btnBell) {
        JDialog dlg = new JDialog(this, "Thông báo", true);
        dlg.setSize(560, 400); dlg.setLocationRelativeTo(this);
        DefaultListModel<String> m = new DefaultListModel<>();
        java.util.List<Integer> ids = new java.util.ArrayList<>();
        try (Connection conn = DatabaseHelper.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT id,noiDung,daDoc,thoiGian FROM ThongBao ORDER BY id DESC")) {
            while (rs.next()) {
                ids.add(rs.getInt("id"));
                m.addElement((rs.getBoolean("daDoc") ? "✓ " : "• ") + rs.getString("noiDung") + " | " + rs.getString("thoiGian"));
            }
        } catch (Exception e) {}
        JList<String> list = new JList<>(m);
        JButton btnDetail = UITheme.primaryButton("Xem chi tiết");
        btnDetail.addActionListener(e -> { int idx = list.getSelectedIndex(); if(idx>=0) showNotificationDetail(ids.get(idx), dlg, btnBell); });
        dlg.add(new JScrollPane(list), BorderLayout.CENTER); dlg.add(btnDetail, BorderLayout.SOUTH); dlg.setVisible(true);
    }

    private void showNotificationDetail(int idThongBao, JDialog parent, JButton btnBell) {
        try (Connection conn = DatabaseHelper.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT tb.id, tb.noiDung, dkt.id idDangKyTam, dkt.idHoiVien, dkt.idHoatDong, dkt.thoiGianDangKy, hv.tenHoiVien, hv.maHoiVien, hd.tenHoatDong " +
                "FROM ThongBao tb JOIN DangKyTam dkt ON tb.idDangKyTam=dkt.id " +
                "JOIN HoiVien hv ON dkt.idHoiVien=hv.id JOIN HoatDong hd ON dkt.idHoatDong=hd.id WHERE tb.id=?");
            ps.setInt(1, idThongBao); ResultSet rs = ps.executeQuery(); if(!rs.next()) return;
            conn.createStatement().executeUpdate("UPDATE ThongBao SET daDoc=1 WHERE id=" + idThongBao);
            JDialog d = new JDialog(this, "Duyệt đăng ký", true); d.setSize(520,320); d.setLocationRelativeTo(this);
            JTextArea ta = new JTextArea("Hội viên: " + rs.getString("tenHoiVien") + " (" + rs.getString("maHoiVien") + ")\nHoạt động: " + rs.getString("tenHoatDong") + "\nThời gian đăng ký: " + rs.getString("thoiGianDangKy") + "\n\n" + rs.getString("noiDung"));
            ta.setEditable(false); ta.setLineWrap(true);
            JButton ok = UITheme.primaryButton("✔ Xác nhận"); JButton no = UITheme.dangerButton("❌ Từ chối");
            JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT)); p.add(no); p.add(ok);
            int idDkt = rs.getInt("idDangKyTam"), idHv = rs.getInt("idHoiVien"), idHd = rs.getInt("idHoatDong");
            ok.addActionListener(ev -> approveRegister(idDkt, idHv, idHd, d));
            no.addActionListener(ev -> rejectRegister(idDkt, d));
            d.add(new JScrollPane(ta), BorderLayout.CENTER); d.add(p, BorderLayout.SOUTH); d.setVisible(true);
            btnBell.setText("🔔 " + getUnreadNotificationCount()); parent.dispose();
        } catch (Exception ignored) {}
    }

    private void approveRegister(int idDkt, int idHv, int idHd, JDialog d) {
        try (Connection conn = DatabaseHelper.getConnection()) {
            PreparedStatement chk = conn.prepareStatement("SELECT 1 FROM ThamGia WHERE idHoiVien=? AND idHoatDong=?");
            chk.setInt(1,idHv); chk.setInt(2,idHd);
            if (!chk.executeQuery().next()) {
                PreparedStatement ins = conn.prepareStatement("INSERT INTO ThamGia(idHoiVien,idHoatDong,trangThai,ngayDangKy) VALUES(?,?,N'Đã đăng ký',GETDATE())");
                ins.setInt(1,idHv); ins.setInt(2,idHd); ins.executeUpdate();
            }
            conn.createStatement().executeUpdate("UPDATE DangKyTam SET trangThai=N'Đã duyệt' WHERE id=" + idDkt);
            JOptionPane.showMessageDialog(d, "Đã duyệt đăng ký."); d.dispose();
        } catch (Exception ex) { JOptionPane.showMessageDialog(d, "Lỗi duyệt: " + ex.getMessage()); }
    }
    private void rejectRegister(int idDkt, JDialog d) {
        try (Connection conn = DatabaseHelper.getConnection()) {
            conn.createStatement().executeUpdate("UPDATE DangKyTam SET trangThai=N'Từ chối' WHERE id=" + idDkt);
            JOptionPane.showMessageDialog(d, "Đã từ chối đăng ký."); d.dispose();
        } catch (Exception ex) { JOptionPane.showMessageDialog(d, "Lỗi từ chối: " + ex.getMessage()); }
    }
    
    
    // ===== SIDEBAR =====
    private JPanel createSidebar() {
        JPanel sb = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, UITheme.BG_SIDEBAR,
                    0, getHeight(), Color.decode("#0F2940"));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        sb.setPreferredSize(new Dimension(230, 0));
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setBorder(new EmptyBorder(10, 0, 20, 0));

        // Menu dùng chung
        String[][] commonMenus = {
            {"🏠", "Trang chủ",  "dashboard"},
            {"👥", "Hội viên",   "hoivien"},
            {"📅", "Hoạt động",  "hoatdong"},
            {"✅", "Tham gia",   "thamgia"},
        };

        // Menu chỉ admin
        String[][] adminMenus = {
            {"👔", "Nhân viên",  "nhanvien"},
            {"📋", "Nhật ký",    "nhatky"},
        };

        sb.add(Box.createVerticalStrut(10));

        for (String[] item : commonMenus) {
            JButton btn = createSidebarBtn(item[0], item[1], item[2]);
            sb.add(btn);
            sb.add(Box.createVerticalStrut(2));
        }

        if (isAdmin()) {
            // Divider
            JSeparator sep = new JSeparator();
            sep.setForeground(new Color(255, 255, 255, 30));
            sep.setMaximumSize(new Dimension(200, 1));
            sep.setAlignmentX(Component.LEFT_ALIGNMENT);
            JPanel sepWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
            sepWrapper.setOpaque(false);
            JLabel adminLabel = new JLabel("QUẢN TRỊ");
            adminLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
            adminLabel.setForeground(new Color(255, 255, 255, 100));
            sepWrapper.add(adminLabel);
            sb.add(Box.createVerticalStrut(8));
            sb.add(sepWrapper);

            for (String[] item : adminMenus) {
                JButton btn = createSidebarBtn(item[0], item[1], item[2]);
                sb.add(btn);
                sb.add(Box.createVerticalStrut(2));
            }
        }

        sb.add(Box.createVerticalGlue());
        return sb;
    }

    private JButton createSidebarBtn(String icon, String label, String panel) {
        JButton btn = new JButton() {
            boolean hovered = false;
            {
                setOpaque(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setMaximumSize(new Dimension(230, 46));
                setPreferredSize(new Dimension(230, 46));
                setAlignmentX(Component.LEFT_ALIGNMENT);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
                });
            }
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean isActive = (activeBtn == this);
                if (isActive) {
                    g2.setColor(UITheme.BG_SIDEBAR_ACTIVE);
                    g2.fillRoundRect(8, 2, getWidth()-16, getHeight()-4, 8, 8);
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 10, 4, getHeight()-20, 4, 4);
                } else if (hovered) {
                    g2.setColor(new Color(255, 255, 255, 20));
                    g2.fillRoundRect(8, 2, getWidth()-16, getHeight()-4, 8, 8);
                }
                boolean isActive2 = (activeBtn == this);
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
                g2.setColor(isActive2 ? Color.WHITE : new Color(255, 255, 255, 180));
                g2.drawString(icon, 20, 28);
                g2.setFont(isActive2 ? UITheme.FONT_NAV : new Font("Segoe UI", Font.PLAIN, 14));
                g2.setColor(isActive2 ? Color.WHITE : new Color(255, 255, 255, 180));
                g2.drawString(label, 50, 29);
            }
        };
        btn.addActionListener(e -> {
            activeBtn = btn;
            switchPanel(panel);
            sidebar.repaint();
        });
        return btn;
    }

    // ===== PROFILE DIALOG =====
    private void showProfileDialog() {
        TaiKhoan user = Session.getUser();
        if (user == null) return;

        JDialog dlg = new JDialog(this, "Thông tin tài khoản", true);
        dlg.setSize(420, 380);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);

        // Header
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UITheme.PRIMARY);
        topBar.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel topContent = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        topContent.setOpaque(false);

        // Avatar lớn
        JLabel avatarBig = new JLabel(String.valueOf(user.getUsername().charAt(0)).toUpperCase()) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillOval(0, 0, 52, 52);
                g2.setColor(UITheme.PRIMARY_DARK);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 24));
                FontMetrics fm = g2.getFontMetrics();
                String t = getText();
                g2.drawString(t, (52 - fm.stringWidth(t))/2, (52 - fm.getHeight())/2 + fm.getAscent());
            }
        };
        avatarBig.setPreferredSize(new Dimension(52, 52));
        avatarBig.setOpaque(false);

        JPanel nameInfo = new JPanel(new GridLayout(2, 1, 0, 2));
        nameInfo.setOpaque(false);
        JLabel lblUname = new JLabel(user.getUsername());
        lblUname.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblUname.setForeground(Color.WHITE);
        JLabel lblRole = new JLabel(user.getRole());
        lblRole.setFont(UITheme.FONT_SMALL);
        lblRole.setForeground(new Color(255, 255, 255, 200));
        nameInfo.add(lblUname);
        nameInfo.add(lblRole);

        topContent.add(avatarBig);
        topContent.add(nameInfo);
        topBar.add(topContent, BorderLayout.CENTER);

        // Info panel
        JPanel info = new JPanel(new GridBagLayout());
        info.setBackground(Color.WHITE);
        info.setBorder(new EmptyBorder(20, 28, 20, 28));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 4, 8, 4);
        gc.anchor = GridBagConstraints.WEST;

        addInfoRow(info, gc, 0, "Tên đăng nhập:", user.getUsername());
        addInfoRow(info, gc, 1, "Phân quyền:", user.getRole());
        addInfoRow(info, gc, 2, "ID nhân viên:", String.valueOf(user.getIdNhanVien()));

        // Lấy thêm thông tin nhân viên nếu có
        try (java.sql.Connection conn = database.DatabaseHelper.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                 "SELECT tenNhanVien, email, sdt FROM NhanVien WHERE id=?")) {
            ps.setInt(1, user.getIdNhanVien());
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                addInfoRow(info, gc, 3, "Tên nhân viên:", rs.getString("tenNhanVien"));
                addInfoRow(info, gc, 4, "Email:",         rs.getString("email"));
                addInfoRow(info, gc, 5, "Số điện thoại:", rs.getString("sdt"));
            }
        } catch (Exception ex) { /* ignore */ }

        // Buttons
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
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

    private void addInfoRow(JPanel p, GridBagConstraints gc, int y, String lbl, String val) {
        gc.gridx=0; gc.gridy=y;
        JLabel l = new JLabel(lbl);
        l.setFont(UITheme.FONT_BOLD);
        l.setForeground(UITheme.TEXT_SECONDARY);
        l.setPreferredSize(new Dimension(150, 24));
        p.add(l, gc);
        gc.gridx=1;
        JLabel v = new JLabel(val != null ? val : "—");
        v.setFont(UITheme.FONT_LABEL);
        v.setForeground(UITheme.TEXT_PRIMARY);
        p.add(v, gc);
    }

    // ===== SWITCH PANEL =====
    private void switchPanel(String name) {
        cardLayout.show(contentPanel, name);
    }

    // ===== LOGOUT =====
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