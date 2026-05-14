package View;

import controller.AuthController;
import Util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * LoginForm – Giao diện đăng nhập hiện đại.
 * Left panel: gradient #1359B9 → #0a1f5c với hoa văn geometric.
 * Right panel: form trắng sạch với focus effects.
 */
public class LoginForm extends JFrame {

    private JTextField    txtUser;
    private JPasswordField txtPass;
    private JLabel        lblError;
    private JButton       btnLogin;

    public LoginForm() {
        setTitle("Đăng nhập – Hệ thống Quản lý Hội viên");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(860, 520);
        setLocationRelativeTo(null);
        setResizable(false);
        setUndecorated(true);
        setShape(new RoundRectangle2D.Double(0, 0, 860, 520, 20, 20));

        JPanel root = new JPanel(new GridLayout(1, 2)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                g2.dispose();
            }
        };
        root.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));
        setContentPane(root);

        root.add(buildLeft());
        root.add(buildRight());

        // Drag to move
        addDragToMove(root);
    }

    // ── LEFT PANEL ────────────────────────────────────────────────────────
    private JPanel buildLeft() {
        JPanel left = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Background gradient
                GradientPaint gp = new GradientPaint(0, 0, Color.decode("#0a1f5c"),
                        getWidth(), getHeight(), UITheme.PRIMARY);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Geometric decorations – circles
                g2.setColor(new Color(159, 228, 251, 18));
                g2.fillOval(-60, -60, 220, 220);
                g2.setColor(new Color(159, 228, 251, 12));
                g2.fillOval(getWidth()-120, getHeight()-120, 240, 240);
                g2.setColor(new Color(255, 255, 255, 8));
                g2.fillOval(40, getHeight()-180, 160, 160);

                // Grid dots
                g2.setColor(new Color(255,255,255,22));
                for (int x = 20; x < getWidth(); x += 28)
                    for (int y = 20; y < getHeight(); y += 28)
                        g2.fillOval(x, y, 2, 2);

                // Accent circle (top-right)
                g2.setStroke(new BasicStroke(1.5f));
                g2.setColor(new Color(159, 228, 251, 50));
                g2.drawOval(getWidth()-90, -50, 180, 180);
                g2.drawOval(getWidth()-70, -30, 140, 140);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        left.setOpaque(false);

        // Logo icon
        JPanel iconCircle = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, UITheme.ACCENT,
                        getWidth(), getHeight(), UITheme.ACCENT_DARK);
                g2.setPaint(gp);
                g2.fillOval(0, 0, getWidth(), getHeight());
                // shadow glow
                g2.setColor(new Color(159, 228, 251, 60));
                g2.setStroke(new BasicStroke(6));
                g2.drawOval(3, 3, getWidth()-6, getHeight()-6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconCircle.setOpaque(false);
        iconCircle.setSize(76, 76);
        iconCircle.setLocation(175, 110);
        JLabel iconTxt = new JLabel("🏛");
        iconTxt.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 34));
        iconTxt.setHorizontalAlignment(SwingConstants.CENTER);
        iconTxt.setBounds(0, 0, 76, 76);
        iconCircle.setLayout(new BorderLayout());
        iconCircle.add(iconTxt);

        JLabel title1 = makeLabel("HỘI VIÊN", new Font("Segoe UI", Font.BOLD, 30), Color.WHITE, 430, 40);
        title1.setBounds(0, 200, 430, 44);
        title1.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel title2 = makeLabel("MANAGER", new Font("Segoe UI", Font.PLAIN, 18), UITheme.ACCENT, 430, 30);
        title2.setBounds(0, 242, 430, 30);
        title2.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel sub = makeLabel("Hệ thống quản lý hồ sơ hội viên",
                new Font("Segoe UI", Font.PLAIN, 13), new Color(255,255,255,160), 430, 22);
        sub.setBounds(0, 280, 430, 22);
        sub.setHorizontalAlignment(SwingConstants.CENTER);

        // Version badge
        JLabel ver = makeLabel("v2.0  •  2025", new Font("Segoe UI", Font.BOLD, 11),
                new Color(159, 228, 251, 180), 430, 22);
        ver.setBounds(0, 390, 430, 22);
        ver.setHorizontalAlignment(SwingConstants.CENTER);

        left.add(iconCircle);
        left.add(title1); left.add(title2); left.add(sub); left.add(ver);
        return left;
    }

    private JLabel makeLabel(String text, Font font, Color fg, int w, int h) {
        JLabel l = new JLabel(text);
        l.setFont(font); l.setForeground(fg);
        l.setSize(w, h);
        return l;
    }

    // ── RIGHT PANEL ───────────────────────────────────────────────────────
    private JPanel buildRight() {
        JPanel right = new JPanel(null);
        right.setBackground(Color.WHITE);

        // Close button
        JButton btnClose = new JButton("✕") {
            boolean hov = false;
            { setOpaque(false); setContentAreaFilled(false); setBorderPainted(false);
              setFocusPainted(false); setFont(new Font("Segoe UI", Font.BOLD, 13));
              setForeground(UITheme.TEXT_MUTED); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
              addMouseListener(new MouseAdapter() {
                  public void mouseEntered(MouseEvent e) { hov=true; repaint(); }
                  public void mouseExited (MouseEvent e) { hov=false; repaint(); }
              });
            }
            @Override protected void paintComponent(Graphics g) {
                if (hov) { g.setColor(new Color(239,68,68,20)); g.fillOval(0,0,getWidth(),getHeight()); }
                super.paintComponent(g);
            }
        };
        btnClose.setBounds(380, 14, 30, 30);
        btnClose.addActionListener(e -> System.exit(0));
        right.add(btnClose);

        // Title
        JLabel lTitle = new JLabel("Đăng nhập");
        lTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lTitle.setForeground(UITheme.TEXT_PRIMARY);
        lTitle.setBounds(60, 70, 310, 38);
        right.add(lTitle);

        JLabel lSub = new JLabel("Nhập thông tin tài khoản của bạn");
        lSub.setFont(UITheme.FONT_LABEL);
        lSub.setForeground(UITheme.TEXT_SECONDARY);
        lSub.setBounds(60, 108, 310, 20);
        right.add(lSub);

        // Accent line
        JPanel accentLine = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, UITheme.PRIMARY, getWidth(), 0, UITheme.ACCENT);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 3, 3);
                g2.dispose();
            }
        };
        accentLine.setBounds(60, 135, 50, 3);
        accentLine.setOpaque(false);
        right.add(accentLine);

        // Username
        JLabel lUser = new JLabel("Tên đăng nhập");
        lUser.setFont(UITheme.FONT_BOLD);
        lUser.setForeground(UITheme.TEXT_SECONDARY);
        lUser.setBounds(60, 160, 310, 18);
        right.add(lUser);

        txtUser = buildTextField("Nhập tên đăng nhập...");
        txtUser.setBounds(60, 182, 310, 38);
        right.add(txtUser);

        // Password
        JLabel lPass = new JLabel("Mật khẩu");
        lPass.setFont(UITheme.FONT_BOLD);
        lPass.setForeground(UITheme.TEXT_SECONDARY);
        lPass.setBounds(60, 236, 310, 18);
        right.add(lPass);

        txtPass = new JPasswordField();
        txtPass.setFont(UITheme.FONT_LABEL);
        txtPass.setBounds(60, 258, 310, 38);
        applyBorder(txtPass, false);
        txtPass.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { applyBorder(txtPass, true); }
            public void focusLost  (FocusEvent e) { applyBorder(txtPass, false); }
        });
        txtPass.addActionListener(e -> doLogin());
        right.add(txtPass);

        // Error label
        lblError = new JLabel();
        lblError.setFont(UITheme.FONT_SMALL);
        lblError.setForeground(UITheme.DANGER);
        lblError.setBounds(60, 302, 310, 18);
        right.add(lblError);

        // Login button
        btnLogin = UITheme.primaryButton("  Đăng nhập  →");
        btnLogin.setBounds(60, 330, 310, 42);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.addActionListener(e -> doLogin());
        right.add(btnLogin);

        // Footer note
        JLabel note = new JLabel("© 2025  Hệ thống Quản lý Hội viên");
        note.setFont(UITheme.FONT_SMALL);
        note.setForeground(UITheme.TEXT_MUTED);
        note.setBounds(60, 450, 310, 18);
        note.setHorizontalAlignment(SwingConstants.CENTER);
        right.add(note);

        return right;
    }

    private JTextField buildTextField(String placeholder) {
        JTextField f = new JTextField();
        f.setFont(UITheme.FONT_LABEL);
        applyBorder(f, false);
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { applyBorder(f, true); }
            public void focusLost  (FocusEvent e) { applyBorder(f, false); }
        });
        f.addActionListener(e -> doLogin());
        return f;
    }

    private void applyBorder(JComponent c, boolean focused) {
        c.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(focused ? UITheme.PRIMARY : UITheme.BORDER_COLOR, focused ? 2 : 1, true),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        c.setBackground(focused ? UITheme.PRIMARY_LIGHT : Color.WHITE);
    }

    private void doLogin() {
        String user = txtUser.getText().trim();
        String pass = new String(txtPass.getPassword());
        if (user.isEmpty() || pass.isEmpty()) {
            lblError.setText("  ⚠  Vui lòng nhập đầy đủ thông tin.");
            return;
        }
        lblError.setText("  ⏳  Đang xác thực...");
        lblError.setForeground(UITheme.PRIMARY);
        btnLogin.setEnabled(false);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override protected Boolean doInBackground() {
                return new AuthController().login(user, pass);
            }
            @Override protected void done() {
                try {
                    if (get()) {
                        showToast(
                                "✔ Đăng nhập thành công!",
                                new Color(22, 163, 74)
);
                        Timer t = new Timer(400, ev -> {
                            dispose();

                            new MainForm().setVisible(true);
                        });
                        t.setRepeats(false); t.start();
                    } else {
                        lblError.setText("  ✕  Sai tên đăng nhập hoặc mật khẩu.");
                        lblError.setForeground(UITheme.DANGER);
                        txtPass.setText(""); txtPass.requestFocus();
                        btnLogin.setEnabled(true);
                        // Shake effect
                        shakeComponent(txtUser);
                        shakeComponent(txtPass);
                    }
                } catch (Exception ex) {
                    lblError.setText("  Lỗi kết nối: " + ex.getMessage());
                    btnLogin.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void shakeComponent(JComponent c) {
        Point original = c.getLocation();
        Timer t = new Timer(30, null);
        final int[] count = {0};
        final int[] offsets = {6, -6, 4, -4, 2, -2, 0};
        t.addActionListener(e -> {
            if (count[0] < offsets.length) {
                c.setLocation(original.x + offsets[count[0]], original.y);
                count[0]++;
            } else {
                c.setLocation(original);
                t.stop();
            }
        });
        t.start();
    }
    
    private void showToast(String message, Color bgColor) {

    JWindow toast = new JWindow();

    JPanel panel = new JPanel(new BorderLayout());

    panel.setBackground(bgColor);

    panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(
                    bgColor.darker(), 1),
            BorderFactory.createEmptyBorder(12, 20, 12, 20)
    ));

    JLabel lbl = new JLabel(message);

    lbl.setForeground(Color.WHITE);

    lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));

    panel.add(lbl, BorderLayout.CENTER);

    toast.add(panel);

    toast.pack();

    // vị trí góc phải dưới
    Dimension screen =
            Toolkit.getDefaultToolkit().getScreenSize();

    int x = screen.width - toast.getWidth() - 30;

    int y = screen.height - toast.getHeight() - 50;

    toast.setLocation(x, y);

    toast.setAlwaysOnTop(true);

    toast.setVisible(true);

    // tự đóng sau 2 giây
    Timer timer = new Timer(2000, e -> {

        toast.setVisible(false);

        toast.dispose();
    });

    timer.setRepeats(false);

    timer.start();
}

    private void addDragToMove(JPanel root) {
        final Point[] dragStart = {null};
        root.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { dragStart[0] = e.getPoint(); }
        });
        root.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (dragStart[0] != null) {
                    Point loc = getLocation();
                    setLocation(loc.x + e.getX() - dragStart[0].x,
                                loc.y + e.getY() - dragStart[0].y);
                }
            }
        });
    }
}