package View;

//import Util.UITheme;
//import Util.StyledTable;
import Util.ExcelExporter;
import Util.Session;
import Util.StyledTable;
import Util.UITheme;
import Util.ValidationHelper;
import dao.NhatKyDAO;
import database.DatabaseHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;

public class NhanVienForm extends JPanel {
    private StyledTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private JComboBox<String> cbTrangThai;

    public NhanVienForm() {
        setLayout(new BorderLayout()); setBackground(UITheme.BG_MAIN); setBorder(new EmptyBorder(24, 28, 24, 28));
        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false); header.setBorder(new EmptyBorder(0,0,16,0));
        JLabel title = new JLabel("Quản lý Nhân viên"); title.setFont(UITheme.FONT_TITLE); title.setForeground(UITheme.TEXT_PRIMARY);
        JButton btnAdd = UITheme.primaryButton("+ Thêm nhân viên"); header.add(title, BorderLayout.WEST); header.add(btnAdd, BorderLayout.EAST); add(header, BorderLayout.NORTH);
        JPanel filterCard = new JPanel(new FlowLayout(FlowLayout.LEFT,10,8)); filterCard.setOpaque(false);
        txtSearch = new JTextField(18); cbTrangThai = new JComboBox<>(new String[]{"Trạng thái","Đang làm","Đã nghỉ"});
        JButton btnSearch = UITheme.primaryButton("🔍 Tìm"), btnReset=UITheme.outlineButton("↺ Đặt lại"), btnExport=UITheme.outlineButton("📥 Xuất Excel");
        filterCard.add(new JLabel("Tìm:")); filterCard.add(txtSearch); filterCard.add(cbTrangThai); filterCard.add(btnSearch); filterCard.add(btnReset); filterCard.add(btnExport);
        model = new DefaultTableModel(new String[]{"ID","Mã NV","Họ tên","Username","Vai trò","Ngày sinh","Giới tính","SĐT","Email","Trạng thái"},0){ public boolean isCellEditable(int r,int c){return false;}};
        table = new StyledTable(model);
        JPanel center = new JPanel(new BorderLayout(0,12)); center.setOpaque(false); center.add(filterCard, BorderLayout.NORTH); center.add(new JScrollPane(table), BorderLayout.CENTER); add(center, BorderLayout.CENTER);

