package Util;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * FormPanel – Tiện ích tạo form dialog đẹp, đồng nhất cho toàn bộ ứng dụng.
 * Sử dụng: tạo JDialog qua FormPanel.createDialog(...)
 */
public class FormPanel {

    /**
     * Tạo JDialog chuẩn với header gradient xanh.
     */
    public static JDialog createDialog(Component parent, String title,
                                       int width, int height) {
        Frame frame = (parent instanceof Frame) ? (Frame) parent
                    : (Frame) SwingUtilities.getWindowAncestor(parent);
        JDialog dlg = new JDialog(frame, title, true);
        dlg.setSize(width, height);
        dlg.setLocationRelativeTo(parent);
        dlg.setResizable(false);
        dlg.getContentPane().setBackground(Color.WHITE);
        dlg.setLayout(new BorderLayout());
        return dlg;
    }

    /**
     * Tạo header panel gradient cho dialog.
     */
    public static JPanel createHeader(String title) {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                    0, 0, UITheme.PRIMARY,
                    getWidth(), 0, UITheme.PRIMARY_HOVER);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Shine line
                g2.setColor(new Color(255,255,255,40));
                g2.fillRect(0, 0, getWidth(), 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, 52));
        header.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel lbl = new JLabel(title);
        lbl.setFont(UITheme.FONT_HEADING);
        lbl.setForeground(Color.WHITE);
        header.add(lbl, BorderLayout.WEST);
        return header;
    }

    /**
     * Tạo body panel (scroll + padding).
     */
    public static JScrollPane createBody(JPanel content) {
        content.setBorder(new EmptyBorder(18, 22, 10, 22));
        JScrollPane sp = new JScrollPane(content);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    /**
     * Tạo footer panel với nút Hủy + Lưu.
     */
    public static JPanel createFooter(JButton... buttons) {
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(UITheme.BG_MAIN);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(UITheme.BORDER_COLOR);
                g.drawLine(0, 0, getWidth(), 0);
                super.paintComponent(g);
            }
        };
        foot.setOpaque(false);
        foot.setBorder(new EmptyBorder(10, 16, 10, 16));
        foot.setPreferredSize(new Dimension(0, 52));
        for (JButton btn : buttons) foot.add(btn);
        return foot;
    }

    /**
     * Thêm 1 hàng label + component vào GridBagLayout panel.
     */
    public static void addRow(JPanel panel, GridBagConstraints gc,
                               int y, String label, JComponent field) {
        gc.gridx = 0; gc.gridy = y; gc.weightx = 0; gc.gridwidth = 1;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_SECONDARY);
        lbl.setPreferredSize(new Dimension(145, 28));
        panel.add(lbl, gc);
        gc.gridx = 1; gc.weightx = 1;
        panel.add(field, gc);
    }

    /**
     * Thêm 1 hàng label + value (readonly) vào GridBagLayout panel.
     */
    public static void addDetailRow(JPanel panel, GridBagConstraints gc,
                                     int y, String label, String value) {
        gc.gridx = 0; gc.gridy = y; gc.gridwidth = 1; gc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_SECONDARY);
        lbl.setPreferredSize(new Dimension(145, 28));
        panel.add(lbl, gc);

        gc.gridx = 1; gc.weightx = 1;
        JLabel val = new JLabel(value != null && !value.isEmpty() ? value : "—");
        val.setFont(UITheme.FONT_LABEL);
        val.setForeground(UITheme.TEXT_PRIMARY);
        panel.add(val, gc);
    }

    /**
     * Tạo JTextField chuẩn với focus effect.
     */
    public static JTextField styledField(int prefWidth) {
        JTextField f = new JTextField();
        f.setFont(UITheme.FONT_LABEL);
        f.setForeground(UITheme.TEXT_PRIMARY);
        f.setPreferredSize(new Dimension(prefWidth, 34));
        applyFieldBorder(f, false);
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) { applyFieldBorder(f, true); }
            public void focusLost (java.awt.event.FocusEvent e) { applyFieldBorder(f, false); }
        });
        return f;
    }

    public static JTextField styledFieldReadonly(int prefWidth) {
        JTextField f = styledField(prefWidth);
        f.setEditable(false);
        f.setBackground(UITheme.BG_MAIN);
        f.setForeground(UITheme.TEXT_SECONDARY);
        return f;
    }

    private static void applyFieldBorder(JTextField f, boolean focused) {
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(focused ? UITheme.PRIMARY : UITheme.BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        if (focused) f.setBackground(Color.WHITE);
        else if (f.isEditable()) f.setBackground(Color.WHITE);
    }

    /**
     * Tạo GridBagConstraints mặc định.
     */
    public static GridBagConstraints defaultGBC() {
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 4, 6, 4);
        gc.weightx = 1;
        return gc;
    }

    /**
     * Tạo label form section (nhóm fields).
     */
    public static JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(UITheme.BORDER_COLOR);
                g2.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
                g2.dispose();
            }
        };
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(UITheme.PRIMARY);
        lbl.setBorder(new EmptyBorder(10, 0, 6, 0));
        return lbl;
    }
}