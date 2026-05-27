package controller;

import dao.TaiKhoanDAO;
import dao.NhatKyDAO;
import model.TaiKhoan;
import Util.Session;
import java.sql.*;
import database.DatabaseHelper;
import javax.swing.JOptionPane;

public class AuthController {

    private TaiKhoanDAO dao = new TaiKhoanDAO();

    public boolean login(String user, String pass){

        TaiKhoan tk = dao.login(user, pass);

        if(tk == null){
            return false;
        }

        try(
            Connection c=
                DatabaseHelper.getConnection();

            PreparedStatement ps=
                c.prepareStatement(
                "SELECT trangThai "
              + "FROM NhanVien "
              + "WHERE id=?")
        ){

            ps.setInt(
                1,
                tk.getIdNhanVien()
            );

            ResultSet rs=
                ps.executeQuery();

            if(rs.next()){

                String status=
                    rs.getString(
                        "trangThai"
                    );

                if(
                    status!=null
                    &&
                    status.equalsIgnoreCase("Nghỉ")
                ){

                    JOptionPane.showMessageDialog(
                        null,
                        "Tài khoản đã ngừng hoạt động.\nLiên hệ quản trị viên."
                    );

                    return false;
                }
            }

        }catch(Exception e){
            e.printStackTrace();
            return false;
        }

        Session.setUser(tk);

        NhatKyDAO.log(
            tk.getIdNhanVien(),
            "ĐĂNG NHẬP",
            "Hệ thống",
            "User: "+tk.getUsername()
        );

        return true;
    }

    public void logout(){
        TaiKhoan tk = Session.getUser();

        if(tk != null){
            NhatKyDAO.log(
                tk.getIdNhanVien(),
                "ĐĂNG XUẤT",
                "Hệ thống",
                "User: " + tk.getUsername()
            );
        }

        Session.clear();
    }
}