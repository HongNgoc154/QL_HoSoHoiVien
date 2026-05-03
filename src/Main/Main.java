/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Main;
import View.LoginForm;
import database.DatabaseHelper;

/**
 *
 * @author ADMIN
 */
public class Main {
    public static void main(String[] args) {

        // test kết nối DB
        DatabaseHelper.getConnection();

        // mở giao diện chính
        java.awt.EventQueue.invokeLater(() -> {
        new LoginForm().setVisible(true);
        });
        
        
    }
}
