package View;

import database.DatabaseHelper;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SpinnerDateModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.CategoryPlot;

import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.category.DefaultCategoryDataset;

public class DashboardPanel extends JPanel {

    private static final Color BG = new Color(12, 14, 18);
    private static final Color CARD_BG = new Color(45, 47, 50);
    private static final Color TEXT_PRIMARY = new Color(235, 236, 240);
    private static final Color TEXT_SECONDARY = new Color(184, 185, 190);

    private final JComboBox<String> periodCombo;
    private final JSpinner fromDate;
    private final JSpinner toDate;
    private final JButton applyButton;
    private final JButton exportButton;

    private JPanel statsPanel;
    private JPanel contentContainer;
    private CardLayout contentLayout;

//    private JPanel chartView;
//    private JPanel tableView;

    public DashboardPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(BG);
        setBorder(new EmptyBorder(18, 18, 18, 18));

        LocalDate now = LocalDate.now();
        periodCombo = new JComboBox<>(new String[]{"Tuần này", "Tháng này", "Năm nay", "Tùy chọn"});
        fromDate = new JSpinner(new SpinnerDateModel(java.sql.Date.valueOf(now.minusDays(6)), null, null, java.util.Calendar.DAY_OF_MONTH));
        toDate = new JSpinner(new SpinnerDateModel(java.sql.Date.valueOf(now), null, null, java.util.Calendar.DAY_OF_MONTH));
        fromDate.setEditor(new JSpinner.DateEditor(fromDate, "dd/MM/yyyy"));
        toDate.setEditor(new JSpinner.DateEditor(toDate, "dd/MM/yyyy"));
        applyButton = createActionButton("Áp dụng", new Color(55, 55, 55));
        exportButton = createActionButton("Xuất", new Color(55, 55, 55));

        add(createHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(16, 16));
        body.setOpaque(false);
        statsPanel = new JPanel(new GridLayout(1, 4, 16, 16));
        statsPanel.setOpaque(false);
        body.add(statsPanel, BorderLayout.NORTH);
        
        JPanel center = new JPanel(new BorderLayout(16, 16));
        center.setOpaque(false);
        center.add(createViewSwitch(), BorderLayout.NORTH);

        contentLayout = new CardLayout();
        contentContainer = new JPanel(contentLayout);
        contentContainer.setOpaque(false);
        contentContainer.add(createChartView(), "chart");
        contentContainer.add(createTableView(), "table");
        center.add(contentContainer, BorderLayout.CENTER);

        body.add(center, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        bindEvents();
        refreshDashboard();
    }

    private JPanel createHeader() {
        JPanel wrapper = new JPanel(new BorderLayout(10, 10));
        wrapper.setOpaque(false);

        JPanel titleBlock = new JPanel(new BorderLayout());
        titleBlock.setOpaque(false);

        JLabel title = new JLabel("Dashboard Quản lý Hội viên");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Theo dõi tăng trưởng và hoạt động theo thời gian thực");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_SECONDARY);

        titleBlock.add(title, BorderLayout.NORTH);
        titleBlock.add(subtitle, BorderLayout.SOUTH);

        JPanel filterCard = createCardContainer();
        filterCard.setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        periodCombo.setPreferredSize(new Dimension(140, 34));
        fromDate.setPreferredSize(new Dimension(120, 34));
        toDate.setPreferredSize(new Dimension(120, 34));
//        filterCard.add(new JLabel("Bộ lọc:"));
        filterCard.add(periodCombo);
//        filterCard.add(new JLabel("Từ"));
        filterCard.add(fromDate);
//        filterCard.add(new JLabel("Đến"));
        filterCard.add(toDate);
        filterCard.add(applyButton);
        filterCard.add(exportButton);

        wrapper.add(titleBlock, BorderLayout.WEST);
        wrapper.add(filterCard, BorderLayout.EAST);
        return wrapper;
    }

    private JPanel createViewSwitch() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        panel.setOpaque(false);
        JLabel lb = new JLabel("Hiển thị:");
        lb.setForeground(TEXT_SECONDARY);

