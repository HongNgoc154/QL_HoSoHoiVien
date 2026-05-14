package View;

import Util.*;
import dao.NhatKyDAO;
import database.DatabaseHelper;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ArchiveForm extends JPanel {
    private JTabbedPane tabs;
    private JTextField txtSearch;
    private StyledTable table;
    private DefaultTableModel model;

    public ArchiveForm() {
        setLayout(new BorderLayout(0, 10));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(22, 26, 22, 26));
        add(UITheme.pageTitlePanel("Kho lưu trữ hệ thống", "Xem, tìm kiếm, khôi phục, xóa vĩnh viễn"), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        loadData();
    }

    private JPanel buildCenter() {
        JPanel p = new JPanel(new BorderLayout(0, 10)); p.setOpaque(false);
        JPanel bar = UITheme.cardPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        txtSearch = new JTextField(20);
        bar.add(UITheme.searchField(txtSearch, "Tìm theo mã hoặc tên..."));
        JButton btnSearch = UITheme.primaryButton("Tìm");
        JButton btnReset = UITheme.outlineButton("Đặt lại");
        tabs = new JTabbedPane();
        tabs.addTab("Hội viên", null); tabs.addTab("Hoạt động", null); tabs.addTab("Tham gia", null); tabs.addTab("Nhân viên", null);
        btnSearch.addActionListener(e -> loadData());
        btnReset.addActionListener(e -> {

            txtSearch.setText("");

            tabs.setSelectedIndex(0);

            loadData();
        });
        tabs.addChangeListener(e -> loadData());
        bar.add(btnSearch);
        bar.add(btnReset);
        p.add(bar, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"ID", "Thông tin 1", "Thông tin 2", "Trạng thái", "Thời gian lưu trữ"}, 0){public boolean isCellEditable(int r,int c){return false;}};
        table = new StyledTable(model);
        JPanel card = UITheme.cardPanel(new BorderLayout());
        JPanel head = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8)); head.setOpaque(false);
        JButton btnRestore = UITheme.primaryButton("Khôi phục");
        JButton btnDeleteForever = UITheme.dangerButton("Xóa vĩnh viễn");
        btnRestore.addActionListener(e -> restoreSelected());
        btnDeleteForever.addActionListener(e -> deleteForeverSelected());
        head.add(tabs); head.add(btnRestore); head.add(btnDeleteForever);
        card.add(head, BorderLayout.NORTH);
        card.add(UITheme.styledScrollPane(table), BorderLayout.CENTER);
        p.add(card, BorderLayout.CENTER);
        return p;
    }
    
    private void updateColumns() {

    String tab = tabType();

    if ("Hội viên".equals(tab)) {

        model.setColumnIdentifiers(new String[]{
            "ID",
            "Mã HV",
            "Họ tên",
            "Trạng thái",
            "Thời gian lưu trữ"
        });

    } else if ("Hoạt động".equals(tab)) {

        model.setColumnIdentifiers(new String[]{
            "ID",
            "Tên hoạt động",
            "Loại",
            "Trạng thái",
            "Thời gian lưu trữ"
        });

    } else if ("Tham gia".equals(tab)) {

        model.setColumnIdentifiers(new String[]{
            "ID",
            "Hội viên",
            "Hoạt động",
            "Trạng thái",
            "Thời gian lưu trữ"
        });

    } else {

        model.setColumnIdentifiers(new String[]{
            "ID",
            "Mã NV",
            "Họ tên",
            "Trạng thái",
            "Thời gian lưu trữ"
        });
    }
}

    private String tabType() {
        return new String[]{"Hội viên","Hoạt động","Tham gia","Nhân viên"}[tabs.getSelectedIndex()];
    }

    private void loadData() {
        updateColumns();
        model.setRowCount(0);
        String kw = txtSearch.getText().trim();
        String sql;
        String tab = tabType();
        if ("Hội viên".equals(tab)) {

            sql =
                "SELECT id, maHoiVien, tenHoiVien, " +
                "trangThai, thoiGianLuuTru " +
                "FROM HoiVien " +
                "WHERE daLuuTru=1 "
                + (kw.isEmpty()
                    ? ""
                    : " AND (maHoiVien LIKE ? OR tenHoiVien LIKE ?) ")
                + " ORDER BY thoiGianLuuTru DESC";

        }
        else if ("Hoạt động".equals(tab)) {

            sql =
                "SELECT id, tenHoatDong, loaiHoatDong, " +
                "trangThai, thoiGianLuuTru " +
                "FROM HoatDong " +
                "WHERE daLuuTru=1 "
                + (kw.isEmpty()
                    ? ""
                    : " AND (tenHoatDong LIKE ? OR loaiHoatDong LIKE ?) ")
                + " ORDER BY thoiGianLuuTru DESC";

        }
        else if ("Tham gia".equals(tab)) {

            sql =
                "SELECT tg.id, hv.tenHoiVien, hd.tenHoatDong, " +
                "tg.trangThai, tg.thoiGianLuuTru " +
                "FROM ThamGia tg " +
                "JOIN HoiVien hv ON tg.idHoiVien=hv.id " +
                "JOIN HoatDong hd ON tg.idHoatDong=hd.id " +
                "WHERE tg.daLuuTru=1 "
                + (kw.isEmpty()
                    ? ""
                    : " AND (hv.tenHoiVien LIKE ? OR hd.tenHoatDong LIKE ?) ")
                + " ORDER BY tg.thoiGianLuuTru DESC";

        }
        else {

            sql =
                "SELECT id, maNhanVien, tenNhanVien, " +
                "trangThai, thoiGianLuuTru " +
                "FROM NhanVien " +
                "WHERE daLuuTru=1 "
                + (kw.isEmpty()
                    ? ""
                    : " AND (maNhanVien LIKE ? OR tenNhanVien LIKE ?) ")
                + " ORDER BY thoiGianLuuTru DESC";
        }
        try (Connection c = DatabaseHelper.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (!kw.isEmpty()) { ps.setString(1, "%"+kw+"%"); ps.setString(2, "%"+kw+"%"); }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getTimestamp(5)});
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Lỗi tải kho lưu trữ: " + e.getMessage());}
    }

    private void restoreSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {

            JOptionPane.showMessageDialog(
                this,
                "Vui lòng chọn dữ liệu."
            );

            return;
        }
        String loai = tabType();
        int id = (int) model.getValueAt(row, 0);
        String sql;
        if ("Hội viên".equals(loai)) {

            sql =
                "UPDATE HoiVien " +
                "SET daLuuTru=0, thoiGianLuuTru=NULL " +
                "WHERE id=?";

        }
        else if ("Hoạt động".equals(loai)) {

            sql =
                "UPDATE HoatDong " +
                "SET daLuuTru=0, thoiGianLuuTru=NULL " +
                "WHERE id=?";

        }
        else if ("Tham gia".equals(loai)) {

            sql =
                "UPDATE ThamGia " +
                "SET daLuuTru=0, thoiGianLuuTru=NULL " +
                "WHERE id=?";

        }
        else {

            sql =
                "UPDATE NhanVien " +
                "SET daLuuTru=0, thoiGianLuuTru=NULL " +
                "WHERE id=?";
        }
        try (Connection c = DatabaseHelper.getConnection()) {
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
            NhatKyDAO.log(Session.getCurrentUserId(),"KHÔI PHỤC","Kho lưu trữ","Khôi phục dữ liệu "+loai+" từ kho lưu trữ");
            JOptionPane.showMessageDialog(this, "Khôi phục thành công.");
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Lỗi khôi phục: " + e.getMessage()); return; }
        loadData();
    }

    private void deleteForeverSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int id = (int) model.getValueAt(row, 0);
        String loai = tabType();
        int ok = JOptionPane.showConfirmDialog(this, "Dữ liệu sẽ không thể khôi phục sau khi xóa vĩnh viễn.", "Cảnh báo", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;
        String sql;

        if ("Hội viên".equals(loai)) {

            sql = "UPDATE HoiVien\n" +
                    "SET\n" +
                    "    daLuuTru = 1,\n" +
                    "    thoiGianLuuTru = GETDATE()\n" +
                    "WHERE id = ?";

        }
        else if ("Hoạt động".equals(loai)) {

            sql = "UPDATE HoatDong\n" +
                    "SET\n" +
                    "    daLuuTru = 1,\n" +
                    "    thoiGianLuuTru = GETDATE()\n" +
                    "WHERE id = ?";

        }
        else if ("Tham gia".equals(loai)) {

            sql = "UPDATE ThamGia\n" +
                    "SET\n" +
                    "    daLuuTru = 1,\n" +
                    "    thoiGianLuuTru = GETDATE()\n" +
                    "WHERE id = ?";

        }
        else {

            sql = "UPDATE NhanVien\n" +
                    "SET\n" +
                    "    daLuuTru = 1,\n" +
                    "    thoiGianLuuTru = GETDATE()\n" +
                    "WHERE id = ?";
        }
        try (Connection c = DatabaseHelper.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id); ps.executeUpdate();
            NhatKyDAO.log(Session.getCurrentUserId(),"XÓA VĨNH VIỄN","Kho lưu trữ","Xóa vĩnh viễn ID lưu trữ "+id);
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Lỗi: "+e.getMessage()); return; }
        loadData();
    }
}
