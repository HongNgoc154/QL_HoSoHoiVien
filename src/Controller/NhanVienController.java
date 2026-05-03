package controller;

import dao.NhanVienDAO;
import dao.NhatKyDAO;
import model.NhanVien;

import java.util.List;

public class NhanVienController {

    private NhanVienDAO dao = new NhanVienDAO();
    private int currentUser = 1; // giả lập user đăng nhập

    public List<NhanVien> getAll(){
        return dao.getAll();
    }

    public boolean insert(NhanVien nv){
        boolean result = dao.insert(nv);

        if(result){
            NhatKyDAO.log(currentUser,"THÊM","NhanVien","Thêm nhân viên: "+nv.getTenNhanVien());
        }
        return result;
    }

    public boolean update(NhanVien nv){
        boolean result = dao.update(nv);

        if(result){
            NhatKyDAO.log(currentUser,"SỬA","NhanVien","Sửa nhân viên ID: "+nv.getId());
        }
        return result;
    }

    public boolean delete(int id){
        boolean result = dao.delete(id);

        if(result){
            NhatKyDAO.log(currentUser,"XÓA","NhanVien","Xóa nhân viên ID: "+id);
        }
        return result;
    }
}