        JRadioButton chart = createToggle("Biểu đồ", true);
        JRadioButton table = createToggle("Bảng số liệu", false);
        ButtonGroup group = new ButtonGroup();
        group.add(chart);
        group.add(table);
        chart.addActionListener(e -> contentLayout.show(contentContainer, "chart"));
        table.addActionListener(e -> contentLayout.show(contentContainer, "table"));

        panel.add(lb);
        panel.add(chart);
        panel.add(table);
        return panel;
    }

    private JRadioButton createToggle(String text, boolean selected) {
        JRadioButton btn = new JRadioButton(text, selected);
        btn.setOpaque(true);
        btn.setBackground(new Color(30, 31, 34));
        btn.setForeground(TEXT_PRIMARY);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(90, 92, 95)), new EmptyBorder(10, 20, 10, 20)));
        return btn;
    }
    
    

    private JPanel createChartView() {
        JPanel root = new JPanel(new GridLayout(2, 2, 16, 16));
        root.setOpaque(false);
        root.add(createMemberDonutChart());
        root.add(createActivityTypeChart());
        root.add(createTopMemberChart());
        root.add(new PlaceholderCard("Tình trạng tham gia", "Biểu đồ sẽ cập nhật"));
        return root;
    }
    
    private JPanel createMemberDonutChart() {

    DefaultPieDataset dataset = new DefaultPieDataset();

    dataset.setValue("Hoạt động", count("SELECT COUNT(*) FROM HoiVien WHERE trangThai=N'Hoạt động'"));
    dataset.setValue("Đã rời", count("SELECT COUNT(*) FROM HoiVien WHERE trangThai LIKE N'%Rời%'"));
    dataset.setValue("Mới", countNewMembers());

    JFreeChart chart = ChartFactory.createRingChart(
            "Cơ cấu hội viên",
            dataset,
            true,
            true,
            false
    );

    PiePlot plot = (PiePlot) chart.getPlot();
    plot.setBackgroundPaint(CARD_BG);
    plot.setOutlineVisible(false);

    ChartPanel panel = new ChartPanel(chart);
    panel.setMouseWheelEnabled(true);

    JPanel wrapper = createCardContainer();
    wrapper.setLayout(new BorderLayout());
    wrapper.add(panel);

    return wrapper;
}
    
    
    private JPanel createActivityTypeChart() {

    DefaultCategoryDataset dataset = new DefaultCategoryDataset();

    String sql =
            "SELECT hd.loaiHoatDong, " +
            "COUNT(DISTINCT hd.id) soHD, " +
            "COUNT(tg.idHoiVien) soTG " +
            "FROM HoatDong hd " +
            "LEFT JOIN ThamGia tg ON hd.id=tg.idHoatDong " +
            "GROUP BY hd.loaiHoatDong";

    try (Connection c = DatabaseHelper.getConnection();
         ResultSet rs = c.createStatement().executeQuery(sql)) {

        while (rs.next()) {

            String loai = rs.getString("loaiHoatDong");

            dataset.addValue(rs.getInt("soHD"), "Hoạt động", loai);
            dataset.addValue(rs.getInt("soTG"), "Tham gia", loai);
        }

    } catch (Exception ex) {
        ex.printStackTrace();
    }

    JFreeChart chart = ChartFactory.createBarChart(
            "Hoạt động theo loại",
            "Loại",
            "Số lượng",
            dataset
    );

    CategoryPlot plot = chart.getCategoryPlot();
    plot.setBackgroundPaint(Color.WHITE);

    ChartPanel cp = new ChartPanel(chart);

    JPanel wrapper = createCardContainer();
    wrapper.setLayout(new BorderLayout());
    wrapper.add(cp);

    return wrapper;
}
    
    
    private JPanel createTopMemberChart() {

    DefaultCategoryDataset dataset = new DefaultCategoryDataset();

    String sql =
            "SELECT TOP 5 hv.tenHoiVien, COUNT(tg.idHoatDong) soLan " +
            "FROM HoiVien hv " +
            "JOIN ThamGia tg ON hv.id=tg.idHoiVien " +
            "GROUP BY hv.tenHoiVien " +
            "ORDER BY soLan DESC";

    try (Connection c = DatabaseHelper.getConnection();
         ResultSet rs = c.createStatement().executeQuery(sql)) {

        while (rs.next()) {
            dataset.addValue(
                    rs.getInt("soLan"),
                    "Tham gia",
                    rs.getString("tenHoiVien")
            );
        }

    } catch (Exception ex) {
        ex.printStackTrace();
    }

    JFreeChart chart = ChartFactory.createBarChart(
            "Top hội viên tích cực",
            "Hội viên",
            "Số lần",
            dataset
    );

    CategoryPlot plot = chart.getCategoryPlot();
    plot.setRangeGridlinePaint(Color.GRAY);

    ChartPanel cp = new ChartPanel(chart);

    JPanel wrapper = createCardContainer();
    wrapper.setLayout(new BorderLayout());
    wrapper.add(cp);

    return wrapper;
}
    

    private JPanel createTableView() {
        JPanel panel = createCardContainer();
        panel.setLayout(new BorderLayout(10, 10));

//        JPanel top = new JPanel(new BorderLayout(8, 8));
//        top.setOpaque(false);
        JTextField searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(260, 34));
