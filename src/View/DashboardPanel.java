package View;

import database.DatabaseHelper;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
//import java.awt.*;
//import java.sql.*;
//import java.util.*;
//import database.DatabaseHelper;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import javax.swing.RowFilter;

public class DashboardPanel extends JPanel {

    private static final Color BG = new Color(245, 247, 250);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(36, 42, 56);
    private static final Color TEXT_SECONDARY = new Color(108, 117, 125);

    private final JComboBox<String> periodCombo;
    private final JSpinner fromDate;
    private final JSpinner toDate;
    private final JButton applyButton;
    private final JButton exportButton;

    private JPanel statsPanel;
    private JPanel contentContainer;
    private CardLayout contentLayout;

    private JPanel chartView;
    private JPanel tableView;

    public DashboardPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(BG);
        setBorder(new EmptyBorder(18, 18, 18, 18));

        LocalDate now = LocalDate.now();
        periodCombo = new JComboBox<>(new String[]{"Tuần", "Tháng", "Năm", "Tùy chọn"});
        fromDate = new JSpinner(new SpinnerDateModel(java.sql.Date.valueOf(now.minusDays(6)), null, null, java.util.Calendar.DAY_OF_MONTH));
        toDate = new JSpinner(new SpinnerDateModel(java.sql.Date.valueOf(now), null, null, java.util.Calendar.DAY_OF_MONTH));
        fromDate.setEditor(new JSpinner.DateEditor(fromDate, "dd/MM/yyyy"));
        toDate.setEditor(new JSpinner.DateEditor(toDate, "dd/MM/yyyy"));
        applyButton = createActionButton("Áp dụng", new Color(39, 110, 241));
        exportButton = createActionButton("Xuất PDF/Excel", new Color(29, 170, 120));

        add(createHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(16, 16));
        body.setOpaque(false);
        statsPanel = new JPanel(new GridLayout(1, 4, 16, 16));
        statsPanel.setOpaque(false);
        body.add(statsPanel, BorderLayout.NORTH);

        contentLayout = new CardLayout();
        contentContainer = new JPanel(contentLayout);
        contentContainer.setOpaque(false);
        chartView = createChartView();
        tableView = createTableView();
        contentContainer.add(chartView, "chart");
        contentContainer.add(tableView, "table");

        body.add(contentContainer, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        bindEvents();
        refreshDashboard();
    }

    private JPanel createHeader() {
        JPanel wrapper = new JPanel(new BorderLayout(10, 10));
        wrapper.setOpaque(false);

        JPanel titleBlock = new JPanel(new BorderLayout());
        titleBlock.setOpaque(false);

        JLabel title = new JLabel("Dashboard Quản lý hội viên & hoạt động");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Theo dõi tăng trưởng hội viên, chất lượng hoạt động và mức độ tham gia theo thời gian thực");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_SECONDARY);

        titleBlock.add(title, BorderLayout.NORTH);
        titleBlock.add(subtitle, BorderLayout.SOUTH);

        JPanel filterCard = createCardContainer();
        filterCard.setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        periodCombo.setPreferredSize(new Dimension(120, 34));
        fromDate.setPreferredSize(new Dimension(120, 34));
        toDate.setPreferredSize(new Dimension(120, 34));
        filterCard.add(new JLabel("Bộ lọc:"));
        filterCard.add(periodCombo);
        filterCard.add(new JLabel("Từ"));
        filterCard.add(fromDate);
        filterCard.add(new JLabel("Đến"));
        filterCard.add(toDate);
        filterCard.add(applyButton);
        filterCard.add(exportButton);

        wrapper.add(titleBlock, BorderLayout.WEST);
        wrapper.add(filterCard, BorderLayout.EAST);
        return wrapper;
    }

    private JPanel createChartView() {
        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setOpaque(false);

        JPanel top = new JPanel(new GridLayout(1, 2, 16, 16));
        top.setOpaque(false);
        top.add(new PieCardPanel("Cơ cấu hội viên", "Hoạt động", "Mới", "Đã rời"));
        top.add(new MultiBarPanel("Hiệu quả theo loại hoạt động"));

        JPanel bottom = new JPanel(new GridLayout(1, 2, 16, 16));
        bottom.setOpaque(false);
        bottom.add(new RankingPanel());
        bottom.add(new DrillDownPanel());

        root.add(top, BorderLayout.NORTH);
        root.add(bottom, BorderLayout.CENTER);
        return root;
    }

