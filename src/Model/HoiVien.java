package model;

public class HoiVien {

    private int id;
    private String maHoiVien;
    private String tenHoiVien;
    private String sdt;
    private String email;

    // ===== GET =====
    public int getId() {
        return id;
    }

    public String getMaHoiVien() {
        return maHoiVien;
    }

    public String getTenHoiVien() {
        return tenHoiVien;
    }

    public String getSdt() {
        return sdt;
    }

    public String getEmail() {
        return email;
    }

    // ===== SET =====
    public void setId(int id) {
        this.id = id;
    }

    public void setMaHoiVien(String maHoiVien) {
        this.maHoiVien = maHoiVien;
    }

    public void setTenHoiVien(String tenHoiVien) {
        this.tenHoiVien = tenHoiVien;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}