//        JComboBox<String> pageSize = new JComboBox<>(new String[]{"10", "20", "50"});
//        JButton prev = createActionButton("◀", new Color(130, 140, 150));
//        JButton next = createActionButton("▶", new Color(130, 140, 150));
//        top.add(new JLabel("Tìm kiếm"), BorderLayout.WEST);
//        top.add(searchField, BorderLayout.CENTER);
//        JPanel nav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
//        nav.setOpaque(false);
//        nav.add(new JLabel("/trang"));
//        nav.add(pageSize);
//        nav.add(prev);
//        nav.add(next);
//        top.add(nav, BorderLayout.EAST);

        String[] cols = {"Hội viên", "Email", "Trạng thái", "Số hoạt động tham gia"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        table.setRowHeight(32);
        table.getTableHeader().setReorderingAllowed(false);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            private void filter() {
                String key = searchField.getText();
                sorter.setRowFilter(key.isBlank() ? null : RowFilter.regexFilter("(?i)" + key));
            }
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
        });
        
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel label = new JLabel("Tìm kiếm:");
        label.setForeground(TEXT_SECONDARY);
        top.add(label, BorderLayout.WEST);
        top.add(searchField, BorderLayout.CENTER);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        loadMemberTable(model);
        return panel;
    }

    private void bindEvents() {
        applyButton.addActionListener(e -> refreshDashboard());
        exportButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Chức năng xuất đang được phát triển."));
    }

    private void refreshDashboard() {
        statsPanel.removeAll();
        statsPanel.add(createStatCard("Tổng hội viên", countMembers(), new Color(46, 134, 222)));
        statsPanel.add(createStatCard("Hội viên mới", countNewMembers(), new Color(211, 145, 33)));
        statsPanel.add(createStatCard("Đã rời", countLeftMembers(), new Color(192, 57, 43)));
        statsPanel.add(createStatCard("Tổng hoạt động", countActivities(), new Color(22, 160, 133)));
        statsPanel.revalidate();
        statsPanel.repaint();
    }

        private JPanel createStatCard(String title, int value, Color color) {
        JPanel card = createCardContainer();
        card.setLayout(new BorderLayout(6, 6));
        JLabel head = new JLabel(title.toUpperCase());
        head.setForeground(TEXT_SECONDARY);
        head.setFont(new Font("Segoe UI", Font.BOLD, 15));
        JLabel val = new JLabel(String.valueOf(value));
        val.setFont(new Font("Segoe UI", Font.BOLD, 42));
        val.setForeground(color);

        card.add(head, BorderLayout.NORTH);
        card.add(val, BorderLayout.CENTER);
        return card;
    }

    private JPanel createCardContainer() {
        JPanel p = new JPanel();
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(86, 89, 94)),
                new EmptyBorder(14, 14, 14, 14)));
