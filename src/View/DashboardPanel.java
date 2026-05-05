package View;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.util.*;
import database.DatabaseHelper;
import javax.swing.table.DefaultTableModel;

public class DashboardPanel extends JPanel {

    private JPanel chartPanel;
    private JPanel tablePanel;

    public DashboardPanel() {
        setLayout(new BorderLayout(16,16));
        setBackground(new Color(245,247,250));
        setBorder(new EmptyBorder(20,20,20,20));

        add(createStatsPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
    }

    // =========================
    // 🔥 THỐNG KÊ
    // =========================
    private JPanel createStatsPanel() {
        JPanel p = new JPanel(new GridLayout(1,3,16,16));
        p.setOpaque(false);

        p.add(createCard("Hội viên", count("HoiVien"), new Color(52,152,219)));
        p.add(createCard("Hoạt động", count("HoatDong"), new Color(243,156,18)));
        p.add(createCard("Tham gia", count("ThamGia"), new Color(46,204,113)));

        return p;
    }

    private JPanel createCard(String title, int value, Color color) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(15,15,15,15));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(Color.GRAY);

        JLabel lblValue = new JLabel(String.valueOf(value));
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblValue.setForeground(color);

        p.add(lblTitle, BorderLayout.NORTH);
        p.add(lblValue, BorderLayout.CENTER);

        return p;
    }

    // =========================
    // 🔥 CENTER (CHART + TABLE)
    // =========================
    private JPanel createCenterPanel() {
        JPanel main = new JPanel(new BorderLayout(10,10));
        main.setOpaque(false);

        JComboBox<String> cb = new JComboBox<>(new String[]{"Biểu đồ", "Bảng"});
        main.add(cb, BorderLayout.NORTH);

        chartPanel = createChartPanel();
        tablePanel = createTablePanel();

        main.add(chartPanel, BorderLayout.CENTER);

        cb.addActionListener(e -> {
            main.remove(1);
            if (cb.getSelectedIndex() == 0) {
                main.add(chartPanel, BorderLayout.CENTER);
            } else {
                main.add(tablePanel, BorderLayout.CENTER);
            }
            main.revalidate();
            main.repaint();
        });

        return main;
    }

    // =========================
    // 📊 CHART PANEL
    // =========================
    private JPanel createChartPanel() {
        JPanel p = new JPanel(new GridLayout(1,2,16,16));
        p.setOpaque(false);

        p.add(createStatusChart());
        p.add(createActivityChart());

        return p;
    }

    // =========================
    // 🔥 BIỂU ĐỒ TRẠNG THÁI
    // =========================
    private JPanel createStatusChart() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(15,15,15,15));

        JTextArea txt = new JTextArea();
        txt.setEditable(false);

        try (Connection conn = DatabaseHelper.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(
                     "SELECT trangThai, COUNT(*) as sl FROM HoiVien GROUP BY trangThai")) {

            StringBuilder sb = new StringBuilder("Trạng thái hội viên:\n\n");
            while (rs.next()) {
                sb.append(rs.getString("trangThai"))
                  .append(": ")
                  .append(rs.getInt("sl"))
                  .append("\n");
            }
            txt.setText(sb.toString());

        } catch (Exception e) {
            txt.setText("Lỗi load dữ liệu");
        }

        p.add(txt);
        return p;
    }

    // =========================
    // 🔥 BIỂU ĐỒ HOẠT ĐỘNG
    // =========================
    private JPanel createActivityChart() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(15,15,15,15));

        JTextArea txt = new JTextArea();
        txt.setEditable(false);

        try (Connection conn = DatabaseHelper.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(
                     "SELECT MONTH(thoiGianBatDau) thang, COUNT(*) sl FROM HoatDong GROUP BY MONTH(thoiGianBatDau)")) {

            StringBuilder sb = new StringBuilder("Hoạt động theo tháng:\n\n");
            while (rs.next()) {
                sb.append("Tháng ")
                  .append(rs.getInt("thang"))
                  .append(": ")
                  .append(rs.getInt("sl"))
                  .append("\n");
            }
            txt.setText(sb.toString());

        } catch (Exception e) {
            txt.setText("Lỗi load dữ liệu");
        }

        p.add(txt);
        return p;
    }

    // =========================
    // 📋 TABLE PANEL
    // =========================
    private JPanel createTablePanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);

        String[] cols = {"Tên hoạt động", "Trạng thái"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);

        try (Connection conn = DatabaseHelper.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT tenHoatDong, trangThai FROM HoatDong")) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString(1),
                        rs.getString(2)
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        JTable table = new JTable(model);
        p.add(new JScrollPane(table));

        return p;
    }

    // =========================
    // 🔢 COUNT
    // =========================
    private int count(String table) {
        try (Connection conn = DatabaseHelper.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM " + table)) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }
}