/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package database;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 *
 * @author ADMIN
 */
public class DatabaseHelper {
    private static final String URL =
        "jdbc:sqlserver://localhost:1433;"
      + "databaseName=QuanLyHoSoHoiVien;"
      + "encrypt=true;"
      + "trustServerCertificate=true;"; // 🔥 BẮT BUỘC

    private static final String USER = "doan2";
    private static final String PASSWORD = "1111";

    public static Connection getConnection() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);

            // 🔎 TEST DB ĐANG KẾT NỐI
            System.out.println("CONNECT DB OK: " + conn.getCatalog());

            return conn;
        } catch (Exception e) {
            System.out.println("LỖI KẾT NỐI SQL SERVER");
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        getConnection();
    }
}