//        p.addMouseListener(new MouseAdapter() {
//            public void mouseEntered(MouseEvent e) { p.setBackground(new Color(252, 253, 255)); }
//            public void mouseExited(MouseEvent e) { p.setBackground(CARD_BG); }
//        });
        return p;
    }

    private JButton createActionButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        return btn;
    }

    private int countMembers() { return count("SELECT COUNT(*) FROM HoiVien"); }
    private int countActivities() { return count("SELECT COUNT(*) FROM HoatDong"); }
    private int countNewMembers() { return count("SELECT COUNT(*) FROM HoiVien WHERE ngayThamGia >= ?", toSqlDate(getFromDate())); }
    private int countLeftMembers() { return count("SELECT COUNT(*) FROM HoiVien WHERE trangThai LIKE N'%Rời%'"); }

    private int count(String sql, Object... params) {
        try (Connection c = DatabaseHelper.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception ignored) {}
        return 0;
    }
    private void loadMemberTable(DefaultTableModel model) {
        model.setRowCount(0);
        String sql = "SELECT hv.tenHoiVien, hv.email, hv.trangThai, COUNT(tg.idHoatDong) soLan "
                + "FROM HoiVien hv LEFT JOIN ThamGia tg ON hv.id = tg.idHoiVien "
                + "GROUP BY hv.tenHoiVien, hv.email, hv.trangThai ORDER BY soLan DESC";
        try (Connection c = DatabaseHelper.getConnection(); ResultSet rs = c.createStatement().executeQuery(sql)) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("tenHoiVien"), rs.getString("email"), rs.getString("trangThai"), rs.getInt("soLan")
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

    private Date toSqlDate(LocalDate date) {
        return Date.valueOf(date);
    }
      

    private List<String> loadTopMembers() {
        List<String> data = new ArrayList<>();
        String sql = "SELECT TOP 5 hv.tenHoiVien, COUNT(tg.idHoatDong) soLan "
                + "FROM HoiVien hv JOIN ThamGia tg ON hv.id = tg.idHoiVien "
                + "GROUP BY hv.tenHoiVien ORDER BY soLan DESC";
        try (Connection c = DatabaseHelper.getConnection(); ResultSet rs = c.createStatement().executeQuery(sql)) {
            while (rs.next()) data.add(rs.getString("tenHoiVien") + " (" + rs.getInt("soLan") + " lần)");
        } catch (SQLException ex) {
            data.add("Chưa có dữ liệu");
        }
        return data;
    }

    private class PlaceholderCard extends JPanel {
        PlaceholderCard(String title, String content) {
            setLayout(new BorderLayout());
            setOpaque(false);
            JPanel card = createCardContainer();
            card.setLayout(new BorderLayout(6, 6));
            JLabel t = new JLabel(title);
            t.setForeground(TEXT_PRIMARY);
            t.setFont(new Font("Segoe UI", Font.BOLD, 20));
            JLabel c = new JLabel("<html><div style='color:#BBBBBB;line-height:1.8'>" + content.replace("\n", "<br>") + "</div></html>");
            card.add(t, BorderLayout.NORTH);
            card.add(c, BorderLayout.CENTER);
            add(card, BorderLayout.CENTER);
        }
    }

//    private List<String> loadTopMembers() {
//        List<String> data = new ArrayList<>();
//        String sql = "SELECT TOP 5 hv.tenHoiVien, COUNT(tg.idHoatDong) soLan " +
//             "FROM HoiVien hv JOIN ThamGia tg ON hv.id = tg.idHoiVien " +
//             "GROUP BY hv.tenHoiVien ORDER BY soLan DESC";
//        try (Connection c = DatabaseHelper.getConnection(); ResultSet rs = c.createStatement().executeQuery(sql)) {
//            while (rs.next()) data.add(rs.getString("hoTen") + " (" + rs.getInt("soLan") + " lần)");
//        } catch (SQLException ex) {
//            data.add("Chưa có dữ liệu");
//        }
//        return data;
//    }
}