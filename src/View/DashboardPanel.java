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
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.axis.*;
import org.jfree.data.category.CategoryDataset;
import java.awt.BasicStroke;

/**
 * DashboardPanel – Nâng cấp đầy đủ:
 *  1. Bảng số liệu: TẤT CẢ khung đều có nút "Xem đầy đủ" mở Dialog riêng
 *     - Tìm kiếm + Đặt lại + Placeholder rõ ràng
 *     - Phân trang, sắp xếp theo cột, hover highlight, responsive
 *  2. Thông báo: 4 tab (Chưa đọc / Đã đọc / Chưa duyệt / Đã duyệt)
 *     - Màu sắc phân biệt rõ ràng, hiển thị nội dung + thời gian + trạng thái
 *  3. % So với kỳ trước: tính đúng theo filter (Tuần/Tháng/Năm/Tùy chọn)
 *  4. Xuất Excel: đúng kỳ lọc, đa sheet, header màu căn giữa
 */
public class DashboardPanel extends JPanel {

    // ─── Palette ──────────────────────────────────────────────────────────
    private static final Color BG       = Color.decode("#F5F7FA");
    private static final Color CARD     = Color.WHITE;
    private static final Color BORDER_C = Color.decode("#E8ECF1");
    private static final Color TXT_H    = Color.decode("#1A202C");
    private static final Color TXT_S    = Color.decode("#718096");
    private static final Color BLUE     = Color.decode("#1359B9");
    private static final Color BLUE2    = Color.decode("#4361EE");
    private static final Color YELLOW   = Color.decode("#F6C90E");
    private static final Color RED      = Color.decode("#EF4444");
    private static final Color GREEN    = Color.decode("#10B981");
    private static final Color ORANGE   = Color.decode("#F97316");
    private static final Color PURPLE   = Color.decode("#8B5CF6");
    private static final Color CYAN     = Color.decode("#9FE4FB");

    private static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font FONT_LABEL   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_STAT    = new Font("Segoe UI", Font.BOLD, 36);
    private static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FONT_BOLD    = new Font("Segoe UI", Font.BOLD, 13);

    private static final int PAGE_SIZE = 10;

    // ─── State ────────────────────────────────────────────────────────────
    private JComboBox<String> cbPeriod;
    private JSpinner spFrom, spTo;
    private JPanel pnlStats;
    private CardLayout viewCard;
    private JPanel viewContainer;

    // Chart holders
    private JPanel donutHolder, barHolder;
    private JPanel topMembersChartPanel;
    private JPanel recentActChartPanel;

    // Table models
    private DefaultTableModel tblCoCapHoiVien;
    private DefaultTableModel tblHoatDongLoai;
    private DefaultTableModel tblTopHoiVien;
    private DefaultTableModel tblHoatDongGanDay;

    // Full data for detail dialogs
    private List<Object[]> fullDataCoCap    = new ArrayList<>();
    private List<Object[]> fullDataActType  = new ArrayList<>();
    private List<Object[]> fullDataTopMembers = new ArrayList<>();
    private List<Object[]> fullDataRecentActs = new ArrayList<>();

    private JPanel bodyPanel;

    // ─── Notification state ───────────────────────────────────────────────
    // (passed from MainForm via showNotificationDialog – see inner class)

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

