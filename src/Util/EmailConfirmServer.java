package Util;

//import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import database.DatabaseHelper;

import java.io.OutputStream;
import java.net.InetSocketAddress;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EmailConfirmServer {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static void startServer() {
        try {
            
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/xacnhan", EmailConfirmServer::handleConfirm);
            server.start();
            System.out.println("Server chạy tại http://localhost:8080");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleConfirm(HttpExchange exchange) {
        String token = getToken(exchange);
        boolean confirmAction = (exchange.getRequestURI().getQuery() != null && exchange.getRequestURI().getQuery().contains("action=confirm"));
        String html = buildPage(token, !confirmAction);
        try {
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }        

    private static String getToken(HttpExchange exchange) {
        String query = exchange.getRequestURI().getQuery();
        if (query == null || query.isBlank()) return "";
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && "token".equals(kv[0])) {
                return URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
            }
        }
        return "";
    }

    private static String buildPage(String token, boolean viewOnly) {
        if (token.isBlank()) return renderSimple("❌ Thiếu token xác nhận", "Link xác nhận không hợp lệ.");

        try (Connection conn = DatabaseHelper.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("""
                SELECT y.id, y.lyDo, y.nguonYeuCau, y.trangThai, y.thoiGianTao, y.thoiGianXacNhan,
                       h.maHoiVien, h.tenHoiVien, h.ngaySinh, h.gioiTinh, h.sdt, h.email, h.diaChi, h.hinhAnh, h.trangThai AS trangThaiHoiVien
                FROM YeuCauRoiHoi y
                JOIN HoiVien h ON y.idHoiVien = h.id
                WHERE y.token = ?
            """);
            ps.setString(1, token);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return renderSimple("❌ Token không hợp lệ", "Yêu cầu không tồn tại hoặc đã bị xóa.");

            Timestamp tao = rs.getTimestamp("thoiGianTao");
            Timestamp xacNhan = rs.getTimestamp("thoiGianXacNhan");
            String status = rs.getString("trangThai");
            boolean expired = tao == null || tao.toLocalDateTime().plusHours(24).isBefore(LocalDateTime.now());
            boolean confirmed = "Đã xác nhận".equalsIgnoreCase(status) || xacNhan != null;

            if (!viewOnly && !expired && !confirmed) {
                PreparedStatement ups = conn.prepareStatement("""
                    UPDATE YeuCauRoiHoi
                    SET trangThai = N'Đã xác nhận', thoiGianXacNhan = GETDATE()
                    WHERE token = ? AND trangThai = N'Chờ xác nhận'
                """);
                ups.setString(1, token);
                int updated = ups.executeUpdate();
                if (updated > 0) {
                    PreparedStatement notifyPs = conn.prepareStatement("INSERT INTO ThongBao(noiDung,idDangKyTam) VALUES(?,NULL)");
                    notifyPs.setString(1, "Hội viên " + rs.getString("tenHoiVien") + " đã xác nhận rời hội");
                    notifyPs.executeUpdate();
                    status = "Đã xác nhận";
                    confirmed = true;
                }
            }

            return renderProfileCard(rs, status, expired, confirmed, token, tao, xacNhan);

        } catch (Exception e) {
            return renderSimple("❌ Có lỗi xảy ra", "Không thể xử lý yêu cầu: " + e.getMessage());
        }
    }

    private static String renderProfileCard(ResultSet rs, String status, boolean expired, boolean confirmed,
                                            String token, Timestamp tao, Timestamp xacNhan) throws Exception {
        String avatar = safe(rs.getString("hinhAnh"));
        String avatarHtml = avatar.isBlank()
                ? "<div class='avatar-placeholder'>4x6</div>"
                : "<img class='avatar' src='" + avatar + "' alt='avatar'/>";

        String action;
        if (confirmed) {
            action = "<div class='ok'>✅ Xác nhận thành công. Bạn đã xác nhận yêu cầu rời hội. Vui lòng chờ hệ thống xử lý.</div>";
        } else if (expired) {
            action = "<div class='expired'>❌ Yêu cầu đã hết hạn xác nhận (quá 24 giờ).</div>";
        } else {
            action = "<a class='btn' href='/xacnhan?token=" + token + "&action=confirm'>Xác nhận rời hội</a>";
        }
        return """
            <!doctype html>
            <html>
            <head>
            <meta charset='utf-8'>
            <title>Xác nhận yêu cầu rời hội</title>

            <style>
            body{
                font-family:'Segoe UI',sans-serif;
                background:#f4f7fc;
                margin:0;
                padding:24px;
                color:#1d2a3a;
            }

            .card{
                max-width:900px;
                margin:auto;
                background:#fff;
                border-radius:16px;
                box-shadow:0 8px 26px rgba(19,89,185,.15);
                padding:24px;
            }

            .header{
                display:flex;
                gap:20px;
                align-items:flex-start;
            }

            .avatar{
                width:160px;
                height:220px;
                object-fit:cover;
                border-radius:10px;
                border:1px solid #d8e4f5;
            }

            .avatar-placeholder{
                width:160px;
                height:220px;
                border-radius:10px;
                border:2px dashed #9FE4FB;
                display:flex;
                align-items:center;
                justify-content:center;
                color:#1359B9;
                font-weight:700;
            }

            h1{
                margin:0 0 12px;
            }

            .grid{
                display:grid;
                grid-template-columns:1fr 1fr;
                gap:8px 20px;
            }

            .muted{
                color:#5f7085;
            }

            .section{
                margin-top:18px;
                padding:16px;
                border:1px solid #e5edf8;
                border-radius:12px;
                background:#fbfdff;
            }

            .btn{
                display:inline-block;
                padding:12px 20px;
                background:#1359B9;
                color:#fff;
                text-decoration:none;
                border-radius:10px;
                font-weight:600;
            }

            .btn:hover{
                background:#0f4ba0;
            }

            .ok{
                color:#0a7f3f;
                font-weight:600;
            }

            .expired{
                color:#c73737;
                font-weight:600;
            }

            .tag{
                display:inline-block;
                background:#e9f1ff;
                color:#1359B9;
                padding:4px 10px;
                border-radius:999px;
                font-size:13px;
            }
            </style>

            </head>

            <body>

            <div class='card'>

            <div class='header'>
            """
            + avatarHtml +
            """
            <div>

            <h1>Tờ hồ sơ xác nhận rời hội</h1>

            <div class='muted'>
            Kiểm tra kỹ thông tin trước khi xác nhận.
            </div>

            <div class='tag'>
            Trạng thái yêu cầu: """
            + safe(status) +
            """
            </div>

            </div>
            </div>

            <div class='section'>

            <h3>👤 Thông tin hội viên</h3>

            <div class='grid'>

            <div><b>Mã hội viên:</b> """
            + safe(rs.getString("maHoiVien")) +
            """
            </div>

            <div><b>Họ tên:</b> """
            + safe(rs.getString("tenHoiVien")) +
            """
            </div>

            <div><b>Ngày sinh:</b> """
            + safeDate(rs.getDate("ngaySinh")) +
            """
            </div>

            <div><b>Giới tính:</b> """
            + safe(rs.getString("gioiTinh")) +
            """
            </div>

            <div><b>Số điện thoại:</b> """
            + safe(rs.getString("sdt")) +
            """
            </div>

            <div><b>Email:</b> """
            + safe(rs.getString("email")) +
            """
            </div>

            <div><b>Địa chỉ:</b> """
            + safe(rs.getString("diaChi")) +
            """
            </div>

            <div><b>Trạng thái hiện tại:</b> """
            + safe(rs.getString("trangThaiHoiVien")) +
            """
            </div>

            </div>
            </div>

            <div class='section'>

            <h3>📋 Thông tin yêu cầu rời hội</h3>

            <div class='grid'>

            <div>
            <b>📅 Ngày yêu cầu:</b>
            """
            + fmt(tao) +
            """
            </div>

            <div>
            <b>📌 Nguồn yêu cầu:</b>
            """
            + safe(rs.getString("nguonYeuCau")) +
            """
            </div>

            <div style='grid-column:1/span 2'>
            <b>📝 Lý do rời hội:</b>
            """
            + safe(rs.getString("lyDo")) +
            """
            </div>

            <div>
            <b>Thời gian xác nhận:</b>
            """
            + fmt(xacNhan) +
            """
            </div>

            </div>
            </div>

            <div class='section'>

            <h3>⚠️ Trạng thái xác nhận</h3>

            """
            + action +
            """

            </div>

            </div>

            </body>
            </html>
            """;
    }
    private static String renderSimple(String title, String msg) {
        return "<html><meta charset='utf-8'><body style='font-family:Segoe UI;padding:24px'><h2>" + title + "</h2><p>" + msg + "</p></body></html>";
    }

    private static String safe(String v) { return v == null ? "" : v; }
    private static String safeDate(java.sql.Date d) { return d == null ? "" : d.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")); }
    private static String fmt(Timestamp t) { return t == null ? "Chưa xác nhận" : t.toLocalDateTime().format(DATE_FMT); }
}
