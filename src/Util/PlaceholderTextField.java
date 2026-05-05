package Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * JTextField có placeholder (gợi ý nhập) hiển thị khi chưa có nội dung
 */
public class PlaceholderTextField extends JTextField {

    private String placeholder;
    private Color placeholderColor = Color.decode("#AAAAAA");

    public PlaceholderTextField(String placeholder) {
        this.placeholder = placeholder;
        setFont(UITheme.FONT_LABEL);
    }

    public PlaceholderTextField(String placeholder, int columns) {
        super(columns);
        this.placeholder = placeholder;
        setFont(UITheme.FONT_LABEL);
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getText().isEmpty() && placeholder != null && !placeholder.isEmpty()) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setFont(getFont().deriveFont(Font.ITALIC));
            g2.setColor(placeholderColor);
            Insets insets = getInsets();
            g2.drawString(placeholder, insets.left + 2, getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 1);
            g2.dispose();
        }
    }
}