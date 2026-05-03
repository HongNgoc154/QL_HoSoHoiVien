package Util;

import java.awt.*;
import javax.swing.border.Border;
import javax.swing.*;

public class UITheme {
    // Primary Colors
    public static final Color PRIMARY       = Color.decode("#2872A1");
    public static final Color PRIMARY_DARK  = Color.decode("#1A5480");
    public static final Color PRIMARY_LIGHT = Color.decode("#CBDDE9");
    public static final Color ACCENT        = Color.decode("#3B98D4");

    // Background Colors
    public static final Color BG_MAIN    = Color.decode("#F0F4F8");
    public static final Color BG_CARD    = Color.WHITE;
    public static final Color BG_SIDEBAR = Color.decode("#1A3E5C");
    public static final Color BG_SIDEBAR_HOVER = Color.decode("#2872A1");
    public static final Color BG_SIDEBAR_ACTIVE = Color.decode("#2872A1");

    // Text Colors
    public static final Color TEXT_PRIMARY   = Color.decode("#1E293B");
    public static final Color TEXT_SECONDARY = Color.decode("#64748B");
    public static final Color TEXT_WHITE     = Color.WHITE;
    public static final Color TEXT_MUTED     = Color.decode("#94A3B8");

    // Status Colors
    public static final Color SUCCESS = Color.decode("#10B981");
    public static final Color WARNING = Color.decode("#F59E0B");
    public static final Color DANGER  = Color.decode("#EF4444");
    public static final Color INFO    = Color.decode("#3B98D4");

    // Border
    public static final Color BORDER_COLOR = Color.decode("#E2E8F0");

    // Fonts
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_LABEL   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BOLD    = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_NAV     = new Font("Segoe UI", Font.BOLD, 14);

    // Table row height
    public static final int TABLE_ROW_HEIGHT = 38;

    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(16, 20, 16, 20)
        );
    }

    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFont(FONT_BOLD);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        btn.setOpaque(true);
        addButtonHover(btn, PRIMARY, PRIMARY_DARK);
        return btn;
    }

    public static JButton dangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(DANGER);
        btn.setForeground(Color.WHITE);
        btn.setFont(FONT_BOLD);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        btn.setOpaque(true);
        addButtonHover(btn, DANGER, Color.decode("#DC2626"));
        return btn;
    }

    public static JButton successButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(SUCCESS);
        btn.setForeground(Color.WHITE);
        btn.setFont(FONT_BOLD);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        btn.setOpaque(true);
        addButtonHover(btn, SUCCESS, Color.decode("#059669"));
        return btn;
    }

    public static JButton outlineButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(Color.WHITE);
        btn.setForeground(PRIMARY);
        btn.setFont(FONT_BOLD);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY, 1),
            BorderFactory.createEmptyBorder(7, 17, 7, 17)
        ));
        btn.setOpaque(true);
        return btn;
    }

    private static void addButtonHover(JButton btn, Color normal, Color hover) {
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(hover); }
            public void mouseExited(java.awt.event.MouseEvent e) { btn.setBackground(normal); }
        });
    }

    public static JTextField styledTextField() {
        JTextField f = new JTextField();
        f.setFont(FONT_LABEL);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return f;
    }

    public static JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setFont(FONT_LABEL);
        c.setBackground(Color.WHITE);
        c.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        return c;
    }
}