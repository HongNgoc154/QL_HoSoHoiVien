package Util;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.OutputStream;
import java.net.InetSocketAddress;

import java.sql.Connection;
import java.sql.PreparedStatement;
import database.DatabaseHelper;

public class EmailConfirmServer {

    public static void startServer() {

        try {

            HttpServer server = HttpServer.create(
                    new InetSocketAddress(8080),
                    0
            );

            server.createContext("/xacnhan", exchange -> {

                String query =
                        exchange.getRequestURI().getQuery();

                String token = "";

                if (query != null && query.startsWith("token=")) {

                    token = query.substring(6);
                }

                String response;

                try (
                    Connection conn =
                        DatabaseHelper.getConnection()
                ) {

                    PreparedStatement ps =
                        conn.prepareStatement(
                            """
                            UPDATE YeuCauRoiHoi
                            SET trangThai = N'Đã xác nhận'
                            WHERE token = ?
                            """
                        );

                    ps.setString(1, token);

                    int updated = ps.executeUpdate();

                    if (updated > 0) {

                        response =
                            """
                            <h1>Xác nhận thành công!</h1>
                            <p>Bạn đã xác nhận rời hội.</p>
                            """;

                    } else {

                        response =
                            """
                            <h1>Token không hợp lệ!</h1>
                            """;
                    }

                } catch (Exception ex) {

                    response =
                        "<h1>Lỗi: "
                        + ex.getMessage()
                        + "</h1>";
                }

                exchange.getResponseHeaders().set(
                    "Content-Type",
                    "text/html; charset=UTF-8"
                );

                byte[] bytes = response.getBytes("UTF-8");

                exchange.sendResponseHeaders(
                    200,
                    bytes.length
                );

                OutputStream os =
                        exchange.getResponseBody();

                os.write(bytes);

                os.close();
            });

            server.start();

            System.out.println(
                "Server chạy tại http://localhost:8080"
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}