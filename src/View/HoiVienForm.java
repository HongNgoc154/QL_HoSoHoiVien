package View;

import dao.NhatKyDAO;
import dao.ArchiveDAO;
import Util.*;
import database.DatabaseHelper;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * HoiVienForm – Quản lý hội viên.
 * Giao diện đồng nhất với card layout, gradient header, styled table.
 * ✅ Nút Xuất Excel nằm trên filter card, không bị mất.
 */
public class HoiVienForm extends JPanel {

    private StyledTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private JComboBox<String> cbThang, cbNam, cbTrangThai;

    // Column indices
    private static final int COL_ID        = 0;
    private static final int COL_HINHANH   = 1;
    private static final int COL_MA        = 2;
    private static final int COL_TEN       = 3;
    private static final int COL_GIOITINH  = 4;
    private static final int COL_NGAYSINH  = 5;
    private static final int COL_SDT       = 6;
    private static final int COL_EMAIL     = 7;
    private static final int COL_DIACHI    = 8;
    private static final int COL_TRANGTHAI = 9;
    private static final int COL_NGAYTG    = 10;

    public HoiVienForm() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(22, 26, 22, 26));
        buildUI();
        loadTable();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  BUILD UI
    // ══════════════════════════════════════════════════════════════════════
    private void buildUI() {
        // ── PAGE HEADER ───────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 14, 0));
        header.add(
            UITheme.pageTitlePanel("Quản lý Hội viên",
                "Danh sách và hồ sơ hội viên trong hệ thống"),
            BorderLayout.WEST
        );
        JButton btnAdd = UITheme.primaryButton("Thêm hội viên");
        btnAdd.setPreferredSize(new Dimension(170, 36));
        btnAdd.addActionListener(e -> openForm(null));
        header.add(btnAdd, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── CENTER ────────────────────────────────────────────────────────
        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        center.add(buildFilterCard(), BorderLayout.NORTH);
        center.add(buildTableCard(),  BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  FILTER CARD
    // ══════════════════════════════════════════════════════════════════════
    private JPanel buildFilterCard() {
        JPanel card = UITheme.cardPanel(new BorderLayout());
        card.setBorder(new EmptyBorder(10, 14, 10, 14));

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bar.setOpaque(false);

        // Search field
        txtSearch = new JTextField(16);
        JPanel searchWrap = UITheme.searchField(txtSearch, "Tìm tên, mã hội viên...");

        // Combos
        cbThang = new JComboBox<>(new String[]{
            "Tháng","T1","T2","T3","T4","T5","T6","T7","T8","T9","T10","T11","T12"
        });
        cbNam = new JComboBox<>(new String[]{"Năm","2023","2024","2025","2026"});
        cbTrangThai = new JComboBox<>(new String[]{"Trạng thái","Hoạt động","Tạm dừng","Đã rời"});
        UITheme.styleCombo(cbThang);
        UITheme.styleCombo(cbNam);
        UITheme.styleCombo(cbTrangThai);

        // Buttons
        JButton btnSearch = UITheme.primaryButton("Tìm");
        JButton btnReset  = UITheme.outlineButton("Đặt lại");
        btnSearch.setPreferredSize(new Dimension(90, 34));
        btnReset .setPreferredSize(new Dimension(90, 34));

        btnSearch.addActionListener(e -> search());
        btnReset .addActionListener(e -> { txtSearch.setText(""); cbThang.setSelectedIndex(0);
                                           cbNam.setSelectedIndex(0); cbTrangThai.setSelectedIndex(0);
                                           loadTable(); });

        // ── Search field Enter key ──
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) search();
            }
        });

        bar.add(searchWrap);
        bar.add(cbThang);
        bar.add(cbNam);
        bar.add(cbTrangThai);
        bar.add(btnSearch);
        bar.add(btnReset);

        card.add(bar, BorderLayout.CENTER);
        return card;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  TABLE CARD
    // ══════════════════════════════════════════════════════════════════════
    private JPanel buildTableCard() {
        // Model
        model = new DefaultTableModel(new String[]{
            "ID","Hình ảnh","Mã HV","Họ tên","Giới tính","Ngày sinh",
            "SĐT","Email","Địa chỉ","Trạng thái","Ngày tham gia"
        }, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new StyledTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setRowHeight(52); // cao hơn để hiện ảnh tốt hơn

        int[] widths = {50, 70, 75, 165, 75, 95, 110, 160, 150, 100, 110};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Ảnh renderer
        table.getColumnModel().getColumn(COL_HINHANH).setCellRenderer(new ImageCellRenderer());

        // Click row → show detail
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) showDetail(row);
            }
        });

        // Card wrapper
        JPanel card = UITheme.cardPanel(new BorderLayout());

        // Card header with toolbar
        JPanel tblHead = buildTableHeader();
        card.add(tblHead, BorderLayout.NORTH);
        card.add(UITheme.styledScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTableHeader() {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, UITheme.PRIMARY_LIGHT,
                        getWidth(), 0, Color.WHITE);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(UITheme.BORDER_COLOR);
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(9, 14, 9, 14));

        JLabel tblTitle = new JLabel("Danh sách hội viên");
        tblTitle.setFont(UITheme.FONT_BOLD);
        tblTitle.setForeground(UITheme.TEXT_PRIMARY);
        p.add(tblTitle, BorderLayout.WEST);

        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        acts.setOpaque(false);
        JButton btnExportTbl = UITheme.outlineButton("Xuất Excel");
        JButton btnEdit      = UITheme.outlineButton("Chỉnh sửa");
        JButton btnDel       = UITheme.dangerButton("Xóa");
        btnExportTbl.setPreferredSize(new Dimension(130, 32));
        btnEdit     .setPreferredSize(new Dimension(120, 32));
        btnDel      .setPreferredSize(new Dimension(90,  32));
        btnExportTbl.addActionListener(e -> ExcelExporter.exportToCSV(table, "HoiVien", this));
        btnEdit     .addActionListener(e -> editSelected());
        btnDel      .addActionListener(e -> deleteSelected());
        acts.add(btnExportTbl);
        acts.add(btnEdit);
        acts.add(btnDel);
        p.add(acts, BorderLayout.EAST);
        return p;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  LOAD TABLE
    // ══════════════════════════════════════════════════════════════════════
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
                    rs.getString("hinhAnh"),
                    rs.getString("maHoiVien"),
                    rs.getString("tenHoiVien"),
                    rs.getString("gioiTinh"),
                    ngaySinhDisplay,
                    rs.getString("sdt"),
                    rs.getString("email"),
                    rs.getString("diaChi"),
                    rs.getString("trangThai"),
                    rs.getDate("ngayThamGia")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  SEARCH
    // ══════════════════════════════════════════════════════════════════════
    private void search() {
        model.setRowCount(0);
        String kw    = txtSearch.getText().trim();
        String thang = (String) cbThang.getSelectedItem();
        String nam   = (String) cbNam.getSelectedItem();
        String tt    = (String) cbTrangThai.getSelectedItem();

        StringBuilder sql = new StringBuilder(
            "SELECT * FROM HoiVien WHERE (tenHoiVien LIKE ? OR maHoiVien LIKE ?)");
        if (!"Tháng".equals(thang))
            sql.append(" AND MONTH(ngayThamGia)=").append(thang.replace("T", ""));
        if (!"Năm".equals(nam))
            sql.append(" AND YEAR(ngayThamGia)=").append(nam);
        if (!"Trạng thái".equals(tt))
            sql.append(" AND trangThai=N'").append(tt).append("'");
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
                    rs.getString("hinhAnh"),
                    rs.getString("maHoiVien"),
                    rs.getString("tenHoiVien"),
                    rs.getString("gioiTinh"),
                    ngaySinhDisplay,
                    rs.getString("sdt"),
                    rs.getString("email"),
                    rs.getString("diaChi"),
                    rs.getString("trangThai"),
                    rs.getDate("ngayThamGia")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  SHOW DETAIL DIALOG
    // ══════════════════════════════════════════════════════════════════════
    private void showDetail(int row) {
        String ten     = str(model.getValueAt(row, COL_TEN));
        String ma      = str(model.getValueAt(row, COL_MA));
        String ngaySinh= str(model.getValueAt(row, COL_NGAYSINH));
        String gt      = str(model.getValueAt(row, COL_GIOITINH));
        String sdt     = str(model.getValueAt(row, COL_SDT));
        String email   = str(model.getValueAt(row, COL_EMAIL));
        String tt      = str(model.getValueAt(row, COL_TRANGTHAI));
        String hinhAnh = str(model.getValueAt(row, COL_HINHANH));
        String diaChi  = str(model.getValueAt(row, COL_DIACHI));
        String ngayTG  = str(model.getValueAt(row, COL_NGAYTG));

        JDialog dlg = FormPanel.createDialog(this, "Chi tiết hội viên", 680, 540);
        dlg.setLayout(new BorderLayout());

        // ── Header xanh ──────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, UITheme.PRIMARY,
                        getWidth(), 0, UITheme.PRIMARY_HOVER);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(16, 22, 16, 22));

        JPanel topLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        topLeft.setOpaque(false);
        topLeft.add(createAvatarPanel(ten, hinhAnh, 72, 108));

        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
        namePanel.setOpaque(false);
        namePanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        JLabel lblTen = new JLabel(ten);
        lblTen.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTen.setForeground(Color.WHITE);
        JLabel lblMa = new JLabel(ma + "  ·  " + tt);
        lblMa.setFont(UITheme.FONT_SMALL);
        lblMa.setForeground(new Color(255, 255, 255, 185));
        namePanel.add(lblTen);
        namePanel.add(Box.createVerticalStrut(4));
        namePanel.add(lblMa);
        topLeft.add(namePanel);
        topBar.add(topLeft, BorderLayout.CENTER);

        // ── Info grid ─────────────────────────────────────────────────────
        JPanel info = new JPanel(new GridBagLayout());
        info.setBackground(Color.WHITE);
        info.setBorder(new EmptyBorder(22, 28, 16, 28));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 4, 6, 4);
        gc.anchor = GridBagConstraints.WEST;

        addDetailRow(info, gc, 0, "Ngày sinh:",       ngaySinh);
        addDetailRow(info, gc, 1, "Giới tính:",       gt);
        addDetailRow(info, gc, 2, "Số điện thoại:",   sdt);
        addDetailRow(info, gc, 3, "Email:",            email);
        addDetailRow(info, gc, 4, "Địa chỉ:",         diaChi);
        addDetailRow(info, gc, 5, "Trạng thái:",      tt);
        addDetailRow(info, gc, 6, "Ngày tham gia:",   ngayTG);

        // ── Footer buttons ────────────────────────────────────────────────
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        btns.setBackground(UITheme.BG_MAIN);
        btns.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));

        JButton btnLeave = UITheme.dangerButton("Yêu cầu rời hội");
        JButton btnRestore = UITheme.primaryButton("Khôi phục hội viên");
        JButton btnPdf   = UITheme.outlineButton("Xuất PDF");
        JButton btnEdit2 = UITheme.outlineButton("Chỉnh sửa");
        JButton btnClose = UITheme.primaryButton("Đóng");
        btnLeave.setPreferredSize(new Dimension(150, 34));
        btnPdf  .setPreferredSize(new Dimension(110, 34));
        btnEdit2.setPreferredSize(new Dimension(110, 34));
        btnClose.setPreferredSize(new Dimension(90,  34));
        btnRestore.setPreferredSize(new Dimension(170, 34));
        btnRestore.setVisible("Đã rời".equalsIgnoreCase(tt));

        btns.add(btnLeave);
        btns.add(btnRestore);
        btns.add(btnPdf);
        btns.add(btnEdit2);
        btns.add(btnClose);

        JScrollPane infoScroll = new JScrollPane(info);
        infoScroll.setBorder(null);
        infoScroll.getViewport().setBackground(Color.WHITE);

        dlg.add(topBar, BorderLayout.NORTH);
        dlg.add(infoScroll, BorderLayout.CENTER);
        dlg.add(btns, BorderLayout.SOUTH);

        btnClose.addActionListener(e -> dlg.dispose());
        btnEdit2.addActionListener(e -> { dlg.dispose(); openForm(row); });
        btnLeave.addActionListener(e -> openLeaveRequestDialog(
            (int) model.getValueAt(row, COL_ID), ma, ten, email, dlg));
         btnRestore.addActionListener(e -> openRestoreRequestDialog(
            (int) model.getValueAt(row, COL_ID), ma, ten, email, ngayTG, tt, dlg));
        btnPdf.addActionListener(e -> {
            HoiVienPdfExporter.MemberProfileData data = new HoiVienPdfExporter.MemberProfileData();
            data.maHoiVien  = ma;       data.tenHoiVien = ten;
            data.gioiTinh   = gt;       data.ngaySinh   = ngaySinh;
            data.sdt        = sdt;      data.email       = email;
            data.diaChi     = diaChi;   data.trangThai   = tt;
            data.ngayThamGia= ngayTG;   data.hinhAnh     = hinhAnh;
            HoiVienPdfExporter.exportMemberProfile(data, dlg);
        });

        dlg.setVisible(true);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  OPEN FORM (ADD / EDIT)
    // ══════════════════════════════════════════════════════════════════════
    private void openForm(Integer row) {
        boolean isEdit = row != null;
        JDialog dlg = FormPanel.createDialog(this,
            isEdit ? "Chỉnh sửa hội viên" : "Thêm hội viên mới", 560, 600);
        dlg.setLayout(new BorderLayout());
        dlg.add(FormPanel.createHeader(
            isEdit ? "Chỉnh sửa hội viên" : "Thêm hội viên mới"), BorderLayout.NORTH);

        // ── Fields ──────────────────────────────────────────────────────
        JPanel fields = new JPanel(new GridBagLayout());
        fields.setBackground(Color.WHITE);
        GridBagConstraints gc = FormPanel.defaultGBC();

        JTextField txtMa = FormPanel.styledFieldReadonly(280);

        PlaceholderTextField txtTen      = new PlaceholderTextField("Nhập họ và tên *");
        PlaceholderTextField txtNgaySinh = new PlaceholderTextField("dd/MM/yyyy  (VD: 15/03/2000)");
        PlaceholderTextField txtSdt      = new PlaceholderTextField("VD: 0901234567 *");
        PlaceholderTextField txtEmail    = new PlaceholderTextField("VD: example@gmail.com *");
        PlaceholderTextField txtDiaChi   = new PlaceholderTextField("Nhập địa chỉ *");
        for (PlaceholderTextField f : new PlaceholderTextField[]{
                txtTen, txtNgaySinh, txtSdt, txtEmail, txtDiaChi}) {
            f.setFont(UITheme.FONT_LABEL);
            f.setPreferredSize(new Dimension(280, 34));
            f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        }

        JComboBox<String> cbGT = new JComboBox<>(new String[]{"Nam", "Nữ"});
        UITheme.styleCombo(cbGT);
        cbGT.setPreferredSize(new Dimension(280, 34));

        // ── Ảnh panel ──────────────────────────────────────────────────
        final String[] selectedImagePath = {""};
        JPanel imgPanel = new JPanel(new BorderLayout(12, 0));
        imgPanel.setOpaque(false);

        JLabel lblPreview = new JLabel("Chưa có ảnh") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_MAIN);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.setColor(UITheme.BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblPreview.setPreferredSize(new Dimension(70, 70));
        lblPreview.setHorizontalAlignment(SwingConstants.CENTER);
        lblPreview.setFont(UITheme.FONT_SMALL);
        lblPreview.setForeground(UITheme.TEXT_MUTED);

        JPanel imgRight = new JPanel(new GridLayout(2, 1, 0, 6));
        imgRight.setOpaque(false);
        JButton btnChooseImg = UITheme.outlineButton("Chọn ảnh...");
        JLabel  lblImgStatus = new JLabel("(chưa chọn)");
        lblImgStatus.setFont(UITheme.FONT_SMALL);
        lblImgStatus.setForeground(UITheme.TEXT_MUTED);
        imgRight.add(btnChooseImg);
        imgRight.add(lblImgStatus);
        imgPanel.add(lblPreview, BorderLayout.WEST);
        imgPanel.add(imgRight,   BorderLayout.CENTER);

        // ── Gán dữ liệu khi sửa ────────────────────────────────────────
        if (isEdit) {
            txtMa.setText(str(model.getValueAt(row, COL_MA)));
            txtTen.setText(str(model.getValueAt(row, COL_TEN)));
            txtNgaySinh.setText(str(model.getValueAt(row, COL_NGAYSINH)));
            cbGT.setSelectedItem(str(model.getValueAt(row, COL_GIOITINH)));
            txtSdt.setText(str(model.getValueAt(row, COL_SDT)));
            txtEmail.setText(str(model.getValueAt(row, COL_EMAIL)));
            txtDiaChi.setText(str(model.getValueAt(row, COL_DIACHI)));
            selectedImagePath[0] = str(model.getValueAt(row, COL_HINHANH));
            if (!selectedImagePath[0].isEmpty()) {
                lblImgStatus.setText("Đã có ảnh");
                lblImgStatus.setForeground(UITheme.SUCCESS);
                loadImagePreview(lblPreview, selectedImagePath[0]);
            }
        } else {
            try (Connection conn = DatabaseHelper.getConnection()) {
                ResultSet rs = conn.createStatement().executeQuery(
                    "SELECT ISNULL(MAX(id), 0) + 1 AS nextId FROM HoiVien");
                if (rs.next()) txtMa.setText(String.format("HV%03d", rs.getInt("nextId")));
            } catch (Exception ex) { txtMa.setText("HV001"); }
        }

        // ── Layout rows ─────────────────────────────────────────────────
        FormPanel.addRow(fields, gc, 0, "Mã hội viên",       txtMa);
        FormPanel.addRow(fields, gc, 1, "Họ và tên *",        txtTen);
        FormPanel.addRow(fields, gc, 2, "Ngày sinh *",        txtNgaySinh);
        FormPanel.addRow(fields, gc, 3, "Giới tính *",        cbGT);
        FormPanel.addRow(fields, gc, 4, "Số điện thoại *",   txtSdt);
        FormPanel.addRow(fields, gc, 5, "Email *",             txtEmail);
        FormPanel.addRow(fields, gc, 6, "Địa chỉ *",          txtDiaChi);
        FormPanel.addRow(fields, gc, 7, "Hình ảnh",           imgPanel);

        // Chọn ảnh
        btnChooseImg.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter(
                "Ảnh (jpg, png, gif, webp)", "jpg","jpeg","png","gif","webp"));
            if (fc.showOpenDialog(dlg) == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                if (!isValidLocalImage(f)) {
                    JOptionPane.showMessageDialog(dlg,
                        "File không hợp lệ! Chỉ chấp nhận jpg, png, gif, webp.");
                    return;
                }
                selectedImagePath[0] = f.getAbsolutePath();
                lblImgStatus.setText("✔  " + f.getName());
                lblImgStatus.setForeground(UITheme.SUCCESS);
                loadLocalPreview(lblPreview, f);
            }
        });

        // ── Footer buttons ───────────────────────────────────────────────
        JButton btnCancel = UITheme.outlineButton("Hủy");
        JButton btnSave   = UITheme.primaryButton(isEdit ? "Lưu thay đổi" : "Thêm mới");

        dlg.add(FormPanel.createBody(fields), BorderLayout.CENTER);
        dlg.add(FormPanel.createFooter(btnCancel, btnSave), BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dlg.dispose());
        btnSave.addActionListener(e -> {
            String ten      = txtTen.getText().trim();
            String ngaySinh = txtNgaySinh.getText().trim();
            String sdt      = txtSdt.getText().trim();
            String email    = txtEmail.getText().trim();
            String diaChi   = txtDiaChi.getText().trim();
            String gt       = (String) cbGT.getSelectedItem();
            String maHV     = txtMa.getText().trim();

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
                    "Vui lòng kiểm tra lại:\n" + errors,
                    "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String ngaySinhSql = ValidationHelper.toSqlDate(ngaySinh);
            try (Connection conn = DatabaseHelper.getConnection()) {
                if (!isEdit) {
                    DuplicateHoiVienInfo duplicate = findDuplicateHoiVien(conn, maHV, email, sdt);
                    if (duplicate != null) {
                        String message = "Hội viên này đã tồn tại trên hệ thống.";
                        if ("Đã rời".equalsIgnoreCase(duplicate.trangThai)) {
                            int choice = JOptionPane.showConfirmDialog(dlg,
                                message + "\nHội viên này đã rời hội. Bạn có muốn khôi phục hội viên không?",
                                "Trùng thông tin hội viên", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                            if (choice == JOptionPane.YES_OPTION) {
                                openRestoreRequestDialog(duplicate.id, duplicate.maHoiVien, duplicate.tenHoiVien,
                                    duplicate.email, duplicate.ngayThamGia, duplicate.trangThai, dlg);
                            }
                        } else {
                            JOptionPane.showMessageDialog(dlg, message, "Trùng dữ liệu", JOptionPane.WARNING_MESSAGE);
                        }
                        return;
                    }
                    PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO HoiVien(maHoiVien,tenHoiVien,ngaySinh,gioiTinh," +
                        "sdt,email,diaChi,hinhAnh,trangThai) VALUES(?,?,?,?,?,?,?,?,?)");
                    ps.setString(1, maHV);
                    ps.setString(2, ten);
                    if (ngaySinhSql != null) ps.setDate(3, Date.valueOf(ngaySinhSql));
                    else ps.setNull(3, Types.DATE);
                    ps.setString(4, gt);
                    ps.setString(5, sdt);
                    ps.setString(6, email);
                    ps.setString(7, diaChi);
                    ps.setString(8, selectedImagePath[0]);
                    ps.setString(9, "Hoạt động");
                    ps.executeUpdate();
                    NhatKyDAO.log(Session.getCurrentUserId(), "THÊM", "HoiVien",
                        "Thêm hội viên: " + ten);
                } else {
                    int id = (int) model.getValueAt(row, COL_ID);
                    PreparedStatement ps = conn.prepareStatement(
                        "UPDATE HoiVien SET tenHoiVien=?,ngaySinh=?,gioiTinh=?," +
                        "sdt=?,email=?,diaChi=?,hinhAnh=? WHERE id=?");
                    ps.setString(1, ten);
                    if (ngaySinhSql != null) ps.setDate(2, Date.valueOf(ngaySinhSql));
                    else ps.setNull(2, Types.DATE);
                    ps.setString(3, gt);
                    ps.setString(4, sdt);
                    ps.setString(5, email);
                    ps.setString(6, diaChi);
                    ps.setString(7, selectedImagePath[0]);
                    ps.setInt(8, id);
                    ps.executeUpdate();
                    NhatKyDAO.log(Session.getCurrentUserId(), "SỬA", "HoiVien",
                        "Sửa hội viên: " + ten);
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

    // ══════════════════════════════════════════════════════════════════════
    //  EDIT / DELETE
    // ══════════════════════════════════════════════════════════════════════
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
        String trangThai = str(model.getValueAt(row, COL_TRANGTHAI));
        if (!"Đã rời".equalsIgnoreCase(trangThai)) {
            JOptionPane.showMessageDialog(this, "Chỉ được xóa hội viên ở trạng thái \"Đã rời\".");
            return;
        }
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT COUNT(*) FROM ThamGia tg JOIN HoatDong hd ON tg.idHoatDong=hd.id WHERE tg.idHoiVien=? AND hd.thoiGianKetThuc>=GETDATE()")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Hội viên còn tham gia hoạt động đang/sắp diễn ra, không thể xóa.");
                return;
            }
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Lỗi kiểm tra: " + ex.getMessage()); return; }
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn xóa hội viên này không?",
            "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseHelper.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    ArchiveDAO.archiveByQuery("Hội viên", id, "HoiVien", "id", Session.getCurrentUserId(), "XÓA");

                    // Xóa các bản ghi phụ thuộc trước để tránh lỗi khóa ngoại.
                    try (PreparedStatement psNotifyRestore = conn.prepareStatement(
                        "DELETE FROM ThongBao WHERE idKhoiPhucHoiVien IN (SELECT id FROM YeuCauKhoiPhucHoiVien WHERE idHoiVien=?)")) {
                        psNotifyRestore.setInt(1, id);
                        psNotifyRestore.executeUpdate();
                    }
                    try (PreparedStatement psRestore = conn.prepareStatement("DELETE FROM YeuCauKhoiPhucHoiVien WHERE idHoiVien=?")) {
                        psRestore.setInt(1, id);
                        psRestore.executeUpdate();
                    }
                    try (PreparedStatement psNotifyLeave = conn.prepareStatement(
                        "DELETE FROM ThongBao WHERE idYeuCauRoiHoi IN (SELECT id FROM YeuCauRoiHoi WHERE idHoiVien=?)")) {
                        psNotifyLeave.setInt(1, id);
                        psNotifyLeave.executeUpdate();
                    }
                    try (PreparedStatement psLeave = conn.prepareStatement("DELETE FROM YeuCauRoiHoi WHERE idHoiVien=?")) {
                        psLeave.setInt(1, id);
                        psLeave.executeUpdate();
                    }
                    try (PreparedStatement psNotifyTemp = conn.prepareStatement(
                        "DELETE FROM ThongBao WHERE idDangKyTam IN (SELECT id FROM DangKyTam WHERE idHoiVien=?)")) {
                        psNotifyTemp.setInt(1, id);
                        psNotifyTemp.executeUpdate();
                    }
                    try (PreparedStatement psTempReg = conn.prepareStatement("DELETE FROM DangKyTam WHERE idHoiVien=?")) {
                        psTempReg.setInt(1, id);
                        psTempReg.executeUpdate();
                    }
                    try (PreparedStatement psJoin = conn.prepareStatement("DELETE FROM ThamGia WHERE idHoiVien=?")) {
                        psJoin.setInt(1, id);
                        psJoin.executeUpdate();
                    }
                    try (PreparedStatement psDeleteMember = conn.prepareStatement("DELETE FROM HoiVien WHERE id=?")) {
                        psDeleteMember.setInt(1, id);
                        psDeleteMember.executeUpdate();
                    }

                    conn.commit();
                    NhatKyDAO.log(Session.getCurrentUserId(), "XÓA", "HoiVien",
                        "Xóa hội viên ID: " + id);
                    loadTable();
                    JOptionPane.showMessageDialog(this, "Xóa hội viên thành công",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    conn.rollback();
                    throw ex;
                } finally {
                    conn.setAutoCommit(true);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa: " + e.getMessage());
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  LEAVE REQUEST DIALOG
    // ══════════════════════════════════════════════════════════════════════
    private void openLeaveRequestDialog(int idHoiVien, String maHV,
                                         String tenHV, String emailHV, JDialog parent) {
        JDialog dlg = FormPanel.createDialog(parent.getOwner(), "Yêu cầu rời hội", 520, 400);
        dlg.setLayout(new BorderLayout());
        dlg.add(FormPanel.createHeader("Tạo yêu cầu rời hội"), BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setBackground(Color.WHITE);
        GridBagConstraints gc = FormPanel.defaultGBC();

        JTextField txtMaRO  = FormPanel.styledFieldReadonly(260); txtMaRO .setText(maHV);
        JTextField txtTenRO = FormPanel.styledFieldReadonly(260); txtTenRO.setText(tenHV);
        JTextField txtNgay  = FormPanel.styledField(260);
        txtNgay.setText(ValidationHelper.toDisplayDate(java.time.LocalDate.now().toString()));
        JTextField txtLyDo  = FormPanel.styledField(260);
        JComboBox<String> cbNguon = new JComboBox<>(
            new String[]{"Email", "Điện thoại", "Trực tiếp"});
        UITheme.styleCombo(cbNguon);
        cbNguon.setPreferredSize(new Dimension(260, 34));

        FormPanel.addRow(fields, gc, 0, "Mã hội viên",   txtMaRO);
        FormPanel.addRow(fields, gc, 1, "Tên hội viên",  txtTenRO);
        FormPanel.addRow(fields, gc, 2, "Ngày yêu cầu",  txtNgay);
        FormPanel.addRow(fields, gc, 3, "Lý do rời hội", txtLyDo);
        FormPanel.addRow(fields, gc, 4, "Nguồn yêu cầu", cbNguon);

        JButton btnSubmit = UITheme.primaryButton("Gửi yêu cầu");
        JButton btnCancel = UITheme.outlineButton("Hủy");
        dlg.add(FormPanel.createBody(fields), BorderLayout.CENTER);
        dlg.add(FormPanel.createFooter(btnCancel, btnSubmit), BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dlg.dispose());
        btnSubmit.addActionListener(e -> {
            String lyDo    = txtLyDo.getText().trim();
            String ngaySql = ValidationHelper.toSqlDate(txtNgay.getText().trim());
            if (lyDo.isEmpty() || ngaySql == null) {
                JOptionPane.showMessageDialog(dlg,
                    "Vui lòng nhập lý do và ngày hợp lệ (dd/MM/yyyy).");
                return;
            }
            String token = UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
            try (Connection conn = DatabaseHelper.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO YeuCauRoiHoi(idHoiVien,lyDo,nguonYeuCau," +
                    "trangThai,token,thoiGianTao) VALUES(?,?,?,N'Chờ xác nhận',?,?)");
                ps.setInt(1, idHoiVien);
                ps.setString(2, lyDo);
                ps.setString(3, (String) cbNguon.getSelectedItem());
                ps.setString(4, token);
                ps.setTimestamp(5, Timestamp.valueOf(
                    LocalDateTime.parse(ngaySql + "T00:00:00")));
                ps.executeUpdate();
                NhatKyDAO.log(Session.getCurrentUserId(), "THÊM", "YeuCauRoiHoi",
                    "Tạo yêu cầu rời hội cho " + maHV);
                if (emailHV != null && !emailHV.trim().isEmpty()) {
                    String link = "http://localhost:8080/xacnhan?token=" + token;
                    String body = String.format(
                        "Kính gửi hội viên %s\n\n" +
                        "Hệ thống đã tiếp nhận yêu cầu rời hội của bạn.\n" +
                        "Vui lòng xác nhận trong vòng 24 giờ tại:\n%s\n\n" +
                        "Lưu ý: Link sẽ hết hiệu lực sau 24 giờ.", tenHV, link);
                    EmailSender.send(emailHV, "Xác nhận yêu cầu rời hội", body);
                }
                JOptionPane.showMessageDialog(dlg, "Đã tạo yêu cầu rời hội (Chờ xác nhận).");
                dlg.dispose();
                parent.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Lỗi: " + ex.getMessage());
            }
        });
        dlg.setVisible(true);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════════════

    private static class DuplicateHoiVienInfo {
        int id;
        String maHoiVien, tenHoiVien, email, trangThai, ngayThamGia;
    }

    private DuplicateHoiVienInfo findDuplicateHoiVien(Connection conn, String maHV, String email, String sdt) throws SQLException {
        String sql = "SELECT TOP 1 id, maHoiVien, tenHoiVien, email, trangThai, "
            + "CONVERT(varchar(10), ngayThamGia, 120) AS ngayThamGia "
            + "FROM HoiVien WHERE maHoiVien=? OR email=? OR sdt=? ORDER BY id DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maHV);
            ps.setString(2, email);
            ps.setString(3, sdt);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                DuplicateHoiVienInfo d = new DuplicateHoiVienInfo();
                d.id = rs.getInt("id");
                d.maHoiVien = rs.getString("maHoiVien");
                d.tenHoiVien = rs.getString("tenHoiVien");
                d.email = rs.getString("email");
                d.trangThai = rs.getString("trangThai");
                d.ngayThamGia = rs.getString("ngayThamGia");
                return d;
            }
        }
        return null;
    }

    private void openRestoreRequestDialog(int idHoiVien, String maHV, String tenHV, String emailHV,
                                          String ngayThamGiaCu, String trangThai, JDialog parent) {
        if (!"Đã rời".equalsIgnoreCase(trangThai)) {
            JOptionPane.showMessageDialog(this, "Chỉ khôi phục được hội viên ở trạng thái \"Đã rời\".");
            return;
        }
        String token = UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
        try (Connection conn = DatabaseHelper.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO YeuCauKhoiPhucHoiVien(idHoiVien,trangThai,token,thoiGianTao,noiDung) "
                    + "VALUES(?,N'Chờ xác nhận',?,GETDATE(),?)");
            ps.setInt(1, idHoiVien);
            ps.setString(2, token);
            ps.setString(3, "Yêu cầu khôi phục hội viên " + maHV);
            ps.executeUpdate();
            NhatKyDAO.log(Session.getCurrentUserId(), "THÊM", "YeuCauKhoiPhucHoiVien",
                "Tạo yêu cầu khôi phục hội viên " + maHV);

            if (emailHV != null && !emailHV.trim().isEmpty()) {
                String link = "http://localhost:8080/xac-nhan-khoi-phuc?token=" + token;
                EmailSender.sendHtml(emailHV, "Xác nhận khôi phục hội viên",
                    buildRestoreConfirmEmailHtml(maHV, tenHV, emailHV, ngayThamGiaCu, LocalDate.now().toString(), link));
            }
            JOptionPane.showMessageDialog(this, "Đã tạo yêu cầu khôi phục hội viên.");
            if (parent != null) parent.dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tạo yêu cầu khôi phục: " + ex.getMessage());
        }
    }

    private String buildRestoreConfirmEmailHtml(String ma, String ten, String email, String ngayThamGiaCu,
                                                String ngayRoi, String link) {
        return "<div style='font-family:Segoe UI,Arial,sans-serif;background:#f8fafc;padding:20px'>"
            + "<div style='max-width:700px;margin:auto;background:#fff;border:1px solid #e2e8f0;border-radius:12px;padding:24px'>"
            + "<h2 style='margin-top:0;color:#1e3a8a'>Phiếu xác nhận khôi phục hội viên</h2>"
            + "<p>Vui lòng kiểm tra thông tin và xác nhận quay lại hoạt động.</p>"
            + "<table style='width:100%;border-collapse:collapse'>"
            + htmlRow("Mã hội viên", ma) + htmlRow("Tên hội viên", ten) + htmlRow("Email", email)
            + htmlRow("Ngày tham gia cũ", ngayThamGiaCu) + htmlRow("Ngày rời hội", ngayRoi)
            + "</table>"
            + "<p style='margin-top:16px'><a href='" + link + "' style='padding:10px 16px;background:#2563eb;color:#fff;"
            + "text-decoration:none;border-radius:8px;font-weight:600'>Xác nhận khôi phục</a></p></div></div>";
    }

    private String htmlRow(String key, String value) {
        return "<tr><td style='padding:8px;border-bottom:1px solid #e5e7eb;width:180px'><b>" + key + "</b></td>"
            + "<td style='padding:8px;border-bottom:1px solid #e5e7eb'>" + (value == null ? "—" : value) + "</td></tr>";
    }
    
    
    /** Avatar panel: hiển thị ảnh hoặc chữ cái đầu tên */
    private JPanel createAvatarPanel(String name, String imageUrl, int width, int height) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(width, height));
        panel.setOpaque(false);
        JLabel lbl = new JLabel();
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, Math.max(22, width / 3)));
        lbl.setForeground(UITheme.ACCENT);
        if (imageUrl != null && !imageUrl.isEmpty()) {
            new Thread(() -> {
                try {
                    BufferedImage img = readImage(imageUrl);
                    if (img != null) {
                        Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                        SwingUtilities.invokeLater(() -> {
                            lbl.setIcon(new ImageIcon(scaled));
                            lbl.setText("");
                        });
                        return;
                    }
                } catch (Exception ignored) {}
                SwingUtilities.invokeLater(() ->
                    lbl.setText(name.isEmpty() ? "?" : String.valueOf(
                        Character.toUpperCase(name.charAt(0)))));
            }).start();
        } else {
            lbl.setText(name.isEmpty() ? "?" :
                String.valueOf(Character.toUpperCase(name.charAt(0))));
        }
        panel.add(lbl);
        return panel;
    }

    private void loadImagePreview(JLabel lbl, String url) {
        if (url == null || url.isEmpty()) return;
        new Thread(() -> {
            try {
                BufferedImage img = readImage(url);
                if (img != null) {
                    Image scaled = img.getScaledInstance(70, 70, Image.SCALE_SMOOTH);
                    SwingUtilities.invokeLater(() -> {
                        lbl.setIcon(new ImageIcon(scaled));
                        lbl.setText("");
                    });
                }
            } catch (Exception ignored) {}
        }).start();
    }

    private void loadLocalPreview(JLabel lbl, File f) {
        try {
            BufferedImage img = ImageIO.read(f);
            if (img != null) {
                Image scaled = img.getScaledInstance(70, 70, Image.SCALE_SMOOTH);
                lbl.setIcon(new ImageIcon(scaled));
                lbl.setText("");
            }
        } catch (Exception ignored) {}
    }

    private boolean isValidLocalImage(File f) {
        if (f == null || !f.exists() || !f.isFile()) return false;
        String n = f.getName().toLowerCase();
        return n.endsWith(".jpg") || n.endsWith(".jpeg") ||
               n.endsWith(".png") || n.endsWith(".gif") || n.endsWith(".webp");
    }

    private BufferedImage readImage(String pathOrUrl) throws Exception {
        if (pathOrUrl == null || pathOrUrl.isEmpty()) return null;
        if (pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://"))
            return ImageIO.read(new java.net.URL(pathOrUrl));
        return ImageIO.read(new File(pathOrUrl));
    }

    private void addDetailRow(JPanel p, GridBagConstraints gc, int y,
                               String label, String value) {
        gc.gridx = 0; gc.gridy = y; gc.gridwidth = 1; gc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_SECONDARY);
        lbl.setPreferredSize(new Dimension(155, 28));
        p.add(lbl, gc);
        gc.gridx = 1; gc.weightx = 1;
        JLabel val = new JLabel(value != null && !value.isEmpty() ? value : "—");
        val.setFont(UITheme.FONT_LABEL);
        val.setForeground(UITheme.TEXT_PRIMARY);
        p.add(val, gc);
    }

    private String str(Object o) { return o == null ? "" : o.toString(); }

    // ══════════════════════════════════════════════════════════════════════
    //  IMAGE CELL RENDERER
    // ══════════════════════════════════════════════════════════════════════
    private class ImageCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value,
                boolean isSel, boolean hasFocus, int row, int col) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                tbl, "", isSel, hasFocus, row, col);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setIcon(null);
            String imagePath = str(value);
            if (imagePath.isEmpty()) { lbl.setText("—"); return lbl; }
            try {
                BufferedImage img = readImage(imagePath);
                if (img != null) {
                    // 4:6 ratio thumbnail
                    Image scaled = img.getScaledInstance(33, 50, Image.SCALE_SMOOTH);
                    lbl.setIcon(new ImageIcon(scaled));
                    lbl.setText("");
                } else {
                    lbl.setText("—");
                }
            } catch (Exception ex) {
                lbl.setText("—");
            }
            return lbl;
        }
    }
}