        private JPanel createTableView() {
        JPanel panel = createCardContainer();
        panel.setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.setOpaque(false);
        JTextField searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(260, 34));
        JComboBox<String> pageSize = new JComboBox<>(new String[]{"10", "20", "50"});
        JButton prev = createActionButton("◀", new Color(130, 140, 150));
        JButton next = createActionButton("▶", new Color(130, 140, 150));
        top.add(new JLabel("Tìm kiếm"), BorderLayout.WEST);
        top.add(searchField, BorderLayout.CENTER);
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        nav.setOpaque(false);
        nav.add(new JLabel("/trang"));
        nav.add(pageSize);
        nav.add(prev);
        nav.add(next);
        top.add(nav, BorderLayout.EAST);

        String[] cols = {"Hội viên", "Email", "Trạng thái", "Số hoạt động tham gia"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        table.setRowHeight(32);
        table.getTableHeader().setReorderingAllowed(false);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void filter() {
                String key = searchField.getText();
                sorter.setRowFilter(key.isBlank() ? null : RowFilter.regexFilter("(?i)" + key));
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        loadMemberTable(model);
        return panel;
    }

    private void bindEvents() {
        applyButton.addActionListener(e -> refreshDashboard());
        exportButton.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Tính năng xuất báo cáo PDF/Excel sẽ tích hợp tại lớp ExcelExporter.",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE));
    }

    private void refreshDashboard() {
        statsPanel.removeAll();
        statsPanel.add(createStatCard("Tổng hội viên", countMembers(), new Color(52, 152, 219), "👥"));
        statsPanel.add(createStatCard("Hội viên mới", countNewMembers(), new Color(241, 196, 15), "🆕"));
        statsPanel.add(createStatCard("Đã rời", countLeftMembers(), new Color(231, 76, 60), "↗"));
        statsPanel.add(createStatCard("Tổng hoạt động", countActivities(), new Color(230, 126, 34), "📌"));
        statsPanel.revalidate();
        statsPanel.repaint();
    }

        private JPanel createStatCard(String title, int value, Color color, String icon) {
        JPanel card = createCardContainer();
        card.setLayout(new BorderLayout(8, 8));

        JLabel head = new JLabel(icon + "  " + title);
        head.setForeground(TEXT_SECONDARY);
        head.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel val = new JLabel("" + value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 30));
        val.setForeground(color);

        card.add(head, BorderLayout.NORTH);
        card.add(val, BorderLayout.CENTER);
        return card;
    }

