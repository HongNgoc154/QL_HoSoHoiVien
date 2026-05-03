package dao;

import database.DatabaseHelper;
import model.NhanVien;
import java.sql.*;
import java.util.*;

public class NhanVienDAO {

    public List<NhanVien> getAll(){
        List<NhanVien> list = new ArrayList<>();

        try(Connection conn = DatabaseHelper.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM NhanVien");
            ResultSet rs = ps.executeQuery()){

            while(rs.next()){
                NhanVien nv = new NhanVien();
                nv.setId(rs.getInt("id"));
                nv.setMaNhanVien(rs.getString("maNhanVien"));
                nv.setTenNhanVien(rs.getString("tenNhanVien"));
                nv.setSdt(rs.getString("sdt"));
                nv.setEmail(rs.getString("email"));
                list.add(nv);
            }

        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        return list;
    }

    public boolean insert(NhanVien nv){
        String sql = "INSERT INTO NhanVien(maNhanVien, tenNhanVien, sdt, email) VALUES (?,?,?,?)";

        try(Connection conn = DatabaseHelper.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setString(1, nv.getMaNhanVien());
            ps.setString(2, nv.getTenNhanVien());
            ps.setString(3, nv.getSdt());
            ps.setString(4, nv.getEmail());

            return ps.executeUpdate() > 0;

        }catch(Exception e){
            return false;
        }
    }

    public boolean update(NhanVien nv){
        String sql = "UPDATE NhanVien SET tenNhanVien=?, sdt=?, email=? WHERE id=?";

        try(Connection conn = DatabaseHelper.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setString(1, nv.getTenNhanVien());
            ps.setString(2, nv.getSdt());
            ps.setString(3, nv.getEmail());
            ps.setInt(4, nv.getId());

            return ps.executeUpdate() > 0;
        }catch(Exception e){
            return false;
        }
    }

    public boolean delete(int id){
        try(Connection conn = DatabaseHelper.getConnection();
            PreparedStatement ps = conn.prepareStatement("DELETE FROM NhanVien WHERE id=?")){

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        }catch(Exception e){
            return false;
        }
    }
}