    // ══════════════════════════════════════════════════════════════════════
    //  HEADER
    // ══════════════════════════════════════════════════════════════════════
    private JPanel buildHeader() {
        JPanel card = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
                GradientPaint gp = new GradientPaint(0, 0, BLUE, getWidth(), 0, new Color(0x7B9FFF));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), 5, 4, 4);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new CompoundBorder(new LineBorder(BORDER_C, 1, true), new EmptyBorder(16, 22, 14, 22)));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        JLabel title = new JLabel("Tổng quan Dashboard");
        title.setFont(FONT_TITLE); title.setForeground(TXT_H);
        JLabel sub = new JLabel(LocalDate.now().format(
            DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", new Locale("vi"))));
        sub.setFont(FONT_LABEL); sub.setForeground(TXT_S);
        left.add(title); left.add(Box.createVerticalStrut(4)); left.add(sub);

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setOpaque(false);

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        filterRow.setOpaque(false);
        cbPeriod = new JComboBox<>(new String[]{"Tuần này","Tháng này","Năm nay","Tùy chọn"});
        styleCombo(cbPeriod);
        LocalDate now = LocalDate.now();
        spFrom = dateSpinner(now.minusDays(6));
        spTo   = dateSpinner(now);
        spFrom.setEnabled(false); spTo.setEnabled(false);
        filterRow.add(lbl("Kỳ:", FONT_SMALL, TXT_S));
        filterRow.add(cbPeriod);
        filterRow.add(lbl("Từ", FONT_SMALL, TXT_S));
        filterRow.add(spFrom);
        filterRow.add(lbl("→", FONT_SMALL, TXT_S));
        filterRow.add(spTo);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnRow.setOpaque(false);
        JButton btnApply  = actionBtn("Áp dụng", BLUE);
        JButton btnReload = actionBtn("Tải lại",  GREEN);
        JButton btnExport = actionBtn("Xuất Excel", PURPLE);
        btnApply.setPreferredSize(new Dimension(100, 32));
        btnReload.setPreferredSize(new Dimension(90, 32));
        btnExport.setPreferredSize(new Dimension(110, 32));
        btnRow.add(btnApply); btnRow.add(btnReload); btnRow.add(btnExport);

        right.add(filterRow); right.add(Box.createVerticalStrut(6)); right.add(btnRow);

        cbPeriod.addActionListener(e -> onPeriodChange());
        btnApply.addActionListener(e -> refresh());
        btnReload.addActionListener(e -> { animateReload(btnReload); refresh(); });
        btnExport.addActionListener(e -> exportExcelWithPeriod());

        card.add(left, BorderLayout.WEST);
        card.add(right, BorderLayout.EAST);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0, 0, 16, 0));
        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  BODY
    // ══════════════════════════════════════════════════════════════════════
    private JPanel buildBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        pnlStats = new JPanel(new GridLayout(1, 5, 12, 0));
        pnlStats.setOpaque(false);
        pnlStats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        body.add(pnlStats);
        body.add(Box.createVerticalStrut(18));
        body.add(buildToggleRow());
        body.add(Box.createVerticalStrut(12));

        viewCard = new CardLayout();
        viewContainer = new JPanel(viewCard);
        viewContainer.setOpaque(false);
        viewContainer.add(buildChartView(), "charts");
        viewContainer.add(buildTableView(), "table");
        body.add(viewContainer);
        return body;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  STAT CARDS – tính % đúng theo kỳ lọc
    // ══════════════════════════════════════════════════════════════════════
    private void buildStatCards() {
        pnlStats.removeAll();

        // Xác định khoảng hiện tại và kỳ trước
        PeriodRange cur  = getCurrentPeriod();
        PeriodRange prev = getPreviousPeriod(cur);

        // Tổng hội viên
        int totalCur  = count("SELECT COUNT(*) FROM HoiVien WHERE ngayThamGia <= ?", toSqlDate(cur.end));
        int totalPrev = count("SELECT COUNT(*) FROM HoiVien WHERE ngayThamGia <= ?", toSqlDate(prev.end));

        // Hội viên mới (tham gia trong kỳ)
        int newCur  = count("SELECT COUNT(*) FROM HoiVien WHERE ngayThamGia BETWEEN ? AND ?",
                toSqlDate(cur.start), toSqlDate(cur.end));
        int newPrev = count("SELECT COUNT(*) FROM HoiVien WHERE ngayThamGia BETWEEN ? AND ?",
                toSqlDate(prev.start), toSqlDate(prev.end));

        // Đã rời (ngayRoi trong kỳ)
        int leftCur  = count("SELECT COUNT(*) FROM HoiVien WHERE trangThai LIKE N'%Rời%' AND (ngayRoi BETWEEN ? AND ? OR ngayRoi IS NULL)",
                toSqlDate(cur.start), toSqlDate(cur.end));
        int leftPrev = count("SELECT COUNT(*) FROM HoiVien WHERE trangThai LIKE N'%Rời%' AND (ngayRoi BETWEEN ? AND ? OR ngayRoi IS NULL)",
                toSqlDate(prev.start), toSqlDate(prev.end));

        // Hoạt động (bắt đầu trong kỳ)
        int actCur  = count("SELECT COUNT(*) FROM HoatDong WHERE thoiGianBatDau BETWEEN ? AND ?",
                toSqlDate(cur.start), toSqlDate(cur.end));
        int actPrev = count("SELECT COUNT(*) FROM HoatDong WHERE thoiGianBatDau BETWEEN ? AND ?",
                toSqlDate(prev.start), toSqlDate(prev.end));

        // Lượt tham gia (đăng ký trong kỳ)
        int partCur  = count("SELECT COUNT(*) FROM ThamGia WHERE ngayDangKy BETWEEN ? AND ?",
                toSqlDate(cur.start), toSqlDate(cur.end));
        int partPrev = count("SELECT COUNT(*) FROM ThamGia WHERE ngayDangKy BETWEEN ? AND ?",
                toSqlDate(prev.start), toSqlDate(prev.end));

        pnlStats.add(statCard("👥 Tổng hội viên",   totalCur, pct(totalCur, totalPrev), BLUE,   new Color(232,240,253)));
        pnlStats.add(statCard("✨ Hội viên mới",      newCur,   pct(newCur,   newPrev),   GREEN,  new Color(209,250,229)));
        pnlStats.add(statCard("🚪 Đã rời",             leftCur,  pct(leftCur,  leftPrev),  RED,    new Color(254,226,226)));
        pnlStats.add(statCard("📅 Hoạt động",           actCur,   pct(actCur,   actPrev),   ORANGE, new Color(255,237,213)));
        pnlStats.add(statCard("🎟 Lượt tham gia",       partCur,  pct(partCur,  partPrev),  PURPLE, new Color(237,233,254)));

        pnlStats.revalidate(); pnlStats.repaint();
    }

    /** Tính % thay đổi so với kỳ trước */
    private int pct(int cur, int prev) {
        if (prev == 0) return cur > 0 ? 100 : 0;
        return (int) Math.round((cur - prev) * 100.0 / prev);
    }

    private JPanel statCard(String label, int value, int trendPct,
                             Color accent, Color bg) {
        boolean positive = trendPct >= 0;
        JPanel p = new JPanel(new BorderLayout(0, 8)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 5, getHeight(), 4, 4);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new CompoundBorder(new LineBorder(BORDER_C, 1, true), new EmptyBorder(14, 18, 12, 16)));
        p.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                p.setBorder(new CompoundBorder(new LineBorder(accent, 2, true), new EmptyBorder(13, 17, 11, 15)));
                p.repaint();
            }
            public void mouseExited(MouseEvent e) {
                p.setBorder(new CompoundBorder(new LineBorder(BORDER_C, 1, true), new EmptyBorder(14, 18, 12, 16)));
                p.repaint();
            }
        });

        String icon = label.contains(" ") ? label.split(" ")[0] : "•";
        String labelText = label.contains(" ") ? label.substring(label.indexOf(" ") + 1) : label;

        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));

        JLabel lblLabel = new JLabel(labelText.toUpperCase());
        lblLabel.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lblLabel.setForeground(TXT_S);

        JLabel lblVal = new JLabel(String.format("%,d", value));
        lblVal.setFont(FONT_STAT); lblVal.setForeground(TXT_H);

        String trendText = (positive ? "▲ +" : "▼ ") + trendPct + "% kỳ trước";
        JLabel lblTrend = new JLabel(trendText);
        lblTrend.setFont(FONT_SMALL);
        lblTrend.setForeground(positive ? GREEN : RED);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(lblLabel, BorderLayout.WEST);
        topRow.add(lblIcon, BorderLayout.EAST);

        JPanel bottom = new JPanel(new BorderLayout(0, 2));
        bottom.setOpaque(false);
        bottom.add(lblVal, BorderLayout.NORTH);
        bottom.add(lblTrend, BorderLayout.SOUTH);

        p.add(topRow, BorderLayout.NORTH);
        p.add(bottom, BorderLayout.CENTER);
        return p;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  PERIOD HELPERS
    // ══════════════════════════════════════════════════════════════════════
    private static class PeriodRange {
        LocalDate start, end;
        PeriodRange(LocalDate s, LocalDate e) { start = s; end = e; }
    }

    private PeriodRange getCurrentPeriod() {
        LocalDate now = LocalDate.now();
        int idx = cbPeriod.getSelectedIndex();
        if (idx == 0) { // Tuần này
            LocalDate mon = now.with(java.time.DayOfWeek.MONDAY);
            return new PeriodRange(mon, now);
        } else if (idx == 1) { // Tháng này
            return new PeriodRange(now.withDayOfMonth(1), now);
        } else if (idx == 2) { // Năm nay
            return new PeriodRange(now.withDayOfYear(1), now);
        } else { // Tùy chọn
            LocalDate s = toLocalDate(spFrom.getValue());
            LocalDate e = toLocalDate(spTo.getValue());
            return new PeriodRange(s, e);
        }
    }

    private PeriodRange getPreviousPeriod(PeriodRange cur) {
        long days = java.time.temporal.ChronoUnit.DAYS.between(cur.start, cur.end) + 1;
        LocalDate prevEnd   = cur.start.minusDays(1);
        LocalDate prevStart = prevEnd.minusDays(days - 1);
        return new PeriodRange(prevStart, prevEnd);
    }

    private LocalDate toLocalDate(Object spinnerVal) {
        java.util.Date d = (java.util.Date) spinnerVal;
        return d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }

    private java.sql.Date toSqlDate(LocalDate d) {
        return java.sql.Date.valueOf(d);
    }

    private String periodLabel() {
        int idx = cbPeriod.getSelectedIndex();
        if (idx == 0) return "Tuần này (" + LocalDate.now().with(java.time.DayOfWeek.MONDAY)
                + " → " + LocalDate.now() + ")";
        if (idx == 1) return "Tháng " + LocalDate.now().getMonthValue() + "/" + LocalDate.now().getYear();
        if (idx == 2) return "Năm " + LocalDate.now().getYear();
        LocalDate s = toLocalDate(spFrom.getValue());
        LocalDate e = toLocalDate(spTo.getValue());
        return s + " → " + e;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  TOGGLE ROW
    // ══════════════════════════════════════════════════════════════════════
    private JPanel buildToggleRow() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabs.setOpaque(false);
        JToggleButton tbChart = toggleBtn("📊  Biểu đồ",      true);
        JToggleButton tbTable = toggleBtn("📋  Bảng số liệu", false);
        ButtonGroup bg = new ButtonGroup();
        bg.add(tbChart); bg.add(tbTable);
        tbChart.addActionListener(e -> viewCard.show(viewContainer, "charts"));
        tbTable.addActionListener(e -> { viewCard.show(viewContainer, "table"); refreshTableData(); });
        tabs.add(tbChart); tabs.add(Box.createHorizontalStrut(6)); tabs.add(tbTable);
        p.add(tabs, BorderLayout.WEST);
        return p;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  CHART VIEW
    // ══════════════════════════════════════════════════════════════════════
    private JPanel buildChartView() {
        JPanel outer = new JPanel();
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        outer.setOpaque(false);

        JPanel row1 = new JPanel(new GridLayout(1, 2, 16, 0));
        row1.setOpaque(false);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));
        donutHolder = chartCard("Cơ cấu hội viên");
        barHolder   = chartCard("Hoạt động theo loại");
        row1.add(donutHolder); row1.add(barHolder);
        outer.add(row1); outer.add(Box.createVerticalStrut(16));

        JPanel row2 = new JPanel(new GridLayout(1, 2, 16, 0));
        row2.setOpaque(false);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 360));
        topMembersChartPanel = buildTopMembersChart();
        recentActChartPanel  = buildRecentActivitiesChart();
        row2.add(topMembersChartPanel); row2.add(recentActChartPanel);
        outer.add(row2);
        return outer;
    }

    private JPanel chartCard(String title) {
        Color accent = title.contains("Cơ cấu") ? BLUE : ORANGE;
        JPanel outer = new JPanel(new BorderLayout(0, 10)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD); g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
                g2.setColor(accent); g2.fillRoundRect(0, 0, getWidth(), 4, 4, 4);
                g2.dispose();
            }
        };
        outer.setOpaque(false);
        outer.setBorder(new CompoundBorder(new LineBorder(BORDER_C, 1, true), new EmptyBorder(16, 20, 16, 20)));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 8, 0));
        JLabel lbl = new JLabel(title);
        lbl.setFont(FONT_HEADING); lbl.setForeground(TXT_H);
        header.add(lbl, BorderLayout.WEST);
        outer.add(header, BorderLayout.NORTH);
        outer.setPreferredSize(new Dimension(0, 310));
        return outer;
    }

    private void loadCharts() {
        Color c1 = Color.decode("#1359B9");
        Color c2 = Color.decode("#10B981");
        Color c3 = Color.decode("#F87171");
        Color c4 = Color.decode("#FBBF24");

        // Donut
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
        pie.setBackgroundPaint(CARD); pie.setBorderVisible(false);
        RingPlot rp = (RingPlot) pie.getPlot();
        rp.setBackgroundPaint(CARD); rp.setOutlineVisible(false);
        rp.setShadowPaint(null); rp.setSectionDepth(0.38);
        rp.setSeparatorsVisible(false); rp.setLabelGenerator(null);
        rp.setSectionOutlinesVisible(false);
        for (Comparable key : (List<Comparable>) pieDs.getKeys()) {
            String k = key.toString();
            if (k.startsWith("Hoạt động")) rp.setSectionPaint(key, c1);
            else if (k.startsWith("Mới"))   rp.setSectionPaint(key, c2);
            else if (k.startsWith("Đã rời"))rp.setSectionPaint(key, c3);
            else                             rp.setSectionPaint(key, c4);
        }
        ChartPanel cpPie = new ChartPanel(pie);
        cpPie.setOpaque(false); cpPie.setBorder(null); cpPie.setBackground(CARD);
        clearAndAdd(donutHolder, cpPie);

        // Bar
        DefaultCategoryDataset barDs = new DefaultCategoryDataset();
        String sqlBar = "SELECT hd.loaiHoatDong, COUNT(DISTINCT hd.id) soHD, COUNT(tg.idHoiVien) soTG "
                      + "FROM HoatDong hd LEFT JOIN ThamGia tg ON hd.id=tg.idHoatDong GROUP BY hd.loaiHoatDong";
        try (Connection conn = DatabaseHelper.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(sqlBar)) {
            while (rs.next()) {
                String loai = rs.getString("loaiHoatDong");
                if (loai == null || loai.isEmpty()) loai = "Khác";
                barDs.addValue(rs.getInt("soHD"), "Số hoạt động",   loai);
                barDs.addValue(rs.getInt("soTG"), "Người tham gia", loai);
            }
        } catch (Exception ignored) {}

        JFreeChart bar = ChartFactory.createBarChart("", "Loại", "Số lượng", barDs,
            PlotOrientation.VERTICAL, true, true, false);
        bar.setBackgroundPaint(CARD); bar.setBorderVisible(false);
        CategoryPlot cp2 = bar.getCategoryPlot();
        cp2.setBackgroundPaint(CARD); cp2.setRangeGridlinePaint(new Color(232, 236, 241));
        cp2.setDomainGridlinesVisible(false); cp2.setOutlineVisible(false);
        BarRenderer br = new BarRenderer();
        br.setShadowVisible(false); br.setMaximumBarWidth(0.12);
        br.setBarPainter(new StandardBarPainter());
        cp2.setRenderer(br);
        ChartPanel cpBar = new ChartPanel(bar);
        cpBar.setOpaque(false); cpBar.setBackground(CARD);
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
            Color[] medals = {new Color(255,215,0), new Color(192,192,192), new Color(205,127,50), TXT_S, TXT_S};
            while (rs.next()) {
                outer.add(buildRankRow(rank, rs.getString("tenHoiVien"),
                    rs.getInt("soLan"), 50, medals[Math.min(rank-1, 4)]));
                rank++;
            }
        } catch (Exception e) {
            outer.add(lbl("Chưa có dữ liệu", FONT_LABEL, TXT_S));
        }
        JButton btnMore = makeViewAllButton();
        btnMore.addActionListener(e -> showDetailDialog("topMembers"));
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false); footer.add(btnMore);
        outer.add(Box.createVerticalGlue()); outer.add(footer);
        return outer;
    }

    private JPanel buildRecentActivitiesChart() {
        JPanel outer = sectionCard("Hoạt động gần đây");
        String sql = "SELECT TOP 6 tenHoatDong, loaiHoatDong, "
                   + "CONVERT(varchar,thoiGianBatDau,120) tgBatDau, thoiGianKetThuc, "
                   + "CASE WHEN GETDATE()<thoiGianBatDau THEN N'Sắp diễn ra' "
                   + "WHEN GETDATE() BETWEEN thoiGianBatDau AND thoiGianKetThuc THEN N'Đang diễn ra' "
                   + "ELSE N'Đã kết thúc' END trangThai "
                   + "FROM HoatDong ORDER BY thoiGianBatDau DESC";
        try (Connection c = DatabaseHelper.getConnection();
             ResultSet rs = c.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                outer.add(activityRow(rs.getString("tenHoatDong"),
                    rs.getString("loaiHoatDong"), rs.getString("tgBatDau"), rs.getString("trangThai")));
            }
        } catch (Exception e) {
            outer.add(lbl("Chưa có dữ liệu", FONT_LABEL, TXT_S));
        }
        return outer;
    }

    private JPanel activityRow(String ten, String loai, String time, String status) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setBorder(new CompoundBorder(new MatteBorder(0,0,1,0,BORDER_C), new EmptyBorder(10,0,10,0)));
        row.setAlignmentX(LEFT_ALIGNMENT);

        Color dotColor = "Đang diễn ra".equals(status) ? GREEN :
                         "Sắp diễn ra".equals(status)  ? YELLOW : TXT_S;
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(dotColor); g2.fillOval(2,6,10,10); g2.dispose();
            }
        };
        dot.setOpaque(false); dot.setPreferredSize(new Dimension(14,24));

        JPanel info = new JPanel(new BorderLayout(0,2));
        info.setOpaque(false);
        JLabel nameLbl = new JLabel(ten!=null?ten:"—");
        nameLbl.setFont(FONT_BOLD); nameLbl.setForeground(TXT_H);
        String meta = (loai!=null?loai:"") + (time!=null?"  •  "+time.substring(0,Math.min(10,time.length())):"");
        JLabel metaLbl = new JLabel(meta);
        metaLbl.setFont(FONT_SMALL); metaLbl.setForeground(TXT_S);
        info.add(nameLbl,BorderLayout.NORTH); info.add(metaLbl,BorderLayout.SOUTH);

        JLabel badge = new JLabel(status!=null?status:"—");
        badge.setFont(FONT_SMALL); badge.setForeground(dotColor); badge.setOpaque(true);
        badge.setBackground(dotColor.equals(GREEN)?new Color(209,250,229):
                            dotColor.equals(YELLOW)?new Color(254,243,199):new Color(243,244,246));
        badge.setBorder(new EmptyBorder(3,8,3,8));
        badge.setPreferredSize(new Dimension(120,24));
        badge.setHorizontalAlignment(SwingConstants.CENTER);

        row.add(dot,BorderLayout.WEST); row.add(info,BorderLayout.CENTER); row.add(badge,BorderLayout.EAST);
        return row;
    }

    private JPanel buildRankRow(int rank, String name, int value, int max, Color medalColor) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false); row.setBorder(new EmptyBorder(6,0,6,0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE,52)); row.setAlignmentX(LEFT_ALIGNMENT);

        JLabel rankLbl = new JLabel(String.valueOf(rank)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(medalColor); g2.fillOval(0,0,28,28);
                g2.setColor(rank<=3?Color.decode("#1A202C"):Color.WHITE);
                g2.setFont(new Font("Segoe UI",Font.BOLD,12));
                FontMetrics fm=g2.getFontMetrics(); String t=getText();
                g2.drawString(t,(28-fm.stringWidth(t))/2,(28-fm.getHeight())/2+fm.getAscent());
                g2.dispose();
            }
        };
        rankLbl.setPreferredSize(new Dimension(28,28)); rankLbl.setOpaque(false);

        JPanel mid = new JPanel(new BorderLayout(0,4)); mid.setOpaque(false);
        JLabel nameLbl = new JLabel(name); nameLbl.setFont(FONT_BOLD); nameLbl.setForeground(TXT_H);
        int pct2 = max>0?Math.min(100,value*100/max):0;
        JPanel bar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(230,234,255)); g2.fillRoundRect(0,4,getWidth(),8,8,8);
                g2.setColor(rank==1?YELLOW:rank==2?new Color(156,163,175):rank==3?ORANGE:BLUE);
                g2.fillRoundRect(0,4,Math.max(8,getWidth()*pct2/100),8,8,8);
                g2.dispose();
            }
        };
        bar.setOpaque(false); bar.setPreferredSize(new Dimension(0,16));
        mid.add(nameLbl,BorderLayout.NORTH); mid.add(bar,BorderLayout.CENTER);

        JLabel countLbl = new JLabel(value+" lần"); countLbl.setFont(FONT_BOLD);
        countLbl.setForeground(BLUE); countLbl.setPreferredSize(new Dimension(55,28));
        countLbl.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(rankLbl,BorderLayout.WEST); row.add(mid,BorderLayout.CENTER); row.add(countLbl,BorderLayout.EAST);
        return row;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  TABLE VIEW – tất cả có nút "Xem đầy đủ"
    // ══════════════════════════════════════════════════════════════════════
    private JPanel buildTableView() {
        JPanel outer = new JPanel();
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        outer.setOpaque(false);

        // 1. Cơ cấu hội viên
        tblCoCapHoiVien = new DefaultTableModel(
            new String[]{"Trạng thái","Số lượng","Tỷ lệ (%)","So kỳ trước"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        outer.add(buildSectionTableWithViewAll("Cơ cấu hội viên", tblCoCapHoiVien,
            "Tìm theo trạng thái hội viên...", "coCap", 170));
        outer.add(Box.createVerticalStrut(16));

        // 2. Hoạt động theo loại
        tblHoatDongLoai = new DefaultTableModel(
            new String[]{"Loại hoạt động","Số HĐ","Tổng lượt TG","TB TG/HĐ","HĐ phổ biến nhất"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        outer.add(buildSectionTableWithViewAll("Hoạt động theo loại", tblHoatDongLoai,
            "Tìm theo loại hoạt động...", "actType", 190));
        outer.add(Box.createVerticalStrut(16));

        // 3. Top hội viên tích cực
        tblTopHoiVien = new DefaultTableModel(
            new String[]{"#","Tên hội viên","Email","Số lần TG","HĐ gần nhất","Trạng thái"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        outer.add(buildSectionTableWithViewAll("Top hội viên tích cực", tblTopHoiVien,
            "Tìm theo tên hoặc email hội viên...", "topMembers", 230));
        outer.add(Box.createVerticalStrut(16));

        // 4. Hoạt động gần đây
        tblHoatDongGanDay = new DefaultTableModel(
            new String[]{"Tên hoạt động","Loại","Bắt đầu","Kết thúc","Địa điểm","Đăng ký","Trạng thái"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        outer.add(buildSectionTableWithViewAll("Hoạt động gần đây", tblHoatDongGanDay,
            "Tìm theo tên hoạt động hoặc địa điểm...", "recentActs", 230));

        return outer;
    }

    /**
     * Tạo section table có header, tìm kiếm inline và nút "Xem đầy đủ"
     */
    private JPanel buildSectionTableWithViewAll(String title, DefaultTableModel model,
                                                 String searchHint, String dataKey, int height) {
        JPanel outer = new JPanel(new BorderLayout(0, 8)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD); g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,16,16);
                g2.dispose();
            }
        };
        outer.setOpaque(false);
        outer.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        outer.setAlignmentX(LEFT_ALIGNMENT);
        outer.setBorder(new CompoundBorder(new LineBorder(BORDER_C,1,true), new EmptyBorder(14,18,14,18)));

        // Header row
        JPanel topRow = new JPanel(new BorderLayout(10,0));
        topRow.setOpaque(false);
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(FONT_HEADING); lblTitle.setForeground(TXT_H);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightPanel.setOpaque(false);

        // Inline search (compact)
        JTextField inlineSearch = new JTextField(16);
        inlineSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        inlineSearch.setPreferredSize(new Dimension(180, 28));
        inlineSearch.setBorder(new CompoundBorder(new LineBorder(BORDER_C,1,true), new EmptyBorder(3,8,3,8)));
        inlineSearch.setToolTipText(searchHint);
        // Placeholder effect
        inlineSearch.setForeground(TXT_S);
        inlineSearch.setText(searchHint);
        inlineSearch.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (inlineSearch.getText().equals(searchHint)) {
                    inlineSearch.setText(""); inlineSearch.setForeground(TXT_H);
                }
            }
            public void focusLost(FocusEvent e) {
                if (inlineSearch.getText().isEmpty()) {
                    inlineSearch.setText(searchHint); inlineSearch.setForeground(TXT_S);
                }
            }
        });

        JButton btnViewAll = makeViewAllButton();
        btnViewAll.addActionListener(e -> showDetailDialog(dataKey));

        // Inline sort via TableRowSorter
        JTable tbl = createStyledTable(model);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        tbl.setRowSorter(sorter);

        inlineSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            void applyFilter() {
                String k = inlineSearch.getText().trim();
                if (k.isEmpty() || k.equals(searchHint)) { sorter.setRowFilter(null); return; }
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(k)));
            }
        });

        rightPanel.add(inlineSearch); rightPanel.add(btnViewAll);
        topRow.add(lblTitle, BorderLayout.WEST);
        topRow.add(rightPanel, BorderLayout.EAST);
        outer.add(topRow, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(tbl);
        scroll.setBorder(new LineBorder(BORDER_C,1,true));
        scroll.getViewport().setBackground(CARD);
        outer.add(scroll, BorderLayout.CENTER);
        return outer;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  DETAIL DIALOG – đầy đủ với tìm kiếm, đặt lại, phân trang, sắp xếp
    // ══════════════════════════════════════════════════════════════════════
    private void showDetailDialog(String dataKey) {
        // Đảm bảo dữ liệu đã được load
        if (fullDataCoCap.isEmpty() && fullDataActType.isEmpty()
                && fullDataTopMembers.isEmpty() && fullDataRecentActs.isEmpty()) {
            refreshTableData();
        }

        String title;
        String placeholder;
        String[] columns;
        List<Object[]> data;

        switch (dataKey) {
            case "coCap":
                title = "Chi tiết – Cơ cấu hội viên";
                placeholder = "Tìm theo trạng thái hội viên...";
                columns = new String[]{"Trạng thái","Số lượng","Tỷ lệ (%)","So kỳ trước"};
                data = new ArrayList<>(fullDataCoCap);
                break;
            case "actType":
                title = "Chi tiết – Hoạt động theo loại";
                placeholder = "Tìm theo loại hoạt động...";
                columns = new String[]{"Loại hoạt động","Số HĐ","Tổng lượt TG","TB TG/HĐ","HĐ phổ biến nhất"};
                data = new ArrayList<>(fullDataActType);
                break;
            case "topMembers":
                title = "Chi tiết – Top hội viên tích cực";
                placeholder = "Tìm theo tên hoặc email hội viên...";
                columns = new String[]{"#","Tên hội viên","Email","Số lần TG","HĐ gần nhất","Trạng thái"};
                data = new ArrayList<>(fullDataTopMembers);
                break;
            case "recentActs":
                title = "Chi tiết – Hoạt động gần đây";
                placeholder = "Tìm theo tên hoạt động hoặc địa điểm...";
                columns = new String[]{"Tên hoạt động","Loại","Bắt đầu","Kết thúc","Địa điểm","Đăng ký","Trạng thái"};
                data = new ArrayList<>(fullDataRecentActs);
                break;
            default:
                return;
        }

        openFullDetailDialog(title, placeholder, columns, data);
    }

    private void openFullDetailDialog(String title, String placeholder,
                                       String[] columns, List<Object[]> allData) {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(owner, title, true);
        dlg.setSize(920, 580);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        // ── Dialog header (gradient) ──
        JPanel head = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                GradientPaint gp=new GradientPaint(0,0,BLUE,getWidth(),0,new Color(0x4361EE));
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight());
                g2.dispose(); super.paintComponent(g);
            }
        };
        head.setOpaque(false); head.setPreferredSize(new Dimension(0,52));
        head.setBorder(new EmptyBorder(12,20,12,20));
        JLabel hTitle = new JLabel("📋  " + title);
        hTitle.setFont(FONT_HEADING); hTitle.setForeground(Color.WHITE);
        JLabel periodLbl = new JLabel("Kỳ: " + periodLabel());
        periodLbl.setFont(FONT_SMALL); periodLbl.setForeground(new Color(255,255,255,180));
        head.add(hTitle, BorderLayout.WEST); head.add(periodLbl, BorderLayout.EAST);
        dlg.add(head, BorderLayout.NORTH);

        // ── Content ──
        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBackground(CARD); content.setBorder(new EmptyBorder(14,18,14,18));

        // Search bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBar.setOpaque(false);

        JTextField txtSearch = new JTextField(28);
        txtSearch.setFont(FONT_LABEL);
        txtSearch.setPreferredSize(new Dimension(300, 34));
        txtSearch.setBorder(new CompoundBorder(new LineBorder(BORDER_C,1,true), new EmptyBorder(5,10,5,10)));
        txtSearch.setToolTipText(placeholder);
        // Placeholder
        txtSearch.setForeground(TXT_S); txtSearch.setText(placeholder);
        txtSearch.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (txtSearch.getText().equals(placeholder)) {
                    txtSearch.setText(""); txtSearch.setForeground(TXT_H);
                }
            }
            public void focusLost(FocusEvent e) {
                if (txtSearch.getText().isEmpty()) {
                    txtSearch.setText(placeholder); txtSearch.setForeground(TXT_S);
                }
            }
        });

        JButton btnReset = actionBtn("Đặt lại", new Color(107,114,128));
        btnReset.setPreferredSize(new Dimension(90, 34));
        JLabel lblCount = new JLabel("Tổng: " + allData.size() + " bản ghi");
        lblCount.setFont(FONT_SMALL); lblCount.setForeground(TXT_S);

        searchBar.add(lbl("🔍 Tìm kiếm:", FONT_BOLD, TXT_S));
        searchBar.add(txtSearch); searchBar.add(btnReset); searchBar.add(lblCount);
        content.add(searchBar, BorderLayout.NORTH);

        // Table
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tbl = createStyledTable(tableModel);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        tbl.setRowSorter(sorter);

        JScrollPane scroll = new JScrollPane(tbl);
        scroll.setBorder(new LineBorder(BORDER_C,1,true));
        scroll.getViewport().setBackground(CARD);
        content.add(scroll, BorderLayout.CENTER);

        // Pagination
        final int[] currentPage = {0};
        final List<Object[]>[] filteredData = new List[]{new ArrayList<>(allData)};

        JPanel pagPanel = new JPanel(new BorderLayout());
        pagPanel.setOpaque(false); pagPanel.setBorder(new EmptyBorder(8,0,0,0));

        JPanel pagLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pagLeft.setOpaque(false);
        JButton btnFirst = pageBtn("«");
        JButton btnPrev  = pageBtn("‹ Trước");
        JButton btnNext  = pageBtn("Tiếp ›");
        JButton btnLast  = pageBtn("»");
        JLabel lblPage   = new JLabel();
        lblPage.setFont(FONT_LABEL); lblPage.setForeground(TXT_S);

        pagLeft.add(btnFirst); pagLeft.add(btnPrev); pagLeft.add(lblPage);
        pagLeft.add(btnNext); pagLeft.add(btnLast);

        JPanel pagRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        pagRight.setOpaque(false);
        JLabel lblPageSize = new JLabel("Hiển thị " + PAGE_SIZE + " dòng/trang");
        lblPageSize.setFont(FONT_SMALL); lblPageSize.setForeground(TXT_S);
        pagRight.add(lblPageSize);

        pagPanel.add(pagLeft, BorderLayout.WEST); pagPanel.add(pagRight, BorderLayout.EAST);
        content.add(pagPanel, BorderLayout.SOUTH);

        // ── Helper: load trang ──
        Runnable loadPage = () -> {
            tableModel.setRowCount(0);
            List<Object[]> fd = filteredData[0];
            int total  = fd.size();
            int pages  = Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE);
            if (currentPage[0] >= pages) currentPage[0] = pages - 1;
            int start2 = currentPage[0] * PAGE_SIZE;
            int end2   = Math.min(start2 + PAGE_SIZE, total);
            for (int i = start2; i < end2; i++) tableModel.addRow(fd.get(i));
            lblPage.setText(" Trang " + (currentPage[0]+1) + " / " + pages
                + "  (" + total + " bản ghi) ");
            lblCount.setText("Tổng: " + total + " bản ghi");
            btnFirst.setEnabled(currentPage[0] > 0);
            btnPrev.setEnabled(currentPage[0] > 0);
            btnNext.setEnabled((currentPage[0]+1)*PAGE_SIZE < total);
            btnLast.setEnabled((currentPage[0]+1)*PAGE_SIZE < total);
        };

        // ── Helper: filter ──
        Runnable applyFilter = () -> {
            String kw = txtSearch.getText().trim();
            if (kw.isEmpty() || kw.equals(placeholder)) {
                filteredData[0] = new ArrayList<>(allData);
            } else {
                String kLow = kw.toLowerCase();
                List<Object[]> res = new ArrayList<>();
                for (Object[] row : allData) {
                    for (Object cell : row) {
                        if (cell != null && cell.toString().toLowerCase().contains(kLow)) {
                            res.add(row); break;
                        }
                    }
                }
                filteredData[0] = res;
            }
            currentPage[0] = 0;
            loadPage.run();
        };

        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { applyFilter.run(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { applyFilter.run(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter.run(); }
        });
        btnReset.addActionListener(e -> {
            txtSearch.setText(placeholder); txtSearch.setForeground(TXT_S);
            filteredData[0] = new ArrayList<>(allData);
            currentPage[0] = 0; loadPage.run();
        });
        btnFirst.addActionListener(e -> { currentPage[0] = 0; loadPage.run(); });
        btnPrev.addActionListener(e  -> { if (currentPage[0]>0){ currentPage[0]--; loadPage.run(); }});
        btnNext.addActionListener(e  -> { currentPage[0]++; loadPage.run(); });
        btnLast.addActionListener(e  -> {
            int pages = Math.max(1,(filteredData[0].size()+PAGE_SIZE-1)/PAGE_SIZE);
            currentPage[0] = pages-1; loadPage.run();
        });

        loadPage.run();

        dlg.add(content, BorderLayout.CENTER);

        // Footer close
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        foot.setBackground(BG); foot.setBorder(new MatteBorder(1,0,0,0,BORDER_C));
        JButton btnClose = actionBtn("Đóng", new Color(107,114,128));
        btnClose.setPreferredSize(new Dimension(90, 34));
        btnClose.addActionListener(e -> dlg.dispose());
        foot.add(btnClose);
        dlg.add(foot, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    private JButton pageBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_SMALL);
        btn.setPreferredSize(new Dimension(text.length() > 2 ? 80 : 36, 28));
        btn.setBackground(new Color(235,240,255));
        btn.setForeground(BLUE);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(BLUE); btn.setForeground(Color.WHITE); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(new Color(235,240,255)); btn.setForeground(BLUE); }
        });
        return btn;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  REFRESH TABLE DATA
    // ══════════════════════════════════════════════════════════════════════
    private void refreshTableData() {
        fullDataCoCap.clear();
        fullDataActType.clear();
        fullDataTopMembers.clear();
        fullDataRecentActs.clear();

        // 1. Cơ cấu hội viên
        if (tblCoCapHoiVien != null) {
            tblCoCapHoiVien.setRowCount(0);
            int total    = count("SELECT COUNT(*) FROM HoiVien");
            int hoatDong = count("SELECT COUNT(*) FROM HoiVien WHERE trangThai=N'Hoạt động'");
            int moiMem   = count("SELECT COUNT(*) FROM HoiVien WHERE ngayThamGia >= ?", fromDate());
            int daRoi    = count("SELECT COUNT(*) FROM HoiVien WHERE trangThai LIKE N'%Rời%'");
            int tamDung  = count("SELECT COUNT(*) FROM HoiVien WHERE trangThai=N'Tạm dừng'");
            PeriodRange cur = getCurrentPeriod(); PeriodRange prev = getPreviousPeriod(cur);
            int newCur2  = count("SELECT COUNT(*) FROM HoiVien WHERE ngayThamGia BETWEEN ? AND ?", toSqlDate(cur.start), toSqlDate(cur.end));
            int newPrev2 = count("SELECT COUNT(*) FROM HoiVien WHERE ngayThamGia BETWEEN ? AND ?", toSqlDate(prev.start), toSqlDate(prev.end));
            if (total > 0) {
                addCoCapRow("Hoạt động", hoatDong, total, pct(hoatDong, total>0?hoatDong:1));
                addCoCapRow("Mới",        moiMem,  total, pct(newCur2, newPrev2));
                addCoCapRow("Đã rời",     daRoi,   total, pct(daRoi, Math.max(1,daRoi)));
                if (tamDung>0) addCoCapRow("Tạm dừng", tamDung, total, 0);
                Object[] totalRow = new Object[]{"Tổng", total, "100%", "—"};
                tblCoCapHoiVien.addRow(totalRow);
                fullDataCoCap.add(totalRow);
            }
        }

        // 2. Hoạt động theo loại
        if (tblHoatDongLoai != null) {
            tblHoatDongLoai.setRowCount(0);
            String sqlLoai = "SELECT hd.loaiHoatDong, COUNT(DISTINCT hd.id) soHD, "
                           + "ISNULL(SUM(tgCount.cnt),0) soTG "
                           + "FROM HoatDong hd "
                           + "LEFT JOIN (SELECT idHoatDong, COUNT(*) cnt FROM ThamGia GROUP BY idHoatDong) tgCount "
                           + "ON hd.id=tgCount.idHoatDong GROUP BY hd.loaiHoatDong";
            try (Connection c = DatabaseHelper.getConnection();
                 ResultSet rs = c.createStatement().executeQuery(sqlLoai)) {
                while (rs.next()) {
                    String loai = rs.getString("loaiHoatDong");
                    if (loai==null||loai.isEmpty()) loai="Khác";
                    int soHD=rs.getInt("soHD"), soTG=rs.getInt("soTG");
                    double tb = soHD>0?(double)soTG/soHD:0;
                    String hdPb="—";
                    try {
                        String sqlPb = "SELECT TOP 1 hd2.tenHoatDong FROM HoatDong hd2 "
                            + "LEFT JOIN (SELECT idHoatDong,COUNT(*) cnt FROM ThamGia GROUP BY idHoatDong) x ON hd2.id=x.idHoatDong "
                            + "WHERE hd2.loaiHoatDong=N'"+loai.replace("'","''")+"' ORDER BY ISNULL(x.cnt,0) DESC";
                        ResultSet rs2 = c.createStatement().executeQuery(sqlPb);
                        if (rs2.next()) hdPb=rs2.getString(1);
                    } catch (Exception ignored) {}
                    Object[] row = {loai, soHD, soTG, String.format("%.1f",tb), hdPb};
                    tblHoatDongLoai.addRow(row);
                    fullDataActType.add(row);
                }
            } catch (Exception e) { e.printStackTrace(); }
        }

        // 3. Top hội viên
        if (tblTopHoiVien != null) {
            tblTopHoiVien.setRowCount(0);
            String sqlTop = "SELECT hv.tenHoiVien, hv.email, hv.trangThai, "
                + "COUNT(tg.idHoatDong) soLan, "
                + "ISNULL((SELECT TOP 1 hd2.tenHoatDong FROM ThamGia tg2 "
                + "  JOIN HoatDong hd2 ON tg2.idHoatDong=hd2.id "
                + "  WHERE tg2.idHoiVien=hv.id ORDER BY tg2.ngayDangKy DESC),'—') lastHD "
                + "FROM HoiVien hv LEFT JOIN ThamGia tg ON hv.id=tg.idHoiVien "
                + "GROUP BY hv.id, hv.tenHoiVien, hv.email, hv.trangThai ORDER BY soLan DESC";
            try (Connection c = DatabaseHelper.getConnection();
                 ResultSet rs = c.createStatement().executeQuery(sqlTop)) {
                int rank = 1;
                while (rs.next()) {
                    Object[] row = {rank, rs.getString("tenHoiVien"), rs.getString("email"),
                        rs.getInt("soLan"), rs.getString("lastHD"), rs.getString("trangThai")};
                    fullDataTopMembers.add(row);
                    if (rank <= PAGE_SIZE) tblTopHoiVien.addRow(row);
                    rank++;
                }
            } catch (Exception e) { e.printStackTrace(); }
        }

        // 4. Hoạt động gần đây
        if (tblHoatDongGanDay != null) {
            tblHoatDongGanDay.setRowCount(0);
            String sqlHD = "SELECT hd.tenHoatDong, hd.loaiHoatDong, "
                + "CONVERT(varchar,hd.thoiGianBatDau,103) tgBD, "
                + "CONVERT(varchar,hd.thoiGianKetThuc,103) tgKT, "
                + "hd.diaDiem, ISNULL(tgCnt.cnt,0) soDK, "
                + "CASE WHEN GETDATE()<hd.thoiGianBatDau THEN N'Sắp diễn ra' "
                + "WHEN GETDATE() BETWEEN hd.thoiGianBatDau AND hd.thoiGianKetThuc THEN N'Đang diễn ra' "
                + "ELSE N'Đã kết thúc' END trangThai "
                + "FROM HoatDong hd "
                + "LEFT JOIN (SELECT idHoatDong,COUNT(*) cnt FROM ThamGia GROUP BY idHoatDong) tgCnt "
                + "ON hd.id=tgCnt.idHoatDong ORDER BY hd.thoiGianBatDau DESC";
            try (Connection c = DatabaseHelper.getConnection();
                 ResultSet rs = c.createStatement().executeQuery(sqlHD)) {
                while (rs.next()) {
                    Object[] row = {rs.getString("tenHoatDong"), rs.getString("loaiHoatDong"),
                        rs.getString("tgBD"), rs.getString("tgKT"),
                        rs.getString("diaDiem"), rs.getInt("soDK"), rs.getString("trangThai")};
                    tblHoatDongGanDay.addRow(row);
                    fullDataRecentActs.add(row);
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void addCoCapRow(String tt, int count2, int total, int pct2) {
        double pctVal = total>0 ? count2*100.0/total : 0;
        Object[] row = {tt, count2, String.format("%.1f%%", pctVal), (pct2>=0?"+":"")+pct2+"%"};
        tblCoCapHoiVien.addRow(row);
        fullDataCoCap.add(row);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  STYLED TABLE
    // ══════════════════════════════════════════════════════════════════════
    private JTable createStyledTable(DefaultTableModel model) {
        final int[] hoveredRow = {-1};
        JTable table = new JTable(model) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                int modelRow = convertRowIndexToModel(row);
                if (!isRowSelected(row)) {
                    if (row == hoveredRow[0]) {
                        c.setBackground(new Color(232,240,253));
                    } else {
                        c.setBackground(row%2==0 ? CARD : new Color(248,249,252));
                    }
                } else {
                    c.setBackground(new Color(209,226,255));
                }
                return c;
            }
        };
        table.setFont(FONT_LABEL); table.setRowHeight(36);
        table.setShowHorizontalLines(true); table.setShowVerticalLines(false);
        table.setGridColor(BORDER_C);
        table.setSelectionBackground(new Color(209,226,255));
        table.setSelectionForeground(TXT_H);
        table.setAutoCreateRowSorter(false); // Dùng sorter ngoài
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(BLUE);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.getTableHeader().setReorderingAllowed(false);

        // Gradient header renderer
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel lbl = (JLabel)super.getTableCellRendererComponent(t, v, s, f, r, c);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12)); lbl.setForeground(Color.WHITE);
                lbl.setOpaque(false); lbl.setBorder(new EmptyBorder(0,10,0,10));
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
                // Sort indicator
                if (t.getRowSorter() != null) {
                    SortOrder order = null;
                    for (RowSorter.SortKey key : t.getRowSorter().getSortKeys()) {
                        if (key.getColumn() == c) { order = key.getSortOrder(); break; }
                    }
                    if (order == SortOrder.ASCENDING)  lbl.setText(v + " ↑");
                    else if (order == SortOrder.DESCENDING) lbl.setText(v + " ↓");
                    else lbl.setText(v + " ↕");
                }
                return lbl;
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                GradientPaint gp=new GradientPaint(0,0,BLUE,getWidth(),0,new Color(67,97,238));
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight());
                g2.dispose(); super.paintComponent(g);
            }
        });

        // Hover
        table.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != hoveredRow[0]) { hoveredRow[0]=row; table.repaint(); }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            public void mouseExited(MouseEvent e) { hoveredRow[0]=-1; table.repaint(); }
        });
        return table;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  NOTIFICATION PANEL (static inner class dùng từ MainForm)
    //  MainForm.showNotificationDialog() gọi NotificationPanel.show()
    // ══════════════════════════════════════════════════════════════════════
    /**
     * Tạo dialog thông báo 4 tab cho MainForm gọi.
     * Truyền owner, connection getter, và bell button để refresh.
     */
    public static void showNotificationDialog(Frame owner,
                                               java.util.function.Supplier<Connection> connGetter,
                                               JButton bellBtn) {
        JDialog dlg = new JDialog(owner, "Thông báo hệ thống", true);
        dlg.setSize(640, 520); dlg.setLocationRelativeTo(owner);
        dlg.setLayout(new BorderLayout());

        // Header
        JPanel head = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                GradientPaint gp=new GradientPaint(0,0,BLUE,getWidth(),0,new Color(67,97,238));
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight());
                g2.dispose(); super.paintComponent(g);
            }
        };
        head.setOpaque(false); head.setPreferredSize(new Dimension(0,52));
        head.setBorder(new EmptyBorder(12,18,12,18));
        JLabel hTitle=new JLabel("🔔  Thông báo hệ thống");
        hTitle.setFont(new Font("Segoe UI",Font.BOLD,15)); hTitle.setForeground(Color.WHITE);
        head.add(hTitle, BorderLayout.WEST);
        dlg.add(head, BorderLayout.NORTH);

        // Load all notifications
        List<NotifItem> allNotifs = new ArrayList<>();
        try (Connection conn = connGetter.get()) {
            if (conn == null) throw new Exception("No connection");
            String sql = "SELECT tb.id, tb.noiDung, tb.thoiGian, tb.daDoc, tb.idDangKyTam, tb.idYeuCauRoiHoi, "
                + "ISNULL(dkt.trangThai,'') trangThaiDKT, "
                + "ISNULL(ycr.trangThai,'') trangThaiYCR "
                + "FROM ThongBao tb "
                + "LEFT JOIN DangKyTam dkt ON tb.idDangKyTam=dkt.id "
                + "LEFT JOIN (SELECT id,trangThai FROM YeuCauRoiHoi) ycr ON tb.idYeuCauRoiHoi=ycr.id "
                + "ORDER BY tb.id DESC";
            try (ResultSet rs = conn.createStatement().executeQuery(sql)) {
                while (rs.next()) {
                    NotifItem ni = new NotifItem();
                    ni.id        = rs.getInt("id");
                    ni.noiDung   = rs.getString("noiDung");
                    Timestamp ts = rs.getTimestamp("thoiGian");
                    ni.thoiGian  = ts != null ? ts.toString().substring(0,16) : "";
                    ni.daDoc     = rs.getBoolean("daDoc");
                    ni.idDangKyTam = rs.getObject("idDangKyTam") != null ? rs.getInt("idDangKyTam") : null;
                    ni.idYeuCauRoiHoi = rs.getObject("idYeuCauRoiHoi") != null ? rs.getInt("idYeuCauRoiHoi") : null;
                    String ttDKT = rs.getString("trangThaiDKT");
                    String ttYCR = rs.getString("trangThaiYCR");
                    // Xác định trạng thái xử lý
                    if (!ttYCR.isEmpty()) ni.trangThaiXuLy = ttYCR;
                    else if (!ttDKT.isEmpty()) ni.trangThaiXuLy = ttDKT;
                    else ni.trangThaiXuLy = ni.daDoc ? "Đã đọc" : "Chưa đọc";
                    allNotifs.add(ni);
                }
            }
        } catch (Exception ignored) {}

        // Phân loại vào 4 nhóm
        List<NotifItem> unread   = new ArrayList<>();
        List<NotifItem> read     = new ArrayList<>();
        List<NotifItem> pending  = new ArrayList<>();
        List<NotifItem> approved = new ArrayList<>();
        for (NotifItem ni : allNotifs) {
            if (!ni.daDoc) unread.add(ni);
            else read.add(ni);
            String tt2 = ni.trangThaiXuLy.toLowerCase();
            if (tt2.contains("chờ") || tt2.contains("chưa duyệt") || tt2.contains("chưa xử lý")) {
                pending.add(ni);
            } else if (tt2.contains("đã duyệt") || tt2.contains("đã xác nhận") || tt2.contains("đã đọc")) {
                approved.add(ni);
            }
        }

        // Tab panel
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabs.addTab("🔴 Chưa đọc (" + unread.size() + ")",
            buildNotifTab(unread, new Color(235,240,255), BLUE, connGetter, dlg, bellBtn));
        tabs.addTab("✓ Đã đọc (" + read.size() + ")",
            buildNotifTab(read, new Color(248,250,252), new Color(160,174,192), connGetter, dlg, bellBtn));
        tabs.addTab("⏳ Chưa duyệt (" + pending.size() + ")",
            buildNotifTab(pending, new Color(255,251,235), new Color(245,158,11), connGetter, dlg, bellBtn));
        tabs.addTab("✅ Đã duyệt (" + approved.size() + ")",
            buildNotifTab(approved, new Color(236,253,245), new Color(16,185,129), connGetter, dlg, bellBtn));

        dlg.add(tabs, BorderLayout.CENTER);

        // Footer
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        foot.setBackground(BG); foot.setBorder(new MatteBorder(1,0,0,0,BORDER_C));
        JButton btnMarkAll = new JButton("Đánh dấu tất cả đã đọc");
        btnMarkAll.setFont(new Font("Segoe UI",Font.BOLD,12));
        btnMarkAll.setBackground(BLUE); btnMarkAll.setForeground(Color.WHITE);
        btnMarkAll.setBorderPainted(false); btnMarkAll.setFocusPainted(false);
        btnMarkAll.setPreferredSize(new Dimension(200, 34));
        btnMarkAll.addActionListener(ev -> {
            try (Connection c = connGetter.get()) {
                if (c!=null) c.createStatement().executeUpdate("UPDATE ThongBao SET daDoc=1");
            } catch (Exception ignored) {}
            if (bellBtn != null) bellBtn.repaint();
            dlg.dispose();
        });
        JButton btnClose2 = new JButton("Đóng");
        btnClose2.setFont(new Font("Segoe UI",Font.BOLD,12));
        btnClose2.setPreferredSize(new Dimension(90,34));
        btnClose2.addActionListener(ev -> dlg.dispose());
        foot.add(btnMarkAll); foot.add(btnClose2);
        dlg.add(foot, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private static JScrollPane buildNotifTab(List<NotifItem> items, Color bgHighlight,
                                              Color accentColor,
                                              java.util.function.Supplier<Connection> connGetter,
                                              JDialog dlg, JButton bellBtn) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);

        if (items.isEmpty()) {
            JLabel empty = new JLabel("Không có thông báo", SwingConstants.CENTER);
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            empty.setForeground(new Color(160,174,192));
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            empty.setBorder(new EmptyBorder(40,0,40,0));
            panel.add(empty);
        } else {
            for (NotifItem ni : items) {
                JPanel cell = buildNotifCell(ni, bgHighlight, accentColor, connGetter, dlg, bellBtn);
                panel.add(cell);
            }
        }

        JScrollPane sp = new JScrollPane(panel);
        sp.setBorder(null); sp.getViewport().setBackground(Color.WHITE);
        sp.getVerticalScrollBar().setUnitIncrement(12);
        return sp;
    }

    private static JPanel buildNotifCell(NotifItem ni, Color bgColor, Color accentColor,
                                          java.util.function.Supplier<Connection> connGetter,
                                          JDialog dlg, JButton bellBtn) {
        JPanel cell = new JPanel(new BorderLayout(10, 0));
        cell.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        cell.setAlignmentX(Component.LEFT_ALIGNMENT);
        cell.setBorder(new CompoundBorder(
            new MatteBorder(0,0,1,0,new Color(235,237,242)),
            new EmptyBorder(10,16,10,16)));
        cell.setBackground(ni.daDoc ? Color.WHITE : bgColor);
        cell.setOpaque(true);

        // Left accent bar
        JPanel accent = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(accentColor); g.fillRect(0,0,4,getHeight());
            }
        };
        accent.setOpaque(false); accent.setPreferredSize(new Dimension(6, 0));
        cell.add(accent, BorderLayout.WEST);

        // Content
        JPanel content = new JPanel(new BorderLayout(0, 4));
        content.setOpaque(false);

        JLabel lblText = new JLabel("<html><body style='width:380px'>" + ni.noiDung + "</body></html>");
        lblText.setFont(ni.daDoc ? new Font("Segoe UI",Font.PLAIN,12) : new Font("Segoe UI",Font.BOLD,12));
        lblText.setForeground(ni.daDoc ? new Color(100,116,139) : TXT_H);

        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setOpaque(false);
        JLabel lblTime = new JLabel("🕐 " + ni.thoiGian);
        lblTime.setFont(new Font("Segoe UI",Font.PLAIN,10)); lblTime.setForeground(new Color(160,174,192));

        // Status badge
        Color badgeBg; Color badgeFg;
        if (ni.trangThaiXuLy.toLowerCase().contains("chưa") || ni.trangThaiXuLy.toLowerCase().contains("chờ")) {
            badgeBg = new Color(255,251,235); badgeFg = new Color(180,120,0);
        } else if (ni.trangThaiXuLy.toLowerCase().contains("đã duyệt") || ni.trangThaiXuLy.toLowerCase().contains("xác nhận")) {
            badgeBg = new Color(236,253,245); badgeFg = new Color(16,130,90);
        } else if (!ni.daDoc) {
            badgeBg = new Color(235,240,255); badgeFg = BLUE;
        } else {
            badgeBg = new Color(243,244,246); badgeFg = new Color(100,116,139);
        }
        JLabel statusBadge = new JLabel("  " + ni.trangThaiXuLy + "  ") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(badgeBg); g2.fillRoundRect(0,0,getWidth(),getHeight(),getHeight(),getHeight());
                g2.dispose(); super.paintComponent(g);
            }
        };
        statusBadge.setFont(new Font("Segoe UI",Font.BOLD,10));
        statusBadge.setForeground(badgeFg); statusBadge.setOpaque(false);

        bottomRow.add(lblTime, BorderLayout.WEST); bottomRow.add(statusBadge, BorderLayout.EAST);
        content.add(lblText, BorderLayout.CENTER); content.add(bottomRow, BorderLayout.SOUTH);
        cell.add(content, BorderLayout.CENTER);

        // Hover + click đánh dấu đã đọc
        cell.addMouseListener(new MouseAdapter() {
            Color origBg = cell.getBackground();
            public void mouseEntered(MouseEvent e) {
                cell.setBackground(new Color(accentColor.getRed(),accentColor.getGreen(),accentColor.getBlue(),20));
                cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            public void mouseExited(MouseEvent e) { cell.setBackground(origBg); }
            public void mouseClicked(MouseEvent e) {
                if (!ni.daDoc) {
                    try (Connection c = connGetter.get()) {
                        if (c!=null) c.createStatement().executeUpdate("UPDATE ThongBao SET daDoc=1 WHERE id="+ni.id);
                        ni.daDoc = true;
                        cell.setBackground(Color.WHITE);
                        lblText.setFont(new Font("Segoe UI",Font.PLAIN,12));
                        lblText.setForeground(new Color(100,116,139));
                        if (bellBtn!=null) bellBtn.repaint();
                    } catch (Exception ignored) {}
                }
                showNotificationDetail(dlg.getOwner(), ni, connGetter, bellBtn, () -> { if (bellBtn!=null) bellBtn.repaint(); });
            }
        });
        return cell;
    }
    
    
    private static void handleRejectNotification(
        Component parent,
        NotifItem ni) {

    try(Connection conn =
            DatabaseHelper.getConnection()) {

        // =========================
        // TỪ CHỐI ĐĂNG KÝ HOẠT ĐỘNG
        // =========================

        if(ni.idDangKyTam != null){

            PreparedStatement ps =
                    conn.prepareStatement(

                "UPDATE DangKyTam " +
                "SET trangThai=N'Từ chối' " +
                "WHERE id=?"
            );

            ps.setInt(1, ni.idDangKyTam);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(
                    parent,
                    "Đã từ chối đăng ký hoạt động."
            );
        }

        // =========================
        // TỪ CHỐI RỜI HỘI
        // =========================

        if(ni.idYeuCauRoiHoi != null){

            PreparedStatement ps =
                    conn.prepareStatement(

                "UPDATE YeuCauRoiHoi " +
                "SET trangThai=N'Từ chối' " +
                "WHERE id=?"
            );

            ps.setInt(1, ni.idYeuCauRoiHoi);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(
                    parent,
                    "Đã từ chối yêu cầu rời hội."
            );
        }

    } catch(Exception e){

        JOptionPane.showMessageDialog(
                parent,
                "Lỗi: " + e.getMessage()
        );
    }
}
    
    
    /** Hiển thị dialog chi tiết thông báo với UI custom + animation mở. */
    private static void showNotificationDetail(Window owner, NotifItem ni,
                                               java.util.function.Supplier<Connection> connGetter,
                                               JButton bellBtn, Runnable refreshCallback) {
        JDialog detail = new JDialog(owner, "Chi tiết thông báo", Dialog.ModalityType.APPLICATION_MODAL);
        detail.setUndecorated(true);
        detail.setSize(560, 380);
        detail.setLocationRelativeTo(owner);
        detail.setLayout(new BorderLayout());

        NotificationType type = detectNotificationType(ni);
        Color baseColor = type.accent;

        JPanel shell = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(new Color(220, 226, 236));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
            }
        };
        shell.setOpaque(false);
        shell.setBorder(new EmptyBorder(2, 2, 2, 2));

        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, BLUE, getWidth(), 0, new Color(67, 97, 238));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight() + 16, 16, 16);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(14, 16, 14, 16));
        JLabel title = new JLabel(type.icon + " " + type.title);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        JButton btnClose = new JButton("✕");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnClose.setForeground(Color.WHITE);
        btnClose.setOpaque(false); btnClose.setContentAreaFilled(false); btnClose.setBorderPainted(false);
        btnClose.addActionListener(e -> detail.dispose());
        header.add(title, BorderLayout.WEST);
        header.add(btnClose, BorderLayout.EAST);

        JPanel content = new JPanel();
        content.setBackground(Color.WHITE);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(16, 20, 12, 20));

        content.add(buildDetailRow("Tiêu đề", type.title));
        content.add(buildDetailRow("Loại thông báo", type.displayName));
        content.add(buildDetailRow("Trạng thái xử lý", ni.trangThaiXuLy));
        content.add(buildDetailRow("Thời gian tạo", ni.thoiGian));
        JTextArea body = new JTextArea(ni.noiDung == null ? "" : ni.noiDung);
        body.setLineWrap(true); body.setWrapStyleWord(true); body.setEditable(false);
        body.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        body.setBackground(new Color(248, 250, 253));
        body.setBorder(new CompoundBorder(new LineBorder(new Color(228, 234, 244), 1, true), new EmptyBorder(10, 12, 10, 12)));
        body.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        content.add(Box.createVerticalStrut(10));
        content.add(body);

        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        JButton btnApprove =
        new JButton("✔ Duyệt");

        JButton btnReject =
        new JButton("✖ Từ chối");
        btnApprove.addActionListener(e -> {

        handleApproveNotification(
                detail,
                ni
        );
        
        btnReject.addActionListener(e -> {

        handleRejectNotification(
                detail,
                ni
        );

        detail.dispose();
    });

        detail.dispose();
    });
        foot.setOpaque(false);
        JButton actionBtn = new JButton(type.actionLabel);
        actionBtn.setVisible(type != NotificationType.GENERAL);
        actionBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        actionBtn.setBackground(baseColor); actionBtn.setForeground(Color.WHITE);
        actionBtn.setBorderPainted(false); actionBtn.setFocusPainted(false);
        actionBtn.addActionListener(e -> {

        handleApproveNotification(
                detail,
                ni
        );

        detail.dispose();
    });
        JButton closeBtn = new JButton("Đóng");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        closeBtn.addActionListener(e -> detail.dispose());
        foot.add(btnApprove);
        foot.add(btnReject);

        foot.add(actionBtn);
        foot.add(closeBtn);

        shell.add(header, BorderLayout.NORTH);
        shell.add(content, BorderLayout.CENTER);
        shell.add(foot, BorderLayout.SOUTH);
        detail.setContentPane(shell);

        javax.swing.Timer openFx = new javax.swing.Timer(10, null);
        final float[] opacity = {0.0f};
        detail.setOpacity(0.05f);
        openFx.addActionListener(e -> {
            opacity[0] += 0.12f;
            if (opacity[0] >= 1f) { detail.setOpacity(1f); openFx.stop(); }
            else detail.setOpacity(opacity[0]);
        });
        openFx.start();
        detail.setVisible(true);
    }

    private static JPanel buildDetailRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(3, 0, 3, 0));
        JLabel l1 = new JLabel(label + ":");
        l1.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l1.setForeground(new Color(71, 85, 105));
        JLabel l2 = new JLabel(value == null ? "" : value);
        l2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l2.setForeground(TXT_H);
        row.add(l1, BorderLayout.WEST); row.add(l2, BorderLayout.CENTER);
        return row;
    }

    private static NotificationType detectNotificationType(NotifItem ni) {
        String lower = ni.noiDung == null ? "" : ni.noiDung.toLowerCase(new Locale("vi"));
        String status = ni.trangThaiXuLy == null ? "" : ni.trangThaiXuLy.toLowerCase(new Locale("vi"));
        if (ni.idDangKyTam != null || lower.contains("đăng ký") || lower.contains("tham gia")) return NotificationType.REGISTRATION;
        if (ni.idYeuCauRoiHoi != null || lower.contains("rời hội") || status.contains("rời")) return NotificationType.LEAVE_REQUEST;
        return NotificationType.GENERAL;
    }

    private static void handleApproveNotification(
        Component parent,
        NotifItem ni) {

    try(Connection conn =
            DatabaseHelper.getConnection()) {

        // =========================
        // DUYỆT ĐĂNG KÝ HOẠT ĐỘNG
        // =========================

        if(ni.idDangKyTam != null){

            PreparedStatement ps =
                    conn.prepareStatement(

                "SELECT * FROM DangKyTam " +
                "WHERE id=?"
            );

            ps.setInt(1, ni.idDangKyTam);

            ResultSet rs = ps.executeQuery();

            if(rs.next()){

                int idHoiVien =
                        rs.getInt("idHoiVien");

                int idHoatDong =
                        rs.getInt("idHoatDong");

                // cập nhật trạng thái

                PreparedStatement upd =
                        conn.prepareStatement(

                    "UPDATE DangKyTam " +
                    "SET trangThai=N'Đã duyệt' " +
                    "WHERE id=?"
                );

                upd.setInt(1, ni.idDangKyTam);

                upd.executeUpdate();

                // thêm bảng tham gia

                PreparedStatement tg =
                        conn.prepareStatement(

                    "INSERT INTO ThamGia(" +
                    "idHoiVien," +
                    "idHoatDong," +
                    "ngayDangKy," +
                    "trangThai" +
                    ") VALUES(?,?,GETDATE(),N'Đã đăng ký')"
                );

                tg.setInt(1, idHoiVien);

                tg.setInt(2, idHoatDong);

                tg.executeUpdate();

                JOptionPane.showMessageDialog(
                        parent,
                        "Đã duyệt đăng ký hoạt động."
                );
            }
        }

        // =========================
        // DUYỆT RỜI HỘI
        // =========================

        if(ni.idYeuCauRoiHoi != null){

            PreparedStatement ps =
                    conn.prepareStatement(

                "SELECT * FROM YeuCauRoiHoi " +
                "WHERE id=?"
            );

            ps.setInt(1, ni.idYeuCauRoiHoi);

            ResultSet rs = ps.executeQuery();

            if(rs.next()){

                int idHoiVien =
                        rs.getInt("idHoiVien");

                // cập nhật yêu cầu

                PreparedStatement upd =
                        conn.prepareStatement(

                    "UPDATE YeuCauRoiHoi " +
                    "SET trangThai=N'Đã duyệt' " +
                    "WHERE id=?"
                );

                upd.setInt(1, ni.idYeuCauRoiHoi);

                upd.executeUpdate();

                // cập nhật hội viên

                PreparedStatement hv =
                        conn.prepareStatement(

                    "UPDATE HoiVien " +
                    "SET trangThai=N'Đã rời' " +
                    "WHERE id=?"
                );

                hv.setInt(1, idHoiVien);

                hv.executeUpdate();

                JOptionPane.showMessageDialog(
                        parent,
                        "Đã duyệt rời hội."
                );
            }
        }

    } catch(Exception e){

        JOptionPane.showMessageDialog(
                parent,
                "Lỗi: " + e.getMessage()
        );
    }
}

    private enum NotificationType {
        REGISTRATION("Đăng ký hoạt động", "📅", "Thông báo đăng ký hoạt động", "Mở ThamGiaForm", new Color(59, 130, 246)),
        LEAVE_REQUEST("Yêu cầu rời hội", "🚪", "Yêu cầu rời hội", "Duyệt yêu cầu rời hội", new Color(239, 68, 68)),
        GENERAL("Thông báo chung", "🔔", "Thông báo hệ thống", "", new Color(100, 116, 139));
        final String displayName, icon, title, actionLabel; final Color accent;
        NotificationType(String displayName, String icon, String title, String actionLabel, Color accent) {
            this.displayName = displayName; this.icon = icon; this.title = title; this.actionLabel = actionLabel; this.accent = accent;
        }
    }

    /** Data class cho thông báo */
    private static class NotifItem {
        int id; String noiDung; String thoiGian;
        boolean daDoc; String trangThaiXuLy = "";
        Integer idDangKyTam; Integer idYeuCauRoiHoi;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  EXPORT EXCEL – đúng kỳ lọc, đa sheet, header màu căn giữa
    // ══════════════════════════════════════════════════════════════════════
    private void exportExcelWithPeriod() {
        // Đảm bảo có dữ liệu mới nhất
        refreshTableData();
        String periodStr = periodLabel();

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Lưu file Excel");
        String safeLabel = periodStr.replaceAll("[^a-zA-Z0-9_\\-]","_");
        fc.setSelectedFile(new java.io.File("Dashboard_" + safeLabel + ".xml"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(fc.getSelectedFile()), "UTF-8"))) {

            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            pw.println("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\"");
            pw.println(" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\">");

            // Styles
            pw.println("<Styles>");
            pw.println("<Style ss:ID=\"sHeader\"><Font ss:Bold=\"1\" ss:Color=\"#FFFFFF\" ss:Size=\"12\"/>");
            pw.println("<Interior ss:Color=\"#1359B9\" ss:Pattern=\"Solid\"/>");
            pw.println("<Alignment ss:Horizontal=\"Center\" ss:Vertical=\"Center\"/></Style>");
            pw.println("<Style ss:ID=\"sTitle\"><Font ss:Bold=\"1\" ss:Color=\"#1359B9\" ss:Size=\"14\"/>");
            pw.println("<Alignment ss:Horizontal=\"Center\"/></Style>");
            pw.println("<Style ss:ID=\"sPeriod\"><Font ss:Italic=\"1\" ss:Color=\"#718096\" ss:Size=\"11\"/>");
            pw.println("<Alignment ss:Horizontal=\"Center\"/></Style>");
            pw.println("<Style ss:ID=\"sAlt\"><Interior ss:Color=\"#F8FAFF\" ss:Pattern=\"Solid\"/></Style>");
            pw.println("<Style ss:ID=\"sTotal\"><Font ss:Bold=\"1\"/>");
            pw.println("<Interior ss:Color=\"#E8F0FD\" ss:Pattern=\"Solid\"/></Style>");
            pw.println("</Styles>");

            // Sheet 1: Cơ cấu hội viên
            String[] hdCoCap = {"Trạng thái","Số lượng","Tỷ lệ (%)","So kỳ trước"};
            writeExcelSheet(pw, "Cơ cấu hội viên", periodStr, hdCoCap, fullDataCoCap);

            // Sheet 2: Hoạt động theo loại
            String[] hdActType = {"Loại hoạt động","Số HĐ","Tổng lượt TG","TB TG/HĐ","HĐ phổ biến nhất"};
            writeExcelSheet(pw, "Hoạt động theo loại", periodStr, hdActType, fullDataActType);

            // Sheet 3: Top hội viên
            String[] hdTop = {"#","Tên hội viên","Email","Số lần TG","HĐ gần nhất","Trạng thái"};
            writeExcelSheet(pw, "Top hội viên", periodStr, hdTop, fullDataTopMembers);

            // Sheet 4: Hoạt động gần đây
            String[] hdRecent = {"Tên hoạt động","Loại","Bắt đầu","Kết thúc","Địa điểm","Đăng ký","Trạng thái"};
            writeExcelSheet(pw, "Hoạt động gần đây", periodStr, hdRecent, fullDataRecentActs);

            // Sheet 5: Thống kê tổng hợp
            writeStatsSummarySheet(pw, periodStr);

            pw.println("</Workbook>");

            JOptionPane.showMessageDialog(this,
                "✅ Xuất Excel thành công!\n" + fc.getSelectedFile().getAbsolutePath()
                + "\n\nKỳ thống kê: " + periodStr,
                "Thành công", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi xuất Excel: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void writeExcelSheet(PrintWriter pw, String sheetName, String period,
                                   String[] headers, List<Object[]> data) {
        pw.println("<Worksheet ss:Name=\"" + escXml(sheetName) + "\"><Table>");

        int colCount = headers.length;
        // Dòng tiêu đề (merge)
        pw.println("<Row ss:Height=\"28\">");
        pw.println("<Cell ss:MergeAcross=\"" + (colCount-1) + "\" ss:StyleID=\"sTitle\">"
            + "<Data ss:Type=\"String\">" + escXml(sheetName.toUpperCase()) + "</Data></Cell></Row>");

        // Dòng kỳ lọc (merge)
        pw.println("<Row ss:Height=\"22\">");
        pw.println("<Cell ss:MergeAcross=\"" + (colCount-1) + "\" ss:StyleID=\"sPeriod\">"
            + "<Data ss:Type=\"String\">Kỳ thống kê: " + escXml(period) + "</Data></Cell></Row>");

        // Dòng trống
        pw.println("<Row ss:Height=\"10\"><Cell><Data ss:Type=\"String\"></Data></Cell></Row>");

        // Header row
        pw.println("<Row ss:Height=\"36\">");
        for (String h : headers) {
            pw.println("<Cell ss:StyleID=\"sHeader\"><Data ss:Type=\"String\">" + escXml(h) + "</Data></Cell>");
        }
        pw.println("</Row>");

        // Data rows
        for (int i = 0; i < data.size(); i++) {
            Object[] row = data.get(i);
            pw.println("<Row ss:Height=\"24\">");
            for (Object v : row) {
                String val = v == null ? "" : v.toString();
                boolean isNum = v instanceof Number;
                String type = isNum ? "Number" : "String";
                pw.println("<Cell" + (i%2!=0 ? " ss:StyleID=\"sAlt\"" : "") + ">"
                    + "<Data ss:Type=\"" + type + "\">" + escXml(val) + "</Data></Cell>");
            }
            pw.println("</Row>");
        }

        pw.println("</Table></Worksheet>");
    }

    private void writeStatsSummarySheet(PrintWriter pw, String period) {
        PeriodRange cur  = getCurrentPeriod();
        PeriodRange prev = getPreviousPeriod(cur);

        int totalCur  = count("SELECT COUNT(*) FROM HoiVien");
        int totalPrev = count("SELECT COUNT(*) FROM HoiVien WHERE ngayThamGia <= ?", toSqlDate(prev.end));
        int newCur2   = count("SELECT COUNT(*) FROM HoiVien WHERE ngayThamGia BETWEEN ? AND ?", toSqlDate(cur.start), toSqlDate(cur.end));
        int newPrev2  = count("SELECT COUNT(*) FROM HoiVien WHERE ngayThamGia BETWEEN ? AND ?", toSqlDate(prev.start), toSqlDate(prev.end));
        int leftCur2  = count("SELECT COUNT(*) FROM HoiVien WHERE trangThai LIKE N'%Rời%'");
        int actCur2   = count("SELECT COUNT(*) FROM HoatDong WHERE thoiGianBatDau BETWEEN ? AND ?", toSqlDate(cur.start), toSqlDate(cur.end));
        int actPrev2  = count("SELECT COUNT(*) FROM HoatDong WHERE thoiGianBatDau BETWEEN ? AND ?", toSqlDate(prev.start), toSqlDate(prev.end));
        int partCur2  = count("SELECT COUNT(*) FROM ThamGia WHERE ngayDangKy BETWEEN ? AND ?", toSqlDate(cur.start), toSqlDate(cur.end));
        int partPrev2 = count("SELECT COUNT(*) FROM ThamGia WHERE ngayDangKy BETWEEN ? AND ?", toSqlDate(prev.start), toSqlDate(prev.end));

        String[] headers = {"Chỉ số","Giá trị kỳ này","Giá trị kỳ trước","% Thay đổi","Nhận xét"};
        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{"Tổng hội viên",   totalCur,  totalPrev,  pct(totalCur, totalPrev)+"%" , totalCur >= totalPrev ? "↑ Tăng" : "↓ Giảm"});
        rows.add(new Object[]{"Hội viên mới",    newCur2,   newPrev2,   pct(newCur2, newPrev2)+"%",    newCur2  >= newPrev2  ? "↑ Tăng" : "↓ Giảm"});
        rows.add(new Object[]{"Hội viên đã rời", leftCur2,  "—",        "—",                           "—"});
        rows.add(new Object[]{"Tổng hoạt động",  actCur2,   actPrev2,   pct(actCur2, actPrev2)+"%",    actCur2  >= actPrev2  ? "↑ Tăng" : "↓ Giảm"});
        rows.add(new Object[]{"Lượt tham gia",   partCur2,  partPrev2,  pct(partCur2, partPrev2)+"%",  partCur2 >= partPrev2 ? "↑ Tăng" : "↓ Giảm"});
        writeExcelSheet(pw, "Tổng hợp", period, headers, rows);
    }

    private String escXml(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;");
    }

    // ══════════════════════════════════════════════════════════════════════
    //  MAIN REFRESH
    // ══════════════════════════════════════════════════════════════════════
    public void refresh() {
        buildStatCards();
        loadCharts();
        refreshTableData();
        revalidate(); repaint();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  SECTION CARD helper
    // ══════════════════════════════════════════════════════════════════════
    private JPanel sectionCard(String title) {
        Color accent = title.contains("Top") ? YELLOW : BLUE;
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD); g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,16,16);
                g2.setColor(accent); g2.fillRoundRect(0,0,4,getHeight(),4,4);
                g2.dispose();
            }
        };
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(new CompoundBorder(new LineBorder(BORDER_C,1,true), new EmptyBorder(18,22,16,20)));
        p.setPreferredSize(new Dimension(0, 340));

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        headerRow.setAlignmentX(LEFT_ALIGNMENT);
        JLabel lbl = new JLabel(title);
        lbl.setFont(FONT_HEADING); lbl.setForeground(TXT_H);
        headerRow.add(lbl, BorderLayout.WEST);
        p.add(headerRow); p.add(Box.createVerticalStrut(14));
        return p;
    }

    // ── "Xem đầy đủ" button factory ──────────────────────────────────────
    private JButton makeViewAllButton() {
        JButton btn = new JButton("Xem đầy đủ →") {
            boolean hov = false;
            { setOpaque(false); setContentAreaFilled(false); setBorderPainted(false);
              setFocusPainted(false); setFont(new Font("Segoe UI",Font.BOLD,11));
              setForeground(BLUE); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
              addMouseListener(new MouseAdapter(){
                  public void mouseEntered(MouseEvent e){ hov=true; repaint(); }
                  public void mouseExited(MouseEvent e){ hov=false; repaint(); }
              });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hov?BLUE:new Color(235,240,255));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.setColor(hov?Color.WHITE:BLUE);
                g2.setFont(getFont());
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,
                    (getHeight()-fm.getHeight())/2+fm.getAscent());
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(120, 30));
        return btn;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  UTILITIES
    // ══════════════════════════════════════════════════════════════════════
    private void onPeriodChange() {
        int idx = cbPeriod.getSelectedIndex();
        LocalDate now = LocalDate.now();
        LocalDate from = switch (idx) {
            case 0 -> now.with(java.time.DayOfWeek.MONDAY);
            case 1 -> now.withDayOfMonth(1);
            case 2 -> now.withDayOfYear(1);
            default -> now.minusDays(6);
        };
        boolean custom = (idx == 3);
        spFrom.setEnabled(custom); spTo.setEnabled(custom);
        if (!custom) {
            ((SpinnerDateModel)spFrom.getModel()).setValue(java.sql.Date.valueOf(from));
            ((SpinnerDateModel)spTo.getModel()).setValue(java.sql.Date.valueOf(now));
        }
    }

    private java.sql.Date fromDate() {
        return new java.sql.Date(((java.util.Date) spFrom.getValue()).getTime());
    }

    private void clearAndAdd(JPanel holder, Component newComp) {
        Component[] comps = holder.getComponents();
        for (Component c : comps) {
            if (c instanceof ChartPanel) { holder.remove(c); break; }
        }
        holder.add(newComp, BorderLayout.CENTER);
        holder.revalidate(); holder.repaint();
    }

    private void animateReload(JButton btn) {
        String orig = btn.getText();
        javax.swing.Timer t = new javax.swing.Timer(120, null);
        String[] frames = {"↻ Đang tải...", "↺ Đang tải...", "↻ Đang tải...", "↺ Đang tải..."};
        final int[] idx = {0};
        t.addActionListener(e -> {
            btn.setText(frames[idx[0]++ % frames.length]);
            if (idx[0] >= 8) { t.stop(); btn.setText(orig); }
        });
        t.start();
    }

    private JToggleButton toggleBtn(String text, boolean selected) {
        JToggleButton btn = new JToggleButton(text, selected) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                if (isSelected()) {
                    g2.setColor(BLUE); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                    g2.setColor(Color.WHITE);
                } else {
                    g2.setColor(CARD); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                    g2.setColor(BORDER_C); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10);
                    g2.setColor(TXT_S);
                }
                g2.setFont(FONT_BOLD); FontMetrics fm=g2.getFontMetrics();
                g2.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,
                    (getHeight()-fm.getHeight())/2+fm.getAscent());
                g2.dispose();
            }
        };
        btn.setFont(FONT_BOLD); btn.setOpaque(false); btn.setContentAreaFilled(false);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(160, 38));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton actionBtn(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover()?bg.darker():bg);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.setColor(Color.WHITE); g2.setFont(FONT_BOLD);
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,
                    (getHeight()-fm.getHeight())/2+fm.getAscent());
                g2.dispose();
            }
        };
        btn.setFont(FONT_BOLD); btn.setOpaque(false); btn.setContentAreaFilled(false);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(100, 34));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JSpinner dateSpinner(LocalDate d) {
        JSpinner sp = new JSpinner(new SpinnerDateModel(
            java.sql.Date.valueOf(d), null, null, java.util.Calendar.DAY_OF_MONTH));
        sp.setEditor(new JSpinner.DateEditor(sp, "dd/MM/yyyy"));
        sp.setFont(FONT_LABEL); sp.setPreferredSize(new Dimension(110, 32));
        return sp;
    }

    private void styleCombo(JComboBox<?> cb) {
        cb.setFont(FONT_LABEL); cb.setBackground(CARD);
        cb.setPreferredSize(new Dimension(130, 34));
    }

    private JLabel lbl(String text, Font f, Color c) {
        JLabel l = new JLabel(text); l.setFont(f); l.setForeground(c); return l;
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