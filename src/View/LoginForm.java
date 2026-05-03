package View;

import controller.AuthController;
import Util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class LoginForm extends JFrame {

    private JTextField txtUser;
    private JPasswordField txtPass;
    private AuthController controller = new AuthController();

    public LoginForm() {
        setTitle("Đăng nhập hệ thống");
        setUndecorated(true);
        setSize(900, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setShape(new RoundRectangle2D.Double(0, 0, 900, 560, 20, 20));

        JPanel root = new JPanel(new GridLayout(1, 2)) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        root.setBackground(Color.WHITE);

        // ===== LEFT PANEL (branding) =====
        JPanel left = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, UITheme.PRIMARY_DARK, getWidth(), getHeight(), UITheme.ACCENT);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBorder(new EmptyBorder(60, 40, 60, 40));

        JLabel logo = new JLabel("◆");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 48));
        logo.setForeground(new Color(255, 255, 255, 120));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("QuanLy");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title2 = new JLabel("HoiVien");
        title2.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title2.setForeground(new Color(203, 221, 233));
        title2.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("<html><center>Hệ thống quản lý<br>hồ sơ hội viên</center></html>");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        sub.setForeground(new Color(255, 255, 255, 180));
        sub.setHorizontalAlignment(SwingConstants.CENTER);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        left.add(Box.createVerticalGlue());
        left.add(logo);
        left.add(Box.createVerticalStrut(16));
        left.add(title);
        left.add(title2);
        left.add(Box.createVerticalStrut(20));
        left.add(sub);
        left.add(Box.createVerticalGlue());

        // ===== RIGHT PANEL (form) =====
        JPanel right = new JPanel();
        right.setBackground(Color.WHITE);
        right.setLayout(new GridBagLayout());

        JPanel form = new JPanel();
        form.setBackground(Color.WHITE);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(320, 380));

        JLabel loginTitle = new JLabel("Đăng nhập");
        loginTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        loginTitle.setForeground(UITheme.TEXT_PRIMARY);
        loginTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel loginSub = new JLabel("Nhập thông tin tài khoản của bạn");
        loginSub.setFont(UITheme.FONT_LABEL);
        loginSub.setForeground(UITheme.TEXT_SECONDARY);
        loginSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(loginTitle);
        form.add(Box.createVerticalStrut(4));
        form.add(loginSub);
        form.add(Box.createVerticalStrut(32));

        // Username field
        form.add(makeLabel("Tên đăng nhập"));
        form.add(Box.createVerticalStrut(6));
        txtUser = new JTextField();
        styleInput(txtUser);
        form.add(txtUser);
        form.add(Box.createVerticalStrut(18));

        // Password field
        form.add(makeLabel("Mật khẩu"));
        form.add(Box.createVerticalStrut(6));
        txtPass = new JPasswordField();
        styleInput(txtPass);
        form.add(txtPass);
        form.add(Box.createVerticalStrut(28));

        // Login button
        JButton btnLogin = new JButton("Đăng nhập");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnLogin.setBackground(UITheme.PRIMARY);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setOpaque(true);
        btnLogin.setPreferredSize(new Dimension(320, 44));
        btnLogin.setMaximumSize(new Dimension(320, 44));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnLogin.setBackground(UITheme.PRIMARY_DARK); }
            public void mouseExited(MouseEvent e)  { btnLogin.setBackground(UITheme.PRIMARY); }
        });
        form.add(btnLogin);

        // Close button top-right
        JButton btnClose = new JButton("✕");
        btnClose.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnClose.setBackground(Color.decode("#F1F5F9"));
        btnClose.setForeground(UITheme.TEXT_SECONDARY);
        btnClose.setFocusPainted(false);
        btnClose.setBorderPainted(false);
        btnClose.setOpaque(true);
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.setBounds(858, 10, 32, 32);
        btnClose.addActionListener(e -> System.exit(0));

        right.add(form);
        right.setLayout(null);
        form.setBounds(90, 90, 320, 400);
        right.add(form);
        right.add(btnClose);

        root.add(left);
        root.add(right);
        add(root);

        // Events
        btnLogin.addActionListener(e -> login());
        txtPass.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) login();
            }
        });

        // Drag window
        final Point[] dragOffset = {new Point()};
        left.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { dragOffset[0] = e.getPoint(); }
        });
        left.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point loc = getLocation();
                setLocation(loc.x + e.getX() - dragOffset[0].x, loc.y + e.getY() - dragOffset[0].y);
            }
        });
    }

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_PRIMARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private void styleInput(JTextField field) {
        field.setFont(UITheme.FONT_LABEL);
        field.setPreferredSize(new Dimension(320, 40));
        field.setMaximumSize(new Dimension(320, 40));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
    }

    private void login() {
        String user = txtUser.getText().trim();
        String pass = new String(txtPass.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            showError("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        if (controller.login(user, pass)) {
            dispose();
            SwingUtilities.invokeLater(() -> new MainForm().setVisible(true));
        } else {
            showError("Sai tên đăng nhập hoặc mật khẩu!");
            txtPass.setText("");
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi đăng nhập", JOptionPane.ERROR_MESSAGE);
    }
}