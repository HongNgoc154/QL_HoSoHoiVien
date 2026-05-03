package View;

import controller.NhanVienController;
import model.NhanVien;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class NhanVienForm extends JFrame {

    private JTable table;
    private DefaultTableModel model;

    private JTextField txtMa, txtTen, txtSdt, txtEmail;

    private NhanVienController controller = new NhanVienController();

    public NhanVienForm() {

        setTitle("Quản lý Nhân viên");
        setSize(850, 500);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());

        // ===== TABLE =====
        model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[]{
                "ID", "Mã", "Tên", "SĐT", "Email"
        });

        table = new JTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // ===== FORM =====
        JPanel form = new JPanel(new GridLayout(5, 2, 10, 10));
        form.setBorder(BorderFactory.createTitledBorder("Thông tin nhân viên"));

        txtMa = new JTextField();
        txtTen = new JTextField();
        txtSdt = new JTextField();
        txtEmail = new JTextField();

        form.add(new JLabel("Mã nhân viên:"));
        form.add(txtMa);

        form.add(new JLabel("Tên nhân viên:"));
        form.add(txtTen);

        form.add(new JLabel("SĐT:"));
        form.add(txtSdt);

        form.add(new JLabel("Email:"));
        form.add(txtEmail);

        panel.add(form, BorderLayout.NORTH);

        // ===== BUTTON =====
        JPanel btnPanel = new JPanel();

        JButton btnAdd = new JButton("Thêm");
        JButton btnUpdate = new JButton("Sửa");
        JButton btnDelete = new JButton("Xóa");
        JButton btnRefresh = new JButton("Làm mới");

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnRefresh);

        panel.add(btnPanel, BorderLayout.SOUTH);

        add(panel);

        // ===== LOAD DATA =====
        loadTable(controller.getAll());

        // ===== EVENT =====

        // THÊM
        btnAdd.addActionListener(e -> {
            NhanVien nv = new NhanVien();

            nv.setMaNhanVien(txtMa.getText());
            nv.setTenNhanVien(txtTen.getText());
            nv.setSdt(txtSdt.getText());
            nv.setEmail(txtEmail.getText());

            if (controller.insert(nv)) {
                JOptionPane.showMessageDialog(null, "Thêm thành công");
                loadTable(controller.getAll());
                clearForm();
            } else {
                JOptionPane.showMessageDialog(null, "Thêm thất bại");
            }
        });

        // SỬA
        btnUpdate.addActionListener(e -> {
            int row = table.getSelectedRow();

            if (row >= 0) {
                NhanVien nv = new NhanVien();

                nv.setId((int) model.getValueAt(row, 0));
                nv.setTenNhanVien(txtTen.getText());
                nv.setSdt(txtSdt.getText());
                nv.setEmail(txtEmail.getText());

                if (controller.update(nv)) {
                    JOptionPane.showMessageDialog(null, "Cập nhật thành công");
                    loadTable(controller.getAll());
                }
            } else {
                JOptionPane.showMessageDialog(null, "Chọn dòng cần sửa");
            }
        });

        // XÓA
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();

            if (row >= 0) {
                int id = (int) model.getValueAt(row, 0);

                int confirm = JOptionPane.showConfirmDialog(
                        null, "Xóa nhân viên này?", "Xác nhận", JOptionPane.YES_NO_OPTION
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    if (controller.delete(id)) {
                        JOptionPane.showMessageDialog(null, "Xóa thành công");
                        loadTable(controller.getAll());
                        clearForm();
                    }
                }
            } else {
                JOptionPane.showMessageDialog(null, "Chọn dòng cần xóa");
            }
        });

        // REFRESH
        btnRefresh.addActionListener(e -> {
            loadTable(controller.getAll());
            clearForm();
        });

        // CLICK TABLE
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();

            if (row >= 0) {
                txtMa.setText(model.getValueAt(row, 1).toString());
                txtTen.setText(model.getValueAt(row, 2).toString());
                txtSdt.setText(model.getValueAt(row, 3).toString());
                txtEmail.setText(model.getValueAt(row, 4).toString());
            }
        });
    }

    // ===== LOAD TABLE =====
    private void loadTable(List<NhanVien> list) {
        model.setRowCount(0);

        for (NhanVien nv : list) {
            model.addRow(new Object[]{
                    nv.getId(),
                    nv.getMaNhanVien(),
                    nv.getTenNhanVien(),
                    nv.getSdt(),
                    nv.getEmail()
            });
        }
    }

    // ===== CLEAR FORM =====
    private void clearForm() {
        txtMa.setText("");
        txtTen.setText("");
        txtSdt.setText("");
        txtEmail.setText("");
    }
}