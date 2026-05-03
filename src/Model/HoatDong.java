package model;

import java.util.Date;

public class HoatDong {

    private int id;
    private String tenHoatDong;
    private String loaiHoatDong;

    private Date thoiGianBatDau;
    private Date thoiGianKetThuc;

    private String diaDiem;
    private String moTa;
    private String trangThai;

    // ===== GETTER =====
    public int getId() {
        return id;
    }

    public String getTenHoatDong() {
        return tenHoatDong;
    }

    public String getLoaiHoatDong() {
        return loaiHoatDong;
    }

    public Date getThoiGianBatDau() {
        return thoiGianBatDau;
    }

    public Date getThoiGianKetThuc() {
        return thoiGianKetThuc;
    }

    public String getDiaDiem() {
        return diaDiem;
    }

    public String getMoTa() {
        return moTa;
    }

    public String getTrangThai() {
        return trangThai;
    }

    // ===== SETTER =====
    public void setId(int id) {
        this.id = id;
    }

    public void setTenHoatDong(String tenHoatDong) {
        this.tenHoatDong = tenHoatDong;
    }

    public void setLoaiHoatDong(String loaiHoatDong) {
        this.loaiHoatDong = loaiHoatDong;
    }

    public void setThoiGianBatDau(Date thoiGianBatDau) {
        this.thoiGianBatDau = thoiGianBatDau;
    }

    public void setThoiGianKetThuc(Date thoiGianKetThuc) {
        this.thoiGianKetThuc = thoiGianKetThuc;
    }

    public void setDiaDiem(String diaDiem) {
        this.diaDiem = diaDiem;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}