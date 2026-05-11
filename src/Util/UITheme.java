package Util;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * UITheme – Hệ thống thiết kế đồng nhất cho toàn bộ ứng dụng.
 * Màu chủ đạo: #1359B9 (Primary Blue) & #9FE4FB (Accent Cyan)
 */
public class UITheme {

    // ═══════════════════════════════════════════════════════════════
    //  PRIMARY PALETTE
    // ═══════════════════════════════════════════════════════════════
    public static final Color PRIMARY        = Color.decode("#1359B9");
    public static final Color PRIMARY_DARK   = Color.decode("#0e43a0");
    public static final Color PRIMARY_LIGHT  = Color.decode("#e8f0fd");
    public static final Color PRIMARY_HOVER  = Color.decode("#1a6bc9");

    public static final Color ACCENT         = Color.decode("#9FE4FB");
    public static final Color ACCENT_DARK    = Color.decode("#6dd3f7");
    public static final Color ACCENT_LIGHT   = Color.decode("#e6f9fe");

    // ═══════════════════════════════════════════════════════════════
    //  SIDEBAR
    // ═══════════════════════════════════════════════════════════════
    public static final Color BG_SIDEBAR        = Color.decode("#0a1f5c");
    public static final Color BG_SIDEBAR_MID    = Color.decode("#0f2d6e");
    public static final Color BG_SIDEBAR_ACTIVE = Color.decode("#1359B9");
    public static final Color BG_SIDEBAR_HOVER  = new Color(255, 255, 255, 22);
    public static final Color SIDEBAR_INDICATOR = Color.decode("#9FE4FB");

    // ═══════════════════════════════════════════════════════════════
    //  BACKGROUNDS
    // ═══════════════════════════════════════════════════════════════
    public static final Color BG_MAIN = Color.decode("#F0F5FF");
    public static final Color BG_CARD = Color.WHITE;

    // ═══════════════════════════════════════════════════════════════
    //  TEXT
    // ═══════════════════════════════════════════════════════════════
    public static final Color TEXT_PRIMARY   = Color.decode("#1E293B");
    public static final Color TEXT_SECONDARY = Color.decode("#64748B");
    public static final Color TEXT_MUTED     = Color.decode("#94A3B8");
    public static final Color TEXT_WHITE     = Color.WHITE;

    // ═══════════════════════════════════════════════════════════════
    //  STATUS
    // ═══════════════════════════════════════════════════════════════
    public static final Color SUCCESS      = Color.decode("#10B981");
    public static final Color SUCCESS_BG   = Color.decode("#d1fae5");
    public static final Color WARNING      = Color.decode("#F59E0B");
    public static final Color WARNING_BG   = Color.decode("#fef3c7");
    public static final Color DANGER       = Color.decode("#EF4444");
    public static final Color DANGER_BG    = Color.decode("#fee2e2");
    public static final Color INFO         = Color.decode("#3B98D4");
    public static final Color INFO_BG      = Color.decode("#dbeafe");

    // ═══════════════════════════════════════════════════════════════
    //  BORDER
    // ═══════════════════════════════════════════════════════════════
    public static final Color BORDER_COLOR = Color.decode("#dde6f5");

    // ═══════════════════════════════════════════════════════════════
    //  FONTS
    // ═══════════════════════════════════════════════════════════════
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_LABEL   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BOLD    = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_NAV     = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_CODE    = new Font("Consolas", Font.BOLD, 12);

    public static final int TABLE_ROW_HEIGHT = 40;

