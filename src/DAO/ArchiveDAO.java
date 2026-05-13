package dao;

import database.DatabaseHelper;
import java.sql.*;

public class ArchiveDAO {

    public static void ensureArchiveTable() throws SQLException {
        String sql = "IF OBJECT_ID('KhoLuuTruHeThong','U') IS NULL "
                + "BEGIN "
                + "CREATE TABLE KhoLuuTruHeThong ("
                + "id INT IDENTITY(1,1) PRIMARY KEY, "
                + "loaiDuLieu NVARCHAR(50), "
                + "idGoc INT, "
                + "duLieuJson NVARCHAR(MAX), "
                + "nguoiThucHien INT, "
                + "hanhDong NVARCHAR(30), "
                + "thoiGian DATETIME DEFAULT GETDATE()"
                + ") END";
        try (Connection c = DatabaseHelper.getConnection();
             Statement st = c.createStatement()) {
            st.execute(sql);
        }
    }

    public static void archiveByQuery(String loai, int idGoc, String table, String whereCol, int userId, String hanhDong) throws SQLException {
        ensureArchiveTable();
        String query = "SELECT * FROM " + table + " WHERE " + whereCol + "=?";
        String json = null;
        try (Connection c = DatabaseHelper.getConnection();
             PreparedStatement ps = c.prepareStatement(query)) {
            ps.setInt(1, idGoc);
            ResultSet rs = ps.executeQuery();
            ResultSetMetaData md = rs.getMetaData();
            if (rs.next()) {
                StringBuilder sb = new StringBuilder("{");
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    if (i > 1) sb.append(",");
                    sb.append("\"").append(md.getColumnName(i)).append("\":");
                    Object v = rs.getObject(i);
                    if (v == null) sb.append("null");
                    else sb.append("\"").append(String.valueOf(v).replace("\"", "\\\"")).append("\"");
                }
                sb.append("}");
                json = sb.toString();
            }
        }
        if (json == null) return;
        try (Connection c = DatabaseHelper.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO KhoLuuTruHeThong(loaiDuLieu,idGoc,duLieuJson,nguoiThucHien,hanhDong) VALUES(?,?,?,?,?)")) {
            ps.setString(1, loai);
            ps.setInt(2, idGoc);
            ps.setString(3, json);
            ps.setInt(4, userId);
            ps.setString(5, hanhDong);
            ps.executeUpdate();
        }
    }
}
