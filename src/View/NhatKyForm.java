package View;

import Util.UITheme;
import Util.StyledTable;
import database.DatabaseHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/**
 * NhatKyForm – Nhật ký hệ thống.
 * Giao diện đồng nhất với các form khác: màu chủ đạo #1359B9 / #9FE4FB.
 */
public class NhatKyForm extends JPanel {

    private StyledTable table;
    private DefaultTableModel model;
    private JSpinner fromDate;
    private JSpinner toDate;

    public NhatKyForm() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(22, 26, 22, 26));
        buildUI();
        loadData();
    }

    private void buildUI() {
        // ── PAGE HEADER ───────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 14, 0));
        header.add(UITheme.pageTitlePanel("Nhật ký hệ thống",
                "Lịch sử thao tác và hoạt động của nhân viên"), BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // ── CENTER ────────────────────────────────────────────────────────
        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);

        // Filter card
        JPanel filterCard = UITheme.cardPanel(new BorderLayout());
        filterCard.setBorder(new EmptyBorder(10, 14, 10, 14));
        filterCard.add(buildFilterBar(), BorderLayout.CENTER);
        center.add(filterCard, BorderLayout.NORTH);

        // Table card
        JPanel tableCard = UITheme.cardPanel(new BorderLayout());
        tableCard.add(buildTableHeader(), BorderLayout.NORTH);
        tableCard.add(UITheme.styledScrollPane(buildTable()), BorderLayout.CENTER);
        center.add(tableCard, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);
    }

    // ── FILTER BAR ────────────────────────────────────────────────────────
    private JPanel buildFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.setOpaque(false);

        JLabel lblFrom = new JLabel("Từ ngày:");
        lblFrom.setFont(UITheme.FONT_BOLD);
        lblFrom.setForeground(UITheme.TEXT_SECONDARY);

        fromDate = makeSpinner();
        toDate   = makeSpinner();

        JLabel lblTo = new JLabel("Đến ngày:");
        lblTo.setFont(UITheme.FONT_BOLD);
        lblTo.setForeground(UITheme.TEXT_SECONDARY);

        JButton btnFilter = UITheme.primaryButton("  🔍  Lọc");
        JButton btnReset  = UITheme.outlineButton("  ↺  Đặt lại");
        btnFilter.setPreferredSize(new Dimension(110, 34));
        btnReset .setPreferredSize(new Dimension(110, 34));
        btnFilter.addActionListener(e -> loadData());
        btnReset .addActionListener(e -> {
            fromDate.setValue(new java.util.Date(0));
            toDate.setValue(new java.util.Date());
            loadData();
        });

        bar.add(lblFrom);
        bar.add(fromDate);
        bar.add(lblTo);
        bar.add(toDate);
        bar.add(btnFilter);
        bar.add(btnReset);
        return bar;
    }

    private JSpinner makeSpinner() {
        JSpinner sp = new JSpinner(new SpinnerDateModel());
        sp.setEditor(new JSpinner.DateEditor(sp, "dd/MM/yyyy"));
        sp.setFont(UITheme.FONT_LABEL);
        sp.setPreferredSize(new Dimension(130, 34));
        ((JSpinner.DefaultEditor) sp.getEditor()).getTextField().setFont(UITheme.FONT_LABEL);
        return sp;
    }

    // ── TABLE CARD HEADER ─────────────────────────────────────────────────
    private JPanel buildTableHeader() {
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

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        JLabel title = new JLabel("Danh sách nhật ký");
        title.setFont(UITheme.FONT_BOLD);
        title.setForeground(UITheme.TEXT_PRIMARY);
        left.add(title);
        p.add(left, BorderLayout.WEST);

        JButton btnExport = UITheme.outlineButton("📥  Xuất Excel");
        btnExport.setPreferredSize(new Dimension(130, 32));
        p.add(btnExport, BorderLayout.EAST);
        return p;
    }

    // ── TABLE ────────────────────────────────────────────────────────────
    private StyledTable buildTable() {
        model = new DefaultTableModel(
            new String[]{"ID", "Nhân viên", "Hành động", "Đối tượng", "Mô tả", "Thời gian"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new StyledTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Column widths
        int[] widths = {50, 130, 90, 100, 260, 130};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Hành động column – custom renderer with badge colors
        table.getColumnModel().getColumn(2).setCellRenderer(
            new javax.swing.table.DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                    JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                    String txt = v != null ? v.toString() : "";
                    String bg = "#f1f5f9", fg = "#475569";
                    if (txt.contains("THÊM") || txt.contains("Thêm")) { bg="#d1fae5"; fg="#065f46"; }
                    else if (txt.contains("SỬA") || txt.contains("Sửa") || txt.contains("CẬP NHẬT")) { bg="#fef3c7"; fg="#92400e"; }
                    else if (txt.contains("XÓA") || txt.contains("Xóa")) { bg="#fee2e2"; fg="#991b1b"; }
                    else if (txt.contains("TẠO") || txt.contains("Tạo") || txt.contains("ĐĂNG NHẬP")) { bg="#dbeafe"; fg="#1e40af"; }
                    lbl.setText("<html><span style='padding:3px 10px;border-radius:20px;"
                            + "font-weight:700;font-size:11px;background:" + bg + ";color:" + fg + "'>"
                            + txt + "</span></html>");
                    lbl.setHorizontalAlignment(SwingConstants.LEFT);
                    lbl.setOpaque(true);
                    lbl.setBackground(sel ? Color.decode("#D4E6FF")
                                    : row % 2 == 0 ? Color.WHITE : Color.decode("#F8FBFF"));
                    lbl.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                    return lbl;
                }
            });
        return table;
    }

    // ── LOAD DATA ────────────────────────────────────────────────────────
    public void loadData() {
        model.setRowCount(0);
        String sql = "SELECT nk.id, nv.tenNhanVien, nk.hanhDong, nk.doiTuong, nk.moTa, nk.thoiGian "
                   + "FROM NhatKyHeThong nk "
                   + "LEFT JOIN NhanVien nv ON nk.idNhanVien = nv.id "
                   + "ORDER BY nk.id DESC";
        try (Connection conn = DatabaseHelper.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(sql)) {
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
            JOptionPane.showMessageDialog(this, "Lỗi tải nhật ký: " + e.getMessage());
        }
    }
}