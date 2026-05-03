package model;

public class NhanVien {
    private int id;
    private String maNhanVien;
    private String tenNhanVien;
    private String sdt;
    private String email;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMaNhanVien() { return maNhanVien; }
    public void setMaNhanVien(String maNhanVien) { this.maNhanVien = maNhanVien; }

    public String getTenNhanVien() { return tenNhanVien; }
    public void setTenNhanVien(String tenNhanVien) { this.tenNhanVien = tenNhanVien; }

    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}