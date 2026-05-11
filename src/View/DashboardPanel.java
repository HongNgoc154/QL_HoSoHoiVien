package View;

import Util.UITheme;
import database.DatabaseHelper;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.*;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.labels.*;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.axis.*;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.HorizontalAlignment;
import org.jfree.chart.plot.RingPlot;
import org.jfree.data.category.CategoryDataset;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.Rectangle2D;
import java.awt.BasicStroke;

public class DashboardPanel extends JPanel {

    // ─── Palette ─────────────────────────────────────────────────────────────
    private static final Color BG       = Color.decode("#F5F7FA");
    private static final Color CARD     = Color.WHITE;
    private static final Color BORDER_C = Color.decode("#E8ECF1");
    private static final Color TXT_H    = Color.decode("#1A202C");
    private static final Color TXT_S    = Color.decode("#718096");
    private static final Color BLUE     = Color.decode("#4361EE");
    private static final Color YELLOW   = Color.decode("#F6C90E");
    private static final Color RED      = Color.decode("#EF4444");
    private static final Color GREEN    = Color.decode("#10B981");
    private static final Color ORANGE   = Color.decode("#F97316");
    private static final Color PURPLE   = Color.decode("#8B5CF6");

    private static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font FONT_LABEL   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_STAT    = new Font("Segoe UI", Font.BOLD, 36);
    private static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FONT_BOLD    = new Font("Segoe UI", Font.BOLD, 13);

    // ─── State ───────────────────────────────────────────────────────────────
    private JComboBox<String> cbPeriod;
    private JSpinner spFrom, spTo;
    private JPanel pnlStats;
    private CardLayout viewCard;
    private JPanel viewContainer;

    // Chart-mode panels
    private JPanel donutHolder, barHolder;
    private JPanel topMembersChartPanel;
    private JPanel recentActChartPanel;

    // Table-mode models
    private DefaultTableModel tblCoCapHoiVien;
    private DefaultTableModel tblHoatDongLoai;
    private DefaultTableModel tblTopHoiVien;
    private DefaultTableModel tblHoatDongGanDay;

    // Top members full-list pagination
    private List<Object[]> topMembersFullData = new ArrayList<>();
    private int topMembersPage = 0;
    private static final int PAGE_SIZE = 10;
    private JLabel lblPageInfo;
    private JButton btnPrevPage, btnNextPage;
    private JTextField txtTopSearch;
    private DefaultTableModel tblTopFull;
    private JDialog dlgTopFull = null;

    // Loading overlay
    private JLabel lblLoading;
    private JPanel loadingOverlay;

    // Stat value labels
    private JLabel lblTotal, lblNew, lblLeft, lblActs;

    private JPanel bodyPanel;

