package dao;

import database.DatabaseHelper;
import model.HoiVien;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HoiVienDAO {

    // ===== LẤY DANH SÁCH =====
    public List<HoiVien> getAll() {
        List<HoiVien> list = new ArrayList<>();

        String sql = "SELECT * FROM HoiVien";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                HoiVien hv = new HoiVien();
                hv.setId(rs.getInt("id"));
                hv.setMaHoiVien(rs.getString("maHoiVien"));
                hv.setTenHoiVien(rs.getString("tenHoiVien"));
                hv.setSdt(rs.getString("sdt"));
                hv.setEmail(rs.getString("email"));

                list.add(hv);
            }

        } catch (Exception e) {
            System.out.println("Lỗi getAll: " + e.getMessage());
        }
        return list;
    }

    // ===== THÊM =====
    public boolean insert(HoiVien hv) {

        if (hv.getMaHoiVien().isEmpty() || hv.getTenHoiVien().isEmpty()) {
            System.out.println("Thiếu dữ liệu!");
            return false;
        }

        String sql = "INSERT INTO HoiVien(maHoiVien, tenHoiVien, sdt, email) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, hv.getMaHoiVien());
            ps.setString(2, hv.getTenHoiVien());
            ps.setString(3, hv.getSdt());
            ps.setString(4, hv.getEmail());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Lỗi insert: " + e.getMessage());
            return false;
        }
    }

    // ===== SỬA =====
    public boolean update(HoiVien hv) {

        String sql = "UPDATE HoiVien SET tenHoiVien=?, sdt=?, email=? WHERE id=?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, hv.getTenHoiVien());
            ps.setString(2, hv.getSdt());
            ps.setString(3, hv.getEmail());
            ps.setInt(4, hv.getId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Lỗi update: " + e.getMessage());
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
            System.out.println("Lỗi delete: " + e.getMessage());
            return false;
        }
    }

    // ===== TÌM KIẾM =====
    public List<HoiVien> search(String keyword) {
        List<HoiVien> list = new ArrayList<>();

        String sql = "SELECT * FROM HoiVien WHERE tenHoiVien LIKE ?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                HoiVien hv = new HoiVien();
                hv.setId(rs.getInt("id"));
                hv.setMaHoiVien(rs.getString("maHoiVien"));
                hv.setTenHoiVien(rs.getString("tenHoiVien"));
                hv.setSdt(rs.getString("sdt"));
                hv.setEmail(rs.getString("email"));

                list.add(hv);
            }

        } catch (Exception e) {
            System.out.println("Lỗi search: " + e.getMessage());
        }
        return list;
    }
}