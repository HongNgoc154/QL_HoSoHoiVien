package View;

import Util.UITheme;
import database.DatabaseHelper;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.sql.*;
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

public class DashboardPanel extends JPanel {

    // ─── Palette ────────────────────────────────────────────────────────────────
    private static final Color BG            = Color.decode("#F5F7FA");
    private static final Color CARD          = Color.WHITE;
    private static final Color BORDER_C      = Color.decode("#E8ECF1");
    private static final Color TXT_H         = Color.decode("#1A202C");
    private static final Color TXT_S         = Color.decode("#718096");
    private static final Color BLUE          = Color.decode("#4361EE");
    private static final Color YELLOW        = Color.decode("#F6C90E");
    private static final Color RED           = Color.decode("#EF4444");
    private static final Color GREEN         = Color.decode("#10B981");
    private static final Color ORANGE        = Color.decode("#F97316");

    private static final Font FONT_TITLE     = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_HEADING   = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font FONT_LABEL     = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_STAT      = new Font("Segoe UI", Font.BOLD, 36);
    private static final Font FONT_SMALL     = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FONT_BOLD      = new Font("Segoe UI", Font.BOLD, 13);

    // ─── State ───────────────────────────────────────────────────────────────────
    private JComboBox<String> cbPeriod;
    private JSpinner spFrom, spTo;
    private JPanel pnlStats, pnlCharts, pnlRanking, pnlTable;
    private CardLayout viewCard;
    private JPanel viewContainer;
    private DefaultTableModel tblModel;
    private JLabel lblSubtitle;

    // ─── Stat labels (for refresh) ───────────────────────────────────────────────
    private JLabel lblTotalVal, lblNewVal, lblLeftVal, lblActVal;
    private JLabel lblTotalTrend, lblNewTrend, lblLeftTrend, lblActTrend;

    // Chart panels
    private JPanel donutHolder, barHolder, hbarHolder;