    // ═══════════════════════════════════════════════════════════════
    //  CARD BORDER
    // ═══════════════════════════════════════════════════════════════
    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)
        );
    }

    // ═══════════════════════════════════════════════════════════════
    //  BUTTONS
    // ═══════════════════════════════════════════════════════════════

    /** Nút chính – gradient xanh với hover effect */
    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text) {
            boolean hov = false;
            { init(); }
            void init() {
                setOpaque(false); setContentAreaFilled(false);
                setBorderPainted(false); setFocusPainted(false);
                setForeground(Color.WHITE); setFont(FONT_BOLD);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) { hov = true; repaint(); }
                    public void mouseExited (java.awt.event.MouseEvent e) { hov = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c1 = hov ? PRIMARY_HOVER : PRIMARY;
                Color c2 = hov ? PRIMARY : PRIMARY_DARK;
                GradientPaint gp = new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                if (hov) {
                    g2.setColor(new Color(255,255,255,30));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight()/2, 10, 10);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        return btn;
    }

    /** Nút outline */
    public static JButton outlineButton(String text) {
        JButton btn = new JButton(text) {
            boolean hov = false;
            { init(); }
            void init() {
                setOpaque(false); setContentAreaFilled(false);
                setFocusPainted(false);
                setForeground(PRIMARY); setFont(FONT_BOLD);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                    BorderFactory.createEmptyBorder(7, 15, 7, 15)));
                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) { hov = true; repaint(); }
                    public void mouseExited (java.awt.event.MouseEvent e) { hov = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hov ? PRIMARY_LIGHT : Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(hov ? PRIMARY : BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        return btn;
    }

    /** Nút xóa – đỏ */
    public static JButton dangerButton(String text) {
        JButton btn = new JButton(text) {
            boolean hov = false;
            { init(); }
            void init() {
                setOpaque(false); setContentAreaFilled(false);
                setBorderPainted(false); setFocusPainted(false);
                setForeground(Color.WHITE); setFont(FONT_BOLD);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) { hov = true; repaint(); }
                    public void mouseExited (java.awt.event.MouseEvent e) { hov = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hov ? Color.decode("#DC2626") : DANGER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        return btn;
    }

    /** Nút success – xanh lá */
    public static JButton successButton(String text) {
        JButton btn = new JButton(text) {
            boolean hov = false;
            { init(); }
            void init() {
                setOpaque(false); setContentAreaFilled(false);
                setBorderPainted(false); setFocusPainted(false);
                setForeground(Color.WHITE); setFont(FONT_BOLD);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) { hov = true; repaint(); }
                    public void mouseExited (java.awt.event.MouseEvent e) { hov = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hov ? Color.decode("#059669") : SUCCESS);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        return btn;
    }

    // ═══════════════════════════════════════════════════════════════
    //  SEARCH FIELD
    // ═══════════════════════════════════════════════════════════════

    /**
     * Tạo search field chuẩn với icon tìm kiếm và focus effect.
     * Trả về JPanel bọc ngoài để dùng trong FlowLayout / filter bar.
     */
    public static JPanel searchField(JTextField field, String placeholder) {
        field.setFont(FONT_LABEL);
        field.setForeground(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        field.setOpaque(false);
        if (field.getText().isEmpty()) {
            field.setForeground(TEXT_MUTED);
        }
        JPanel wrap = new JPanel(new BorderLayout()) {
            boolean focused = false;
            {
                setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
                setBackground(BG_MAIN);
                field.addFocusListener(new java.awt.event.FocusAdapter() {
                    public void focusGained(java.awt.event.FocusEvent e) {
                        focused = true;
                        setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(PRIMARY, 1, true),
                            BorderFactory.createLineBorder(PRIMARY_LIGHT, 2, true)
                        ));
                        setBackground(Color.WHITE);
                        repaint();
                    }
                    public void focusLost(java.awt.event.FocusEvent e) {
                        focused = false;
                        setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
                        setBackground(BG_MAIN);
                        repaint();
                    }
                });
            }
        };
        JLabel ico = new JLabel("  🔍");
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        wrap.add(ico, BorderLayout.WEST);
        wrap.add(field, BorderLayout.CENTER);
        wrap.setPreferredSize(new Dimension(210, 34));
        return wrap;
    }

    // ═══════════════════════════════════════════════════════════════
    //  STATUS BADGE (inline HTML label)
    // ═══════════════════════════════════════════════════════════════

    /** Trả về JLabel dạng badge tròn theo trạng thái */
    public static JLabel statusBadge(String status) {
        if (status == null) status = "";
        JLabel lbl = new JLabel("  " + status + "  ") {
            Color bg; Color fg;
            {
                String s = getText().trim();
                if (s.contains("Hoạt động") || s.contains("Đã tham gia") || s.contains("Đang làm")) {
                    bg = SUCCESS_BG; fg = Color.decode("#065f46");
                } else if (s.contains("Tạm dừng") || s.contains("Đăng ký") || s.contains("Chờ")) {
                    bg = WARNING_BG; fg = Color.decode("#92400e");
                } else if (s.contains("Rời") || s.contains("Vắng") || s.contains("Từ chối") || s.contains("Đã nghỉ")) {
                    bg = DANGER_BG; fg = Color.decode("#991b1b");
                } else {
                    bg = INFO_BG; fg = Color.decode("#1e40af");
                }
                setForeground(fg);
                setFont(FONT_SMALL);
                setOpaque(false);
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        return lbl;
    }

    // ═══════════════════════════════════════════════════════════════
    //  TABLE HEADER RENDERER
    // ═══════════════════════════════════════════════════════════════
    public static void styleTableHeader(javax.swing.table.JTableHeader header) {
        header.setFont(FONT_BOLD);
        header.setForeground(Color.WHITE);
        header.setBackground(PRIMARY);
        header.setPreferredSize(new Dimension(0, 40));
        header.setOpaque(true);
        header.setReorderingAllowed(false);

        // Gradient header renderer
        header.setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                lbl.setFont(FONT_BOLD);
                lbl.setForeground(Color.WHITE);
                lbl.setOpaque(false);
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
                return lbl;
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY, getWidth(), 0, PRIMARY_HOVER);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════
    //  COMBO BOX STYLE
    // ═══════════════════════════════════════════════════════════════
    public static void styleCombo(JComboBox<?> cb) {
        cb.setFont(FONT_LABEL);
        cb.setBackground(Color.WHITE);
        cb.setForeground(TEXT_SECONDARY);
        cb.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)));
        cb.setPreferredSize(new Dimension(120, 34));
        cb.setFocusable(false);
    }

    // ═══════════════════════════════════════════════════════════════
    //  TEXT FIELD STYLE
    // ═══════════════════════════════════════════════════════════════
    public static void styleField(JTextField f) {
        f.setFont(FONT_LABEL);
        f.setForeground(TEXT_PRIMARY);
        f.setPreferredSize(new Dimension(200, 34));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        f.setBackground(Color.WHITE);
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY, 1, true),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════
    //  CARD PANEL
    // ═══════════════════════════════════════════════════════════════

    /** Tạo JPanel dạng card trắng bo góc với shadow nhẹ */
    public static JPanel cardPanel(LayoutManager layout) {
        return new JPanel(layout) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Shadow
                g2.setColor(new Color(19, 89, 185, 18));
                g2.fillRoundRect(2, 3, getWidth()-3, getHeight()-2, 12, 12);
                // Card
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth()-2, getHeight()-2, 12, 12);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth()-3, getHeight()-3, 12, 12);
                g2.dispose();
            }
        };
    }

    // ═══════════════════════════════════════════════════════════════
    //  PAGE TITLE PANEL
    // ═══════════════════════════════════════════════════════════════

    /** Tạo panel tiêu đề trang với đường kẻ màu bên trái */
    public static JPanel pageTitlePanel(String title, String subtitle) {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY, 0, getHeight(), ACCENT_DARK);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 4, 4, getHeight() - 8, 4, 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 0));

        JLabel lTitle = new JLabel(title);
        lTitle.setFont(FONT_TITLE);
        lTitle.setForeground(TEXT_PRIMARY);
        lTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lTitle);

        if (subtitle != null && !subtitle.isEmpty()) {
            JLabel lSub = new JLabel(subtitle);
            lSub.setFont(FONT_SMALL);
            lSub.setForeground(TEXT_SECONDARY);
            lSub.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(lSub);
        }
        return p;
    }

    // ═══════════════════════════════════════════════════════════════
    //  SECTION HEADER (inside table card)
    // ═══════════════════════════════════════════════════════════════

    /** Panel header bên trong table card với gradient nhẹ */
    public static JPanel tableHeaderPanel(String title, int count) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_LIGHT, getWidth(), 0, Color.WHITE);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(BORDER_COLOR);
                g2.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        JLabel lTitle = new JLabel(title);
        lTitle.setFont(FONT_BOLD);
        lTitle.setForeground(TEXT_PRIMARY);
        left.add(lTitle);

        if (count >= 0) {
            JLabel badge = new JLabel("  " + count + "  ") {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(PRIMARY);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            badge.setForeground(Color.WHITE);
            badge.setOpaque(false);
            badge.setHorizontalAlignment(SwingConstants.CENTER);
            left.add(badge);
        }
        p.add(left, BorderLayout.WEST);
        return p;
    }

    // ═══════════════════════════════════════════════════════════════
    //  HOVER HELPER
    // ═══════════════════════════════════════════════════════════════
    public static void addButtonHover(JButton btn, Color normal, Color hover) {
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(hover); }
            public void mouseExited (java.awt.event.MouseEvent e) { btn.setBackground(normal); }
        });
    }

    // ═══════════════════════════════════════════════════════════════
    //  SCROLL PANE STYLE
    // ═══════════════════════════════════════════════════════════════
    public static JScrollPane styledScrollPane(Component view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(Color.WHITE);
        sp.getVerticalScrollBar().setUnitIncrement(12);
        sp.getVerticalScrollBar().setOpaque(false);
        return sp;
    }
}