package View;

import Util.UITheme;
import Util.Session;
import model.TaiKhoan;
import controller.AuthController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class MainForm extends JFrame {

    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JLabel lblUser;
    private JButton activeBtn = null;

    public MainForm() {
        setTitle("Hệ thống Quản lý Hội viên");
        setSize(1300, 760);
        setMinimumSize(new Dimension(1100, 640));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ===== TOP NAVBAR =====
        JPanel navbar = createNavbar();
        add(navbar, BorderLayout.NORTH);

        // ===== SIDEBAR =====
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // ===== CONTENT =====
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UITheme.BG_MAIN);

        contentPanel.add(new DashboardPanel(), "dashboard");
        contentPanel.add(new HoiVienForm(), "hoivien");
        contentPanel.add(new HoatDongForm(), "hoatdong");
        contentPanel.add(new ThamGiaForm(), "thamgia");
        contentPanel.add(new NhanVienForm(), "nhanvien");
        contentPanel.add(new NhatKyForm(), "nhatky");

        add(contentPanel, BorderLayout.CENTER);
        switchPanel("dashboard");
    }

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

        // Left: App name
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
        String role = user != null ? user.getRole() : "";

        JLabel roleLabel = new JLabel(role);
        roleLabel.setFont(UITheme.FONT_SMALL);
        roleLabel.setForeground(UITheme.PRIMARY);
        roleLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.PRIMARY_LIGHT),
            BorderFactory.createEmptyBorder(2, 8, 2, 8)
        ));

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
                int x = (34 - fm.stringWidth(t)) / 2;
                int y = (34 - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(t, x, y);
            }
        };
        avatar.setPreferredSize(new Dimension(34, 34));
        avatar.setOpaque(false);

        // User popup button
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
        JMenuItem itemInfo = new JMenuItem("👤  " + username);
        itemInfo.setFont(UITheme.FONT_LABEL);
        itemInfo.setEnabled(false);
        JMenuItem itemSep = new JMenuItem("─────────────");
        itemSep.setEnabled(false);
        JMenuItem itemLogout = new JMenuItem("⏻  Đăng xuất");
        itemLogout.setFont(UITheme.FONT_LABEL);
        itemLogout.setForeground(UITheme.DANGER);
        itemLogout.addActionListener(e -> logout());

        popup.add(itemInfo);
        popup.add(new JSeparator());
        popup.add(itemLogout);

        userBtn.addActionListener(e ->
            popup.show(userBtn, 0, userBtn.getHeight())
        );

        right.add(roleLabel);
        right.add(Box.createHorizontalStrut(6));
        right.add(avatar);
        right.add(userBtn);
        nav.add(right, BorderLayout.EAST);

        return nav;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, UITheme.BG_SIDEBAR,
                    0, getHeight(), Color.decode("#0F2940"));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(10, 0, 20, 0));

        String[][] menuItems = {
            {"🏠", "Trang chủ", "dashboard"},
            {"👥", "Hội viên", "hoivien"},
            {"📅", "Hoạt động", "hoatdong"},
            {"✅", "Tham gia", "thamgia"},
            {"👔", "Nhân viên", "nhanvien"},
            {"📋", "Nhật ký", "nhatky"},
        };

        sidebar.add(Box.createVerticalStrut(10));

        for (String[] item : menuItems) {
            JButton btn = createSidebarBtn(item[0], item[1], item[2]);
            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(2));
        }

        sidebar.add(Box.createVerticalGlue());

        return sidebar;
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
                    public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
                });
            }

            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean isActive = (activeBtn == this);
                if (isActive) {
                    g2.setColor(UITheme.BG_SIDEBAR_ACTIVE);
                    g2.fillRoundRect(8, 2, getWidth()-16, getHeight()-4, 8, 8);
                    // Left accent bar
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 10, 4, getHeight()-20, 4, 4);
                } else if (hovered) {
                    g2.setColor(new Color(255, 255, 255, 20));
                    g2.fillRoundRect(8, 2, getWidth()-16, getHeight()-4, 8, 8);
                }

                // Icon
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
                g2.setColor(isActive ? Color.WHITE : new Color(255, 255, 255, 180));
                g2.drawString(icon, 20, 28);

                // Label
                g2.setFont(isActive ? UITheme.FONT_NAV : new Font("Segoe UI", Font.PLAIN, 14));
                g2.setColor(isActive ? Color.WHITE : new Color(255, 255, 255, 180));
                g2.drawString(label, 50, 29);
            }
        };

        btn.addActionListener(e -> {
            activeBtn = btn;
            switchPanel(panel);
            repaintSidebar();
        });

        return btn;
    }

    private void repaintSidebar() {
        Component sidebar = getContentPane().getComponent(0);
        if (sidebar instanceof JPanel) sidebar.repaint();
        // Repaint all sidebar buttons
        for (Component c : getContentPane().getComponents()) {
            if (c instanceof JPanel) c.repaint();
        }
        // Find sidebar panel (WEST)
        BorderLayout bl = (BorderLayout) getContentPane().getLayout();
        Component west = bl.getLayoutComponent(BorderLayout.WEST);
        if (west != null) west.repaint();
    }

    private void switchPanel(String name) {
        cardLayout.show(contentPanel, name);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn đăng xuất?", "Đăng xuất",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            new AuthController().logout();
            dispose();
            SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
        }
    }
}