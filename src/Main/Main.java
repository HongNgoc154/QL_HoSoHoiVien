/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Main;
import View.LoginForm;
import database.DatabaseHelper;
import Util.EmailConfirmServer;
import com.formdev.flatlaf.FlatLightLaf;

/**
 *
 * @author ADMIN
 */
public class Main {
    public static void main(String[] args) {
        
        
        try {
            // bật giao diện FlatLaf
            FlatLightLaf.setup();
        } catch (Exception ex) {
            System.out.println("Failed to initialize FlatLaf");
        }

        // test kết nối DB
        DatabaseHelper.getConnection();
        
        // start email confirm server
        EmailConfirmServer.startServer();

        // mở giao diện chính
        java.awt.EventQueue.invokeLater(() -> {
        new LoginForm().setVisible(true);
        });
        
        
    }
}
