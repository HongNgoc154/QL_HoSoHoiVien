package View;

import controller.HoatDongController;
import model.HoatDong;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;

public class HoatDongForm extends JPanel {

    private JTable table;
    private DefaultTableModel model;

    private HoatDongController controller = new HoatDongController();
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public HoatDongForm() {
        setLayout(new BorderLayout());

        // ===== TOP =====
        JPanel top = new JPanel();

        JButton btnAdd = new JButton("Thêm");
        JButton btnEdit = new JButton("Sửa");
        JButton btnDelete = new JButton("Xóa");

        top.add(btnAdd);
        top.add(btnEdit);
        top.add(btnDelete);

        add(top, BorderLayout.NORTH);

        // ===== TABLE =====
        model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[]{
                "ID","Tên","Loại","Bắt đầu","Kết thúc","Địa điểm"
        });

        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadTable();

        // EVENT
        btnAdd.addActionListener(e -> openForm(null));
        btnEdit.addActionListener(e -> edit());
        btnDelete.addActionListener(e -> delete());
    }

    private void loadTable(){
        model.setRowCount(0);
        for(HoatDong hd : controller.getAll()){
            model.addRow(new Object[]{
                    hd.getId(),
                    hd.getTenHoatDong(),
                    hd.getLoaiHoatDong(),
                    hd.getThoiGianBatDau(),
                    hd.getThoiGianKetThuc(),
                    hd.getDiaDiem()
            });
        }
    }

    private void delete(){
        int row = table.getSelectedRow();
        if(row < 0) return;

        int id = (int) model.getValueAt(row,0);
        controller.delete(id);
        loadTable();
    }

    private void edit(){
        int row = table.getSelectedRow();
        if(row < 0) return;
        openForm(row);
    }

    private void openForm(Integer row){
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), true);
        dialog.setSize(400,400);
        dialog.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridLayout(6,2));

        JTextField txtTen = new JTextField();
        JTextField txtLoai = new JTextField();
        JTextField txtDiaDiem = new JTextField();
        JTextField txtStart = new JTextField();
        JTextField txtEnd = new JTextField();

        p.add(new JLabel("Tên")); p.add(txtTen);
        p.add(new JLabel("Loại")); p.add(txtLoai);
        p.add(new JLabel("Địa điểm")); p.add(txtDiaDiem);
        p.add(new JLabel("Bắt đầu")); p.add(txtStart);
        p.add(new JLabel("Kết thúc")); p.add(txtEnd);

        JButton save = new JButton("Lưu");

        dialog.add(p, BorderLayout.CENTER);
        dialog.add(save, BorderLayout.SOUTH);

        if(row != null){
            txtTen.setText(model.getValueAt(row,1).toString());
            txtLoai.setText(model.getValueAt(row,2).toString());
            txtStart.setText(model.getValueAt(row,3).toString());
            txtEnd.setText(model.getValueAt(row,4).toString());
            txtDiaDiem.setText(model.getValueAt(row,5).toString());
        }

        save.addActionListener(e -> {
            try{
                HoatDong hd = new HoatDong();

                if(row != null){
                    hd.setId((int) model.getValueAt(row,0));
                }

                hd.setTenHoatDong(txtTen.getText());
                hd.setLoaiHoatDong(txtLoai.getText());
                hd.setDiaDiem(txtDiaDiem.getText());
                hd.setThoiGianBatDau(sdf.parse(txtStart.getText()));
                hd.setThoiGianKetThuc(sdf.parse(txtEnd.getText()));

                if(row == null) controller.insert(hd);
                else controller.update(hd);

                loadTable();
                dialog.dispose();

            }catch(Exception ex){
                JOptionPane.showMessageDialog(dialog,"Sai định dạng ngày!");
            }
        });

        dialog.setVisible(true);
    }
}