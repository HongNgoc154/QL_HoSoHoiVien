package View;

import database.DatabaseHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class NhatKyForm extends JPanel {  // ✅ Đổi từ JFrame → JPanel

    private JTable table;
    private DefaultTableModel model;
    private JSpinner fromDate;
    private JSpinner toDate;

    public NhatKyForm() {
        setLayout(new BorderLayout());
        setBackground(Color.decode("#F0F4F8"));
        setBorder(new EmptyBorder(24, 28, 24, 28));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));
        JLabel title = new JLabel("Nhật ký hệ thống");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.decode("#1E293B"));
        header.add(title, BorderLayout.WEST);
        JPanel filter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filter.setOpaque(false);
        fromDate = new JSpinner(new SpinnerDateModel());
        toDate = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor fromEditor = new JSpinner.DateEditor(fromDate, "yyyy-MM-dd");
        JSpinner.DateEditor toEditor = new JSpinner.DateEditor(toDate, "yyyy-MM-dd");
        fromDate.setEditor(fromEditor);
        toDate.setEditor(toEditor);
        JButton btnFilter = new JButton("Lọc");
        btnFilter.addActionListener(e -> loadData());
        filter.add(new JLabel("Từ ngày"));
        filter.add(fromDate);
        filter.add(new JLabel("Đến ngày"));
        filter.add(toDate);
        filter.add(btnFilter);
        header.add(filter, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Table
        model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[]{
                "ID", "Nhân viên", "Hành động", "Đối tượng", "Mô tả", "Thời gian"
        });

        table = new JTable(model) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(38);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(Color.decode("#E2E8F0"));
        table.setSelectionBackground(Color.decode("#CBDDE9"));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(Color.decode("#2872A1"));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Color.decode("#E2E8F0")));
        scroll.getViewport().setBackground(Color.WHITE);

        // Card wrapper
        JPanel card = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.setColor(Color.decode("#E2E8F0"));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        JPanel cardHeader = new JPanel(new BorderLayout());
        cardHeader.setOpaque(false);
        cardHeader.setBorder(new EmptyBorder(0, 0, 12, 0));
        JLabel cardTitle = new JLabel("Danh sách nhật ký");
        cardTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        cardTitle.setForeground(Color.decode("#1E293B"));
        cardHeader.add(cardTitle, BorderLayout.WEST);

        JButton btnRefresh = new JButton("↻ Làm mới");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefresh.setBackground(Color.decode("#2872A1"));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBorderPainted(false);
        btnRefresh.setOpaque(true);
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefresh.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        btnRefresh.addActionListener(e -> loadData());
        cardHeader.add(btnRefresh, BorderLayout.EAST);

        card.add(cardHeader, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);

        loadData();
    }

    private void loadData() {
        model.setRowCount(0);

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT nk.id, nv.tenNhanVien, nk.hanhDong, nk.doiTuong, nk.moTa, nk.thoiGian " +
                 "FROM NhatKyHeThong nk LEFT JOIN NhanVien nv ON nk.idNhanVien = nv.id " +
                 "WHERE CAST(nk.thoiGian AS DATE) BETWEEN ? AND ? " +
                 "ORDER BY nk.thoiGian DESC");
             ) {
            java.util.Date from = (java.util.Date) fromDate.getValue();
            java.util.Date to = (java.util.Date) toDate.getValue();
            ps.setDate(1, new java.sql.Date(from.getTime()));
            ps.setDate(2, new java.sql.Date(to.getTime()));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("tenNhanVien"),
                        rs.getString("hanhDong"),
                        rs.getString("doiTuong"),
                        rs.getString("moTa"),
                        rs.getTimestamp("thoiGian")
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}