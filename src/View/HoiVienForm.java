package View;

import Util.*;
import database.DatabaseHelper;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
//import java.net.URL;
import java.sql.*;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;

public class HoiVienForm extends JPanel {

    private StyledTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private JComboBox<String> cbThang, cbNam, cbTrangThai;

    // Column indices in table model
    private static final int COL_ID       = 0;
    private static final int COL_MA       = 1;
    private static final int COL_TEN      = 2;
    private static final int COL_NGAYSINH = 3;
    private static final int COL_GIOITINH = 4;
    private static final int COL_SDT      = 5;
    private static final int COL_EMAIL    = 6;
    private static final int COL_TRANGTHAI= 7;
    private static final int COL_NGAYTG   = 8;
    private static final int COL_HINHANH  = 9;
    private static final int COL_DIACHI   = 10;

    public HoiVienForm() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        // ===== HEADER =====
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));
        JLabel title = new JLabel("Quản lý Hội viên");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);
        JButton btnAdd = UITheme.primaryButton("+ Thêm mới");
        header.add(title, BorderLayout.WEST);
        header.add(btnAdd, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ===== FILTER BAR =====
        JPanel filterCard = createCard();
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 8));
        txtSearch = new JTextField(18);
        txtSearch.setFont(UITheme.FONT_LABEL);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        cbThang = new JComboBox<>(new String[]{"Tháng","T1","T2","T3","T4","T5","T6","T7","T8","T9","T10","T11","T12"});
        cbNam   = new JComboBox<>(new String[]{"Năm","2023","2024","2025","2026"});
        cbTrangThai = new JComboBox<>(new String[]{"Trạng thái","Hoạt động","Tạm dừng","Đã rời"});
        styleCombo(cbThang); styleCombo(cbNam); styleCombo(cbTrangThai);

        JButton btnSearch = UITheme.primaryButton("🔍 Tìm kiếm");
        JButton btnReset  = UITheme.outlineButton("↺ Đặt lại");
        JButton btnExport = UITheme.outlineButton("📥 Xuất Excel");

        filterCard.add(new JLabel("Tìm kiếm:"));
        filterCard.add(txtSearch);
        filterCard.add(cbThang);
        filterCard.add(cbNam);
        filterCard.add(cbTrangThai);
        filterCard.add(btnSearch);
        filterCard.add(btnReset);
        filterCard.add(Box.createHorizontalStrut(10));
        filterCard.add(btnExport);

        // ===== TABLE =====
        model = new DefaultTableModel(new String[]{
            "ID","Mã HV","Họ tên","Ngày sinh","Giới tính","SĐT","Email","Trạng thái","Ngày tham gia","Hình ảnh","Địa chỉ"
        }, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new StyledTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.getColumnModel().getColumn(COL_ID).setPreferredWidth(40);
        table.getColumnModel().getColumn(COL_MA).setPreferredWidth(70);
        table.getColumnModel().getColumn(COL_TEN).setPreferredWidth(160);
        table.getColumnModel().getColumn(COL_TRANGTHAI).setPreferredWidth(90);
        // Hiển thị cột hình ảnh trong grid
        table.getColumnModel().getColumn(COL_HINHANH).setPreferredWidth(220);
        table.getColumnModel().getColumn(COL_DIACHI).setMinWidth(0);
        table.getColumnModel().getColumn(COL_DIACHI).setMaxWidth(0);
        table.getColumnModel().getColumn(COL_DIACHI).setWidth(0);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) showDetail(row);
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        scrollPane.getViewport().setBackground(Color.WHITE);

        // ===== TABLE CARD =====
        JPanel tableCard = createCard();
        tableCard.setLayout(new BorderLayout());
        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        tableHeader.setBorder(new EmptyBorder(0, 0, 12, 0));
        JLabel tblTitle = new JLabel("Danh sách hội viên");
        tblTitle.setFont(UITheme.FONT_HEADING);
        tblTitle.setForeground(UITheme.TEXT_PRIMARY);

        JPanel tblActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tblActions.setOpaque(false);
        JButton btnEdit = UITheme.outlineButton("✏ Sửa");
        JButton btnDel  = UITheme.dangerButton("🗑 Xóa");
        tblActions.add(btnEdit);
        tblActions.add(btnDel);
        tableHeader.add(tblTitle, BorderLayout.WEST);
        tableHeader.add(tblActions, BorderLayout.EAST);
        tableCard.add(tableHeader, BorderLayout.NORTH);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(filterCard, BorderLayout.NORTH);
        center.add(tableCard, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        // Events
        loadTable();
        btnSearch.addActionListener(e -> search());
        btnReset.addActionListener(e -> { txtSearch.setText(""); loadTable(); });
        btnAdd.addActionListener(e -> openForm(null));
        btnEdit.addActionListener(e -> editSelected());
        btnDel.addActionListener(e -> deleteSelected());
        btnExport.addActionListener(e -> ExcelExporter.exportToCSV(table, "HoiVien", this));
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) search();
            }
        });
    }

    // ========== LOAD TABLE ==========
    public void loadTable() {
        model.setRowCount(0);
        String sql = "SELECT * FROM HoiVien ORDER BY id DESC";
        try (Connection conn = DatabaseHelper.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                String ngaySinhDisplay = ValidationHelper.toDisplayDate(
                    rs.getDate("ngaySinh") != null ? rs.getDate("ngaySinh").toString() : "");
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("maHoiVien"),
                    rs.getString("tenHoiVien"),
                    ngaySinhDisplay,
                    rs.getString("gioiTinh"),
                    rs.getString("sdt"),
                    rs.getString("email"),
                    rs.getString("trangThai"),
                    rs.getDate("ngayThamGia"),
                    rs.getString("hinhAnh"),
                    rs.getString("diaChi")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ========== SEARCH ==========
    private void search() {
        model.setRowCount(0);
        String kw     = txtSearch.getText().trim();
        String thang  = (String) cbThang.getSelectedItem();
        String nam    = (String) cbNam.getSelectedItem();
        String tt     = (String) cbTrangThai.getSelectedItem();

        StringBuilder sql = new StringBuilder(
            "SELECT * FROM HoiVien WHERE (tenHoiVien LIKE ? OR maHoiVien LIKE ?)");
        if (!"Tháng".equals(thang)) sql.append(" AND MONTH(ngayThamGia)=").append(thang.replace("T",""));
        if (!"Năm".equals(nam))     sql.append(" AND YEAR(ngayThamGia)=").append(nam);
        if (!"Trạng thái".equals(tt)) sql.append(" AND trangThai=N'").append(tt).append("'");
        sql.append(" ORDER BY id DESC");

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, "%" + kw + "%");
            ps.setString(2, "%" + kw + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String ngaySinhDisplay = ValidationHelper.toDisplayDate(
                    rs.getDate("ngaySinh") != null ? rs.getDate("ngaySinh").toString() : "");
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("maHoiVien"),
                    rs.getString("tenHoiVien"),
                    ngaySinhDisplay,
                    rs.getString("gioiTinh"),
                    rs.getString("sdt"),
                    rs.getString("email"),
                    rs.getString("trangThai"),
                    rs.getDate("ngayThamGia"),
                    rs.getString("hinhAnh"),
                    rs.getString("diaChi")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ========== SHOW DETAIL ==========
    private void showDetail(int row) {
        String ten      = str(model.getValueAt(row, COL_TEN));
        String ma       = str(model.getValueAt(row, COL_MA));
        String ngaySinh = str(model.getValueAt(row, COL_NGAYSINH));
        String gt       = str(model.getValueAt(row, COL_GIOITINH));
        String sdt      = str(model.getValueAt(row, COL_SDT));
        String email    = str(model.getValueAt(row, COL_EMAIL));
        String tt       = str(model.getValueAt(row, COL_TRANGTHAI));
        String hinhAnh  = str(model.getValueAt(row, COL_HINHANH));
        String diaChi   = str(model.getValueAt(row, COL_DIACHI));

        JDialog dlg = createDialog("Chi tiết hội viên", 480, 520);
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);

        // Header xanh
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UITheme.PRIMARY);
        topBar.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Avatar + tên
        JPanel topLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        topLeft.setOpaque(false);
        JPanel avatarPanel = createAvatarPanel(ten, hinhAnh, 56);
        JPanel namePanel = new JPanel(new BorderLayout());
        namePanel.setOpaque(false);
        JLabel lblTen = new JLabel(ten);
        lblTen.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTen.setForeground(Color.WHITE);
        JLabel lblMa = new JLabel(ma);
        lblMa.setFont(UITheme.FONT_SMALL);
        lblMa.setForeground(new Color(255, 255, 255, 200));
        namePanel.add(lblTen, BorderLayout.NORTH);
        namePanel.add(lblMa, BorderLayout.SOUTH);
        topLeft.add(avatarPanel);
        topLeft.add(namePanel);
        topBar.add(topLeft, BorderLayout.CENTER);

        // Info grid
        JPanel info = new JPanel(new GridBagLayout());
        info.setBackground(Color.WHITE);
        info.setBorder(new EmptyBorder(18, 24, 10, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 4, 5, 4);
        gc.anchor = GridBagConstraints.WEST;

        addDetailRow(info, gc, 0, "Ngày sinh:",     ngaySinh);
        addDetailRow(info, gc, 1, "Giới tính:",     gt);
        addDetailRow(info, gc, 2, "Số điện thoại:", sdt);
        addDetailRow(info, gc, 3, "Email:",         email);
        addDetailRow(info, gc, 4, "Địa chỉ:",       diaChi);
        addDetailRow(info, gc, 5, "Trạng thái:",    tt);
        if (!hinhAnh.isEmpty()) {
            gc.gridx=0; gc.gridy=6; gc.gridwidth=1;
            JLabel lHa = new JLabel("Hình ảnh:");
            lHa.setFont(UITheme.FONT_BOLD);
            lHa.setForeground(UITheme.TEXT_SECONDARY);
            lHa.setPreferredSize(new Dimension(130, 26));
            info.add(lHa, gc);
            gc.gridx=1;
            JLabel lUrl = new JLabel("<html><a href='" + hinhAnh + "'>" +
                (hinhAnh.length() > 40 ? hinhAnh.substring(0, 40) + "..." : hinhAnh) + "</a></html>");
            lUrl.setFont(UITheme.FONT_SMALL);
            lUrl.setForeground(UITheme.PRIMARY);
            info.add(lUrl, gc);
        }

        // Buttons
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        btns.setBackground(Color.decode("#F8FAFC"));
        btns.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));
        JButton btnEdit  = UITheme.outlineButton("✏ Chỉnh sửa");
        JButton btnClose = UITheme.primaryButton("Đóng");
        btns.add(btnEdit);
        btns.add(btnClose);

        content.add(topBar, BorderLayout.NORTH);
        content.add(new JScrollPane(info), BorderLayout.CENTER);
        content.add(btns, BorderLayout.SOUTH);
        dlg.add(content);
        btnClose.addActionListener(e -> dlg.dispose());
        btnEdit.addActionListener(e -> { dlg.dispose(); openForm(row); });
        dlg.setVisible(true);
    }

    // ========== OPEN FORM (ADD / EDIT) ==========
    private void openForm(Integer row) {
        boolean isEdit = row != null;
        JDialog dlg = createDialog(isEdit ? "Chỉnh sửa hội viên" : "Thêm hội viên mới", 560, 620);
        dlg.setResizable(false);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);

        // Form Header
        JPanel fh = new JPanel(new BorderLayout());
        fh.setBackground(UITheme.PRIMARY);
        fh.setBorder(new EmptyBorder(14, 20, 14, 20));
        JLabel hl = new JLabel(isEdit ? "✏  Chỉnh sửa hội viên" : "➕  Thêm hội viên mới");
        hl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        hl.setForeground(Color.WHITE);
        fh.add(hl);

        // Fields panel
        JPanel fields = new JPanel(new GridBagLayout());
        fields.setBackground(Color.WHITE);
        fields.setBorder(new EmptyBorder(16, 24, 10, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 4, 5, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;

        // Mã HV - tự động, không cho sửa
        JTextField txtMa = new JTextField();
        txtMa.setFont(UITheme.FONT_LABEL);
        txtMa.setPreferredSize(new Dimension(260, 34));
        txtMa.setEditable(false);
        txtMa.setBackground(UITheme.BG_MAIN);
        txtMa.setForeground(UITheme.TEXT_SECONDARY);
        styleBorder(txtMa);

        // Họ tên
        PlaceholderTextField txtTen = new PlaceholderTextField("Nhập họ và tên *");
        styleFld(txtTen);

        // Ngày sinh
        PlaceholderTextField txtNgaySinh = new PlaceholderTextField("dd/MM/yyyy  (VD: 15/03/2000)");
        styleFld(txtNgaySinh);

        // Giới tính
        JComboBox<String> cbGT = new JComboBox<>(new String[]{"Nam", "Nữ"});
        cbGT.setFont(UITheme.FONT_LABEL);
        cbGT.setBackground(Color.WHITE);

        // SĐT
        PlaceholderTextField txtSdt = new PlaceholderTextField("VD: 0901234567 *");
        styleFld(txtSdt);

        // Email
        PlaceholderTextField txtEmail = new PlaceholderTextField("VD: example@gmail.com *");
        styleFld(txtEmail);

        // Địa chỉ
        PlaceholderTextField txtDiaChi = new PlaceholderTextField("Nhập địa chỉ *");
        styleFld(txtDiaChi);

        // Trạng thái
        JComboBox<String> cbTT = new JComboBox<>(new String[]{"Hoạt động", "Tạm dừng", "Đã rời"});
        cbTT.setFont(UITheme.FONT_LABEL);
        cbTT.setBackground(Color.WHITE);

        // Ảnh panel
        final String[] selectedImagePath = {""};
        JPanel imgPanel = new JPanel(new BorderLayout(10, 0));
        imgPanel.setOpaque(false);
        JLabel lblPreview = new JLabel("Chưa có ảnh") {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_MAIN);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.setColor(UITheme.BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                super.paintComponent(g);
            }
        };
        lblPreview.setPreferredSize(new Dimension(80, 80));
        lblPreview.setHorizontalAlignment(SwingConstants.CENTER);
        lblPreview.setFont(UITheme.FONT_SMALL);
        lblPreview.setForeground(UITheme.TEXT_MUTED);

        JPanel imgBtns = new JPanel(new GridLayout(3, 1, 0, 6));
        imgBtns.setOpaque(false);
        JButton btnChooseImg = UITheme.outlineButton("📁 Chọn ảnh");
        JLabel lblImgStatus  = new JLabel("(chưa chọn)");
        lblImgStatus.setFont(UITheme.FONT_SMALL);
        lblImgStatus.setForeground(UITheme.TEXT_MUTED);
        JLabel lblNote = new JLabel("<html><i>Ảnh được chọn từ máy của bạn</i></html>");
        lblNote.setFont(UITheme.FONT_SMALL);
        lblNote.setForeground(UITheme.TEXT_MUTED);

        imgBtns.add(btnChooseImg);
        imgBtns.add(lblImgStatus);
        imgBtns.add(lblNote);
        imgPanel.add(lblPreview, BorderLayout.WEST);
        imgPanel.add(imgBtns, BorderLayout.CENTER);

        // Gán dữ liệu khi sửa
        if (isEdit) {
            String maCurrent = str(model.getValueAt(row, COL_MA));
            txtMa.setText(maCurrent);
            txtTen.setText(str(model.getValueAt(row, COL_TEN)));
            txtNgaySinh.setText(str(model.getValueAt(row, COL_NGAYSINH)));
            cbGT.setSelectedItem(str(model.getValueAt(row, COL_GIOITINH)));
            txtSdt.setText(str(model.getValueAt(row, COL_SDT)));
            txtEmail.setText(str(model.getValueAt(row, COL_EMAIL)));
            txtDiaChi.setText(str(model.getValueAt(row, COL_DIACHI)));
            cbTT.setSelectedItem(str(model.getValueAt(row, COL_TRANGTHAI)));
            selectedImagePath[0] = str(model.getValueAt(row, COL_HINHANH));
            if (!selectedImagePath[0].isEmpty()) {
                lblImgStatus.setText("Đã có ảnh");
                lblImgStatus.setForeground(UITheme.SUCCESS);
                loadImagePreview(lblPreview, selectedImagePath[0]);
            }
        } else {
            // Auto-generate mã khi thêm mới
            try (Connection conn = DatabaseHelper.getConnection()) {
                ResultSet rs = conn.createStatement().executeQuery(
                    "SELECT ISNULL(MAX(id), 0) + 1 AS nextId FROM HoiVien");
                if (rs.next()) txtMa.setText(String.format("HV%03d", rs.getInt("nextId")));
            } catch (Exception ex) { txtMa.setText("HV001"); }
        }

        // Layout fields
        int r = 0;
        addFormRow(fields, gc, r++, "Mã hội viên",         txtMa);
        addFormRow(fields, gc, r++, "Họ và tên *",          txtTen);
        addFormRow(fields, gc, r++, "Ngày sinh *",          txtNgaySinh);
        addFormRow(fields, gc, r++, "Giới tính *",          cbGT);
        addFormRow(fields, gc, r++, "Số điện thoại *",      txtSdt);
        addFormRow(fields, gc, r++, "Email *",               txtEmail);
        addFormRow(fields, gc, r++, "Địa chỉ *",            txtDiaChi);
        addFormRow(fields, gc, r++, "Trạng thái",           cbTT);
        addFormRow(fields, gc, r++, "Hình ảnh",             imgPanel);

        // Chọn ảnh
        btnChooseImg.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("Ảnh (jpg, png, gif, webp)", "jpg","jpeg","png","gif","webp"));
            if (fc.showOpenDialog(dlg) == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                if (!isValidLocalImage(f)) {
                    JOptionPane.showMessageDialog(dlg, "File không hợp lệ! Chỉ chấp nhận jpg, png, gif, webp.");
                    return;
                }
                selectedImagePath[0] = f.getAbsolutePath();
                lblImgStatus.setText("Đã chọn: " + f.getName());
                lblImgStatus.setForeground(UITheme.SUCCESS);
            }
        });

        // Buttons
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        btns.setBackground(Color.decode("#F8FAFC"));
        btns.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));
        JButton btnCancel = UITheme.outlineButton("Hủy");
        JButton btnSave   = UITheme.primaryButton(isEdit ? "Lưu thay đổi" : "Thêm mới");
        btns.add(btnCancel);
        btns.add(btnSave);

        JScrollPane fieldsScroll = new JScrollPane(fields);
        fieldsScroll.setBorder(null);
        fieldsScroll.getVerticalScrollBar().setUnitIncrement(12);

        content.add(fh, BorderLayout.NORTH);
        content.add(fieldsScroll, BorderLayout.CENTER);
        content.add(btns, BorderLayout.SOUTH);
        dlg.add(content);

        btnCancel.addActionListener(e -> dlg.dispose());
        btnSave.addActionListener(e -> {
            // ===== VALIDATION =====
            String ten     = txtTen.getText().trim();
            String ngaySinh= txtNgaySinh.getText().trim();
            String sdt     = txtSdt.getText().trim();
            String email   = txtEmail.getText().trim();
            String diaChi  = txtDiaChi.getText().trim();
            String gt      = (String) cbGT.getSelectedItem();
            String ttVal   = (String) cbTT.getSelectedItem();

            StringBuilder errors = new StringBuilder();
            if (!ValidationHelper.isNotEmpty(ten))
                errors.append("• Họ và tên không được để trống\n");
            if (!ValidationHelper.isValidNgaySinh(ngaySinh))
                errors.append("• Ngày sinh không hợp lệ (định dạng: dd/MM/yyyy)\n");
            if (!ValidationHelper.isValidSdt(sdt))
                errors.append("• Số điện thoại không hợp lệ (VD: 0901234567)\n");
            if (!ValidationHelper.isValidEmail(email))
                errors.append("• Email không hợp lệ (VD: example@gmail.com)\n");
            if (!ValidationHelper.isNotEmpty(diaChi))
                errors.append("• Địa chỉ không được để trống\n");

            if (errors.length() > 0) {
                JOptionPane.showMessageDialog(dlg,
                    "Vui lòng kiểm tra lại:\n" + errors.toString(),
                    "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
                return;
            }
            

            // Chuyển ngày sinh sang yyyy-MM-dd
            String ngaySinhSql = ValidationHelper.toSqlDate(ngaySinh);
            String maHV = txtMa.getText().trim();

            try (Connection conn = DatabaseHelper.getConnection()) {
                if (!isEdit) {
                    PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO HoiVien(maHoiVien,tenHoiVien,ngaySinh,gioiTinh,sdt,email,diaChi,hinhAnh,trangThai) "
                      + "VALUES(?,?,?,?,?,?,?,?,?)");
                    ps.setString(1, maHV);
                    ps.setString(2, ten);
                    if (ngaySinhSql != null) ps.setDate(3, Date.valueOf(ngaySinhSql));
                    else ps.setNull(3, Types.DATE);
                    ps.setString(4, gt);
                    ps.setString(5, sdt);
                    ps.setString(6, email);
                    ps.setString(7, diaChi);
                    ps.setString(8, selectedImagePath[0]);
                    ps.setString(9, ttVal);
                    ps.executeUpdate();
                } else {
                    int id = (int) model.getValueAt(row, COL_ID);
                    PreparedStatement ps = conn.prepareStatement(
                        "UPDATE HoiVien SET tenHoiVien=?,ngaySinh=?,gioiTinh=?,sdt=?,email=?,diaChi=?,hinhAnh=?,trangThai=? WHERE id=?");
                    ps.setString(1, ten);
                    if (ngaySinhSql != null) ps.setDate(2, Date.valueOf(ngaySinhSql));
                    else ps.setNull(2, Types.DATE);
                    ps.setString(3, gt);
                    ps.setString(4, sdt);
                    ps.setString(5, email);
                    ps.setString(6, diaChi);
                    ps.setString(7, selectedImagePath[0]);
                    ps.setString(8, ttVal);
                    ps.setInt(9, id);
                    ps.executeUpdate();
                }
                loadTable();
                dlg.dispose();
                JOptionPane.showMessageDialog(this,
                    isEdit ? "Cập nhật thành công!" : "Thêm mới thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Lỗi: " + ex.getMessage());
            }
        });

        dlg.setVisible(true);
    }

    // ========== DELETE ==========
    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng cần sửa!"); return; }
        openForm(row);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng cần xóa!"); return; }
        int id   = (int) model.getValueAt(row, COL_ID);
        String name = str(model.getValueAt(row, COL_TEN));
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn xóa hội viên\n\"" + name + "\" không?",
            "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM HoiVien WHERE id=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
                loadTable();
                JOptionPane.showMessageDialog(this, "Xóa thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa: " + e.getMessage());
            }
        }
    }

    // ========== HELPERS ==========
    private JPanel createAvatarPanel(String name, String imageUrl, int size) {

    JPanel panel = new JPanel(new BorderLayout());
    panel.setPreferredSize(new Dimension(size, size));
    panel.setOpaque(false);

    JLabel lbl = new JLabel();
    lbl.setHorizontalAlignment(SwingConstants.CENTER);

    if (imageUrl != null && !imageUrl.isEmpty()) {
        new Thread(() -> {
            try {
                BufferedImage img = readImage(imageUrl);
                if (img != null) {
                    Image scaled = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);

                SwingUtilities.invokeLater(() -> {
                        lbl.setIcon(new ImageIcon(scaled));
                    });
                    return;
                }

            } catch (Exception e) {
                // ignore
            }
            lbl.setText(name.substring(0,1).toUpperCase());
        }).start();

    } else {
        lbl.setText(name.substring(0,1).toUpperCase());
    }

    lbl.setFont(new Font("Segoe UI", Font.BOLD, size/2));
    lbl.setForeground(UITheme.PRIMARY);

    panel.add(lbl);
    return panel;
}

    private void loadImagePreview(JLabel lbl, String url) {
        if (url == null || url.isEmpty()) return;
        new Thread(() -> {
            try {
//              BufferedImage img = ImageIO.read(new URL(url));
                BufferedImage img = readImage(url);
                if (img != null) {
                    Image scaled = img.getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                    SwingUtilities.invokeLater(() -> {
                        lbl.setIcon(new ImageIcon(scaled));
                        lbl.setText("");
                    });
                }
            } catch (Exception e) { /* ignore */ }
        }).start();
    }

    private void loadLocalPreview(JLabel lbl, File f) {
        try {
            BufferedImage img = ImageIO.read(f);
            if (img != null) {
                Image scaled = img.getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                lbl.setIcon(new ImageIcon(scaled));
                lbl.setText("");
            }
        } catch (Exception e) { /* ignore */ }
    }
    
    private boolean isValidLocalImage(File f) {
        if (f == null || !f.exists() || !f.isFile()) return false;
        String name = f.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg")
            || name.endsWith(".png") || name.endsWith(".gif") || name.endsWith(".webp");
    }

    private BufferedImage readImage(String pathOrUrl) throws Exception {
        if (pathOrUrl == null || pathOrUrl.isEmpty()) return null;
        if (pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://")) {
            return ImageIO.read(new java.net.URL(pathOrUrl));
        }
        return ImageIO.read(new File(pathOrUrl));
    }

    private JPanel createCard() {
        JPanel p = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.setColor(UITheme.BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(14, 16, 14, 16));
        return p;
    }

    private JDialog createDialog(String title, int w, int h) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dlg.setSize(w, h);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(Color.WHITE);
        return dlg;
    }

    private void styleFld(JTextField f) {
        f.setFont(UITheme.FONT_LABEL);
        f.setPreferredSize(new Dimension(280, 34));
        styleBorder(f);
    }

    private void styleBorder(JTextField f) {
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
    }

    private void addFormRow(JPanel p, GridBagConstraints gc, int y, String label, JComponent field) {
        gc.gridx=0; gc.gridy=y; gc.weightx=0; gc.gridwidth=1;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_SECONDARY);
        lbl.setPreferredSize(new Dimension(150, 26));
        p.add(lbl, gc);
        gc.gridx=1; gc.weightx=1;
        p.add(field, gc);
    }

    private void addDetailRow(JPanel p, GridBagConstraints gc, int y, String label, String value) {
        gc.gridx=0; gc.gridy=y; gc.gridwidth=1;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_SECONDARY);
        lbl.setPreferredSize(new Dimension(140, 26));
        p.add(lbl, gc);
        gc.gridx=1;
        JLabel val = new JLabel(value != null && !value.isEmpty() ? value : "—");
        val.setFont(UITheme.FONT_LABEL);
        val.setForeground(UITheme.TEXT_PRIMARY);
        p.add(val, gc);
    }

    private void styleCombo(JComboBox<String> cb) {
        cb.setFont(UITheme.FONT_LABEL);
        cb.setBackground(Color.WHITE);
        cb.setPreferredSize(new Dimension(110, 32));
    }

    private String str(Object o) { return o == null ? "" : o.toString(); }
}