package Util;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

public class StyledTable extends JTable {

    private int hoveredRow = -1;
    private RowDetailCallback detailCallback;

    public interface RowDetailCallback {
        void showDetail(int row);
    }

    public StyledTable(DefaultTableModel model) {
        super(model);
        setFont(UITheme.FONT_LABEL);
        setRowHeight(UITheme.TABLE_ROW_HEIGHT);
        setShowHorizontalLines(true);
        setShowVerticalLines(false);
        setGridColor(UITheme.BORDER_COLOR);
        setSelectionBackground(UITheme.PRIMARY_LIGHT);
        setSelectionForeground(UITheme.TEXT_PRIMARY);
        setFocusable(false);
        getTableHeader().setFont(UITheme.FONT_BOLD);
        getTableHeader().setBackground(UITheme.PRIMARY);
        getTableHeader().setForeground(Color.WHITE);
        getTableHeader().setPreferredSize(new Dimension(0, 40));
        getTableHeader().setBorder(BorderFactory.createEmptyBorder());
        setDefaultRenderer(Object.class, new StripedRenderer());
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                if (row != hoveredRow) {
                    hoveredRow = row;
                    repaint();
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            public void mouseExited(MouseEvent e) {
                hoveredRow = -1;
                repaint();
            }
            public void mouseClicked(MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                if (row >= 0 && detailCallback != null && e.getClickCount() == 1) {
                    int col = columnAtPoint(e.getPoint());
                    // Trigger detail on last column or if action column
                    // Just delegate to callback for single click
                }
            }
        });
    }

    public void setDetailCallback(RowDetailCallback cb) {
        this.detailCallback = cb;
    }

    private class StripedRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            if (isSelected) {
                c.setBackground(UITheme.PRIMARY_LIGHT);
                c.setForeground(UITheme.TEXT_PRIMARY);
            } else if (row == hoveredRow) {
                c.setBackground(Color.decode("#EBF4FA"));
                c.setForeground(UITheme.TEXT_PRIMARY);
            } else if (row % 2 == 0) {
                c.setBackground(Color.WHITE);
                c.setForeground(UITheme.TEXT_PRIMARY);
            } else {
                c.setBackground(Color.decode("#F8FBFD"));
                c.setForeground(UITheme.TEXT_PRIMARY);
            }
            return c;
        }
    }
}