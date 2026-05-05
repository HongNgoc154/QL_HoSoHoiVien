package View;

import Util.UITheme;
import Util.StyledTable;
import Util.ExcelExporter;
import Util.Session;
import Util.StyledTable;
import Util.UITheme;
import dao.NhatKyDAO;
import database.DatabaseHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class NhanVienForm extends JPanel {

    private StyledTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private JComboBox<String> cbTrangThai;

    public NhanVienForm() { /* unchanged layout init */
        setLayout(new BorderLayout()); setBackground(UITheme.BG_MAIN); setBorder(new EmptyBorder(24, 28, 24, 28));
        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false); header.setBorder(new EmptyBorder(0,0,16,0));
        JLabel title = new JLabel("Quản lý Nhân viên"); title.setFont(UITheme.FONT_TITLE); title.setForeground(UITheme.TEXT_PRIMARY);
        JButton btnAdd = UITheme.primaryButton("+ Thêm nhân viên"); header.add(title, BorderLayout.WEST); header.add(btnAdd, BorderLayout.EAST); add(header, BorderLayout.NORTH);
        JPanel filterCard = createCard(); filterCard.setLayout(new FlowLayout(FlowLayout.LEFT,10,8));
        txtSearch = new JTextField(18); txtSearch.setFont(UITheme.FONT_LABEL);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR),BorderFactory.createEmptyBorder(6,10,6,10)));
        cbTrangThai = new JComboBox<>(new String[]{"Trạng thái","Đang làm","Đã nghỉ"}); cbTrangThai.setFont(UITheme.FONT_LABEL); cbTrangThai.setPreferredSize(new Dimension(130,32));
        JButton btnSearch = UITheme.primaryButton("🔍 Tìm"), btnReset=UITheme.outlineButton("↺ Đặt lại"), btnExport=UITheme.outlineButton("📥 Xuất Excel");
        filterCard.add(new JLabel("Tìm:")); filterCard.add(txtSearch); filterCard.add(cbTrangThai); filterCard.add(btnSearch); filterCard.add(btnReset); filterCard.add(Box.createHorizontalStrut(10)); filterCard.add(btnExport);
        model = new DefaultTableModel(new String[]{"ID","Mã NV","Họ tên","Username","Vai trò","Ngày sinh","Giới tính","SĐT","Email","Trạng thái"},0){ public boolean isCellEditable(int r,int c){return false;}};
        table = new StyledTable(model);
        JScrollPane scroll = new JScrollPane(table); scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        JPanel tableCard = createCard(); tableCard.setLayout(new BorderLayout());
        JPanel tblHeader = new JPanel(new BorderLayout()); tblHeader.setOpaque(false); tblHeader.setBorder(new EmptyBorder(0,0,12,0));
        JLabel tblTitle = new JLabel("Danh sách nhân viên"); tblTitle.setFont(UITheme.FONT_HEADING);
        JPanel act = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); act.setOpaque(false);
        JButton btnEdit = UITheme.outlineButton("✏ Sửa"), btnDel = UITheme.dangerButton("🗑 Xóa"); act.add(btnEdit); act.add(btnDel);
        tblHeader.add(tblTitle, BorderLayout.WEST); tblHeader.add(act, BorderLayout.EAST); tableCard.add(tblHeader, BorderLayout.NORTH); tableCard.add(scroll, BorderLayout.CENTER);
        JPanel center = new JPanel(new BorderLayout(0,12)); center.setOpaque(false); center.add(filterCard, BorderLayout.NORTH); center.add(tableCard, BorderLayout.CENTER); add(center, BorderLayout.CENTER);

        loadTable(); btnSearch.addActionListener(e->search()); btnReset.addActionListener(e->{txtSearch.setText(""); loadTable();});
        btnAdd.addActionListener(e->openForm(null)); btnEdit.addActionListener(e->editSelected()); btnDel.addActionListener(e->deleteSelected()); btnExport.addActionListener(e-> ExcelExporter.exportToCSV(table,"NhanVien",this));
    }

    private JPanel createCard(){ JPanel p=new JPanel(); p.setOpaque(false); p.setBorder(new EmptyBorder(14,16,14,16)); return p; }

    void loadTable(){ fillTable("SELECT nv.*,tk.username,tk.role FROM NhanVien nv LEFT JOIN TaiKhoan tk ON tk.idNhanVien=nv.id ORDER BY nv.id DESC", null, null); }
    private void search(){ String kw=txtSearch.getText().trim(), tt=(String)cbTrangThai.getSelectedItem(); String sql="SELECT nv.*,tk.username,tk.role FROM NhanVien nv LEFT JOIN TaiKhoan tk ON tk.idNhanVien=nv.id WHERE (nv.tenNhanVien LIKE ? OR nv.maNhanVien LIKE ?)" + (!"Trạng thái".equals(tt)?" AND nv.trangThai=N'"+tt+"'":"") + " ORDER BY nv.id DESC"; fillTable(sql, kw, kw); }
    private void fillTable(String sql, String p1, String p2){ model.setRowCount(0); try(Connection c=DatabaseHelper.getConnection(); PreparedStatement ps=c.prepareStatement(sql)){ if(p1!=null){ ps.setString(1,"%"+p1+"%"); ps.setString(2,"%"+p2+"%"); } ResultSet rs=ps.executeQuery(); while(rs.next()) model.addRow(new Object[]{rs.getInt("id"),rs.getString("maNhanVien"),rs.getString("tenNhanVien"),rs.getString("username"),rs.getString("role"),rs.getDate("ngaySinh"),rs.getString("gioiTinh"),rs.getString("sdt"),rs.getString("email"),rs.getString("trangThai")}); } catch(Exception e){e.printStackTrace();}}

    private void editSelected(){ int row=table.getSelectedRow(); if(row<0){JOptionPane.showMessageDialog(this,"Chọn dòng cần sửa!");return;} openForm(row);}    
    private void deleteSelected(){ int row=table.getSelectedRow(); if(row<0){JOptionPane.showMessageDialog(this,"Chọn dòng cần xóa!");return;} int id=(int)model.getValueAt(row,0); String name=str(model.getValueAt(row,2)); try(Connection c=DatabaseHelper.getConnection()){ PreparedStatement ps=c.prepareStatement("DELETE FROM NhanVien WHERE id=?"); ps.setInt(1,id); ps.executeUpdate(); NhatKyDAO.log(Session.getCurrentUserId(),"XÓA","Nhân viên","Xóa nhân viên: "+name); loadTable(); } catch(Exception ex){JOptionPane.showMessageDialog(this,"Lỗi: "+ex.getMessage());}}

    private void openForm(Integer row){ boolean isEdit=row!=null; JDialog dlg=new JDialog((Frame)SwingUtilities.getWindowAncestor(this),isEdit?"Sửa nhân viên":"Thêm nhân viên",true); dlg.setSize(920,500); dlg.setLocationRelativeTo(this);
        JPanel fields=new JPanel(new GridLayout(1,2,12,0)); JPanel nv=new JPanel(new GridLayout(7,2,8,8)); JPanel tk=new JPanel(new GridLayout(3,2,8,8));
        JTextField txtMa=fld(),txtTen=fld(),txtNgay=fld(),txtGT=fld(),txtSdt=fld(),txtEmail=fld(),txtUser=fld(); JPasswordField txtPass=new JPasswordField(); JComboBox<String> cbTT=new JComboBox<>(new String[]{"Đang làm","Đã nghỉ"}), cbRole=new JComboBox<>(new String[]{"NhanVien","Admin"});
        if(isEdit){ txtMa.setText(str(model.getValueAt(row,1))); txtTen.setText(str(model.getValueAt(row,2))); txtUser.setText(str(model.getValueAt(row,3))); cbRole.setSelectedItem(str(model.getValueAt(row,4)).isEmpty()?"NhanVien":str(model.getValueAt(row,4))); txtNgay.setText(str(model.getValueAt(row,5))); txtGT.setText(str(model.getValueAt(row,6))); txtSdt.setText(str(model.getValueAt(row,7))); txtEmail.setText(str(model.getValueAt(row,8))); cbTT.setSelectedItem(str(model.getValueAt(row,9))); txtMa.setEditable(false);}        
        addPair(nv,"Mã nhân viên *",txtMa); addPair(nv,"Họ tên *",txtTen); addPair(nv,"Ngày sinh",txtNgay); addPair(nv,"Giới tính",txtGT); addPair(nv,"SĐT",txtSdt); addPair(nv,"Email",txtEmail); addPair(nv,"Trạng thái",cbTT);
        addPair(tk,"Username *",txtUser); addPair(tk,isEdit?"Mật khẩu mới":"Mật khẩu *",txtPass); addPair(tk,"Vai trò",cbRole); fields.add(nv); fields.add(tk);
        JButton save=UITheme.primaryButton("Lưu"), cancel=UITheme.outlineButton("Hủy"); JPanel btns=new JPanel(new FlowLayout(FlowLayout.RIGHT)); btns.add(cancel); btns.add(save); cancel.addActionListener(e->dlg.dispose());
        save.addActionListener(e->{ if(txtMa.getText().trim().isEmpty()||txtTen.getText().trim().isEmpty()||txtUser.getText().trim().isEmpty()||(!isEdit&&new String(txtPass.getPassword()).trim().isEmpty())){JOptionPane.showMessageDialog(dlg,"Thiếu thông tin bắt buộc");return;} try(Connection c=DatabaseHelper.getConnection()){ c.setAutoCommit(false); if(!isEdit){ PreparedStatement ps=c.prepareStatement("INSERT INTO NhanVien(maNhanVien,tenNhanVien,ngaySinh,gioiTinh,sdt,email,trangThai) VALUES(?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS); ps.setString(1,txtMa.getText().trim()); ps.setString(2,txtTen.getText().trim()); setDate(ps,3,txtNgay.getText().trim()); ps.setString(4,txtGT.getText().trim()); ps.setString(5,txtSdt.getText().trim()); ps.setString(6,txtEmail.getText().trim()); ps.setString(7,(String)cbTT.getSelectedItem()); ps.executeUpdate(); ResultSet k=ps.getGeneratedKeys(); int idNv=0; if(k.next()) idNv=k.getInt(1); PreparedStatement at=c.prepareStatement("INSERT INTO TaiKhoan(username,password,role,idNhanVien) VALUES(?,?,?,?)"); at.setString(1,txtUser.getText().trim()); at.setString(2,new String(txtPass.getPassword()).trim()); at.setString(3,(String)cbRole.getSelectedItem()); at.setInt(4,idNv); at.executeUpdate(); NhatKyDAO.log(Session.getCurrentUserId(),"THÊM","Nhân viên","Thêm nhân viên: "+txtTen.getText().trim()); } else { int id=(int)model.getValueAt(row,0); PreparedStatement ps=c.prepareStatement("UPDATE NhanVien SET tenNhanVien=?,ngaySinh=?,gioiTinh=?,sdt=?,email=?,trangThai=? WHERE id=?"); ps.setString(1,txtTen.getText().trim()); setDate(ps,2,txtNgay.getText().trim()); ps.setString(3,txtGT.getText().trim()); ps.setString(4,txtSdt.getText().trim()); ps.setString(5,txtEmail.getText().trim()); ps.setString(6,(String)cbTT.getSelectedItem()); ps.setInt(7,id); ps.executeUpdate(); String pw=new String(txtPass.getPassword()).trim(); PreparedStatement t=c.prepareStatement(pw.isEmpty()?"UPDATE TaiKhoan SET username=?, role=? WHERE idNhanVien=?":"UPDATE TaiKhoan SET username=?, role=?, password=? WHERE idNhanVien=?"); t.setString(1,txtUser.getText().trim()); t.setString(2,(String)cbRole.getSelectedItem()); if(pw.isEmpty()) t.setInt(3,id); else {t.setString(3,pw); t.setInt(4,id);} t.executeUpdate(); NhatKyDAO.log(Session.getCurrentUserId(),"SỬA","Nhân viên","Sửa nhân viên ID: "+id); } c.commit(); loadTable(); dlg.dispose(); } catch(Exception ex){JOptionPane.showMessageDialog(dlg,"Lỗi: "+ex.getMessage()); }});
        dlg.setLayout(new BorderLayout()); dlg.add(fields,BorderLayout.CENTER); dlg.add(btns,BorderLayout.SOUTH); dlg.setVisible(true);
    }

    private void addPair(JPanel p, String l, JComponent c){ p.add(new JLabel(l)); p.add(c);} private JTextField fld(){ return new JTextField(); }
    private void setDate(PreparedStatement ps, int idx, String v) throws SQLException { if(v.isEmpty()) ps.setNull(idx, Types.DATE); else ps.setDate(idx, Date.valueOf(v)); }
    private String str(Object o){ return o==null?"":o.toString(); }
}