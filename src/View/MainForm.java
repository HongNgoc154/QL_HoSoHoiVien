package View;

import javax.swing.*;
import java.awt.*;

public class MainForm extends JFrame {

    private JPanel contentPanel;
    private CardLayout cardLayout;

    public MainForm() {
        setTitle("Hệ thống quản lý hội viên");
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ===== SIDEBAR =====
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBackground(Color.decode("#1E293B"));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel("  DASHBOARD");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logo.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        sidebar.add(logo);

        JButton btnDashboard = createMenuButton("Trang chủ");
        JButton btnHoiVien = createMenuButton("Hội viên");
        JButton btnHoatDong = createMenuButton("Hoạt động");
        JButton btnNhanVien = createMenuButton("Nhân viên");
        JButton btnNhatKy = createMenuButton("Nhật ký");

        sidebar.add(btnDashboard);
        sidebar.add(btnHoiVien);
        sidebar.add(btnHoatDong);
        sidebar.add(btnNhanVien);
        sidebar.add(btnNhatKy);

        // ===== CONTENT =====
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Color.decode("#F1F5F9"));

        contentPanel.add(new DashboardPanel(), "dashboard");
        contentPanel.add(new HoiVienForm(), "hoivien");

        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        // ===== EVENT =====
        btnDashboard.addActionListener(e -> switchPanel("dashboard"));
        btnHoiVien.addActionListener(e -> switchPanel("hoivien"));

        setVisible(true);
    }

    private JButton createMenuButton(String text){
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(220, 50));
        btn.setFocusPainted(false);
        btn.setBackground(Color.decode("#1E293B"));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(Color.decode("#334155"));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(Color.decode("#1E293B"));
            }
        });

        return btn;
    }

    // ===== ANIMATION SWITCH =====
    private void switchPanel(String name){
        cardLayout.show(contentPanel, name);
    }
}