    public DashboardPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG);

        // Scroll wrapper
        JPanel inner = new JPanel(new BorderLayout(0, 16));
        inner.setBackground(BG);
        inner.setBorder(new EmptyBorder(24, 28, 24, 28));

        inner.add(buildHeader(),  BorderLayout.NORTH);
        inner.add(buildBody(),    BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(inner);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(BG);
        scroll.getViewport().setBackground(BG);
        add(scroll, BorderLayout.CENTER);

        SwingUtilities.invokeLater(this::refresh);
    }

    // ══════════════════════════════════════════════════════════════════════════════
    //  HEADER
    // ══════════════════════════════════════════════════════════════════════════════
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(16, 0));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 20, 0));

        // Left: title block
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        JLabel title = new JLabel("Dashboard");
        title.setFont(FONT_TITLE);
        title.setForeground(TXT_H);
        lblSubtitle = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", new Locale("vi"))));
        lblSubtitle.setFont(FONT_LABEL);
        lblSubtitle.setForeground(TXT_S);
        left.add(title);
        left.add(Box.createVerticalStrut(2));
        left.add(lblSubtitle);

        // Right: filter card
        JPanel filter = card(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        filter.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(8, 14, 8, 14)
        ));

        cbPeriod = new JComboBox<>(new String[]{"Tuần này","Tháng này","Năm nay","Tùy chọn"});
        style(cbPeriod);

        LocalDate now = LocalDate.now();
        spFrom = dateSpinner(now.minusDays(6));
        spTo   = dateSpinner(now);

        JButton btnApply  = actionBtn("Áp dụng", BLUE);
        JButton btnExport = actionBtn("📥 Xuất", new Color(99,102,241));

        filter.add(new JLabel("Kỳ:") {{ setFont(FONT_LABEL); setForeground(TXT_S); }});
        filter.add(cbPeriod);
        filter.add(new JLabel("Từ") {{ setFont(FONT_SMALL); setForeground(TXT_S); }});
        filter.add(spFrom);
        filter.add(new JLabel("→") {{ setFont(FONT_SMALL); setForeground(TXT_S); }});
        filter.add(spTo);
        filter.add(btnApply);
        filter.add(btnExport);

        cbPeriod.addActionListener(e -> onPeriodChange());
        btnApply.addActionListener(e -> refresh());
        btnExport.addActionListener(e -> exportCSV());

        p.add(left,   BorderLayout.WEST);
        p.add(filter, BorderLayout.EAST);
        return p;
    }

    // ══════════════════════════════════════════════════════════════════════════════
    //  BODY
    // ══════════════════════════════════════════════════════════════════════════════
    private JPanel buildBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        // 1. Stat cards row
        pnlStats = new JPanel(new GridLayout(1, 4, 16, 0));
        pnlStats.setOpaque(false);
        pnlStats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        buildStatCards();
        body.add(pnlStats);
        body.add(Box.createVerticalStrut(20));

        // 2. View toggle + charts/table
        body.add(buildToggleRow());
        body.add(Box.createVerticalStrut(12));

        viewCard = new CardLayout();
        viewContainer = new JPanel(viewCard);
        viewContainer.setOpaque(false);
        viewContainer.add(buildChartView(), "charts");
        viewContainer.add(buildTableView(), "table");
        body.add(viewContainer);
        body.add(Box.createVerticalStrut(20));

        // 3. Ranking section
        body.add(buildRankingSection());

        return body;
    }

    // ─── Stat cards ─────────────────────────────────────────────────────────────
    private void buildStatCards() {
        pnlStats.removeAll();

        int total = count("SELECT COUNT(*) FROM HoiVien");
        int newM  = count("SELECT COUNT(*) FROM HoiVien WHERE ngayThamGia >= ?", fromDate());
        int left  = count("SELECT COUNT(*) FROM HoiVien WHERE trangThai LIKE N'%Rời%'");
        int acts  = count("SELECT COUNT(*) FROM HoatDong");

        pnlStats.add(statCard("👥 Tổng hội viên", total, "+12%", BLUE,   new Color(235,240,255)));
        pnlStats.add(statCard("✨ Hội viên mới",   newM,  "+8%",  GREEN,  new Color(209,250,229)));
        pnlStats.add(statCard("🚪 Đã rời",          left,  "-3%",  RED,    new Color(254,226,226)));
        pnlStats.add(statCard("📅 Hoạt động",        acts,  "+5%",  ORANGE, new Color(255,237,213)));

        pnlStats.revalidate();
        pnlStats.repaint();
    }

    private JPanel statCard(String label, int value, String trend, Color accent, Color bg) {
        JPanel p = new JPanel(new BorderLayout(0, 6)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),16,16);
                // Accent bar top
                g2.setColor(accent);
                g2.fillRoundRect(0,0,getWidth(),4,4,4);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C,1,true),
            new EmptyBorder(18,20,16,20)
        ));
        addShadow(p);

        // Hover effect
        p.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e){ p.setBorder(new CompoundBorder(new LineBorder(accent,2,true),new EmptyBorder(17,19,15,19))); p.repaint(); }
            public void mouseExited(MouseEvent e){ p.setBorder(new CompoundBorder(new LineBorder(BORDER_C,1,true),new EmptyBorder(18,20,16,20))); p.repaint(); }
        });

        // Icon + label
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(FONT_LABEL);
        lblLabel.setForeground(TXT_S);

        // Value
        JLabel lblVal = new JLabel(String.valueOf(value));
        lblVal.setFont(FONT_STAT);
        lblVal.setForeground(TXT_H);

        // Trend badge
        boolean positive = trend.startsWith("+");
        JLabel lblTrend = new JLabel(trend + " so với kỳ trước");
        lblTrend.setFont(FONT_SMALL);
        lblTrend.setForeground(positive ? GREEN : RED);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(lblVal, BorderLayout.WEST);
        bottom.add(lblTrend, BorderLayout.SOUTH);

        p.add(lblLabel, BorderLayout.NORTH);
        p.add(bottom,   BorderLayout.CENTER);
        return p;
    }

    // ─── Toggle row ─────────────────────────────────────────────────────────────
    private JPanel buildToggleRow() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabs.setOpaque(false);

        JToggleButton tbChart = toggleBtn("📊 Biểu đồ",   true);
        JToggleButton tbTable = toggleBtn("📋 Bảng số liệu", false);
        ButtonGroup bg = new ButtonGroup();
        bg.add(tbChart); bg.add(tbTable);

        tbChart.addActionListener(e -> viewCard.show(viewContainer, "charts"));
        tbTable.addActionListener(e -> viewCard.show(viewContainer, "table"));

        tabs.add(tbChart);
        tabs.add(Box.createHorizontalStrut(6));
        tabs.add(tbTable);
        p.add(tabs, BorderLayout.WEST);
        return p;
    }

    // ─── Chart view ─────────────────────────────────────────────────────────────
    private JPanel buildChartView() {
        JPanel p = new JPanel(new GridLayout(1, 2, 16, 16));
        p.setOpaque(false);

        donutHolder = chartCard("🧩 Cơ cấu hội viên");
        barHolder   = chartCard("📊 Hoạt động theo loại");

        p.add(donutHolder);
        p.add(barHolder);
        return p;
    }

    private JPanel chartCard(String title) {
        JPanel outer = new JPanel(new BorderLayout(0,10)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,16,16);
                g2.dispose();
            }
        };
        outer.setOpaque(false);
        outer.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C,1,true),
            new EmptyBorder(18,20,18,20)
        ));
        addShadow(outer);

        JLabel lbl = new JLabel(title);
        lbl.setFont(FONT_HEADING);
        lbl.setForeground(TXT_H);
        outer.add(lbl, BorderLayout.NORTH);
        outer.setPreferredSize(new Dimension(0, 300));
        return outer;
    }

    private void loadCharts() {
        // Donut chart
        DefaultPieDataset pieDs = new DefaultPieDataset();
        pieDs.setValue("Hoạt động", count("SELECT COUNT(*) FROM HoiVien WHERE trangThai=N'Hoạt động'"));
        pieDs.setValue("Mới",       count("SELECT COUNT(*) FROM HoiVien WHERE ngayThamGia >= ?", fromDate()));
        pieDs.setValue("Đã rời",    count("SELECT COUNT(*) FROM HoiVien WHERE trangThai LIKE N'%Rời%'"));

        JFreeChart pie = ChartFactory.createRingChart("", pieDs, true, true, false);
        pie.setBackgroundPaint(CARD);
        pie.getLegend().setBackgroundPaint(CARD);
        PiePlot pp = (PiePlot) pie.getPlot();
        pp.setBackgroundPaint(CARD);
        pp.setOutlineVisible(false);
        pp.setSectionPaint("Hoạt động", BLUE);
        pp.setSectionPaint("Mới",       GREEN);
        pp.setSectionPaint("Đã rời",    RED);
        pp.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {1} ({2})"));
        pp.setShadowPaint(null);
        pp.setInsets(new RectangleInsets(4,4,4,4));
        pp.setInteriorGap(0.06);

        ChartPanel cpPie = new ChartPanel(pie);
        cpPie.setOpaque(false);
        cpPie.setBorder(null);
        // Remove old chart if any
        Component[] comps = donutHolder.getComponents();
        for (Component c : comps) if (c instanceof ChartPanel) donutHolder.remove(c);
        donutHolder.add(cpPie, BorderLayout.CENTER);
        donutHolder.revalidate();

        // Bar chart
        DefaultCategoryDataset barDs = new DefaultCategoryDataset();
        String sql = "SELECT hd.loaiHoatDong, COUNT(DISTINCT hd.id) soHD, COUNT(tg.idHoiVien) soTG " +
                     "FROM HoatDong hd LEFT JOIN ThamGia tg ON hd.id=tg.idHoatDong GROUP BY hd.loaiHoatDong";
        try (Connection c = DatabaseHelper.getConnection();
             ResultSet rs = c.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                String loai = rs.getString("loaiHoatDong");
                if (loai == null) loai = "Khác";
                barDs.addValue(rs.getInt("soHD"), "Số hoạt động",   loai);
                barDs.addValue(rs.getInt("soTG"), "Người tham gia", loai);
            }
        } catch (Exception ignored) {}

        JFreeChart bar = ChartFactory.createBarChart("", "Loại", "Số lượng", barDs,
                PlotOrientation.VERTICAL, true, true, false);
        bar.setBackgroundPaint(CARD);
        CategoryPlot cp2 = bar.getCategoryPlot();
        cp2.setBackgroundPaint(CARD);
        cp2.setRangeGridlinePaint(BORDER_C);
        cp2.setOutlineVisible(false);
        BarRenderer br = (BarRenderer) cp2.getRenderer();
        br.setSeriesPaint(0, BLUE);
        br.setSeriesPaint(1, GREEN);
        br.setShadowVisible(false);
        br.setMaximumBarWidth(0.15);
        br.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());

        ChartPanel cpBar = new ChartPanel(bar);
        cpBar.setOpaque(false);
        Component[] comps2 = barHolder.getComponents();
        for (Component c2 : comps2) if (c2 instanceof ChartPanel) barHolder.remove(c2);
        barHolder.add(cpBar, BorderLayout.CENTER);
        barHolder.revalidate();
    }

    // ─── Table view ─────────────────────────────────────────────────────────────
    private JPanel buildTableView() {
        JPanel outer = new JPanel(new BorderLayout(0,12)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,16,16);
                g2.dispose();
            }
        };
        outer.setOpaque(false);
        outer.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C,1,true),
            new EmptyBorder(18,20,18,20)
        ));
        addShadow(outer);

        // Top bar
        JPanel top = new JPanel(new BorderLayout(10,0));
        top.setOpaque(false);
        JLabel lbl = new JLabel("Danh sách hội viên");
        lbl.setFont(FONT_HEADING);
        lbl.setForeground(TXT_H);

        JTextField txtSearch = new JTextField(20);
        txtSearch.setFont(FONT_LABEL);
        txtSearch.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C,1,true),
            new EmptyBorder(6,10,6,10)
        ));
        txtSearch.putClientProperty("JTextField.placeholderText", "🔍 Tìm kiếm...");

        top.add(lbl,       BorderLayout.WEST);
        top.add(txtSearch, BorderLayout.EAST);

        // Table
        String[] cols = {"#","Hội viên","Email","Trạng thái","Ngày tham gia","Số hoạt động"};
        tblModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tblModel);
        table.setFont(FONT_LABEL);
        table.setRowHeight(38);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(BORDER_C);
        table.setSelectionBackground(new Color(235,240,255));
        table.setSelectionForeground(TXT_H);
        table.getTableHeader().setFont(FONT_BOLD);
        table.getTableHeader().setBackground(new Color(248,249,252));
        table.getTableHeader().setForeground(TXT_S);
        table.getTableHeader().setPreferredSize(new Dimension(0, 42));
        table.getTableHeader().setBorder(new MatteBorder(0,0,1,0,BORDER_C));
        table.setAutoCreateRowSorter(true);

        // Status renderer
        table.getColumnModel().getColumn(3).setCellRenderer(new StatusRenderer());

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tblModel);
        table.setRowSorter(sorter);
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { doFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { doFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { doFilter(); }
            private void doFilter() {
                String k = txtSearch.getText().trim();
                sorter.setRowFilter(k.isEmpty() ? null : RowFilter.regexFilter("(?i)" + k));
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(BORDER_C,1,true));
        scroll.getViewport().setBackground(CARD);

        outer.add(top, BorderLayout.NORTH);
        outer.add(scroll, BorderLayout.CENTER);
        outer.setPreferredSize(new Dimension(0, 340));
        return outer;
    }

    private void loadTable() {
        if (tblModel == null) return;
        tblModel.setRowCount(0);
        String sql = "SELECT ROW_NUMBER() OVER(ORDER BY soLan DESC) rn, tenHoiVien, email, trangThai, " +
                     "CONVERT(varchar,ngayThamGia,103) ngayTG, soLan FROM (" +
                     "SELECT hv.tenHoiVien, hv.email, hv.trangThai, hv.ngayThamGia, " +
                     "COUNT(tg.idHoatDong) soLan FROM HoiVien hv " +
                     "LEFT JOIN ThamGia tg ON hv.id=tg.idHoiVien GROUP BY hv.tenHoiVien,hv.email,hv.trangThai,hv.ngayThamGia) x";
        try (Connection c = DatabaseHelper.getConnection();
             ResultSet rs = c.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                tblModel.addRow(new Object[]{
                    rs.getInt("rn"), rs.getString("tenHoiVien"),
                    rs.getString("email"),     rs.getString("trangThai"),
                    rs.getString("ngayTG"),    rs.getInt("soLan")
                });
            }
        } catch (Exception ignored) {}
    }

    // ─── Ranking section ─────────────────────────────────────────────────────────
    private JPanel buildRankingSection() {
        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setOpaque(false);

        row.add(buildTopMembers());
        row.add(buildRecentActivities());
        return row;
    }

    private JPanel buildTopMembers() {
        JPanel outer = sectionCard("🏆 Top 5 hội viên tích cực");

        String sql = "SELECT TOP 5 hv.tenHoiVien, COUNT(tg.idHoatDong) soLan " +
                     "FROM HoiVien hv JOIN ThamGia tg ON hv.id=tg.idHoiVien " +
                     "GROUP BY hv.tenHoiVien ORDER BY soLan DESC";
        try (Connection c = DatabaseHelper.getConnection();
             ResultSet rs = c.createStatement().executeQuery(sql)) {
            int rank = 1;
            Color[] medals = {new Color(255,215,0), new Color(192,192,192), new Color(205,127,50), TXT_S, TXT_S};
            while (rs.next()) {
                String name  = rs.getString("tenHoiVien");
                int    times = rs.getInt("soLan");
                int    maxT  = 50; // rough max for bar
                outer.add(buildRankRow(rank, name, times, maxT, medals[rank-1]));
                rank++;
            }
        } catch (Exception e) {
            outer.add(label("Chưa có dữ liệu", FONT_LABEL, TXT_S));
        }

        // "Xem thêm" button
        JButton btnMore = new JButton("Xem đầy đủ →");
        btnMore.setFont(FONT_SMALL);
        btnMore.setForeground(BLUE);
        btnMore.setBackground(new Color(235,240,255));
        btnMore.setBorder(new EmptyBorder(6,12,6,12));
        btnMore.setFocusPainted(false);
        btnMore.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnMore.addMouseListener(new MouseAdapter(){
            public void mouseEntered(MouseEvent e){ btnMore.setBackground(new Color(220,230,255)); }
            public void mouseExited(MouseEvent e){ btnMore.setBackground(new Color(235,240,255)); }
        });

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

        // Rank badge
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
                g2.drawString(t, (28-fm.stringWidth(t))/2, (28-fm.getHeight())/2+fm.getAscent());
                g2.dispose();
            }
        };
        rankLbl.setPreferredSize(new Dimension(28, 28));
        rankLbl.setOpaque(false);

        // Name + bar
        JPanel mid = new JPanel(new BorderLayout(0, 4));
        mid.setOpaque(false);

        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(FONT_BOLD);
        nameLbl.setForeground(TXT_H);

        // Progress bar
        int pct = max > 0 ? Math.min(100, value * 100 / max) : 0;
        JPanel bar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(230,234,255));
                g2.fillRoundRect(0, 4, getWidth(), 8, 8, 8);
                g2.setColor(rank==1?YELLOW : rank==2?new Color(156,163,175) : rank==3?ORANGE : BLUE);
                g2.fillRoundRect(0, 4, Math.max(8, getWidth()*pct/100), 8, 8, 8);
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0, 16));

        mid.add(nameLbl, BorderLayout.NORTH);
        mid.add(bar,     BorderLayout.CENTER);

        // Count
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

    private JPanel buildRecentActivities() {
        JPanel outer = sectionCard("📅 Hoạt động gần đây");

        String sql = "SELECT TOP 6 tenHoatDong, loaiHoatDong, thoiGianBatDau, trangThai " +
                     "FROM HoatDong ORDER BY thoiGianBatDau DESC";
        try (Connection c = DatabaseHelper.getConnection();
             ResultSet rs = c.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                outer.add(activityRow(
                    rs.getString("tenHoatDong"),
                    rs.getString("loaiHoatDong"),
                    rs.getString("thoiGianBatDau"),
                    rs.getString("trangThai")
                ));
            }
        } catch (Exception e) {
            outer.add(label("Chưa có dữ liệu", FONT_LABEL, TXT_S));
        }
        return outer;
    }

    private JPanel activityRow(String ten, String loai, String time, String status) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setBorder(new CompoundBorder(
            new MatteBorder(0,0,1,0,BORDER_C),
            new EmptyBorder(10,0,10,0)
        ));

        // Color dot based on status
        Color dotColor = "Đang diễn ra".equals(status) ? GREEN
                        : "Sắp diễn ra".equals(status) ? YELLOW : TXT_S;
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(dotColor);
                g2.fillOval(2,6,10,10);
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

        JLabel metaLbl = new JLabel((loai != null ? loai : "") + (time != null ? "  •  " + time.substring(0, Math.min(10, time.length())) : ""));
        metaLbl.setFont(FONT_SMALL);
        metaLbl.setForeground(TXT_S);

        info.add(nameLbl, BorderLayout.NORTH);
        info.add(metaLbl, BorderLayout.SOUTH);

        // Status badge
        JLabel badge = new JLabel(status != null ? status : "—");
        badge.setFont(FONT_SMALL);
        badge.setForeground(dotColor);
        badge.setOpaque(true);
        badge.setBackground(dotColor.equals(GREEN) ? new Color(209,250,229)
                           : dotColor.equals(YELLOW) ? new Color(254,243,199)
                           : new Color(243,244,246));
        badge.setBorder(new EmptyBorder(3,8,3,8));
        badge.setPreferredSize(new Dimension(100, 24));
        badge.setHorizontalAlignment(SwingConstants.CENTER);

        row.add(dot,   BorderLayout.WEST);
        row.add(info,  BorderLayout.CENTER);
        row.add(badge, BorderLayout.EAST);
        return row;
    }

    private JPanel sectionCard(String title) {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,16,16);
                g2.dispose();
            }
        };
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C,1,true),
            new EmptyBorder(20,20,16,20)
        ));
        addShadow(p);
        p.setPreferredSize(new Dimension(0, 340));

        JLabel lbl = new JLabel(title);
        lbl.setFont(FONT_HEADING);
        lbl.setForeground(TXT_H);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        p.add(lbl);
        p.add(Box.createVerticalStrut(14));
        return p;
    }

    // ══════════════════════════════════════════════════════════════════════════════
    //  REFRESH
    // ══════════════════════════════════════════════════════════════════════════════
    private void refresh() {
        buildStatCards();
        loadCharts();
        loadTable();
        // rebuild ranking section is done lazily via initial build; for simplicity rebuild
        SwingUtilities.invokeLater(() -> {
            revalidate();
            repaint();
        });
    }

    // ══════════════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════════════════════
    private JPanel card(LayoutManager lm) {
        JPanel p = new JPanel(lm) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,12,12);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        return p;
    }

    private void addShadow(JComponent c) {
        // Swing doesn't support real shadows; simulate via border offset
        // We rely on card background vs BG contrast
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
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2,
                    (getHeight()-fm.getHeight())/2+fm.getAscent());
                g2.dispose();
            }
        };
        btn.setFont(FONT_BOLD);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(140, 38));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton actionBtn(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.setColor(Color.WHITE);
                g2.setFont(FONT_BOLD);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2,
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

    private void style(JComboBox<?> cb) {
        cb.setFont(FONT_LABEL);
        cb.setBackground(CARD);
        cb.setBorder(new LineBorder(BORDER_C,1,true));
        cb.setPreferredSize(new Dimension(130, 34));
    }

    private JLabel label(String text, Font f, Color c) {
        JLabel l = new JLabel(text); l.setFont(f); l.setForeground(c); return l;
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
        ((SpinnerDateModel)spFrom.getModel()).setValue(java.sql.Date.valueOf(from));
        ((SpinnerDateModel)spTo.getModel()).setValue(java.sql.Date.valueOf(now));
        boolean custom = idx == 3;
        spFrom.setEnabled(custom);
        spTo.setEnabled(custom);
    }

    private java.sql.Date fromDate() {
        return new java.sql.Date(((java.util.Date)spFrom.getValue()).getTime());
    }

    private void exportCSV() {
        if (tblModel == null) return;
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("Dashboard_" +
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try (java.io.PrintWriter pw = new java.io.PrintWriter(
            new java.io.OutputStreamWriter(new java.io.FileOutputStream(fc.getSelectedFile()), "UTF-8"))) {
            pw.write('\uFEFF');
            StringBuilder sb = new StringBuilder();
            for (int i=0;i<tblModel.getColumnCount();i++){
                if(i>0)sb.append(",");
                sb.append("\"").append(tblModel.getColumnName(i)).append("\"");
            }
            pw.println(sb);
            for (int r=0;r<tblModel.getRowCount();r++){
                sb = new StringBuilder();
                for(int c2=0;c2<tblModel.getColumnCount();c2++){
                    if(c2>0)sb.append(",");
                    Object v=tblModel.getValueAt(r,c2);
                    sb.append("\"").append(v==null?"":v.toString().replace("\"","\"\"")).append("\"");
                }
                pw.println(sb);
            }
            JOptionPane.showMessageDialog(this,"Xuất thành công!\n"+fc.getSelectedFile());
        } catch(Exception e){ JOptionPane.showMessageDialog(this,"Lỗi: "+e.getMessage()); }
    }

    private int count(String sql, Object... params) {
        try (Connection c = DatabaseHelper.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i=0;i<params.length;i++) ps.setObject(i+1,params[i]);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        } catch (Exception ignored) {}
        return 0;
    }

    // Status badge renderer
    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
            JTable t, Object val, boolean sel, boolean focus, int row, int col) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t,val,sel,focus,row,col);
            String s = val == null ? "" : val.toString();
            lbl.setHorizontalAlignment(CENTER);
            lbl.setOpaque(true);
            switch (s) {
                case "Hoạt động" -> { lbl.setBackground(new Color(209,250,229)); lbl.setForeground(new Color(6,95,70)); }
                case "Tạm dừng"  -> { lbl.setBackground(new Color(254,243,199)); lbl.setForeground(new Color(146,64,14)); }
                default          -> { lbl.setBackground(new Color(243,244,246)); lbl.setForeground(new Color(75,85,99)); }
            }
            lbl.setBorder(new EmptyBorder(4,10,4,10));
            return lbl;
        }
    }
}