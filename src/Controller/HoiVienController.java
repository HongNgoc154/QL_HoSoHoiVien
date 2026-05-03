package controller;

import dao.HoiVienDAO;
import model.HoiVien;
import java.util.List;

public class HoiVienController {

    private HoiVienDAO dao = new HoiVienDAO();

    public List<HoiVien> getAll() {
        return dao.getAll();
    }

    public void insert(HoiVien hv) {
        dao.insert(hv);
    }

    public void update(HoiVien hv) {
        dao.update(hv);
    }

    public void delete(int id) {
        dao.delete(id);
    }

    public List<HoiVien> search(String keyword) {
        return dao.search(keyword);
    }
}