    public DashboardPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG);

        JPanel inner = new JPanel(new BorderLayout(0, 16));
        inner.setBackground(BG);
        inner.setBorder(new EmptyBorder(24, 28, 24, 28));

        inner.add(buildHeader(), BorderLayout.NORTH);
        bodyPanel = buildBody();
        inner.add(bodyPanel, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(inner);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(BG);
        scroll.getViewport().setBackground(BG);
        add(scroll, BorderLayout.CENTER);

        SwingUtilities.invokeLater(this::refresh);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  HEADER
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildHeader() {
        // Card-style header with gradient top accent
        JPanel card = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                GradientPaint gp = new GradientPaint(0, 0, BLUE, getWidth(), 0, new Color(0x7B9FFF));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), 5, 4, 4);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(16, 22, 14, 22)));

        // Left: title + date
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        JLabel title = new JLabel("Tổng quan Dashboard");
        title.setFont(FONT_TITLE);
        title.setForeground(TXT_H);
        JLabel sub = new JLabel(LocalDate.now().format(
            DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", new Locale("vi"))));
        sub.setFont(FONT_LABEL);
        sub.setForeground(TXT_S);
        left.add(title);
        left.add(Box.createVerticalStrut(4));
        left.add(sub);

        // Right: 2-row filter area
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setOpaque(false);

        // Row 1: period + date pickers
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        filterRow.setOpaque(false);
        cbPeriod = new JComboBox<>(new String[]{"Tuần này", "Tháng này", "Năm nay", "Tùy chọn"});
        styleCombo(cbPeriod);
        LocalDate now = LocalDate.now();
        spFrom = dateSpinner(now.minusDays(6));
        spTo   = dateSpinner(now);
        spFrom.setEnabled(false);
        spTo.setEnabled(false);
        filterRow.add(lbl("Kỳ:", FONT_SMALL, TXT_S));
        filterRow.add(cbPeriod);
        filterRow.add(lbl("Từ", FONT_SMALL, TXT_S));
        filterRow.add(spFrom);
        filterRow.add(lbl("→", FONT_SMALL, TXT_S));
        filterRow.add(spTo);

        // Row 2: action buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnRow.setOpaque(false);
        JButton btnApply  = actionBtn("Áp dụng", BLUE);
        JButton btnReload = actionBtn("Tải lại", GREEN);
        JButton btnExport = actionBtn("Xuất file",   PURPLE);
        btnApply.setPreferredSize(new Dimension(100, 32));
        btnReload.setPreferredSize(new Dimension(100, 32));
        btnExport.setPreferredSize(new Dimension(90,  32));
        btnRow.add(btnApply);
        btnRow.add(btnReload);
        btnRow.add(btnExport);

        right.add(filterRow);
        right.add(Box.createVerticalStrut(6));
        right.add(btnRow);

        cbPeriod.addActionListener(e -> onPeriodChange());
        btnApply.addActionListener(e -> refresh());
        btnReload.addActionListener(e -> { animateReload(btnReload); refresh(); });
        btnExport.addActionListener(e -> showExportDialog());

        card.add(left,  BorderLayout.WEST);
        card.add(right, BorderLayout.EAST);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0, 0, 16, 0));
        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  BODY
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        // Stat cards row
        pnlStats = new JPanel(new GridLayout(1, 4, 16, 0));
        pnlStats.setOpaque(false);
        pnlStats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        pnlStats.setMinimumSize(new Dimension(0, 110));
        body.add(pnlStats);
        body.add(Box.createVerticalStrut(18));

        // Toggle row
        body.add(buildToggleRow());
        body.add(Box.createVerticalStrut(12));

        // Chart / Table view
        viewCard = new CardLayout();
        viewContainer = new JPanel(viewCard);
        viewContainer.setOpaque(false);
        viewContainer.add(buildChartView(),  "charts");
        viewContainer.add(buildTableView(),  "table");
        body.add(viewContainer);

        return body;
    }

    // ─── Stat cards ──────────────────────────────────────────────────────────
    private void buildStatCards() {
        pnlStats.removeAll();

        int total = count("SELECT COUNT(*) FROM HoiVien");
        int newM  = count("SELECT COUNT(*) FROM HoiVien WHERE ngayThamGia >= ?", fromDate());
        int left  = count("SELECT COUNT(*) FROM HoiVien WHERE trangThai LIKE N'%Rời%'");
        int acts  = count("SELECT COUNT(*) FROM HoatDong");

        pnlStats.add(statCard("👥 Tổng hội viên", total, "+12%", true,  BLUE,   new Color(235,240,255)));
        pnlStats.add(statCard("✨ Hội viên mới",   newM,  "+8%",  true,  GREEN,  new Color(209,250,229)));
        pnlStats.add(statCard("🚪 Đã rời",          left,  "-3%",  false, RED,    new Color(254,226,226)));
        pnlStats.add(statCard("📅 Hoạt động",        acts,  "+5%",  true,  ORANGE, new Color(255,237,213)));

        pnlStats.revalidate();
        pnlStats.repaint();
    }

    private JPanel statCard(String label, int value, String trend, boolean positive,
                             Color accent, Color bg) {
        JPanel p = new JPanel(new BorderLayout(0, 8)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // White background
                g2.setColor(CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                // Left accent stripe
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 5, getHeight(), 4, 4);
                // Subtle icon circle background
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 22));
                g2.fillOval(getWidth() - 56, 14, 42, 42);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(16, 22, 14, 20)));

        p.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                p.setBorder(new CompoundBorder(new LineBorder(accent, 2, true),
                    new EmptyBorder(15, 21, 13, 19)));
                p.repaint();
            }
            public void mouseExited(MouseEvent e) {
                p.setBorder(new CompoundBorder(new LineBorder(BORDER_C, 1, true),
                    new EmptyBorder(16, 22, 14, 20)));
                p.repaint();
            }
        });

        // Extract emoji icon from label (first character or emoji)
        String icon = label.contains(" ") ? label.split(" ")[0] : "•";
        String labelText = label.contains(" ") ? label.substring(label.indexOf(" ") + 1) : label;

        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblLabel = new JLabel(labelText.toUpperCase());
        lblLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblLabel.setForeground(TXT_S);
        lblLabel.setOpaque(true);
        lblLabel.setBackground(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 20));
        lblLabel.setBorder(new EmptyBorder(2, 6, 2, 6));

        JLabel lblVal = new JLabel(String.format("%,d", value));
        lblVal.setFont(FONT_STAT);
        lblVal.setForeground(TXT_H);

        String trendSymbol = positive ? "▲" : "▼";
        JLabel lblTrend = new JLabel(trendSymbol + " " + trend + " so với kỳ trước");
        lblTrend.setFont(FONT_SMALL);
        lblTrend.setForeground(positive ? GREEN : RED);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(lblLabel, BorderLayout.WEST);
        topRow.add(lblIcon,  BorderLayout.EAST);

        JPanel bottom = new JPanel(new BorderLayout(0, 2));
        bottom.setOpaque(false);
        bottom.add(lblVal,   BorderLayout.NORTH);
        bottom.add(lblTrend, BorderLayout.SOUTH);

        p.add(topRow,  BorderLayout.NORTH);
        p.add(bottom,  BorderLayout.CENTER);
        return p;
    }

    // ─── Toggle row ──────────────────────────────────────────────────────────
    private JPanel buildToggleRow() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabs.setOpaque(false);

        JToggleButton tbChart = toggleBtn("Biểu đồ",       true);
        JToggleButton tbTable = toggleBtn("Bảng số liệu",  false);
        ButtonGroup bg = new ButtonGroup();
        bg.add(tbChart); bg.add(tbTable);

        tbChart.addActionListener(e -> {
            viewCard.show(viewContainer, "charts");
        });
        tbTable.addActionListener(e -> {
            viewCard.show(viewContainer, "table");
            refreshTableData();
        });

        tabs.add(tbChart);
        tabs.add(Box.createHorizontalStrut(6));
        tabs.add(tbTable);
        p.add(tabs, BorderLayout.WEST);
        return p;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  CHART VIEW
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildChartView() {
        JPanel outer = new JPanel();
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        outer.setOpaque(false);

        // Row 1: Donut + Bar
        JPanel row1 = new JPanel(new GridLayout(1, 2, 16, 0));
        row1.setOpaque(false);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        donutHolder = chartCard("Cơ cấu hội viên");
        barHolder   = chartCard("Hoạt động theo loại");
        row1.add(donutHolder);
        row1.add(barHolder);

        outer.add(row1);
        outer.add(Box.createVerticalStrut(16));

        // Row 2: Top members (Horizontal Bar) + Recent activities
        JPanel row2 = new JPanel(new GridLayout(1, 2, 16, 0));
        row2.setOpaque(false);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 360));

        topMembersChartPanel  = buildTopMembersChart();
        recentActChartPanel   = buildRecentActivitiesChart();
        row2.add(topMembersChartPanel);
        row2.add(recentActChartPanel);

        outer.add(row2);
        return outer;
    }

    private JPanel chartCard(String title) {
        // Accent color map per chart
        Color accent = title.contains("Cơ cấu") ? BLUE : ORANGE;
        JPanel outer = new JPanel(new BorderLayout(0, 10)) {
            boolean hov = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hov = true; repaint(); }
                    public void mouseExited (MouseEvent e) { hov = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Card bg
                g2.setColor(CARD);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
                // Top accent strip
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, getWidth(), 4, 4, 4);
                // Hover glow
                if (hov) {
                    g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 10));
                    g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
                }
                g2.dispose();
            }
        };
        outer.setOpaque(false);
        outer.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(16, 20, 16, 20)));

        // Header row: title + subtle pill label
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel lbl = new JLabel(title);
        lbl.setFont(FONT_HEADING);
        lbl.setForeground(TXT_H);

        JLabel badge = new JLabel(title.contains("Cơ cấu") ? "Hội viên" : "Hoạt động");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badge.setForeground(accent);
        badge.setOpaque(true);
        badge.setBackground(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 22));
        badge.setBorder(new EmptyBorder(3, 8, 3, 8));

        header.add(lbl,   BorderLayout.WEST);
        header.add(badge, BorderLayout.EAST);
        outer.add(header, BorderLayout.NORTH);
        outer.setPreferredSize(new Dimension(0, 310));
        return outer;
    }

    private void loadCharts() {
        // ── Soft palette for charts ──────────────────────────────────────
        Color c1 = Color.decode("#4361EE"); // blue  – Hoạt động
        Color c2 = Color.decode("#10B981"); // green – Mới
        Color c3 = Color.decode("#F87171"); // red   – Đã rời
        Color c4 = Color.decode("#FBBF24"); // amber – Tạm dừng

        // ══════════════════════════════════════════════════════════════════
        //  RING CHART – cơ cấu hội viên
        // ══════════════════════════════════════════════════════════════════
        DefaultPieDataset pieDs = new DefaultPieDataset();
        int hvHD  = count("SELECT COUNT(*) FROM HoiVien WHERE trangThai=N'Hoạt động'");
        int hvMoi = count("SELECT COUNT(*) FROM HoiVien WHERE ngayThamGia >= ?", fromDate());
        int hvRoi = count("SELECT COUNT(*) FROM HoiVien WHERE trangThai LIKE N'%Rời%'");
        int hvTam = count("SELECT COUNT(*) FROM HoiVien WHERE trangThai=N'Tạm dừng'");
        if (hvHD  > 0) pieDs.setValue("Hoạt động (" + hvHD  + ")", hvHD);
        if (hvMoi > 0) pieDs.setValue("Mới ("        + hvMoi + ")", hvMoi);
        if (hvRoi > 0) pieDs.setValue("Đã rời ("     + hvRoi + ")", hvRoi);
        if (hvTam > 0) pieDs.setValue("Tạm dừng ("   + hvTam + ")", hvTam);

        JFreeChart pie = ChartFactory.createRingChart("", pieDs, true, true, false);
        pie.setBackgroundPaint(CARD);
        pie.setBorderVisible(false);
        pie.setPadding(new RectangleInsets(8, 8, 8, 8));

        // Legend styling
        if (pie.getLegend() != null) {
            pie.getLegend().setBackgroundPaint(CARD);
            pie.getLegend().setFrame(org.jfree.chart.block.BlockBorder.NONE);
            pie.getLegend().setItemFont(new Font("Segoe UI", Font.PLAIN, 11));
            pie.getLegend().setItemPaint(TXT_S);
            pie.getLegend().setPosition(org.jfree.chart.ui.RectangleEdge.BOTTOM);
        }

        RingPlot rp = (RingPlot) pie.getPlot();
        rp.setBackgroundPaint(CARD);
        rp.setOutlineVisible(false);
        rp.setShadowPaint(null);
        rp.setInsets(new RectangleInsets(6, 6, 6, 6));
        rp.setInteriorGap(0.12);
        rp.setSectionDepth(0.38);           // thinner ring = more elegant
        rp.setSeparatorsVisible(false);
        rp.setLabelGenerator(null);          // no clutter labels on slices
        rp.setSectionOutlinesVisible(false);

        // Soft colors with explode effect on first slice
        for (Comparable key : (List<Comparable>) pieDs.getKeys()) {
            String k = key.toString();
            if (k.startsWith("Hoạt động")) { rp.setSectionPaint(key, c1); rp.setExplodePercent(key, 0.04); }
            else if (k.startsWith("Mới"))   rp.setSectionPaint(key, c2);
            else if (k.startsWith("Đã rời"))rp.setSectionPaint(key, c3);
            else                             rp.setSectionPaint(key, c4);
        }

        // Custom ChartPanel with soft drop shadow
        ChartPanel cpPie = new ChartPanel(pie) {
            @Override public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        cpPie.setOpaque(false);
        cpPie.setBorder(null);
        cpPie.setBackground(CARD);
        clearAndAdd(donutHolder, cpPie);

        // ══════════════════════════════════════════════════════════════════
        //  BAR CHART – hoạt động theo loại
        // ══════════════════════════════════════════════════════════════════
        DefaultCategoryDataset barDs = new DefaultCategoryDataset();
        String sqlBar = "SELECT hd.loaiHoatDong, COUNT(DISTINCT hd.id) soHD, COUNT(tg.idHoiVien) soTG "
                      + "FROM HoatDong hd LEFT JOIN ThamGia tg ON hd.id=tg.idHoatDong "
                      + "GROUP BY hd.loaiHoatDong";
        try (Connection conn = DatabaseHelper.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(sqlBar)) {
            while (rs.next()) {
                String loai = rs.getString("loaiHoatDong");
                if (loai == null || loai.isEmpty()) loai = "Khác";
                barDs.addValue(rs.getInt("soHD"), "Số hoạt động",   loai);
                barDs.addValue(rs.getInt("soTG"), "Người tham gia", loai);
            }
        } catch (Exception ignored) {}

        JFreeChart bar = ChartFactory.createBarChart(
            "", "Loại", "Số lượng", barDs,
            PlotOrientation.VERTICAL, true, true, false);
        bar.setBackgroundPaint(CARD);
        bar.setBorderVisible(false);
        bar.setPadding(new RectangleInsets(8, 4, 4, 4));

        if (bar.getLegend() != null) {
            bar.getLegend().setBackgroundPaint(CARD);
            bar.getLegend().setFrame(org.jfree.chart.block.BlockBorder.NONE);
            bar.getLegend().setItemFont(new Font("Segoe UI", Font.PLAIN, 11));
            bar.getLegend().setItemPaint(TXT_S);
            bar.getLegend().setPosition(org.jfree.chart.ui.RectangleEdge.TOP);
        }

        CategoryPlot cp2 = bar.getCategoryPlot();
        cp2.setBackgroundPaint(CARD);
        cp2.setRangeGridlinePaint(new Color(232, 236, 241));
        cp2.setRangeGridlineStroke(new BasicStroke(1.0f));
        cp2.setDomainGridlinesVisible(false);
        cp2.setOutlineVisible(false);
        cp2.setInsets(new RectangleInsets(4, 4, 4, 4));

        // Axis styling
        CategoryAxis domainAxis = cp2.getDomainAxis();
        domainAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        domainAxis.setTickLabelPaint(TXT_S);
        domainAxis.setAxisLinePaint(BORDER_C);
        domainAxis.setTickMarksVisible(false);
        domainAxis.setCategoryMargin(0.25);

        ValueAxis rangeAxis = cp2.getRangeAxis();
        rangeAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        rangeAxis.setTickLabelPaint(TXT_S);
        rangeAxis.setAxisLinePaint(BORDER_C);
        rangeAxis.setTickMarksVisible(false);

        // Rounded bar renderer with gradient fills
        BarRenderer br = new BarRenderer() {
            @Override
            public Paint getItemPaint(int row, int col) {
                if (row == 0) {
                    return new GradientPaint(0, 0, new Color(67,97,238), 0, 100, new Color(114,137,255));
                } else {
                    return new GradientPaint(0, 0, new Color(16,185,129), 0, 100, new Color(52,211,153));
                }
            }
            @Override
            public void drawItem(Graphics2D g2, CategoryItemRendererState state,
                                 Rectangle2D dataArea, CategoryPlot plot,
                                 CategoryAxis domainAxis2, ValueAxis rangeAxis2,
                                 CategoryDataset dataset, int row, int column, int pass) {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                super.drawItem(g2, state, dataArea, plot, domainAxis2, rangeAxis2, dataset, row, column, pass);
            }
        };
        br.setShadowVisible(false);
        br.setMaximumBarWidth(0.12);
        br.setItemMargin(0.08);
        br.setBarPainter(new StandardBarPainter());
        br.setSeriesPositiveItemLabelPosition(0,
            new org.jfree.chart.labels.ItemLabelPosition(
                org.jfree.chart.labels.ItemLabelAnchor.OUTSIDE12,
                org.jfree.chart.ui.TextAnchor.BOTTOM_CENTER));
        cp2.setRenderer(br);

        ChartPanel cpBar = new ChartPanel(bar) {
            @Override public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        cpBar.setOpaque(false);
        cpBar.setBackground(CARD);
        clearAndAdd(barHolder, cpBar);
    }

    private JPanel buildTopMembersChart() {
        JPanel outer = sectionCard("Top 5 hội viên tích cực");

        String sql = "SELECT TOP 5 hv.tenHoiVien, hv.email, COUNT(tg.idHoatDong) soLan "
                   + "FROM HoiVien hv JOIN ThamGia tg ON hv.id=tg.idHoiVien "
                   + "GROUP BY hv.tenHoiVien, hv.email ORDER BY soLan DESC";
        try (Connection c = DatabaseHelper.getConnection();
             ResultSet rs = c.createStatement().executeQuery(sql)) {
            int rank = 1;
            Color[] medals = {new Color(255,215,0), new Color(192,192,192),
                               new Color(205,127,50), TXT_S, TXT_S};
            int maxVal = 50;
            while (rs.next()) {
                outer.add(buildRankRow(rank, rs.getString("tenHoiVien"),
                    rs.getInt("soLan"), maxVal, medals[Math.min(rank-1, 4)]));
                rank++;
            }
        } catch (Exception e) {
            outer.add(lbl("Chưa có dữ liệu", FONT_LABEL, TXT_S));
        }

        // "Xem đầy đủ" button
        JButton btnMore = new JButton("Xem đầy đủ →");
        btnMore.setFont(FONT_SMALL);
        btnMore.setForeground(BLUE);
        btnMore.setBackground(new Color(235,240,255));
        btnMore.setBorderPainted(false);
        btnMore.setFocusPainted(false);
        btnMore.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnMore.setBorder(new EmptyBorder(6,12,6,12));
        btnMore.addMouseListener(new MouseAdapter(){
            public void mouseEntered(MouseEvent e){ btnMore.setBackground(new Color(220,230,255)); }
            public void mouseExited(MouseEvent e){ btnMore.setBackground(new Color(235,240,255)); }
        });
        btnMore.addActionListener(e -> showTopMembersFullDialog());

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        footer.add(btnMore);
        outer.add(Box.createVerticalGlue());
        outer.add(footer);
        return outer;
    }

    private JPanel buildRankRow(int rank, String name, int value, int max, Color medalColor) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(6, 0, 6, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        row.setAlignmentX(LEFT_ALIGNMENT);

        JLabel rankLbl = new JLabel(String.valueOf(rank)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(medalColor);
                g2.fillOval(0,0,28,28);
                g2.setColor(rank <= 3 ? Color.decode("#1A202C") : Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                String t = getText();
                g2.drawString(t,(28-fm.stringWidth(t))/2,(28-fm.getHeight())/2+fm.getAscent());
                g2.dispose();
            }
        };
        rankLbl.setPreferredSize(new Dimension(28, 28));
        rankLbl.setOpaque(false);

        JPanel mid = new JPanel(new BorderLayout(0, 4));
        mid.setOpaque(false);
        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(FONT_BOLD);
        nameLbl.setForeground(TXT_H);

        int pct = max > 0 ? Math.min(100, value * 100 / max) : 0;
        final int finalPct = pct;
        final int finalRank = rank;
        JPanel bar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(230,234,255));
                g2.fillRoundRect(0, 4, getWidth(), 8, 8, 8);
                Color barColor = finalRank==1 ? YELLOW : finalRank==2 ? new Color(156,163,175)
                               : finalRank==3 ? ORANGE : BLUE;
                g2.setColor(barColor);
                g2.fillRoundRect(0, 4, Math.max(8, getWidth()*finalPct/100), 8, 8, 8);
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0, 16));
        mid.add(nameLbl, BorderLayout.NORTH);
        mid.add(bar,     BorderLayout.CENTER);

        JLabel countLbl = new JLabel(value + " lần");
        countLbl.setFont(FONT_BOLD);
        countLbl.setForeground(BLUE);
        countLbl.setPreferredSize(new Dimension(55, 28));
        countLbl.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(rankLbl,  BorderLayout.WEST);
        row.add(mid,      BorderLayout.CENTER);
        row.add(countLbl, BorderLayout.EAST);
        return row;
    }

    private JPanel buildRecentActivitiesChart() {
        JPanel outer = sectionCard("Hoạt động gần đây");

        String sql = "SELECT TOP 6 tenHoatDong, loaiHoatDong, "
                   + "CONVERT(varchar,thoiGianBatDau,120) tgBatDau, "
                   + "hanDangKy, thoiGianKetThuc, "
                   + "CASE "
                   + "  WHEN GETDATE() < thoiGianBatDau THEN N'Sắp diễn ra' "
                   + "  WHEN GETDATE() BETWEEN thoiGianBatDau AND thoiGianKetThuc THEN N'Đang diễn ra' "
                   + "  WHEN hanDangKy IS NOT NULL AND GETDATE() > hanDangKy AND GETDATE() < thoiGianBatDau THEN N'Hết hạn đăng ký' "
                   + "  ELSE N'Đã kết thúc' END trangThai "
                   + "FROM HoatDong ORDER BY thoiGianBatDau DESC";
        try (Connection c = DatabaseHelper.getConnection();
             ResultSet rs = c.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                outer.add(activityRow(rs.getString("tenHoatDong"),
                    rs.getString("loaiHoatDong"),
                    rs.getString("tgBatDau"),
                    rs.getString("trangThai")));
            }
        } catch (Exception e) {
            outer.add(lbl("Chưa có dữ liệu", FONT_LABEL, TXT_S));
        }
        return outer;
    }

    private JPanel activityRow(String ten, String loai, String time, String status) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, BORDER_C),
            new EmptyBorder(10, 0, 10, 0)));
        row.setAlignmentX(LEFT_ALIGNMENT);

        Color dotColor = switch (status == null ? "" : status) {
            case "Đang diễn ra"    -> GREEN;
            case "Sắp diễn ra"    -> YELLOW;
            case "Hết hạn đăng ký"-> ORANGE;
            default                -> TXT_S;
        };
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(dotColor);
                g2.fillOval(2, 6, 10, 10);
                g2.dispose();
            }
        };
        dot.setOpaque(false);
        dot.setPreferredSize(new Dimension(14, 24));

        JPanel info = new JPanel(new BorderLayout(0, 2));
        info.setOpaque(false);
        JLabel nameLbl = new JLabel(ten != null ? ten : "—");
        nameLbl.setFont(FONT_BOLD);
        nameLbl.setForeground(TXT_H);
        String meta = (loai != null ? loai : "") +
                      (time != null ? "  •  " + time.substring(0, Math.min(10, time.length())) : "");
        JLabel metaLbl = new JLabel(meta);
        metaLbl.setFont(FONT_SMALL);
        metaLbl.setForeground(TXT_S);
        info.add(nameLbl, BorderLayout.NORTH);
        info.add(metaLbl, BorderLayout.SOUTH);

        JLabel badge = new JLabel(status != null ? status : "—");
        badge.setFont(FONT_SMALL);
        badge.setForeground(dotColor);
        badge.setOpaque(true);
        badge.setBackground(dotColor.equals(GREEN)   ? new Color(209,250,229) :
                            dotColor.equals(YELLOW)  ? new Color(254,243,199) :
                            dotColor.equals(ORANGE)  ? new Color(255,237,213) :
                                                       new Color(243,244,246));
        badge.setBorder(new EmptyBorder(3, 8, 3, 8));
        badge.setPreferredSize(new Dimension(120, 24));
        badge.setHorizontalAlignment(SwingConstants.CENTER);

        row.add(dot,   BorderLayout.WEST);
        row.add(info,  BorderLayout.CENTER);
        row.add(badge, BorderLayout.EAST);
        return row;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  TABLE VIEW
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildTableView() {
        JPanel outer = new JPanel();
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        outer.setOpaque(false);

        // 3.1 Cơ cấu hội viên
        tblCoCapHoiVien = new DefaultTableModel(
            new String[]{"Trạng thái", "Số lượng", "Tỷ lệ (%)", "So với kỳ trước"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        outer.add(sectionTable("Cơ cấu hội viên", tblCoCapHoiVien, 150));
        outer.add(Box.createVerticalStrut(16));

        // 3.2 Hoạt động theo loại
        tblHoatDongLoai = new DefaultTableModel(
            new String[]{"Loại hoạt động", "Số hoạt động", "Tổng lượt tham gia",
                         "TB tham gia/HĐ", "HĐ phổ biến nhất"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        outer.add(sectionTable("Hoạt động theo loại", tblHoatDongLoai, 180));
        outer.add(Box.createVerticalStrut(16));

        // 3.3 Top hội viên tích cực
        tblTopHoiVien = new DefaultTableModel(
            new String[]{"#", "Tên hội viên", "Email", "Số lần tham gia",
                         "HĐ tham gia gần nhất", "Trạng thái"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        outer.add(sectionTableWithSearch("Top hội viên tích cực", tblTopHoiVien, 220));
        outer.add(Box.createVerticalStrut(16));

        // 3.4 Hoạt động gần đây
        tblHoatDongGanDay = new DefaultTableModel(
            new String[]{"Tên hoạt động", "Loại", "Bắt đầu", "Kết thúc",
                         "Địa điểm", "Số đăng ký", "Trạng thái"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        outer.add(sectionTable("Hoạt động gần đây", tblHoatDongGanDay, 220));

        return outer;
    }

    private JPanel sectionTable(String title, DefaultTableModel model, int height) {
        JPanel outer = new JPanel(new BorderLayout(0, 8)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,16,16);
                g2.dispose();
            }
        };
        outer.setOpaque(false);
        outer.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        outer.setAlignmentX(LEFT_ALIGNMENT);
        outer.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(16, 18, 16, 18)));

        JLabel lbl = new JLabel(title);
        lbl.setFont(FONT_HEADING);
        lbl.setForeground(TXT_H);
        outer.add(lbl, BorderLayout.NORTH);

        JTable table = createStyledTable(model);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(BORDER_C, 1, true));
        scroll.getViewport().setBackground(CARD);
        outer.add(scroll, BorderLayout.CENTER);
        return outer;
    }

    private JPanel sectionTableWithSearch(String title, DefaultTableModel model, int height) {
        JPanel outer = new JPanel(new BorderLayout(0, 8)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,16,16);
                g2.dispose();
            }
        };
        outer.setOpaque(false);
        outer.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        outer.setAlignmentX(LEFT_ALIGNMENT);
        outer.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(16, 18, 16, 18)));

        // Header row
        JPanel topRow = new JPanel(new BorderLayout(10, 0));
        topRow.setOpaque(false);
        JLabel lbl = new JLabel(title);
        lbl.setFont(FONT_HEADING);
        lbl.setForeground(TXT_H);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightPanel.setOpaque(false);

        JTextField searchFld = new JTextField(18);
        searchFld.setFont(FONT_LABEL);
        searchFld.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(4, 8, 4, 8)));
        searchFld.setToolTipText("Tìm theo tên hoặc email");

        JButton btnViewAll = actionBtn("Xem đầy đủ", BLUE);
        btnViewAll.setPreferredSize(new Dimension(110, 30));
        btnViewAll.addActionListener(e -> showTopMembersFullDialog());

        rightPanel.add(searchFld);
        rightPanel.add(btnViewAll);
        topRow.add(lbl,        BorderLayout.WEST);
        topRow.add(rightPanel, BorderLayout.EAST);
        outer.add(topRow, BorderLayout.NORTH);

        JTable table = createStyledTable(model);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        searchFld.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                String k = searchFld.getText().trim();
                sorter.setRowFilter(k.isEmpty() ? null : RowFilter.regexFilter("(?i)" + k));
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(BORDER_C, 1, true));
        scroll.getViewport().setBackground(CARD);
        outer.add(scroll, BorderLayout.CENTER);
        return outer;
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? CARD : new Color(248, 249, 252));
                }
                return c;
            }
        };
        table.setFont(FONT_LABEL);
        table.setRowHeight(36);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(BORDER_C);
        table.setSelectionBackground(new Color(235, 240, 255));
        table.setSelectionForeground(TXT_H);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setFont(FONT_BOLD);
        table.getTableHeader().setBackground(new Color(248, 249, 252));
        table.getTableHeader().setForeground(TXT_S);
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.getTableHeader().setBorder(new MatteBorder(0, 0, 1, 0, BORDER_C));

        // Hover
        table.addMouseMotionListener(new MouseMotionAdapter() {
            int lastRow = -1;
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != lastRow) { lastRow = row; table.repaint(); }
            }
        });
        return table;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  REFRESH TABLE DATA
    // ══════════════════════════════════════════════════════════════════════════
    private void refreshTableData() {
        // 3.1 Cơ cấu hội viên
        if (tblCoCapHoiVien != null) {
            tblCoCapHoiVien.setRowCount(0);
            int total   = count("SELECT COUNT(*) FROM HoiVien");
            int hoatDong= count("SELECT COUNT(*) FROM HoiVien WHERE trangThai=N'Hoạt động'");
            int moiMem  = count("SELECT COUNT(*) FROM HoiVien WHERE ngayThamGia >= ?", fromDate());
            int daRoi   = count("SELECT COUNT(*) FROM HoiVien WHERE trangThai LIKE N'%Rời%'");
            int tamDung  = count("SELECT COUNT(*) FROM HoiVien WHERE trangThai=N'Tạm dừng'");
            if (total > 0) {
                tblCoCapHoiVien.addRow(new Object[]{"Hoạt động", hoatDong,
                    String.format("%.1f", hoatDong*100.0/total)+"%", "+12%"});
                tblCoCapHoiVien.addRow(new Object[]{"Mới",        moiMem,
                    String.format("%.1f", moiMem*100.0/total)+"%",   "+8%"});
                tblCoCapHoiVien.addRow(new Object[]{"Đã rời",     daRoi,
                    String.format("%.1f", daRoi*100.0/total)+"%",   "-3%"});
                if (tamDung > 0)
                tblCoCapHoiVien.addRow(new Object[]{"Tạm dừng",   tamDung,
                    String.format("%.1f", tamDung*100.0/total)+"%", "N/A"});
                tblCoCapHoiVien.addRow(new Object[]{"Tổng",        total, "100%", "—"});
            }
        }

        // 3.2 Hoạt động theo loại
        if (tblHoatDongLoai != null) {
            tblHoatDongLoai.setRowCount(0);
            String sqlLoai =
                "SELECT hd.loaiHoatDong, " +
                "COUNT(DISTINCT hd.id) soHD, " +
                "COUNT(tg.idHoiVien) soTG, " +
                "ISNULL(MAX(hd2.tenHoatDong),'—') hdPhoBien " +
                "FROM HoatDong hd " +
                "LEFT JOIN ThamGia tg ON hd.id=tg.idHoatDong " +
                "LEFT JOIN (SELECT loaiHoatDong, TOP 1 tenHoatDong " +
                "  FROM HoatDong GROUP BY loaiHoatDong, tenHoatDong) hd2 " +
                "  ON hd.loaiHoatDong=hd2.loaiHoatDong " +
                "GROUP BY hd.loaiHoatDong";
            // Simpler query without the nested TOP:
            String sqlLoai2 =
                "SELECT hd.loaiHoatDong, " +
                "COUNT(DISTINCT hd.id) soHD, " +
                "ISNULL(SUM(tgCount.cnt),0) soTG " +
                "FROM HoatDong hd " +
                "LEFT JOIN (SELECT idHoatDong, COUNT(*) cnt FROM ThamGia GROUP BY idHoatDong) tgCount " +
                "ON hd.id=tgCount.idHoatDong " +
                "GROUP BY hd.loaiHoatDong";
            try (Connection c = DatabaseHelper.getConnection();
                 ResultSet rs = c.createStatement().executeQuery(sqlLoai2)) {
                while (rs.next()) {
                    String loai = rs.getString("loaiHoatDong");
                    if (loai == null || loai.isEmpty()) loai = "Khác";
                    int soHD = rs.getInt("soHD");
                    int soTG = rs.getInt("soTG");
                    double tb = soHD > 0 ? (double)soTG/soHD : 0;

                    // Most popular activity in this type
                    String hdPb = "—";
                    String sqlPb = "SELECT TOP 1 hd2.tenHoatDong FROM HoatDong hd2 "
                                 + "LEFT JOIN (SELECT idHoatDong, COUNT(*) cnt FROM ThamGia GROUP BY idHoatDong) x "
                                 + "ON hd2.id=x.idHoatDong "
                                 + "WHERE hd2.loaiHoatDong=N'" + loai.replace("'","''") + "' "
                                 + "ORDER BY ISNULL(x.cnt,0) DESC";
                    try (ResultSet rsPb = c.createStatement().executeQuery(sqlPb)) {
                        if (rsPb.next()) hdPb = rsPb.getString(1);
                    } catch (Exception ignored) {}

                    tblHoatDongLoai.addRow(new Object[]{loai, soHD, soTG,
                        String.format("%.1f", tb), hdPb});
                }
            } catch (Exception e) { e.printStackTrace(); }
        }

        // 3.3 Top hội viên tích cực (top 10 in table view)
        if (tblTopHoiVien != null) {
            tblTopHoiVien.setRowCount(0);
            topMembersFullData.clear();
            String sqlTop =
                "SELECT hv.tenHoiVien, hv.email, hv.trangThai, " +
                "COUNT(tg.idHoatDong) soLan, " +
                "ISNULL((SELECT TOP 1 hd2.tenHoatDong FROM ThamGia tg2 " +
                "  JOIN HoatDong hd2 ON tg2.idHoatDong=hd2.id " +
                "  WHERE tg2.idHoiVien=hv.id ORDER BY tg2.ngayDangKy DESC),'—') lastHD " +
                "FROM HoiVien hv " +
                "LEFT JOIN ThamGia tg ON hv.id=tg.idHoiVien " +
                "GROUP BY hv.id, hv.tenHoiVien, hv.email, hv.trangThai " +
                "ORDER BY soLan DESC";
            try (Connection c = DatabaseHelper.getConnection();
                 ResultSet rs = c.createStatement().executeQuery(sqlTop)) {
                int rank = 1;
                while (rs.next()) {
                    Object[] row = new Object[]{rank,
                        rs.getString("tenHoiVien"), rs.getString("email"),
                        rs.getInt("soLan"), rs.getString("lastHD"),
                        rs.getString("trangThai")};
                    topMembersFullData.add(row);
                    if (rank <= 10) tblTopHoiVien.addRow(row);
                    rank++;
                }
            } catch (Exception e) { e.printStackTrace(); }
        }

        // 3.4 Hoạt động gần đây
        if (tblHoatDongGanDay != null) {
            tblHoatDongGanDay.setRowCount(0);
            String sqlHD =
                "SELECT hd.tenHoatDong, hd.loaiHoatDong, " +
                "CONVERT(varchar,hd.thoiGianBatDau,120) tgBD, " +
                "CONVERT(varchar,hd.thoiGianKetThuc,120) tgKT, " +
                "hd.diaDiem, " +
                "ISNULL(tgCnt.cnt,0) soDK, " +
                "CASE " +
                "  WHEN GETDATE() < hd.thoiGianBatDau THEN N'Sắp diễn ra' " +
                "  WHEN GETDATE() BETWEEN hd.thoiGianBatDau AND hd.thoiGianKetThuc THEN N'Đang diễn ra' " +
                "  WHEN hd.hanDangKy IS NOT NULL AND GETDATE() > hd.hanDangKy AND GETDATE() < hd.thoiGianBatDau THEN N'Hết hạn đăng ký' " +
                "  ELSE N'Đã kết thúc' END trangThai " +
                "FROM HoatDong hd " +
                "LEFT JOIN (SELECT idHoatDong, COUNT(*) cnt FROM ThamGia GROUP BY idHoatDong) tgCnt " +
                "ON hd.id=tgCnt.idHoatDong " +
                "ORDER BY hd.thoiGianBatDau DESC";
            try (Connection c = DatabaseHelper.getConnection();
                 ResultSet rs = c.createStatement().executeQuery(sqlHD)) {
                while (rs.next()) {
                    tblHoatDongGanDay.addRow(new Object[]{
                        rs.getString("tenHoatDong"), rs.getString("loaiHoatDong"),
                        rs.getString("tgBD"), rs.getString("tgKT"),
                        rs.getString("diaDiem"), rs.getInt("soDK"),
                        rs.getString("trangThai")
                    });
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  TOP MEMBERS FULL DIALOG (with pagination & search)
    // ══════════════════════════════════════════════════════════════════════════
    private void showTopMembersFullDialog() {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        dlgTopFull = new JDialog(owner, "Top hội viên tích cực", true);
        dlgTopFull.setSize(860, 560);
        dlgTopFull.setLocationRelativeTo(this);

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBackground(CARD);
        content.setBorder(new EmptyBorder(16, 18, 16, 18));

        // Search
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topBar.setOpaque(false);
        txtTopSearch = new JTextField(22);
        txtTopSearch.setFont(FONT_LABEL);
        txtTopSearch.setBorder(new CompoundBorder(new LineBorder(BORDER_C,1,true),
            new EmptyBorder(5,8,5,8)));
        JButton btnSearch = actionBtn("Tìm", BLUE);
        btnSearch.setPreferredSize(new Dimension(90, 32));
        JButton btnReset = actionBtn("Đặt lại", new Color(107,114,128));
        btnReset.setPreferredSize(new Dimension(90, 32));
        topBar.add(lbl("Tìm kiếm:", FONT_BOLD, TXT_S));
        topBar.add(txtTopSearch);
        topBar.add(btnSearch);
        topBar.add(btnReset);
        content.add(topBar, BorderLayout.NORTH);

        // Table
        tblTopFull = new DefaultTableModel(
            new String[]{"#","Tên hội viên","Email","Số lần tham gia","HĐ gần nhất","Trạng thái"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tbl = createStyledTable(tblTopFull);
        tbl.getColumnModel().getColumn(0).setPreferredWidth(40);
        tbl.getColumnModel().getColumn(1).setPreferredWidth(200);
        tbl.getColumnModel().getColumn(2).setPreferredWidth(180);
        tbl.getColumnModel().getColumn(3).setPreferredWidth(80);
        tbl.getColumnModel().getColumn(4).setPreferredWidth(200);
        tbl.getColumnModel().getColumn(5).setPreferredWidth(100);

        JScrollPane scroll = new JScrollPane(tbl);
        scroll.setBorder(new LineBorder(BORDER_C,1,true));
        content.add(scroll, BorderLayout.CENTER);

        // Pagination
        JPanel pagPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        pagPanel.setOpaque(false);
        btnPrevPage = actionBtn("◀ Trước", new Color(107,114,128));
        btnPrevPage.setPreferredSize(new Dimension(90, 30));
        btnNextPage = actionBtn("Tiếp ▶", BLUE);
        btnNextPage.setPreferredSize(new Dimension(90, 30));
        lblPageInfo = new JLabel();
        lblPageInfo.setFont(FONT_LABEL);
        lblPageInfo.setForeground(TXT_S);
        pagPanel.add(btnPrevPage);
        pagPanel.add(lblPageInfo);
        pagPanel.add(btnNextPage);
        content.add(pagPanel, BorderLayout.SOUTH);

        topMembersPage = 0;
        loadTopMembersPage(topMembersFullData);

        btnPrevPage.addActionListener(e -> {
            if (topMembersPage > 0) { topMembersPage--; loadTopMembersPage(getCurrentTopList()); }
        });
        btnNextPage.addActionListener(e -> {
            List<Object[]> cur = getCurrentTopList();
            if ((topMembersPage+1)*PAGE_SIZE < cur.size()) { topMembersPage++; loadTopMembersPage(cur); }
        });
        btnSearch.addActionListener(e -> { topMembersPage = 0; loadTopMembersPage(getCurrentTopList()); });
        btnReset.addActionListener(e -> { txtTopSearch.setText(""); topMembersPage = 0; loadTopMembersPage(topMembersFullData); });

        dlgTopFull.add(content);
        dlgTopFull.setVisible(true);
    }

    private List<Object[]> getCurrentTopList() {
        String kw = txtTopSearch != null ? txtTopSearch.getText().trim().toLowerCase() : "";
        if (kw.isEmpty()) return topMembersFullData;
        List<Object[]> filtered = new ArrayList<>();
        for (Object[] row : topMembersFullData) {
            String name  = row[1] == null ? "" : row[1].toString().toLowerCase();
            String email = row[2] == null ? "" : row[2].toString().toLowerCase();
            if (name.contains(kw) || email.contains(kw)) filtered.add(row);
        }
        return filtered;
    }

    private void loadTopMembersPage(List<Object[]> data) {
        if (tblTopFull == null) return;
        tblTopFull.setRowCount(0);
        int start = topMembersPage * PAGE_SIZE;
        int end   = Math.min(start + PAGE_SIZE, data.size());
        for (int i = start; i < end; i++) tblTopFull.addRow(data.get(i));
        int total = data.size();
        int pages = (total + PAGE_SIZE - 1) / PAGE_SIZE;
        if (lblPageInfo != null)
            lblPageInfo.setText("Trang " + (topMembersPage+1) + " / " + Math.max(1, pages)
                + "  (" + total + " kết quả)");
        if (btnPrevPage != null) btnPrevPage.setEnabled(topMembersPage > 0);
        if (btnNextPage != null) btnNextPage.setEnabled((topMembersPage+1)*PAGE_SIZE < total);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  EXPORT DIALOG
    // ══════════════════════════════════════════════════════════════════════════
    private void showExportDialog() {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(owner, "Xuất dữ liệu thống kê", true);
        dlg.setSize(480, 400);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(CARD);
        content.setBorder(new EmptyBorder(20, 24, 16, 24));

        JLabel title = new JLabel("Chọn dữ liệu cần xuất");
        title.setFont(FONT_HEADING);
        title.setForeground(TXT_H);
        content.add(title, BorderLayout.NORTH);

        JPanel options = new JPanel(new GridLayout(0, 1, 0, 10));
        options.setBackground(CARD);
        options.setBorder(new EmptyBorder(10, 0, 10, 0));

        JCheckBox chkAll    = new JCheckBox("Xuất tất cả", true);
        JCheckBox chkCoCap  = new JCheckBox("Cơ cấu hội viên", true);
        JCheckBox chkLoai   = new JCheckBox("Hoạt động theo loại", true);
        JCheckBox chkTop    = new JCheckBox("Top hội viên tích cực", true);
        JCheckBox chkGanDay = new JCheckBox("Hoạt động gần đây", true);

        for (JCheckBox cb : new JCheckBox[]{chkAll, chkCoCap, chkLoai, chkTop, chkGanDay}) {
            cb.setFont(FONT_LABEL);
            cb.setBackground(CARD);
            options.add(cb);
        }
        chkAll.addActionListener(e -> {
            boolean sel = chkAll.isSelected();
            chkCoCap.setSelected(sel); chkLoai.setSelected(sel);
            chkTop.setSelected(sel); chkGanDay.setSelected(sel);
        });

        content.add(options, BorderLayout.CENTER);

        JPanel fmtRow = new JPanel(new BorderLayout(12, 0));
        fmtRow.setBackground(CARD);
        fmtRow.add(lbl("Định dạng:", FONT_BOLD, TXT_S), BorderLayout.WEST);

        JPanel fmtBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        fmtBtns.setBackground(CARD);
        JButton btnExcel = actionBtn("Xuất Excel", GREEN);
        btnExcel.setPreferredSize(new Dimension(130, 36));
        JButton btnCancel = actionBtn("Hủy", new Color(107,114,128));
        btnCancel.setPreferredSize(new Dimension(80, 36));
        fmtBtns.add(btnExcel);
        fmtBtns.add(btnCancel);
        fmtRow.add(fmtBtns, BorderLayout.CENTER);
        content.add(fmtRow, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dlg.dispose());
        btnExcel.addActionListener(e -> {
            dlg.dispose();
            refreshTableData();
            exportMultiSheetExcel(chkCoCap.isSelected(), chkLoai.isSelected(),
                chkTop.isSelected(), chkGanDay.isSelected());
        });

        dlg.add(content);
        dlg.setVisible(true);
    }

    private void exportMultiSheetExcel(boolean coCap, boolean loai, boolean top, boolean ganDay) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Lưu file Excel");
        fc.setSelectedFile(new File("Dashboard_" +
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xml"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(fc.getSelectedFile()), "UTF-8"))) {
            pw.println("<?xml version=\"1.0\"?>");
            pw.println("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\"");
            pw.println(" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\">");

            if (coCap && tblCoCapHoiVien != null) {
                writeModelToExcelSheet(pw, "Cơ cấu hội viên", tblCoCapHoiVien);
            }
            if (loai && tblHoatDongLoai != null) {
                writeModelToExcelSheet(pw, "Hoạt động theo loại", tblHoatDongLoai);
            }
            if (top) {
                writeTopMembersExcelSheet(pw);
            }
            if (ganDay && tblHoatDongGanDay != null) {
                writeModelToExcelSheet(pw, "Hoạt động gần đây", tblHoatDongGanDay);
            }
            pw.println("</Workbook>");

            JOptionPane.showMessageDialog(this,
                "Xuất file thành công!\n" + fc.getSelectedFile().getAbsolutePath(),
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi xuất file: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void writeModelToExcelSheet(PrintWriter pw, String sheetName, DefaultTableModel model) {
        pw.println("<Worksheet ss:Name=\"" + escapeXml(sheetName) + "\"><Table>");
        pw.println("<Row>");
        for (int i = 0; i < model.getColumnCount(); i++) {
            pw.println("<Cell><Data ss:Type=\"String\">" + escapeXml(model.getColumnName(i)) + "</Data></Cell>");
        }
        pw.println("</Row>");
        for (int r = 0; r < model.getRowCount(); r++) {
            pw.println("<Row>");
            for (int c = 0; c < model.getColumnCount(); c++) {
//                if (c > 0) sb.append(",");
                Object v = model.getValueAt(r, c);
                pw.println("<Cell><Data ss:Type=\"String\">" + escapeXml(v == null ? "" : v.toString()) + "</Data></Cell>");
            }
            pw.println("</Row>");
        }
        pw.println("</Table></Worksheet>");
    }

    private void writeTopMembersExcelSheet(PrintWriter pw) {
        pw.println("<Worksheet ss:Name=\"Top hội viên\"><Table>");
        String[] headers = {"#", "Tên hội viên", "Email", "Số lần tham gia", "HĐ gần nhất", "Trạng thái"};
        pw.println("<Row>");
        for (String h : headers) pw.println("<Cell><Data ss:Type=\"String\">" + escapeXml(h) + "</Data></Cell>");
        pw.println("</Row>");
        for (Object[] row : topMembersFullData) {
            pw.println("<Row>");
            for (Object val : row) {
                pw.println("<Cell><Data ss:Type=\"String\">" + escapeXml(val == null ? "" : val.toString()) + "</Data></Cell>");
            }
            pw.println("</Row>");
        }
        pw.println("</Table></Worksheet>");
    }
    
    private String escapeXml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  MAIN REFRESH
    // ══════════════════════════════════════════════════════════════════════════
    public void refresh() {
        buildStatCards();
        loadCharts();
        refreshTableData();
        revalidate();
        repaint();
    }

    private void animateReload(JButton btn) {
        String orig = btn.getText();
        javax.swing.Timer t = new javax.swing.Timer(100, null);
        String[] frames = {"↻", "↺", "↻", "↺"};
        final int[] idx = {0};
        t.addActionListener(e -> {
            btn.setText(frames[idx[0]++ % frames.length] + " Đang tải...");
            if (idx[0] >= 6) { t.stop(); btn.setText(orig); }
        });
        t.start();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  SECTION CARD helper
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel sectionCard(String title) {
        Color accent = title.contains("Top") ? YELLOW : BLUE;
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
                // left accent stripe
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                g2.dispose();
            }
        };
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(18, 22, 16, 20)));
        p.setPreferredSize(new Dimension(0, 340));

        // Header row
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        headerRow.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(title);
        lbl.setFont(FONT_HEADING);
        lbl.setForeground(TXT_H);

        JLabel badge = new JLabel(title.contains("Top") ? "TOP 5" : "GẦN ĐÂY");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 9));
        badge.setForeground(accent.equals(YELLOW) ? Color.decode("#92400E") : BLUE);
        badge.setOpaque(true);
        badge.setBackground(accent.equals(YELLOW) ? new Color(254,243,199) : new Color(235,240,255));
        badge.setBorder(new EmptyBorder(3, 7, 3, 7));

        headerRow.add(lbl,   BorderLayout.WEST);
        headerRow.add(badge, BorderLayout.EAST);
        p.add(headerRow);
        p.add(Box.createVerticalStrut(14));
        return p;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  HELPER METHODS
    // ══════════════════════════════════════════════════════════════════════════
    private void clearAndAdd(JPanel holder, Component newComp) {
        Component[] comps = holder.getComponents();
        for (Component c : comps) {
            if (c instanceof ChartPanel) { holder.remove(c); break; }
        }
        holder.add(newComp, BorderLayout.CENTER);
        holder.revalidate();
        holder.repaint();
    }

    private void onPeriodChange() {
        int idx = cbPeriod.getSelectedIndex();
        LocalDate now = LocalDate.now();
        LocalDate from = switch (idx) {
            case 0 -> now.minusDays(6);
            case 1 -> now.withDayOfMonth(1);
            case 2 -> now.withDayOfYear(1);
            default -> now.minusDays(6);
        };
        boolean custom = (idx == 3);
        spFrom.setEnabled(custom);
        spTo.setEnabled(custom);
        if (!custom) {
            ((SpinnerDateModel)spFrom.getModel()).setValue(java.sql.Date.valueOf(from));
            ((SpinnerDateModel)spTo.getModel()).setValue(java.sql.Date.valueOf(now));
        }
    }

    private java.sql.Date fromDate() {
        return new java.sql.Date(((java.util.Date) spFrom.getValue()).getTime());
    }

    private JToggleButton toggleBtn(String text, boolean selected) {
        JToggleButton btn = new JToggleButton(text, selected) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isSelected()) {
                    g2.setColor(BLUE);
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                    g2.setColor(Color.WHITE);
                } else {
                    g2.setColor(CARD);
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                    g2.setColor(BORDER_C);
                    g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10);
                    g2.setColor(TXT_S);
                }
                g2.setFont(FONT_BOLD);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth()-fm.stringWidth(getText()))/2,
                    (getHeight()-fm.getHeight())/2+fm.getAscent());
                g2.dispose();
            }
        };
        btn.setFont(FONT_BOLD);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(150, 38));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton actionBtn(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.darker() : bg);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.setColor(Color.WHITE);
                g2.setFont(FONT_BOLD);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth()-fm.stringWidth(getText()))/2,
                    (getHeight()-fm.getHeight())/2+fm.getAscent());
                g2.dispose();
            }
        };
        btn.setFont(FONT_BOLD);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(100, 34));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JSpinner dateSpinner(LocalDate d) {
        JSpinner sp = new JSpinner(new SpinnerDateModel(
            java.sql.Date.valueOf(d), null, null, java.util.Calendar.DAY_OF_MONTH));
        sp.setEditor(new JSpinner.DateEditor(sp, "dd/MM/yyyy"));
        sp.setFont(FONT_LABEL);
        sp.setPreferredSize(new Dimension(110, 32));
        return sp;
    }

    private void styleCombo(JComboBox<?> cb) {
        cb.setFont(FONT_LABEL);
        cb.setBackground(CARD);
        cb.setPreferredSize(new Dimension(130, 34));
    }

    private JLabel lbl(String text, Font f, Color c) {
        JLabel l = new JLabel(text);
        l.setFont(f);
        l.setForeground(c);
        return l;
    }

    private int count(String sql, Object... params) {
        try (Connection c = DatabaseHelper.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i+1, params[i]);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        } catch (Exception ignored) {}
        return 0;
    }
}