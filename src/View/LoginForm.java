package View;

import controller.AuthController;

import javax.swing.*;
import java.awt.*;

public class LoginForm extends JFrame {

    private JTextField txtUser;
    private JPasswordField txtPass;

    private AuthController controller = new AuthController();

    public LoginForm(){
        setTitle("Đăng nhập");
        setSize(350,250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(4,2,10,10));

        txtUser = new JTextField();
        txtPass = new JPasswordField();

        JButton btnLogin = new JButton("Đăng nhập");

        panel.add(new JLabel("Username:"));
        panel.add(txtUser);

        panel.add(new JLabel("Password:"));
        panel.add(txtPass);

        panel.add(new JLabel());
        panel.add(btnLogin);

        add(panel);

        btnLogin.addActionListener(e -> login());
    }

    private void login(){
        String user = txtUser.getText();
        String pass = new String(txtPass.getPassword());

        if(controller.login(user, pass)){
            JOptionPane.showMessageDialog(this,"Đăng nhập thành công!");

            // loading 2s
            new Thread(() -> {
                try { Thread.sleep(2000); } catch(Exception e){}

                SwingUtilities.invokeLater(() -> {
                    new MainForm().setVisible(true);
                    dispose();
                });
            }).start();

        }else{
            JOptionPane.showMessageDialog(this,"Sai tài khoản!");
        }
    }
}