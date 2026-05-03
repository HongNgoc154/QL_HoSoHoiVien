package View;

import controller.ThamGiaController;
import database.DatabaseHelper;
import model.ThamGia;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ThamGiaForm extends JFrame {

    private JTable table;
    private DefaultTableModel model;

    private JComboBox<ComboItem> cbHoiVien, cbHoatDong;
    private JComboBox<String> cbTrangThai;

    private ThamGiaController controller = new ThamGiaController();

    public ThamGiaForm() {
        setTitle("Quản lý Tham gia");
        setSize(850, 500);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());

        // ===== TABLE =====
        model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[]{
                "ID","Hội viên","Hoạt động","Trạng thái"
        });

        table = new JTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // ===== FORM =====
        JPanel form = new JPanel(new GridLayout(4,2,10,10));
        form.setBorder(BorderFactory.createTitledBorder("Đăng ký tham gia"));

        cbHoiVien = new JComboBox<>();
        cbHoatDong = new JComboBox<>();
        cbTrangThai = new JComboBox<>(new String[]{
                "Đăng ký","Đã tham gia","Vắng"
        });

        form.add(new JLabel("Hội viên:"));
        form.add(cbHoiVien);

        form.add(new JLabel("Hoạt động:"));
        form.add(cbHoatDong);

        form.add(new JLabel("Trạng thái:"));
        form.add(cbTrangThai);

        panel.add(form, BorderLayout.NORTH);

        // ===== BUTTON =====
        JPanel btnPanel = new JPanel();

        JButton btnAdd = new JButton("Đăng ký");
        JButton btnDelete = new JButton("Xóa");

        btnPanel.add(btnAdd);
        btnPanel.add(btnDelete);

        panel.add(btnPanel, BorderLayout.SOUTH);

        add(panel);

        // ===== LOAD =====
        loadComboBox();
        loadTable();

        // ===== EVENT =====

        // THÊM
        btnAdd.addActionListener(e -> {
            try {
                ComboItem hv = (ComboItem) cbHoiVien.getSelectedItem();
                ComboItem hd = (ComboItem) cbHoatDong.getSelectedItem();

                ThamGia tg = new ThamGia();
                tg.setIdHoiVien(hv.getId());
                tg.setIdHoatDong(hd.getId());
                tg.setTrangThai(cbTrangThai.getSelectedItem().toString());

                if(controller.insert(tg)){
                    JOptionPane.showMessageDialog(null,"Đăng ký thành công");
                    loadTable();
                }else{
                    JOptionPane.showMessageDialog(null,"Trùng đăng ký!");
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,"Lỗi dữ liệu!");
            }
        });

        // XÓA
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();

            if(row >= 0){
                int id = (int) model.getValueAt(row,0);

                if(controller.delete(id)){
                    JOptionPane.showMessageDialog(null,"Xóa thành công");
                    loadTable();
                }
            }
        });

        // CLICK TABLE → set lại combobox
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();

            if(row >= 0){
                String tenHV = model.getValueAt(row,1).toString();
                String tenHD = model.getValueAt(row,2).toString();
                String trangThai = model.getValueAt(row,3).toString();

                selectComboItem(cbHoiVien, tenHV);
                selectComboItem(cbHoatDong, tenHD);
                cbTrangThai.setSelectedItem(trangThai);
            }
        });
    }

    // ===== LOAD COMBO =====
    private void loadComboBox(){
        try(Connection conn = DatabaseHelper.getConnection()){

            // Hội viên
            ResultSet rs1 = conn.createStatement()
                    .executeQuery("SELECT id, tenHoiVien FROM HoiVien");

            while(rs1.next()){
                cbHoiVien.addItem(new ComboItem(
                        rs1.getInt("id"),
                        rs1.getString("tenHoiVien")
                ));
            }

            // Hoạt động
            ResultSet rs2 = conn.createStatement()
                    .executeQuery("SELECT id, tenHoatDong FROM HoatDong");

            while(rs2.next()){
                cbHoatDong.addItem(new ComboItem(
                        rs2.getInt("id"),
                        rs2.getString("tenHoatDong")
                ));
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    // ===== LOAD TABLE =====
    private void loadTable(){
        model.setRowCount(0);

        try(Connection conn = DatabaseHelper.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT tg.id, hv.tenHoiVien, hd.tenHoatDong, tg.trangThai " +
                    "FROM ThamGia tg " +
                    "JOIN HoiVien hv ON tg.idHoiVien = hv.id " +
                    "JOIN HoatDong hd ON tg.idHoatDong = hd.id"
            );
            ResultSet rs = ps.executeQuery()){

            while(rs.next()){
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("tenHoiVien"),
                        rs.getString("tenHoatDong"),
                        rs.getString("trangThai")
                });
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    // ===== CHỌN COMBO =====
    private void selectComboItem(JComboBox<ComboItem> combo, String name){
        for(int i=0;i<combo.getItemCount();i++){
            if(combo.getItemAt(i).toString().equals(name)){
                combo.setSelectedIndex(i);
                break;
            }
        }
    }

    // ===== CLASS COMBO =====
    class ComboItem {
        private int id;
        private String name;

        public ComboItem(int id, String name){
            this.id = id;
            this.name = name;
        }

        public int getId(){
            return id;
        }

        public String toString(){
            return name;
        }
    }
}