package View;

import database.DatabaseHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class HoiVienForm extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;

    public HoiVienForm() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // ===== TOP BAR =====
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));

        txtSearch = new JTextField(20);
        JButton btnSearch = new JButton("Tìm");

        JButton btnAdd = new JButton("Thêm");
        JButton btnUpdate = new JButton("Sửa");
        JButton btnDelete = new JButton("Xóa");

        top.add(new JLabel("Tìm tên:"));
        top.add(txtSearch);
        top.add(btnSearch);
        top.add(btnAdd);
        top.add(btnUpdate);
        top.add(btnDelete);

        add(top, BorderLayout.NORTH);

        // ===== TABLE =====
        model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[]{
                "ID","Mã","Tên","Ngày sinh","Giới tính","SĐT","Email","Trạng thái"
        });

        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadTable();

        // ===== EVENT =====
        btnSearch.addActionListener(e -> search());
        btnAdd.addActionListener(e -> openForm(null));
        btnUpdate.addActionListener(e -> edit());
        btnDelete.addActionListener(e -> delete());
    }

    // ================= LOAD TABLE =================
    void loadTable(){
        model.setRowCount(0);

        try(Connection conn = DatabaseHelper.getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM HoiVien")){

            while(rs.next()){
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("maHoiVien"),
                        rs.getString("tenHoiVien"),
                        rs.getDate("ngaySinh"),
                        rs.getString("gioiTinh"),
                        rs.getString("sdt"),
                        rs.getString("email"),
                        rs.getString("trangThai")
                });
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    // ================= SEARCH =================
    private void search(){
        model.setRowCount(0);

        try(Connection conn = DatabaseHelper.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM HoiVien WHERE tenHoiVien LIKE ?"
            )){

            ps.setString(1, "%" + txtSearch.getText() + "%");
            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("maHoiVien"),
                        rs.getString("tenHoiVien"),
                        rs.getDate("ngaySinh"),
                        rs.getString("gioiTinh"),
                        rs.getString("sdt"),
                        rs.getString("email"),
                        rs.getString("trangThai")
                });
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    // ================= DELETE =================
    private void delete(){
        int row = table.getSelectedRow();
        if(row < 0){
            JOptionPane.showMessageDialog(this,"Chọn dòng!");
            return;
        }

        int id = (int) model.getValueAt(row,0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Xóa hội viên này?", "Xác nhận", JOptionPane.YES_NO_OPTION);

        if(confirm == JOptionPane.YES_OPTION){
            try(Connection conn = DatabaseHelper.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM HoiVien WHERE id=?"
                )){

                ps.setInt(1,id);
                ps.executeUpdate();

                loadTable();

            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    // ================= EDIT =================
    private void edit(){
        int row = table.getSelectedRow();
        if(row < 0){
            JOptionPane.showMessageDialog(this,"Chọn dòng!");
            return;
        }

        openForm(row);
    }

    // ================= POPUP FORM =================
    private void openForm(Integer row){
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), true);
        dialog.setTitle(row == null ? "Thêm hội viên" : "Cập nhật hội viên");
        dialog.setSize(400,400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(8,2,10,10));

        JTextField txtMa = new JTextField();
        JTextField txtTen = new JTextField();
        JTextField txtNgaySinh = new JTextField();
        JTextField txtGioiTinh = new JTextField();
        JTextField txtSdt = new JTextField();
        JTextField txtEmail = new JTextField();

        panel.add(new JLabel("Mã"));
        panel.add(txtMa);

        panel.add(new JLabel("Tên"));
        panel.add(txtTen);

        panel.add(new JLabel("Ngày sinh (yyyy-mm-dd)"));
        panel.add(txtNgaySinh);

        panel.add(new JLabel("Giới tính"));
        panel.add(txtGioiTinh);

        panel.add(new JLabel("SĐT"));
        panel.add(txtSdt);

        panel.add(new JLabel("Email"));
        panel.add(txtEmail);

        JButton btnSave = new JButton("Lưu");

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(btnSave, BorderLayout.SOUTH);

        // ===== FILL DATA (EDIT) =====
        if(row != null){
            txtMa.setText(model.getValueAt(row,1).toString());
            txtTen.setText(model.getValueAt(row,2).toString());
            txtNgaySinh.setText(model.getValueAt(row,3).toString());
            txtGioiTinh.setText(model.getValueAt(row,4).toString());
            txtSdt.setText(model.getValueAt(row,5).toString());
            txtEmail.setText(model.getValueAt(row,6).toString());
        }

        // ===== SAVE =====
        btnSave.addActionListener(e -> {
            try(Connection conn = DatabaseHelper.getConnection()){

                if(row == null){
                    // INSERT
                    PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO HoiVien(maHoiVien,tenHoiVien,ngaySinh,gioiTinh,sdt,email) VALUES (?,?,?,?,?,?)"
                    );

                    ps.setString(1, txtMa.getText());
                    ps.setString(2, txtTen.getText());
                    ps.setDate(3, Date.valueOf(txtNgaySinh.getText()));
                    ps.setString(4, txtGioiTinh.getText());
                    ps.setString(5, txtSdt.getText());
                    ps.setString(6, txtEmail.getText());

                    ps.executeUpdate();

                }else{
                    // UPDATE
                    int id = (int) model.getValueAt(row,0);

                    PreparedStatement ps = conn.prepareStatement(
                            "UPDATE HoiVien SET tenHoiVien=?,ngaySinh=?,gioiTinh=?,sdt=?,email=? WHERE id=?"
                    );

                    ps.setString(1, txtTen.getText());
                    ps.setDate(2, Date.valueOf(txtNgaySinh.getText()));
                    ps.setString(3, txtGioiTinh.getText());
                    ps.setString(4, txtSdt.getText());
                    ps.setString(5, txtEmail.getText());
                    ps.setInt(6, id);

                    ps.executeUpdate();
                }

                loadTable();
                dialog.dispose();

            }catch(Exception ex){
                JOptionPane.showMessageDialog(dialog,"Lỗi dữ liệu!");
            }
        });

        dialog.setVisible(true);
    }
}