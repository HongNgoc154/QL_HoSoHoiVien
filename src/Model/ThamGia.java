package model;

public class ThamGia {
    private int id;
    private int idHoiVien;
    private int idHoatDong;
    private String trangThai;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdHoiVien() { return idHoiVien; }
    public void setIdHoiVien(int idHoiVien) { this.idHoiVien = idHoiVien; }

    public int getIdHoatDong() { return idHoatDong; }
    public void setIdHoatDong(int idHoatDong) { this.idHoatDong = idHoatDong; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}