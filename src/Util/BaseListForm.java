package Util;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

/**
 * BaseListForm – Abstract base cho các panel danh sách.
 *
 * Cung cấp layout chuẩn:
 *   [Header: title + btnAdd]
 *   [Filter bar card]
 *   [Table card: header + table + pagination bar]
 *
 * Subclass override: buildColumns(), buildFilterBar(), loadTable(), search().
 */
public abstract class BaseListForm extends JPanel {

    protected StyledTable table;
    protected DefaultTableModel model;

    // ─── Filter bar components (subclass sets these) ────────────────────
    protected JTextField txtSearch;

    public BaseListForm() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(22, 26, 22, 26));
    }

    /** Gọi sau khi subclass đã init model & table */
    protected void buildUI(String pageTitle, String pageSubtitle,
                           String addLabel,
                           JPanel filterContent,
                           JPanel tableActionsRight) {

        // ── PAGE HEADER ───────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 14, 0));

        JPanel titlePanel = UITheme.pageTitlePanel(pageTitle, pageSubtitle);
        header.add(titlePanel, BorderLayout.WEST);

        if (addLabel != null) {
            JButton btnAdd = UITheme.primaryButton("  ＋  " + addLabel);
            btnAdd.setPreferredSize(new Dimension(0, 36));
            btnAdd.addActionListener(e -> onAdd());
            header.add(btnAdd, BorderLayout.EAST);
        }
        add(header, BorderLayout.NORTH);

        // ── CENTER ────────────────────────────────────────────────────
        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);

        // Filter card
        JPanel filterCard = UITheme.cardPanel(new BorderLayout());
        filterCard.setBorder(new EmptyBorder(10, 14, 10, 14));
        filterCard.add(filterContent, BorderLayout.CENTER);
        center.add(filterCard, BorderLayout.NORTH);

        // Table card
        JPanel tableCard = UITheme.cardPanel(new BorderLayout());

        // Table card header
        JPanel tblHead = buildTableCardHeader(tableActionsRight);
        tableCard.add(tblHead, BorderLayout.NORTH);

        // Table scroll
        JScrollPane scroll = UITheme.styledScrollPane(table);
        tableCard.add(scroll, BorderLayout.CENTER);

        // Pagination bar (optional, subclass can override)
        JPanel paging = buildPagingBar();
        if (paging != null) tableCard.add(paging, BorderLayout.SOUTH);

        center.add(tableCard, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    private JPanel buildTableCardHeader(JPanel actionsRight) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, UITheme.PRIMARY_LIGHT,
                        getWidth(), 0, Color.WHITE);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(UITheme.BORDER_COLOR);
                g2.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(9, 14, 9, 14));

        JLabel lTitle = new JLabel(getTableTitle());
        lTitle.setFont(UITheme.FONT_BOLD);
        lTitle.setForeground(UITheme.TEXT_PRIMARY);
        p.add(lTitle, BorderLayout.WEST);

        if (actionsRight != null) {
            actionsRight.setOpaque(false);
            p.add(actionsRight, BorderLayout.EAST);
        }
        return p;
    }

    // ─── Paging bar (subclass override if needed) ──────────────────────
    protected JPanel buildPagingBar() { return null; }

    // ─── Abstract / overridable ────────────────────────────────────────
    protected abstract String getTableTitle();
    protected abstract void onAdd();
    public    abstract void loadTable();
    protected void search() { loadTable(); }

    // ─── Helpers ───────────────────────────────────────────────────────
    protected JPanel buildDefaultActions(Runnable onEdit, Runnable onDelete) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        p.setOpaque(false);
        JButton btnEdit = UITheme.outlineButton("✏  Sửa");
        JButton btnDel  = UITheme.dangerButton("🗑  Xóa");
        btnEdit.addActionListener(e -> onEdit.run());
        btnDel .addActionListener(e -> onDelete.run());
        p.add(btnEdit); p.add(btnDel);
        return p;
    }

    protected JPanel buildSearchBar(String placeholder, JComponent... extras) {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bar.setOpaque(false);
        txtSearch = new JTextField(18);
        JPanel searchWrap = UITheme.searchField(txtSearch, placeholder);
        bar.add(searchWrap);
        for (JComponent c : extras) bar.add(c);
        return bar;
    }

    protected String str(Object o) { return o == null ? "" : o.toString(); }

    protected int getSelectedId() {
        int row = table.getSelectedRow();
        if (row < 0) return -1;
        Object v = model.getValueAt(row, 0);
        return v instanceof Integer ? (int) v : -1;
    }
}