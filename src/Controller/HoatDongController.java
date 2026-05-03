package controller;

import dao.HoatDongDAO;
import dao.NhatKyDAO;
import model.HoatDong;

import java.util.List;

public class HoatDongController {

    private HoatDongDAO dao = new HoatDongDAO();

    private int currentUser = 1;

    // ===== LẤY DANH SÁCH =====
    public List<HoatDong> getAll(){
        return dao.getAll();
    }

    // ===== THÊM =====
    public boolean insert(HoatDong hd){
        boolean result = dao.insert(hd);

        if(result){
            NhatKyDAO.log(
                currentUser,
                "THÊM",
                "HoatDong",
                "Thêm hoạt động: " + hd.getTenHoatDong()
            );
        }

        return result;
    }

    // ===== SỬA =====
    public boolean update(HoatDong hd){
        boolean result = dao.update(hd);

        if(result){
            NhatKyDAO.log(
                currentUser,
                "SỬA",
                "HoatDong",
                "Sửa hoạt động ID: " + hd.getId()
            );
        }

        return result;
    }

    // ===== XÓA =====
    public boolean delete(int id){
        boolean result = dao.delete(id);

        if(result){
            NhatKyDAO.log(
                currentUser,
                "XÓA",
                "HoatDong",
                "Xóa hoạt động ID: " + id
            );
        }

        return result;
    }
}