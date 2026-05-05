package model;

public class HoiVien {
    private int id;
    private String maHoiVien;
    private String tenHoiVien;
    private String ngaySinh;
    private String gioiTinh;
    private String sdt;
    private String email;
    private String diaChi;
    private String hinhAnh;
    private int idLoaiHoiVien;
    private String trangThai;
    private String ngayThamGia;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMaHoiVien() { return maHoiVien; }
    public void setMaHoiVien(String maHoiVien) { this.maHoiVien = maHoiVien; }

    public String getTenHoiVien() { return tenHoiVien; }
    public void setTenHoiVien(String tenHoiVien) { this.tenHoiVien = tenHoiVien; }

    public String getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(String ngaySinh) { this.ngaySinh = ngaySinh; }

    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }

    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }

    public int getIdLoaiHoiVien() { return idLoaiHoiVien; }
    public void setIdLoaiHoiVien(int idLoaiHoiVien) { this.idLoaiHoiVien = idLoaiHoiVien; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getNgayThamGia() { return ngayThamGia; }
    public void setNgayThamGia(String ngayThamGia) { this.ngayThamGia = ngayThamGia; }
}