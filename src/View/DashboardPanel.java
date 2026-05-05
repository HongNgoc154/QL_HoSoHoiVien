package View;

import Util.UITheme;
import database.DatabaseHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

public class DashboardPanel extends JPanel {

    private JPanel statsRow;
    private JPanel chartsRow;
    private JComboBox<String> cbNam;
    private JComboBox<String> cbThang;

    // Stats
    private int totalHoiVien = 0, totalHoatDong = 0;
    private int hoiVienMoi = 0, hoiVienNghi = 0;

    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        // Header row
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Tổng quan hệ thống");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);

        // Filter row
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterRow.setOpaque(false);

        cbNam = new JComboBox<>(new String[]{"Tất cả", "2024", "2025", "2026"});
        cbThang = new JComboBox<>(new String[]{"Tất cả tháng", "T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12"});
        styleCombo(cbNam);
        styleCombo(cbThang);

        JButton btnFilter = UITheme.primaryButton("Lọc");
        JButton btnExport = UITheme.outlineButton("📊 Xuất báo cáo");

        filterRow.add(new JLabel("Năm:"));
        filterRow.add(cbNam);
        filterRow.add(new JLabel("Tháng:"));
        filterRow.add(cbThang);
        filterRow.add(btnFilter);
        filterRow.add(btnExport);

