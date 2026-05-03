package dao;

import database.DatabaseHelper;
import model.TaiKhoan;

import java.sql.*;

public class TaiKhoanDAO {

    public TaiKhoan login(String username, String password) {
        String sql = "SELECT * FROM TaiKhoan WHERE username=? AND password=?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                TaiKhoan tk = new TaiKhoan();
                tk.setId(rs.getInt("id"));
                tk.setUsername(rs.getString("username"));
                tk.setRole(rs.getString("role"));
                tk.setIdNhanVien(rs.getInt("idNhanVien"));
                return tk;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}