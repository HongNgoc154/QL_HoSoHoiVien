package View;

import database.DatabaseHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class NhatKyForm extends JFrame {

    private JTable table;
    private DefaultTableModel model;

    public NhatKyForm() {
        setTitle("Nhật ký hệ thống");
        setSize(800, 500);
        setLocationRelativeTo(null);

        model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[]{
                "ID","Nhân viên","Hành động","Đối tượng","Mô tả","Thời gian"
        });

        table = new JTable(model);

        add(new JScrollPane(table));

        loadData();
    }

    private void loadData(){
        model.setRowCount(0);

        try(Connection conn = DatabaseHelper.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM NhatKyHeThong ORDER BY thoiGian DESC");
            ResultSet rs = ps.executeQuery()){

            while(rs.next()){
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getInt("idNhanVien"),
                        rs.getString("hanhDong"),
                        rs.getString("doiTuong"),
                        rs.getString("moTa"),
                        rs.getTimestamp("thoiGian")
                });
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}