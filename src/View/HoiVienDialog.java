package View;

import database.DatabaseHelper;
import model.HoiVien;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class HoiVienDialog extends JDialog {

    private JTextField txtMa, txtTen, txtSdt, txtEmail;
    private HoiVien hv;
    private HoiVienForm parent;

    public HoiVienDialog(HoiVien hv, HoiVienForm parent){
        this.hv = hv;
        this.parent = parent;

        setTitle(hv == null ? "Thêm hội viên" : "Sửa hội viên");
        setSize(400,300);
        setLocationRelativeTo(null);
        setModal(true);

        JPanel panel = new JPanel(new GridLayout(5,2,10,10));

        txtMa = new JTextField();
        txtTen = new JTextField();
        txtSdt = new JTextField();
        txtEmail = new JTextField();

        panel.add(new JLabel("Mã"));
        panel.add(txtMa);

        panel.add(new JLabel("Tên"));
        panel.add(txtTen);

        panel.add(new JLabel("SĐT"));
        panel.add(txtSdt);

        panel.add(new JLabel("Email"));
        panel.add(txtEmail);

        JButton btnSave = new JButton("Lưu");
        panel.add(btnSave);

        add(panel);

        // ===== LOAD DATA =====
        if(hv != null){
            txtMa.setText(hv.getMaHoiVien());
            txtTen.setText(hv.getTenHoiVien());
            txtSdt.setText(hv.getSdt());
            txtEmail.setText(hv.getEmail());
        }

        btnSave.addActionListener(e -> save());
    }

    private void save(){
        try(Connection conn = DatabaseHelper.getConnection()){

            if(hv == null){
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO HoiVien(maHoiVien,tenHoiVien,sdt,email) VALUES (?,?,?,?)"
                );

                ps.setString(1, txtMa.getText());
                ps.setString(2, txtTen.getText());
                ps.setString(3, txtSdt.getText());
                ps.setString(4, txtEmail.getText());

                ps.executeUpdate();

            }else{
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE HoiVien SET tenHoiVien=?, sdt=?, email=? WHERE id=?"
                );

                ps.setString(1, txtTen.getText());
                ps.setString(2, txtSdt.getText());
                ps.setString(3, txtEmail.getText());
                ps.setInt(4, hv.getId());

                ps.executeUpdate();
            }

            parent.loadTable();
            dispose();

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}