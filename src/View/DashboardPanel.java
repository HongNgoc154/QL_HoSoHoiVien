package View;

import javax.swing.*;
import java.awt.*;

public class DashboardPanel extends JPanel {

    public DashboardPanel(){
        setLayout(new BorderLayout());
        setBackground(Color.decode("#F1F5F9"));

        JPanel cards = new JPanel(new GridLayout(1,3,20,20));
        cards.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        cards.add(createCard("Hội viên", "120"));
        cards.add(createCard("Hoạt động", "25"));
        cards.add(createCard("Nhân viên", "10"));

        add(cards, BorderLayout.NORTH);

        JLabel welcome = new JLabel("Chào mừng đến hệ thống", JLabel.CENTER);
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 24));

        add(welcome, BorderLayout.CENTER);
    }

    private JPanel createCard(String title, String value){
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new GridLayout(2,1));
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JLabel lblTitle = new JLabel(title, JLabel.CENTER);
        JLabel lblValue = new JLabel(value, JLabel.CENTER);

        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 22));

        panel.add(lblTitle);
        panel.add(lblValue);

        return panel;
    }
}