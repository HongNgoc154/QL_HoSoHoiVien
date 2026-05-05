package controller;

import dao.TaiKhoanDAO;
import dao.NhatKyDAO;
import model.TaiKhoan;
import Util.Session;

public class AuthController {

    private TaiKhoanDAO dao = new TaiKhoanDAO();

    public boolean login(String user, String pass){

        TaiKhoan tk = dao.login(user, pass);

        if(tk != null){
            Session.setUser(tk);

            NhatKyDAO.log(
                tk.getIdNhanVien(),
                "ĐĂNG NHẬP",
                "Hệ thống",
                "User: " + tk.getUsername()
            );

            return true;
        }

        return false;
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