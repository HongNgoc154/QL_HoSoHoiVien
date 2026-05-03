package dao;

import database.DatabaseHelper;
import java.sql.*;

public class NhatKyDAO {

    public static void log(int idNhanVien, String hanhDong, String doiTuong, String moTa){

        String sql = "INSERT INTO NhatKyHeThong(idNhanVien, hanhDong, doiTuong, moTa) VALUES (?,?,?,?)";

        try(Connection conn = DatabaseHelper.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setInt(1, idNhanVien);
            ps.setString(2, hanhDong);
            ps.setString(3, doiTuong);
            ps.setString(4, moTa);

            ps.executeUpdate();

        }catch(Exception e){
            System.out.println("Lỗi log: " + e.getMessage());
        }
    }
}