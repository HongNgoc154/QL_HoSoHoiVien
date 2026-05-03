package dao;

import database.DatabaseHelper;
import model.HoatDong;

import java.sql.*;
import java.util.*;

public class HoatDongDAO {

    public List<HoatDong> getAll() {
        List<HoatDong> list = new ArrayList<>();

        String sql = "SELECT * FROM HoatDong";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                HoatDong hd = new HoatDong();

                hd.setId(rs.getInt("id"));
                hd.setTenHoatDong(rs.getString("tenHoatDong"));
                hd.setLoaiHoatDong(rs.getString("loaiHoatDong"));
                hd.setThoiGianBatDau(rs.getTimestamp("thoiGianBatDau"));
                hd.setThoiGianKetThuc(rs.getTimestamp("thoiGianKetThuc"));
                hd.setDiaDiem(rs.getString("diaDiem"));
                hd.setMoTa(rs.getString("moTa"));
                hd.setTrangThai(rs.getString("trangThai"));

                list.add(hd);
            }

        } catch (Exception e) {
            System.out.println("Lỗi getAll HoatDong: " + e.getMessage());
        }

        return list;
    }

    public boolean insert(HoatDong hd) {
        String sql = "INSERT INTO HoatDong(tenHoatDong, loaiHoatDong, thoiGianBatDau, thoiGianKetThuc, diaDiem, moTa, trangThai) VALUES (?,?,?,?,?,?,?)";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, hd.getTenHoatDong());
            ps.setString(2, hd.getLoaiHoatDong());
            ps.setTimestamp(3, new Timestamp(hd.getThoiGianBatDau().getTime()));
            ps.setTimestamp(4, new Timestamp(hd.getThoiGianKetThuc().getTime()));
            ps.setString(5, hd.getDiaDiem());
            ps.setString(6, hd.getMoTa());
            ps.setString(7, hd.getTrangThai());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Lỗi insert HoatDong: " + e.getMessage());
            return false;
        }
    }

    public boolean update(HoatDong hd) {
        String sql = "UPDATE HoatDong SET tenHoatDong=?, loaiHoatDong=?, thoiGianBatDau=?, thoiGianKetThuc=?, diaDiem=?, moTa=?, trangThai=? WHERE id=?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, hd.getTenHoatDong());
            ps.setString(2, hd.getLoaiHoatDong());
            ps.setTimestamp(3, new Timestamp(hd.getThoiGianBatDau().getTime()));
            ps.setTimestamp(4, new Timestamp(hd.getThoiGianKetThuc().getTime()));
            ps.setString(5, hd.getDiaDiem());
            ps.setString(6, hd.getMoTa());
            ps.setString(7, hd.getTrangThai());
            ps.setInt(8, hd.getId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM HoatDong WHERE id=?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            return false;
        }
    }
}