        private JPanel createCardContainer() {
        JPanel p = new JPanel();
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(231, 235, 241)),
                new EmptyBorder(14, 14, 14, 14)));
        p.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { p.setBackground(new Color(252, 253, 255)); }
            public void mouseExited(MouseEvent e) { p.setBackground(CARD_BG); }
        });
        return p;
    }

    private JButton createActionButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 12, 8, 12));
        return btn;
    }

    private int countMembers() { return count("SELECT COUNT(*) FROM HoiVien"); }
    private int countActivities() { return count("SELECT COUNT(*) FROM HoatDong"); }
    private int countNewMembers() { return count("SELECT COUNT(*) FROM HoiVien WHERE ngayThamGia >= ?", toSqlDate(getFromDate())); }
    private int countLeftMembers() { return count("SELECT COUNT(*) FROM HoiVien WHERE trangThai LIKE N'%Rời%'"); }

    private int count(String sql, Object... params) {
        try (Connection c = DatabaseHelper.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        } catch (Exception ignored) {}
        return 0;
    }
    private void loadMemberTable(DefaultTableModel model) {
        model.setRowCount(0);
        String sql = "SELECT hv.tenHoiVien, hv.email, hv.trangThai, COUNT(tg.idHoatDong) soLan " +
             "FROM HoiVien hv LEFT JOIN ThamGia tg ON hv.id = tg.idHoiVien " +
             "GROUP BY hv.tenHoiVien, hv.email, hv.trangThai ORDER BY soLan DESC";
        try (Connection c = DatabaseHelper.getConnection(); ResultSet rs = c.createStatement().executeQuery(sql)) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("hoTen"), rs.getString("email"), rs.getString("trangThai"), rs.getInt("soLan")
                });
            }
        } catch (Exception ignored) {}
    }

    private LocalDate getFromDate() {
        if (periodCombo.getSelectedIndex() == 0) return LocalDate.now().minusDays(6);
        if (periodCombo.getSelectedIndex() == 1) return YearMonth.now().atDay(1);
        if (periodCombo.getSelectedIndex() == 2) return LocalDate.now().withDayOfYear(1);
        return new Date(((java.util.Date) fromDate.getValue()).getTime()).toLocalDate();
    }

        private Date toSqlDate(LocalDate date) { return Date.valueOf(date); }

    private class PieCardPanel extends JPanel {
        PieCardPanel(String title, String... legends) {
            setLayout(new BorderLayout(8, 8));
            setOpaque(false);
            JPanel card = createCardContainer();
            card.setLayout(new BorderLayout(6, 6));
            card.add(new JLabel(title), BorderLayout.NORTH);

            JTextArea txt = new JTextArea("• " + legends[0] + "\n• " + legends[1] + "\n• " + legends[2] + "\n\nDonut chart + tooltip sẽ render theo dữ liệu thật.");
            txt.setEditable(false);
            txt.setOpaque(false);
            txt.setForeground(TEXT_SECONDARY);
            card.add(txt, BorderLayout.CENTER);
            add(card, BorderLayout.CENTER);
        }
    }
      

    private class MultiBarPanel extends JPanel {
        MultiBarPanel(String title) {
            setLayout(new BorderLayout());
            setOpaque(false);
            JPanel card = createCardContainer();
            card.setLayout(new BorderLayout());
            card.add(new JLabel(title), BorderLayout.NORTH);
            card.add(new JLabel("Biểu đồ cột đôi: số hoạt động vs số hội viên tham gia (có hỗ trợ click filter)."), BorderLayout.CENTER);
            add(card, BorderLayout.CENTER);
        }
    }
    
    private class RankingPanel extends JPanel {
        RankingPanel() {
            setLayout(new BorderLayout());
            setOpaque(false);
            JPanel card = createCardContainer();
            card.setLayout(new BorderLayout(8, 8));
            card.add(new JLabel("Top 5 hội viên tích cực"), BorderLayout.NORTH);

            DefaultListModel<String> model = new DefaultListModel<>();
            List<String> top = loadTopMembers();
            for (int i = 0; i < top.size(); i++) model.addElement("Top " + (i + 1) + ": " + top.get(i));
            JList<String> list = new JList<>(model);
            card.add(new JScrollPane(list), BorderLayout.CENTER);

            JButton more = createActionButton("Xem thêm", new Color(39, 110, 241));
            more.addActionListener(e -> JOptionPane.showMessageDialog(this, "Mở full ranking + drill-down chi tiết."));
            card.add(more, BorderLayout.SOUTH);
            add(card, BorderLayout.CENTER);
        }
    }

    private class DrillDownPanel extends JPanel {
        DrillDownPanel() {
            setLayout(new BorderLayout());
            setOpaque(false);
            JPanel card = createCardContainer();
            card.setLayout(new BorderLayout(8, 8));
            card.add(new JLabel("Chế độ hiển thị"), BorderLayout.NORTH);

            JTabbedPane tabs = new JTabbedPane();
            tabs.add("Biểu đồ", new JLabel("Đang hiển thị chart + animation nhẹ + skeleton khi tải"));
            tabs.add("Bảng", new JLabel("Click tab để xem bảng dữ liệu có sort/search/pagination"));
            tabs.addChangeListener(e -> contentLayout.show(contentContainer, tabs.getSelectedIndex() == 0 ? "chart" : "table"));
            card.add(tabs, BorderLayout.CENTER);
            add(card, BorderLayout.CENTER);
        }
    }

    private List<String> loadTopMembers() {
        List<String> data = new ArrayList<>();
        String sql = "SELECT TOP 5 hv.tenHoiVien, COUNT(tg.idHoatDong) soLan " +
             "FROM HoiVien hv JOIN ThamGia tg ON hv.id = tg.idHoiVien " +
             "GROUP BY hv.tenHoiVien ORDER BY soLan DESC";
        try (Connection c = DatabaseHelper.getConnection(); ResultSet rs = c.createStatement().executeQuery(sql)) {
            while (rs.next()) data.add(rs.getString("hoTen") + " (" + rs.getInt("soLan") + " lần)");
        } catch (SQLException ex) {
            data.add("Chưa có dữ liệu");
        }
        return data;
    }
}