        header.add(title, BorderLayout.WEST);
        header.add(filterRow, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Stats cards
        statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        statsRow.setOpaque(false);
        statsRow.setBorder(new EmptyBorder(20, 0, 20, 0));

        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setOpaque(false);
        mainContent.add(statsRow, BorderLayout.NORTH);

        // Charts area
        chartsRow = new JPanel(new GridLayout(1, 2, 16, 0));
        chartsRow.setOpaque(false);
        chartsRow.setPreferredSize(new Dimension(0, 340));
        mainContent.add(chartsRow, BorderLayout.CENTER);

        // Bottom row - recent table
        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 16, 0));
        bottomRow.setOpaque(false);
        bottomRow.setBorder(new EmptyBorder(16, 0, 0, 0));

        add(mainContent, BorderLayout.CENTER);
        add(bottomRow, BorderLayout.SOUTH);

        loadData();

        btnFilter.addActionListener(e -> loadData());
        btnExport.addActionListener(e -> exportReport());
    }

    private void loadData() {
        try (Connection conn = DatabaseHelper.getConnection()) {
            // Total members
            ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM HoiVien");
            if (rs.next()) totalHoiVien = rs.getInt(1);

            // Total activities
            rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM HoatDong");
            if (rs.next()) totalHoatDong = rs.getInt(1);

            // New members (this month)
            rs = conn.createStatement().executeQuery(
                "SELECT COUNT(*) FROM HoiVien WHERE MONTH(ngayThamGia)=MONTH(GETDATE()) AND YEAR(ngayThamGia)=YEAR(GETDATE())"
            );
            if (rs.next()) hoiVienMoi = rs.getInt(1);

            // Inactive
            rs = conn.createStatement().executeQuery(
                "SELECT COUNT(*) FROM HoiVien WHERE trangThai != N'Hoạt động'"
            );
            if (rs.next()) hoiVienNghi = rs.getInt(1);

        } catch (Exception e) {
            System.out.println("Dashboard load error: " + e.getMessage());
        }

        refreshStats();
        refreshCharts();
    }

    private void refreshStats() {
        statsRow.removeAll();

        statsRow.add(createStatCard("👥 Tổng hội viên", String.valueOf(totalHoiVien),
            "Tất cả hội viên", UITheme.PRIMARY, Color.decode("#EBF4FA")));
        statsRow.add(createStatCard("📅 Hoạt động", String.valueOf(totalHoatDong),
            "Tổng hoạt động", Color.decode("#7C3AED"), Color.decode("#F3EEFF")));
        statsRow.add(createStatCard("🆕 Hội viên mới", String.valueOf(hoiVienMoi),
            "Tháng này", UITheme.SUCCESS, Color.decode("#ECFDF5")));
        statsRow.add(createStatCard("🚫 Đã rời", String.valueOf(hoiVienNghi),
            "Không hoạt động", UITheme.DANGER, Color.decode("#FEF2F2")));

        statsRow.revalidate();
        statsRow.repaint();
    }

    private JPanel createStatCard(String title, String value, String sub, Color accent, Color bg) {
        JPanel card = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
                g2.setColor(UITheme.BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
                // Top accent bar
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, getWidth()-1, 5, 16, 16);
                g2.fillRect(0, 3, getWidth()-1, 2);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_SMALL);
        lblTitle.setForeground(UITheme.TEXT_SECONDARY);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 38));
        lblValue.setForeground(accent);

        JLabel lblSub = new JLabel(sub);
        lblSub.setFont(UITheme.FONT_SMALL);
        lblSub.setForeground(UITheme.TEXT_MUTED);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(lblSub, BorderLayout.WEST);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    private void refreshCharts() {
        chartsRow.removeAll();

        // Chart 1: Hội viên theo tháng (bar chart)
        Map<String, Integer> monthData = new LinkedHashMap<>();
        String[] months = {"T1","T2","T3","T4","T5","T6","T7","T8","T9","T10","T11","T12"};
        for (String m : months) monthData.put(m, 0);

        try (Connection conn = DatabaseHelper.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT MONTH(ngayThamGia) as thang, COUNT(*) as sl FROM HoiVien " +
                "WHERE YEAR(ngayThamGia)=YEAR(GETDATE()) GROUP BY MONTH(ngayThamGia)"
            );
            while (rs.next()) {
                int m = rs.getInt("thang");
                if (m >= 1 && m <= 12) monthData.put(months[m-1], rs.getInt("sl"));
            }
        } catch (Exception e) {}

        chartsRow.add(createBarChart("Hội viên theo tháng", monthData, UITheme.PRIMARY));

        // Chart 2: Hoạt động theo loại (horizontal bar)
        Map<String, Integer> typeData = new LinkedHashMap<>();
        try (Connection conn = DatabaseHelper.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT loaiHoatDong, COUNT(*) as sl FROM HoatDong GROUP BY loaiHoatDong"
            );
            while (rs.next()) {
                String type = rs.getString("loaiHoatDong");
                if (type == null) type = "Khác";
                typeData.put(type, rs.getInt("sl"));
            }
        } catch (Exception e) {}

        chartsRow.add(createHorizBarChart("Hoạt động theo loại", typeData, Color.decode("#7C3AED")));

        chartsRow.revalidate();
        chartsRow.repaint();
    }

    private JPanel createBarChart(String title, Map<String, Integer> data, Color color) {
        JPanel card = createChartCard(title);

        int maxVal = data.values().stream().mapToInt(v -> v).max().orElse(1);
        if (maxVal == 0) maxVal = 1;
        final int maxChartValue = maxVal;

        JPanel chart = new JPanel() {
            final Map<String, Integer> chartData = data;
            final int max = maxChartValue;
            final Color barColor = color;

            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int n = chartData.size();
                int w = getWidth(), h = getHeight();
                int padL = 30, padR = 10, padT = 10, padB = 36;
                int chartW = w - padL - padR;
                int chartH = h - padT - padB;
                int barW = Math.max(4, chartW / n - 8);

                // Grid lines
                g2.setColor(UITheme.BORDER_COLOR);
                for (int i = 0; i <= 4; i++) {
                    int y = padT + chartH - (chartH * i / 4);
                    g2.drawLine(padL, y, w - padR, y);
                    g2.setFont(UITheme.FONT_SMALL);
                    g2.setColor(UITheme.TEXT_MUTED);
                    g2.drawString(String.valueOf(max * i / 4), 0, y + 4);
                    g2.setColor(UITheme.BORDER_COLOR);
                }

                int x = padL + 4;
                int idx = 0;
                for (Map.Entry<String, Integer> e : chartData.entrySet()) {
                    int barH = (int) ((double) e.getValue() / max * chartH);
                    int barX = x + idx * (chartW / n);
                    int barY = padT + chartH - barH;

                    // Bar with gradient
                    GradientPaint gp = new GradientPaint(barX, barY, barColor,
                        barX, padT + chartH, barColor.brighter());
                    g2.setPaint(gp);
                    g2.fillRoundRect(barX, barY, barW, barH, 4, 4);

                    // Value on top
                    if (e.getValue() > 0) {
                        g2.setColor(UITheme.TEXT_PRIMARY);
                        g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                        String val = String.valueOf(e.getValue());
                        g2.drawString(val, barX + (barW - g2.getFontMetrics().stringWidth(val))/2, barY - 3);
                    }

                    // Label
                    g2.setColor(UITheme.TEXT_SECONDARY);
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                    String lbl = e.getKey();
                    int lblW = g2.getFontMetrics().stringWidth(lbl);
                    g2.drawString(lbl, barX + (barW - lblW)/2, h - padB + 14);

                    idx++;
                }
            }
        };
        chart.setOpaque(false);

        ((BorderLayout) card.getLayout()).addLayoutComponent(chart, BorderLayout.CENTER);
        card.add(chart, BorderLayout.CENTER);
        return card;
    }

    private JPanel createHorizBarChart(String title, Map<String, Integer> data, Color color) {
        JPanel card = createChartCard(title);

        int maxVal = data.values().stream().mapToInt(v -> v).max().orElse(1);

        JPanel chart = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int n = data.size();
                if (n == 0) return;
                int w = getWidth(), h = getHeight();
                int padL = 100, padR = 40, padT = 20, padB = 20;
                int chartW = w - padL - padR;
                int rowH = (h - padT - padB) / n;
                int barH = Math.max(4, rowH - 16);

                int idx = 0;
                Color[] colors = {color, UITheme.PRIMARY, UITheme.SUCCESS, UITheme.WARNING, UITheme.ACCENT};
                for (Map.Entry<String, Integer> e : data.entrySet()) {
                    int barW = maxVal == 0 ? 0 : (int)((double)e.getValue() / maxVal * chartW);
                    int y = padT + idx * rowH + (rowH - barH) / 2;

                    // Label
                    g2.setColor(UITheme.TEXT_SECONDARY);
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    String lbl = e.getKey();
                    if (lbl.length() > 14) lbl = lbl.substring(0, 12) + "..";
                    g2.drawString(lbl, padL - g2.getFontMetrics().stringWidth(lbl) - 6, y + barH/2 + 4);

                    // Background bar
                    g2.setColor(new Color(colors[idx % colors.length].getRed(),
                        colors[idx % colors.length].getGreen(),
                        colors[idx % colors.length].getBlue(), 30));
                    g2.fillRoundRect(padL, y, chartW, barH, 6, 6);

                    // Value bar
                    GradientPaint gp = new GradientPaint(padL, y, colors[idx % colors.length],
                        padL + barW, y, colors[idx % colors.length].brighter());
                    g2.setPaint(gp);
                    if (barW > 0) g2.fillRoundRect(padL, y, barW, barH, 6, 6);

                    // Count
                    g2.setColor(UITheme.TEXT_PRIMARY);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    g2.drawString(String.valueOf(e.getValue()), padL + barW + 6, y + barH/2 + 4);

                    idx++;
                }
            }
        };
        chart.setOpaque(false);
        card.add(chart, BorderLayout.CENTER);
        return card;
    }

    private JPanel createChartCard(String title) {
        JPanel card = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
                g2.setColor(UITheme.BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel lbl = new JLabel(title);
        lbl.setFont(UITheme.FONT_HEADING);
        lbl.setForeground(UITheme.TEXT_PRIMARY);
        lbl.setBorder(new EmptyBorder(0, 0, 12, 0));
        card.add(lbl, BorderLayout.NORTH);
        return card;
    }

    private void styleCombo(JComboBox<String> cb) {
        cb.setFont(UITheme.FONT_LABEL);
        cb.setBackground(Color.WHITE);
        cb.setPreferredSize(new Dimension(120, 32));
    }

    private void exportReport() {
        JOptionPane.showMessageDialog(this,
            "Tính năng xuất báo cáo Excel đang được phát triển.\nVui lòng sử dụng chức năng xuất trong từng màn hình.",
            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }
}