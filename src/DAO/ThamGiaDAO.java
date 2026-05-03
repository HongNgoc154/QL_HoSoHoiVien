package dao;

import database.DatabaseHelper;
import model.ThamGia;
import java.sql.*;
import java.util.*;

public class ThamGiaDAO {

    public List<ThamGia> getAll(){
        List<ThamGia> list = new ArrayList<>();

        try(Connection conn = DatabaseHelper.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM ThamGia");
            ResultSet rs = ps.executeQuery()){

            while(rs.next()){
                ThamGia tg = new ThamGia();
                tg.setId(rs.getInt("id"));
                tg.setIdHoiVien(rs.getInt("idHoiVien"));
                tg.setIdHoatDong(rs.getInt("idHoatDong"));
                tg.setTrangThai(rs.getString("trangThai"));

                list.add(tg);
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        return list;
    }

    public boolean insert(ThamGia tg){
        String sql = "INSERT INTO ThamGia(idHoiVien,idHoatDong,trangThai) VALUES (?,?,?)";

        try(Connection conn = DatabaseHelper.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setInt(1, tg.getIdHoiVien());
            ps.setInt(2, tg.getIdHoatDong());
            ps.setString(3, tg.getTrangThai());

            return ps.executeUpdate() > 0;

        }catch(Exception e){
            return false;
        }
    }

    public boolean delete(int id){
        try(Connection conn = DatabaseHelper.getConnection();
            PreparedStatement ps = conn.prepareStatement("DELETE FROM ThamGia WHERE id=?")){

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        }catch(Exception e){
            return false;
        }
    }
}