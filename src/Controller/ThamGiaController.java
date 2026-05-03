package controller;

import dao.ThamGiaDAO;
import dao.NhatKyDAO;
import model.ThamGia;

import java.util.List;

public class ThamGiaController {

    private ThamGiaDAO dao = new ThamGiaDAO();

    // giả lập user đang đăng nhập (sau này thay bằng login thật)
    private int currentUser = 1;

    // ===== LẤY DANH SÁCH =====
    public List<ThamGia> getAll(){
        return dao.getAll();
    }

    // ===== THÊM =====
    public boolean insert(ThamGia tg){
        boolean result = dao.insert(tg);

        if(result){
            NhatKyDAO.log(
                currentUser,
                "THÊM",
                "ThamGia",
                "Hội viên ID " + tg.getIdHoiVien() + 
                " đăng ký hoạt động ID " + tg.getIdHoatDong()
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
                "ThamGia",
                "Xóa đăng ký tham gia ID: " + id
            );
        }

        return result;
    }
}