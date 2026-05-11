package Util;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

/**
 * StyledTable – JTable với giao diện hiện đại, đồng nhất.
 * Tích hợp: striped rows, hover highlight, status badge renderer,
 * gradient header, auto row height.
 */
public class StyledTable extends JTable {

    private static final Color ROW_ODD    = Color.WHITE;
    private static final Color ROW_EVEN   = Color.decode("#F8FBFF");
    private static final Color ROW_HOVER  = Color.decode("#EBF3FF");
    private static final Color ROW_SEL    = Color.decode("#D4E6FF");
    private static final Color SEL_FG     = UITheme.TEXT_PRIMARY;

    private int hoveredRow = -1;

    public StyledTable(TableModel model) {
        super(model);
        applyStyle();
    }

    private void applyStyle() {
        setFont(UITheme.FONT_LABEL);
        setForeground(UITheme.TEXT_PRIMARY);
        setRowHeight(UITheme.TABLE_ROW_HEIGHT);
        setShowHorizontalLines(true);
        setShowVerticalLines(false);
        setGridColor(UITheme.BORDER_COLOR);
        setSelectionBackground(ROW_SEL);
        setSelectionForeground(SEL_FG);
        setFillsViewportHeight(true);
        setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        setIntercellSpacing(new Dimension(0, 0));

        // Header
        JTableHeader header = getTableHeader();
        header.setFont(UITheme.FONT_BOLD);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 42));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new GradientHeaderRenderer());

        // Default cell renderer
        setDefaultRenderer(Object.class, new ModernCellRenderer());

        // Hover mouse listener
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override public void mouseMoved(java.awt.event.MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                if (row != hoveredRow) {
                    hoveredRow = row;
                    repaint();
                }
            }
        });
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                hoveredRow = -1;
                repaint();
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────
    //  GRADIENT HEADER RENDERER
    // ─────────────────────────────────────────────────────────────────────
    private static class GradientHeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            lbl.setFont(UITheme.FONT_BOLD);
            lbl.setForeground(Color.WHITE);
            lbl.setOpaque(false);
            lbl.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            lbl.setHorizontalAlignment(SwingConstants.LEFT);
            return lbl;
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gp = new GradientPaint(
                0, 0, UITheme.PRIMARY,
                getWidth(), 0, UITheme.PRIMARY_HOVER);
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
            // separator
            g2.setColor(new Color(255,255,255,40));
            g2.drawLine(getWidth()-1, 6, getWidth()-1, getHeight()-6);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    //  MODERN CELL RENDERER
    // ─────────────────────────────────────────────────────────────────────
    private class ModernCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            lbl.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            lbl.setFont(UITheme.FONT_LABEL);
            lbl.setOpaque(true);

            // Background
            if (isSelected) {
                lbl.setBackground(ROW_SEL);
                lbl.setForeground(SEL_FG);
            } else if (row == hoveredRow) {
                lbl.setBackground(ROW_HOVER);
                lbl.setForeground(UITheme.TEXT_PRIMARY);
            } else {
                lbl.setBackground(row % 2 == 0 ? ROW_ODD : ROW_EVEN);
                lbl.setForeground(UITheme.TEXT_PRIMARY);
            }

            // Status column: render as colored badge
            String v = value != null ? value.toString().trim() : "";
            if (isStatusValue(v)) {
                lbl.setText(formatStatus(v));
            }

            return lbl;
        }

        private boolean isStatusValue(String v) {
            return v.contains("Hoạt động") || v.contains("Đã rời") || v.contains("Tạm dừng")
                || v.contains("Đã tham gia") || v.contains("Vắng") || v.contains("Đăng ký")
                || v.contains("Đang làm") || v.contains("Đã nghỉ") || v.contains("Chờ duyệt")
                || v.contains("Đã duyệt") || v.contains("Từ chối") || v.contains("Sắp diễn ra")
                || v.contains("Đang diễn ra") || v.contains("Đã kết thúc") || v.contains("Chờ xác nhận");
        }

        private String formatStatus(String v) {
            return "<html><body style='padding:1px 0'>"
                + "<span style='padding:3px 10px; border-radius:20px; font-size:11px; font-weight:700;"
                + "background:" + getBgHex(v) + "; color:" + getFgHex(v) + "'>"
                + "● " + v + "</span></body></html>";
        }

        private String getBgHex(String v) {
            if (v.contains("Hoạt động") || v.contains("Đã tham gia") || v.contains("Đang làm") || v.contains("Đã duyệt"))
                return "#d1fae5";
            if (v.contains("Tạm dừng") || v.contains("Đăng ký") || v.contains("Chờ") || v.contains("Sắp diễn ra"))
                return "#fef3c7";
            if (v.contains("Rời") || v.contains("Vắng") || v.contains("Từ chối") || v.contains("Đã nghỉ"))
                return "#fee2e2";
            if (v.contains("Đang diễn ra"))
                return "#dbeafe";
            return "#f1f5f9";
        }

        private String getFgHex(String v) {
            if (v.contains("Hoạt động") || v.contains("Đã tham gia") || v.contains("Đang làm") || v.contains("Đã duyệt"))
                return "#065f46";
            if (v.contains("Tạm dừng") || v.contains("Đăng ký") || v.contains("Chờ") || v.contains("Sắp diễn ra"))
                return "#92400e";
            if (v.contains("Rời") || v.contains("Vắng") || v.contains("Từ chối") || v.contains("Đã nghỉ"))
                return "#991b1b";
            if (v.contains("Đang diễn ra"))
                return "#1e40af";
            return "#475569";
        }
    }
}