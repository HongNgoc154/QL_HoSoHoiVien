package dao;

import database.DatabaseHelper;
import model.HoiVien;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HoiVienDAO {

    // ===== TẠO MÃ HỘI VIÊN TỰ ĐỘNG: HV + id =====
    public String generateMaHoiVien() {
        String sql = "SELECT ISNULL(MAX(id), 0) + 1 AS nextId FROM HoiVien";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return String.format("HV%03d", rs.getInt("nextId"));
            }
        } catch (Exception e) {
            System.out.println("Lỗi generateMaHoiVien: " + e.getMessage());
        }
        return "HV001";
    }

    // ===== LẤY DANH SÁCH =====
    public List<HoiVien> getAll() {
        List<HoiVien> list = new ArrayList<>();
        String sql = "SELECT * FROM HoiVien ORDER BY id DESC";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            System.out.println("Lỗi getAll HoiVien: " + e.getMessage());
        }
        return list;
    }

    // ===== THÊM =====
    public boolean insert(HoiVien hv) {
        String sql = "INSERT INTO HoiVien(maHoiVien, tenHoiVien, ngaySinh, gioiTinh, sdt, email, diaChi, hinhAnh, trangThai) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hv.getMaHoiVien());
            ps.setString(2, hv.getTenHoiVien());
            setDateOrNull(ps, 3, hv.getNgaySinh());
            ps.setString(4, hv.getGioiTinh());
            ps.setString(5, hv.getSdt());
            ps.setString(6, hv.getEmail());
            ps.setString(7, hv.getDiaChi());
            ps.setString(8, hv.getHinhAnh());
            ps.setString(9, hv.getTrangThai() != null ? hv.getTrangThai() : "Hoạt động");
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Lỗi insert HoiVien: " + e.getMessage());
            return false;
        }
    }

    // ===== SỬA =====
    public boolean update(HoiVien hv) {
        String sql = "UPDATE HoiVien SET tenHoiVien=?, ngaySinh=?, gioiTinh=?, sdt=?, email=?, diaChi=?, hinhAnh=?, trangThai=? WHERE id=?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hv.getTenHoiVien());
            setDateOrNull(ps, 2, hv.getNgaySinh());
            ps.setString(3, hv.getGioiTinh());
            ps.setString(4, hv.getSdt());
            ps.setString(5, hv.getEmail());
            ps.setString(6, hv.getDiaChi());
            ps.setString(7, hv.getHinhAnh());
            ps.setString(8, hv.getTrangThai());
            ps.setInt(9, hv.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Lỗi update HoiVien: " + e.getMessage());
            return false;
        }
    }

    // ===== XÓA =====
    public boolean delete(int id) {
        String sql = "DELETE FROM HoiVien WHERE id=?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Lỗi delete HoiVien: " + e.getMessage());
            return false;
        }
    }

    // ===== TÌM KIẾM =====
    public List<HoiVien> search(String keyword) {
        List<HoiVien> list = new ArrayList<>();
        String sql = "SELECT * FROM HoiVien WHERE tenHoiVien LIKE ? OR maHoiVien LIKE ? ORDER BY id DESC";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            System.out.println("Lỗi search HoiVien: " + e.getMessage());
        }
        return list;
    }

    // ===== MAP ROW =====
    private HoiVien mapRow(ResultSet rs) throws SQLException {
        HoiVien hv = new HoiVien();
        hv.setId(rs.getInt("id"));
        hv.setMaHoiVien(rs.getString("maHoiVien"));
        hv.setTenHoiVien(rs.getString("tenHoiVien"));
        Date ngaySinh = rs.getDate("ngaySinh");
        hv.setNgaySinh(ngaySinh != null ? ngaySinh.toString() : "");
        hv.setGioiTinh(rs.getString("gioiTinh"));
        hv.setSdt(rs.getString("sdt"));
        hv.setEmail(rs.getString("email"));
        hv.setDiaChi(rs.getString("diaChi"));
        hv.setHinhAnh(rs.getString("hinhAnh"));
        hv.setTrangThai(rs.getString("trangThai"));
        Date ngayThamGia = rs.getDate("ngayThamGia");
        hv.setNgayThamGia(ngayThamGia != null ? ngayThamGia.toString() : "");
        return hv;
    }

    private void setDateOrNull(PreparedStatement ps, int idx, String val) throws SQLException {
        if (val == null || val.isEmpty()) {
            ps.setNull(idx, Types.DATE);
        } else {
            ps.setDate(idx, Date.valueOf(val)); // val đã là yyyy-MM-dd
        }
    }
}