        JButton btnEdit = UITheme.outlineButton("✏ Sửa"); JButton btnDel = UITheme.dangerButton("🗑 Xóa"); filterCard.add(btnEdit); filterCard.add(btnDel);
        loadTable();
        btnSearch.addActionListener(e->search()); btnReset.addActionListener(e->{txtSearch.setText(""); loadTable();});
        btnAdd.addActionListener(e->openForm(null)); btnEdit.addActionListener(e->editSelected()); btnDel.addActionListener(e->deleteSelected()); btnExport.addActionListener(e-> ExcelExporter.exportToCSV(table,"NhanVien",this));
    }

    void loadTable(){ fillTable("SELECT nv.*,tk.username,tk.role FROM NhanVien nv LEFT JOIN TaiKhoan tk ON tk.idNhanVien=nv.id ORDER BY nv.id DESC", null); }
    private void search(){ String kw=txtSearch.getText().trim(), tt=(String)cbTrangThai.getSelectedItem(); String sql="SELECT nv.*,tk.username,tk.role FROM NhanVien nv LEFT JOIN TaiKhoan tk ON tk.idNhanVien=nv.id WHERE (nv.tenNhanVien LIKE ? OR nv.maNhanVien LIKE ?)" + (!"Trạng thái".equals(tt)?" AND nv.trangThai=N'"+tt+"'":"") + " ORDER BY nv.id DESC"; fillTable(sql, kw); }
    private void fillTable(String sql, String kw){ model.setRowCount(0); try(Connection c=DatabaseHelper.getConnection(); PreparedStatement ps=c.prepareStatement(sql)){ if(kw!=null){ ps.setString(1,"%"+kw+"%"); ps.setString(2,"%"+kw+"%"); } ResultSet rs=ps.executeQuery(); while(rs.next()) model.addRow(new Object[]{rs.getInt("id"),rs.getString("maNhanVien"),rs.getString("tenNhanVien"),rs.getString("username"),rs.getString("role"),rs.getDate("ngaySinh"),rs.getString("gioiTinh"),rs.getString("sdt"),rs.getString("email"),rs.getString("trangThai")}); } catch(Exception e){e.printStackTrace();}}

    private void editSelected(){ int row=table.getSelectedRow(); if(row<0){JOptionPane.showMessageDialog(this,"Chọn dòng cần sửa!");return;} openForm(row);}    
    private void deleteSelected(){
        int row=table.getSelectedRow(); if(row<0){JOptionPane.showMessageDialog(this,"Chọn dòng cần xóa!");return;}
        int id=(int)model.getValueAt(row,0); String status=str(model.getValueAt(row,9));
        if("Đang làm".equalsIgnoreCase(status)){ JOptionPane.showMessageDialog(this,"Không thể xóa nhân viên đang ở trạng thái Đang làm."); return; }
        if(Session.getCurrentUserId()==id){ JOptionPane.showMessageDialog(this,"Không thể xóa chính tài khoản đang đăng nhập."); return; }
        try(Connection c=DatabaseHelper.getConnection(); PreparedStatement ps=c.prepareStatement("DELETE FROM NhanVien WHERE id=?")){ ps.setInt(1,id); ps.executeUpdate(); NhatKyDAO.log(Session.getCurrentUserId(),"XÓA","Nhân viên","Xóa nhân viên ID: "+id); loadTable(); } catch(Exception ex){JOptionPane.showMessageDialog(this,"Lỗi: "+ex.getMessage());}
    }

    private void openForm(Integer row){
        boolean isEdit=row!=null; JDialog dlg=new JDialog((Frame)SwingUtilities.getWindowAncestor(this),isEdit?"Sửa nhân viên":"Thêm nhân viên",true); dlg.setSize(700,420); dlg.setLocationRelativeTo(this);
        JPanel form=new JPanel(new GridLayout(0,2,8,8)); form.setBorder(new EmptyBorder(16,16,16,16));
        JTextField txtMa=new JTextField(),txtTen=new JTextField(),txtSdt=new JTextField(),txtEmail=new JTextField(),txtUser=new JTextField();
        JComboBox<String> cbGT=new JComboBox<>(new String[]{"Nam","Nữ"});
        JComboBox<String> cbRole=new JComboBox<>(new String[]{"Nhân viên","Admin"});
        JSpinner spNgay = new JSpinner(new SpinnerDateModel()); spNgay.setEditor(new JSpinner.DateEditor(spNgay,"yyyy-MM-dd"));
        txtMa.setEditable(false);
        if(isEdit){ txtMa.setText(str(model.getValueAt(row,1))); txtTen.setText(str(model.getValueAt(row,2))); txtUser.setText(str(model.getValueAt(row,3))); cbRole.setSelectedItem(str(model.getValueAt(row,4))); cbGT.setSelectedItem(str(model.getValueAt(row,6))); txtSdt.setText(str(model.getValueAt(row,7))); txtEmail.setText(str(model.getValueAt(row,8))); }
        else { txtMa.setText(nextCode()); }

    addPair(form,"Mã nhân viên *",txtMa); addPair(form,"Họ tên *",txtTen); addPair(form,"Ngày sinh *",spNgay); addPair(form,"Giới tính *",cbGT);
        addPair(form,"SĐT *",txtSdt); addPair(form,"Email *",txtEmail); addPair(form,"Username *",txtUser); addPair(form,"Vai trò *",cbRole);

        JButton save=UITheme.primaryButton("Lưu"); save.addActionListener(e->{
            String ten=txtTen.getText().trim(), sdt=txtSdt.getText().trim(), email=txtEmail.getText().trim(), user=txtUser.getText().trim();
            LocalDate ns = new java.sql.Date(((java.util.Date)spNgay.getValue()).getTime()).toLocalDate();
            if(ten.isEmpty()||sdt.isEmpty()||email.isEmpty()||user.isEmpty()){JOptionPane.showMessageDialog(dlg,"Vui lòng nhập đầy đủ thông tin.");return;}
            if(!ValidationHelper.isValidSdt(sdt)){JOptionPane.showMessageDialog(dlg,"SĐT không hợp lệ.");return;}
            if(!ValidationHelper.isValidEmail(email)){JOptionPane.showMessageDialog(dlg,"Email không hợp lệ.");return;}
            if(ns.isAfter(LocalDate.now())){JOptionPane.showMessageDialog(dlg,"Ngày sinh không được lớn hơn hiện tại.");return;}
            try(Connection c=DatabaseHelper.getConnection()){
                c.setAutoCommit(false);
                if(existsUsername(c,user,isEdit? (int)model.getValueAt(row,0) : -1)){ JOptionPane.showMessageDialog(dlg,"Username đã tồn tại."); return; }
                if(!isEdit){
                    PreparedStatement ps=c.prepareStatement("INSERT INTO NhanVien(maNhanVien,tenNhanVien,ngaySinh,gioiTinh,sdt,email,trangThai) VALUES(?,?,?,?,?,?,N'Đang làm')",Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1,txtMa.getText().trim()); ps.setString(2,ten); ps.setDate(3, Date.valueOf(ns)); ps.setString(4,(String)cbGT.getSelectedItem()); ps.setString(5,sdt); ps.setString(6,email); ps.executeUpdate();
                    ResultSet k=ps.getGeneratedKeys(); k.next(); int idNv=k.getInt(1);
                    String role = "Admin".equals(cbRole.getSelectedItem()) ? "Admin" : "NhanVien";
                    String pw = "Admin".equals(cbRole.getSelectedItem()) ? "admin1111" : "nhanvien1111";
                    PreparedStatement at=c.prepareStatement("INSERT INTO TaiKhoan(username,password,role,idNhanVien) VALUES(?,?,?,?)"); at.setString(1,user); at.setString(2,pw); at.setString(3,role); at.setInt(4,idNv); at.executeUpdate();
                    NhatKyDAO.log(Session.getCurrentUserId(),"THÊM","Nhân viên","Thêm nhân viên "+txtMa.getText().trim());
                } else {
                    int id=(int)model.getValueAt(row,0);
                    PreparedStatement ps=c.prepareStatement("UPDATE NhanVien SET tenNhanVien=?,ngaySinh=?,gioiTinh=?,sdt=?,email=? WHERE id=?"); ps.setString(1,ten); ps.setDate(2,Date.valueOf(ns)); ps.setString(3,(String)cbGT.getSelectedItem()); ps.setString(4,sdt); ps.setString(5,email); ps.setInt(6,id); ps.executeUpdate();
                    String role = "Admin".equals(cbRole.getSelectedItem()) ? "Admin" : "NhanVien";
                    PreparedStatement t=c.prepareStatement("UPDATE TaiKhoan SET username=?, role=? WHERE idNhanVien=?"); t.setString(1,user); t.setString(2,role); t.setInt(3,id); t.executeUpdate();
                    NhatKyDAO.log(Session.getCurrentUserId(),"SỬA","Nhân viên","Sửa nhân viên ID: "+id);
                }
                c.commit(); loadTable(); dlg.dispose();
            } catch(Exception ex){ JOptionPane.showMessageDialog(dlg,"Lỗi: "+ex.getMessage()); }
        });
        JPanel wrap=new JPanel(new BorderLayout()); wrap.add(form,BorderLayout.CENTER); JPanel b=new JPanel(new FlowLayout(FlowLayout.RIGHT)); b.add(save); wrap.add(b,BorderLayout.SOUTH);
        dlg.add(wrap); dlg.setVisible(true);
    }

    private boolean existsUsername(Connection c, String u, int idNv) throws SQLException {
        PreparedStatement ps=c.prepareStatement("SELECT 1 FROM TaiKhoan WHERE username=? AND (?<0 OR idNhanVien<>?)"); ps.setString(1,u); ps.setInt(2,idNv); ps.setInt(3,idNv); return ps.executeQuery().next();
    }
    private String nextCode(){ try(Connection c=DatabaseHelper.getConnection(); Statement s=c.createStatement(); ResultSet rs=s.executeQuery("SELECT ISNULL(MAX(id),0)+1 n FROM NhanVien")){ rs.next(); return String.format("NV%03d", rs.getInt(1)); } catch(Exception e){ return "NV001"; }}
    private void addPair(JPanel p, String l, JComponent c){ p.add(new JLabel(l)); p.add(c);} 
    private String str(Object o){ return o==null?"":o.toString(); }
}