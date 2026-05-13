package View;

import controller.AuthController;
import model.TaiKhoan;
import Util.*;
import database.DatabaseHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * MainForm – Cửa sổ chính.
 * Thông báo: sử dụng DashboardPanel.showNotificationDialog() 4-tab mới.
 */
public class MainForm extends JFrame {

    private static final int SIDEBAR_W = 225;
    private static final int BTN_H     = 42;

    private static final Color SB_BG         = Color.decode("#0a1f5c");
    private static final Color SB_MID        = Color.decode("#0f2d6e");
    private static final Color SB_ACTIVE     = Color.decode("#1359B9");
    private static final Color SB_HOVER      = new Color(255,255,255,22);
    private static final Color SB_ACTIVE_IND = Color.decode("#9FE4FB");
    private static final Color SB_TXT_ON     = Color.WHITE;
    private static final Color SB_TXT_OFF    = new Color(255,255,255,165);

    private CardLayout cardLayout;
    private JPanel     contentPanel;
    private JPanel     sidebar;
    private JLabel     navPageLabel;
    private JButton    activeBtn = null;
    private JButton    btnBell;
    private Timer      notificationTimer;

    public MainForm() {
        setTitle("Hệ thống Quản lý Hồ sơ Hội viên");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1240, 720);
        setMinimumSize(new Dimension(960, 600));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.BG_MAIN);

        sidebar = buildSidebar();
        add(sidebar, BorderLayout.WEST);
        add(createNavbar(), BorderLayout.NORTH);

        cardLayout   = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UITheme.BG_MAIN);

        contentPanel.add(new DashboardPanel(), "dashboard");
        contentPanel.add(new HoiVienForm(),    "hoivien");
        contentPanel.add(new HoatDongForm(),   "hoatdong");
        contentPanel.add(new ThamGiaForm(),    "thamgia");
        if (isAdmin()) {
            contentPanel.add(new NhanVienForm(), "nhanvien");
            contentPanel.add(new NhatKyForm(),   "nhatky");
            contentPanel.add(new ArchiveForm(),    "archive");
        }
        add(contentPanel, BorderLayout.CENTER);
        switchPanel("dashboard");
        startNotificationAutoRefresh();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  SIDEBAR
    // ══════════════════════════════════════════════════════════════════════
    private JPanel buildSidebar() {
        JPanel sb = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, SB_BG, 0, getHeight(), SB_MID);
                g2.setPaint(gp); g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(159,228,251,30));
                g2.drawLine(getWidth()-1, 0, getWidth()-1, getHeight());
                g2.dispose(); super.paintComponent(g);
            }
        };
        sb.setOpaque(false);
        sb.setPreferredSize(new Dimension(SIDEBAR_W, 0));
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));

        sb.add(buildBrand());
        sb.add(buildDivider());
        sb.add(Box.createVerticalStrut(8));
        sb.add(sectionLabel("MENU CHÍNH"));
        addNavBtn(sb, "🏠", "Trang chủ",        "dashboard");
        addNavBtn(sb, "👥", "Quản lý hội viên", "hoivien");
        addNavBtn(sb, "📅", "Hoạt động",        "hoatdong");
        addNavBtn(sb, "✅", "Tham gia",          "thamgia");

        if (isAdmin()) {
            sb.add(Box.createVerticalStrut(12));
            sb.add(sectionLabel("QUẢN TRỊ"));
            addNavBtn(sb, "🧑", "Nhân viên", "nhanvien");
            addNavBtn(sb, "📋", "Nhật ký",   "nhatky");
            addNavBtn(sb, "🗄", "Kho lưu trữ",       "archive");
        }
        sb.add(Box.createVerticalGlue());
        sb.add(buildUserCard());
        return sb;
    }

    private JPanel buildBrand() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 14));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(SIDEBAR_W, 66));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel logo = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp=new GradientPaint(0,0,UITheme.ACCENT,getWidth(),getHeight(),UITheme.ACCENT_DARK);
                g2.setPaint(gp); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.dispose(); super.paintComponent(g);
            }
        };
        logo.setOpaque(false); logo.setPreferredSize(new Dimension(36,36));
        JLabel logoTxt=new JLabel("🏛",SwingConstants.CENTER);
        logoTxt.setFont(new Font("Segoe UI Emoji",Font.PLAIN,18));
        logo.add(logoTxt);

        JPanel txt=new JPanel(); txt.setOpaque(false);
        txt.setLayout(new BoxLayout(txt,BoxLayout.Y_AXIS));
        JLabel t1=new JLabel("HỘI VIÊN");
        t1.setFont(new Font("Segoe UI",Font.BOLD,13)); t1.setForeground(Color.WHITE);
        JLabel t2=new JLabel("Hệ thống quản lý");
        t2.setFont(new Font("Segoe UI",Font.PLAIN,10)); t2.setForeground(UITheme.ACCENT);
        txt.add(t1); txt.add(t2);
        p.add(logo); p.add(txt);
        return p;
    }

    private JPanel buildDivider() {
        JPanel d=new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                GradientPaint gp=new GradientPaint(16,0,new Color(255,255,255,0),
                    getWidth()/2,0,new Color(159,228,251,55));
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight());
                gp=new GradientPaint(getWidth()/2,0,new Color(159,228,251,55),
                    getWidth()-16,0,new Color(255,255,255,0));
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight());
                g2.dispose();
            }
        };
        d.setOpaque(false);
        d.setMaximumSize(new Dimension(SIDEBAR_W,1));
        d.setAlignmentX(Component.LEFT_ALIGNMENT);
        return d;
    }

    private JLabel sectionLabel(String text) {
        JLabel lbl=new JLabel(text);
        lbl.setFont(new Font("Segoe UI",Font.BOLD,9));
        lbl.setForeground(new Color(159,228,251,110));
        lbl.setBorder(new EmptyBorder(4,18,4,0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setMaximumSize(new Dimension(SIDEBAR_W,22));
        return lbl;
    }

    private void addNavBtn(JPanel sb, String icon, String label, String key) {
        JButton btn=new JButton() {
            boolean hovered=false;
            boolean isActive(){return this==activeBtn;}
            {
                setOpaque(false); setContentAreaFilled(false); setBorderPainted(false);
                setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setMaximumSize(new Dimension(SIDEBAR_W,BTN_H));
                setPreferredSize(new Dimension(SIDEBAR_W,BTN_H));
                setAlignmentX(Component.LEFT_ALIGNMENT);
                setToolTipText(label);
                addMouseListener(new MouseAdapter(){
                    public void mouseEntered(MouseEvent e){hovered=true;repaint();}
                    public void mouseExited(MouseEvent e){hovered=false;repaint();}
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                if(isActive()){
                    GradientPaint gp=new GradientPaint(6,0,SB_ACTIVE,getWidth()-6,0,Color.decode("#1a6bc9"));
                    g2.setPaint(gp); g2.fillRoundRect(6,4,getWidth()-12,BTN_H-8,10,10);
                    g2.setColor(SB_ACTIVE_IND); g2.fillRoundRect(0,(BTN_H-24)/2,4,24,4,4);
                } else if(hovered){
                    g2.setColor(SB_HOVER); g2.fillRoundRect(6,4,getWidth()-12,BTN_H-8,10,10);
                }
                g2.setFont(new Font("Segoe UI Emoji",Font.PLAIN,15));
                g2.setColor(isActive()?SB_TXT_ON:SB_TXT_OFF);
                g2.drawString(icon,18,BTN_H/2+5);
                g2.setFont(isActive()?new Font("Segoe UI",Font.BOLD,13):new Font("Segoe UI",Font.PLAIN,13));
                g2.setColor(isActive()?SB_TXT_ON:SB_TXT_OFF);
                FontMetrics fm=g2.getFontMetrics(); String disp=label;
                int maxW=getWidth()-60;
                while(fm.stringWidth(disp)>maxW&&disp.length()>1) disp=disp.substring(0,disp.length()-1);
                if(!disp.equals(label)) disp+="…";
                g2.drawString(disp,46,BTN_H/2+5);
                g2.dispose();
            }
        };
        btn.addActionListener(e->{activeBtn=btn;switchPanel(key);sidebar.repaint();});
        sb.add(btn);
    }

    private JPanel buildUserCard() {
        TaiKhoan user=Session.getUser();
        String uname=user!=null?user.getUsername():"Người dùng";
        String role=user!=null?user.getRole():"";

        JPanel p=new JPanel(new BorderLayout(8,0)){
            @Override protected void paintComponent(Graphics g){
                g.setColor(new Color(255,255,255,14)); g.fillRect(0,0,getWidth(),getHeight());
                g.setColor(new Color(255,255,255,25)); g.drawLine(0,0,getWidth(),0);
                super.paintComponent(g);
            }
        };
        p.setOpaque(false); p.setBorder(new EmptyBorder(10,12,10,12));
        p.setMaximumSize(new Dimension(SIDEBAR_W,58));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel av=new JPanel(){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp=new GradientPaint(0,0,UITheme.ACCENT,getWidth(),getHeight(),UITheme.PRIMARY);
                g2.setPaint(gp); g2.fillOval(0,0,getWidth(),getHeight());
                g2.setColor(Color.WHITE); g2.setFont(new Font("Segoe UI",Font.BOLD,13));
                FontMetrics fm=g2.getFontMetrics();
                String ini=uname.length()>0?String.valueOf(Character.toUpperCase(uname.charAt(0))):"A";
                g2.drawString(ini,(getWidth()-fm.stringWidth(ini))/2,(getHeight()-fm.getHeight())/2+fm.getAscent());
                g2.dispose();
            }
        };
        av.setOpaque(false); av.setPreferredSize(new Dimension(32,32));

        JPanel info=new JPanel(); info.setOpaque(false);
        info.setLayout(new BoxLayout(info,BoxLayout.Y_AXIS));
        JLabel lN=new JLabel(uname); lN.setFont(new Font("Segoe UI",Font.BOLD,12)); lN.setForeground(Color.WHITE);
        JLabel lR=new JLabel(role); lR.setFont(new Font("Segoe UI",Font.PLAIN,10)); lR.setForeground(UITheme.ACCENT);
        info.add(lN); info.add(lR);

        JButton btnOut=new JButton("⏻"){{
            setOpaque(false); setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
            setFont(new Font("Segoe UI Symbol",Font.PLAIN,14));
            setForeground(new Color(255,255,255,140));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("Đăng xuất");
            addMouseListener(new MouseAdapter(){
                public void mouseEntered(MouseEvent e){setForeground(UITheme.DANGER);}
                public void mouseExited(MouseEvent e){setForeground(new Color(255,255,255,140));}
            });
        }};
        btnOut.addActionListener(e->logout());
        p.add(av,BorderLayout.WEST); p.add(info,BorderLayout.CENTER); p.add(btnOut,BorderLayout.EAST);
        return p;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  NAVBAR
    // ══════════════════════════════════════════════════════════════════════
    private JPanel createNavbar() {
        JPanel nav=new JPanel(new BorderLayout()){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setColor(Color.WHITE); g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(UITheme.BORDER_COLOR); g2.drawLine(0,getHeight()-1,getWidth(),getHeight()-1);
                g2.dispose();
            }
        };
        nav.setPreferredSize(new Dimension(0,54));
        nav.setBorder(new EmptyBorder(0,18,0,18));

        JPanel left=new JPanel(new FlowLayout(FlowLayout.LEFT,6,0));
        left.setOpaque(false);
        JButton btnHome=new JButton("⌂"){{
            setOpaque(false); setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
            setFont(new Font("Segoe UI Symbol",Font.PLAIN,17)); setForeground(UITheme.PRIMARY);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(34,34)); setToolTipText("Trang chủ");
        }};
        btnHome.addActionListener(e->{activeBtn=null;switchPanel("dashboard");sidebar.repaint();});

        navPageLabel=new JLabel("Trang chủ");
        navPageLabel.setFont(new Font("Segoe UI",Font.PLAIN,14));
        navPageLabel.setForeground(UITheme.PRIMARY);

        JLabel sep=new JLabel("›");
        sep.setFont(new Font("Segoe UI",Font.PLAIN,16)); sep.setForeground(UITheme.TEXT_MUTED);
        left.add(btnHome); left.add(sep); left.add(navPageLabel);
        nav.add(left,BorderLayout.WEST);

        JPanel right=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));
        right.setOpaque(false);

        // ── Bell button ──
        btnBell = new JButton("🔔") {
            boolean hov=false;
            {
                setOpaque(false); setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
                setFont(new Font("Segoe UI Emoji",Font.PLAIN,15));
                setForeground(UITheme.TEXT_SECONDARY);
                setPreferredSize(new Dimension(38,38));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setToolTipText("Thông báo");
                addMouseListener(new MouseAdapter(){
                    public void mouseEntered(MouseEvent e){hov=true;repaint();}
                    public void mouseExited(MouseEvent e){hov=false;repaint();}
                });
            }
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hov?UITheme.PRIMARY_LIGHT:UITheme.BG_MAIN);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.setColor(hov?UITheme.PRIMARY:UITheme.BORDER_COLOR);
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
                int unread=getUnreadNotificationCount();
                if(unread>0){
                    g2.setColor(UITheme.DANGER); g2.fillOval(getWidth()-11,3,10,10);
                    g2.setColor(Color.WHITE); g2.setFont(new Font("Segoe UI",Font.BOLD,8));
                    FontMetrics fm=g2.getFontMetrics();
                    String cnt=unread>9?"9+":String.valueOf(unread);
                    g2.drawString(cnt,getWidth()-11+(10-fm.stringWidth(cnt))/2,3+fm.getAscent()-1);
                }
                g2.dispose(); super.paintComponent(g);
            }
        };
        // ← Gọi DashboardPanel.showNotificationDialog() mới (4 tab)
        btnBell.addActionListener(e -> {
            Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
            DashboardPanel.showNotificationDialog(
                owner,
                DatabaseHelper::getConnection,
                btnBell
            );
            btnBell.repaint();
        });
        right.add(btnBell);

        // ── User pill ──
        TaiKhoan user=Session.getUser();
        String username=user!=null?user.getUsername():"Người dùng";
        String role=user!=null?user.getRole():"";

        JButton userPill=new JButton(){
            boolean hov=false;
            {
                setOpaque(false); setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
                setPreferredSize(new Dimension(150,36));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter(){
                    public void mouseEntered(MouseEvent e){hov=true;repaint();}
                    public void mouseExited(MouseEvent e){hov=false;repaint();}
                });
            }
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hov?Color.decode("#dce9fb"):UITheme.PRIMARY_LIGHT);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),18,18);
                g2.setColor(UITheme.BORDER_COLOR);
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,18,18);
                g2.setColor(UITheme.PRIMARY); g2.fillOval(6,5,24,24);
                g2.setColor(Color.WHITE); g2.setFont(new Font("Segoe UI",Font.BOLD,12));
                FontMetrics fm=g2.getFontMetrics();
                String ini=username.isEmpty()?"U":String.valueOf(Character.toUpperCase(username.charAt(0)));
                g2.drawString(ini,6+(24-fm.stringWidth(ini))/2,5+(24-fm.getHeight())/2+fm.getAscent());
                g2.setColor(UITheme.TEXT_PRIMARY); g2.setFont(new Font("Segoe UI",Font.BOLD,12));
                FontMetrics fm2=g2.getFontMetrics();
                String disp=username.length()>9?username.substring(0,8)+"…":username;
                g2.drawString(disp,36,(getHeight()-fm2.getHeight())/2+fm2.getAscent());
                g2.setColor(UITheme.TEXT_MUTED); g2.setFont(new Font("Segoe UI",Font.PLAIN,10));
                FontMetrics fmC=g2.getFontMetrics();
                g2.drawString("▾",getWidth()-14,(getHeight()-fmC.getHeight())/2+fmC.getAscent());
                g2.dispose();
            }
        };

        JPopupMenu popup=new JPopupMenu();
        popup.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR,1),
            BorderFactory.createEmptyBorder(4,0,4,0)));
        JMenuItem mInfo=new JMenuItem(username+"  ("+role+")");
        mInfo.setFont(UITheme.FONT_BOLD); mInfo.setForeground(UITheme.TEXT_SECONDARY); mInfo.setEnabled(false);
        JMenuItem mProfile=new JMenuItem("Thông tin tài khoản");
        mProfile.setFont(UITheme.FONT_LABEL);
        mProfile.addActionListener(e->showProfileDialog());
        JMenuItem mLogout=new JMenuItem("Đăng xuất");
        mLogout.setFont(UITheme.FONT_LABEL); mLogout.setForeground(UITheme.DANGER);
        mLogout.addActionListener(e->logout());
        popup.add(mInfo); popup.add(new JSeparator());
        popup.add(mProfile); popup.add(new JSeparator());
        popup.add(mLogout);
        userPill.addActionListener(e->popup.show(userPill,0,userPill.getHeight()));
        right.add(userPill);
        nav.add(right,BorderLayout.EAST);
        return nav;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  NOTIFICATIONS
    // ══════════════════════════════════════════════════════════════════════
    private int getUnreadNotificationCount() {
        try (Connection c=DatabaseHelper.getConnection();
             ResultSet rs=c.createStatement().executeQuery("SELECT COUNT(*) FROM ThongBao WHERE daDoc=0")) {
            return rs.next()?rs.getInt(1):0;
        } catch (Exception e){return 0;}
    }

    private void startNotificationAutoRefresh() {
        notificationTimer=new Timer(10_000,e->{ if(btnBell!=null) btnBell.repaint(); });
        notificationTimer.setRepeats(true); notificationTimer.start();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  PROFILE + ĐỔI MẬT KHẨU
    // ══════════════════════════════════════════════════════════════════════
    private void showProfileDialog() {
        TaiKhoan user=Session.getUser(); if(user==null) return;
        JDialog dlg=new JDialog(this,"Thông tin tài khoản",true);
        dlg.setSize(440,430); dlg.setLocationRelativeTo(this); dlg.setResizable(false);
        dlg.setLayout(new BorderLayout());

        JPanel topBar=new JPanel(new BorderLayout()){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                GradientPaint gp=new GradientPaint(0,0,UITheme.PRIMARY,getWidth(),0,UITheme.PRIMARY_HOVER);
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose(); super.paintComponent(g);
            }
        };
        topBar.setOpaque(false); topBar.setPreferredSize(new Dimension(0,82));
        topBar.setBorder(new EmptyBorder(14,20,14,20));

        JPanel av=new JPanel(){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp=new GradientPaint(0,0,UITheme.ACCENT,getWidth(),getHeight(),UITheme.ACCENT_DARK);
                g2.setPaint(gp); g2.fillOval(4,4,getWidth()-8,getHeight()-8);
                g2.setColor(UITheme.PRIMARY_DARK); g2.setFont(new Font("Segoe UI",Font.BOLD,22));
                FontMetrics fm=g2.getFontMetrics();
                String t=String.valueOf(Character.toUpperCase(user.getUsername().charAt(0)));
                g2.drawString(t,(getWidth()-fm.stringWidth(t))/2,(getHeight()-fm.getHeight())/2+fm.getAscent());
                g2.dispose();
            }
        };
        av.setOpaque(false); av.setPreferredSize(new Dimension(54,54));

        JPanel nameInfo=new JPanel(new GridLayout(2,1,0,3));
        nameInfo.setOpaque(false); nameInfo.setBorder(new EmptyBorder(0,14,0,0));
        JLabel lN=new JLabel(user.getUsername()); lN.setFont(new Font("Segoe UI",Font.BOLD,16)); lN.setForeground(Color.WHITE);
        JLabel lR=new JLabel(user.getRole()); lR.setFont(new Font("Segoe UI",Font.PLAIN,12)); lR.setForeground(UITheme.ACCENT);
        nameInfo.add(lN); nameInfo.add(lR);
        topBar.add(av,BorderLayout.WEST); topBar.add(nameInfo,BorderLayout.CENTER);
        dlg.add(topBar,BorderLayout.NORTH);

        JPanel body=new JPanel(new GridBagLayout());
        body.setBackground(Color.WHITE); body.setBorder(new EmptyBorder(18,24,10,24));
        GridBagConstraints gc=FormPanel.defaultGBC(); gc.insets=new java.awt.Insets(8,4,8,4);

        String tenNV="",sdtNV="",emailNV="",maNV="";
        try(Connection c=DatabaseHelper.getConnection()){
            PreparedStatement ps=c.prepareStatement(
                "SELECT nv.maNhanVien,nv.tenNhanVien,nv.sdt,nv.email FROM TaiKhoan tk "
                +"LEFT JOIN NhanVien nv ON tk.idNhanVien=nv.id WHERE tk.id=?");
            ps.setInt(1,user.getId()); ResultSet rs=ps.executeQuery();
            if(rs.next()){maNV=safe(rs.getString("maNhanVien"));tenNV=safe(rs.getString("tenNhanVien"));
                sdtNV=safe(rs.getString("sdt"));emailNV=safe(rs.getString("email"));}
        }catch(Exception ignored){}

        FormPanel.addDetailRow(body,gc,0,"Mã nhân viên:",maNV);
        FormPanel.addDetailRow(body,gc,1,"Họ và tên:",tenNV);
        FormPanel.addDetailRow(body,gc,2,"Tên đăng nhập:",user.getUsername());
        FormPanel.addDetailRow(body,gc,3,"Vai trò:",user.getRole());
        FormPanel.addDetailRow(body,gc,4,"Số điện thoại:",sdtNV);
        FormPanel.addDetailRow(body,gc,5,"Email:",emailNV);

        JScrollPane sp=new JScrollPane(body); sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(Color.WHITE);
        dlg.add(sp,BorderLayout.CENTER);

        JButton btnChangePwd=UITheme.outlineButton("Đổi mật khẩu");
        JButton btnClose2=UITheme.primaryButton("Đóng");
        btnClose2.addActionListener(e->dlg.dispose());
        btnChangePwd.addActionListener(e->{dlg.dispose();showChangePasswordDialog();});
        dlg.add(FormPanel.createFooter(btnChangePwd,btnClose2),BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void showChangePasswordDialog() {
        TaiKhoan user=Session.getUser(); if(user==null) return;
        JDialog dlg=new JDialog(this,"Đổi mật khẩu",true);
        dlg.setSize(400,300); dlg.setLocationRelativeTo(this); dlg.setResizable(false);
        dlg.setLayout(new BorderLayout());
        dlg.add(FormPanel.createHeader("Đổi mật khẩu tài khoản"),BorderLayout.NORTH);

        JPanel fields=new JPanel(new GridBagLayout());
        fields.setBackground(Color.WHITE); fields.setBorder(new EmptyBorder(18,22,10,22));
        GridBagConstraints gc=FormPanel.defaultGBC();
        JPasswordField txtOld=new JPasswordField(),txtNew=new JPasswordField(),txtNew2=new JPasswordField();
        for(JPasswordField f:new JPasswordField[]{txtOld,txtNew,txtNew2}){
            f.setFont(UITheme.FONT_LABEL); f.setPreferredSize(new Dimension(220,34));
            f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR,1,true),
                BorderFactory.createEmptyBorder(6,10,6,10)));
        }
        FormPanel.addRow(fields,gc,0,"Mật khẩu hiện tại *",txtOld);
        FormPanel.addRow(fields,gc,1,"Mật khẩu mới *",txtNew);
        FormPanel.addRow(fields,gc,2,"Xác nhận mật khẩu *",txtNew2);
        dlg.add(FormPanel.createBody(fields),BorderLayout.CENTER);

        JButton btnSave=UITheme.primaryButton("Lưu"),btnCancel=UITheme.outlineButton("Hủy");
        btnCancel.addActionListener(e->dlg.dispose());
        btnSave.addActionListener(e->{
            String o=new String(txtOld.getPassword()).trim();
            String n=new String(txtNew.getPassword()).trim();
            String n2=new String(txtNew2.getPassword()).trim();
            if(o.isEmpty()||n.isEmpty()||n2.isEmpty()){JOptionPane.showMessageDialog(dlg,"Vui lòng nhập đầy đủ.");return;}
            if(!n.equals(n2)){JOptionPane.showMessageDialog(dlg,"Mật khẩu xác nhận không khớp.");return;}
            if(n.length()<6){JOptionPane.showMessageDialog(dlg,"Mật khẩu mới phải ≥ 6 ký tự.");return;}
            try(Connection c=DatabaseHelper.getConnection()){
                PreparedStatement chk=c.prepareStatement("SELECT id FROM TaiKhoan WHERE id=? AND password=?");
                chk.setInt(1,user.getId()); chk.setString(2,o);
                if(!chk.executeQuery().next()){JOptionPane.showMessageDialog(dlg,"Mật khẩu hiện tại không đúng.");return;}
                PreparedStatement upd=c.prepareStatement("UPDATE TaiKhoan SET password=? WHERE id=?");
                upd.setString(1,n); upd.setInt(2,user.getId()); upd.executeUpdate();
                JOptionPane.showMessageDialog(dlg,"Đổi mật khẩu thành công!"); dlg.dispose();
            }catch(Exception ex){JOptionPane.showMessageDialog(dlg,"Lỗi: "+ex.getMessage());}
        });
        dlg.add(FormPanel.createFooter(btnCancel,btnSave),BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  UTILS
    // ══════════════════════════════════════════════════════════════════════
    private static final java.util.Map<String,String> PAGE_NAMES=new java.util.HashMap<>(){{
        put("dashboard","Trang chủ"); put("hoivien","Quản lý hồ sơ hội viên");
        put("hoatdong","Hoạt động"); put("thamgia","Tham gia");
        put("nhanvien","Nhân viên"); put("nhatky","Nhật ký");
    }};

    private void switchPanel(String name){
        cardLayout.show(contentPanel,name);
        if(navPageLabel!=null) navPageLabel.setText(PAGE_NAMES.getOrDefault(name,"Trang chủ"));
    }

    private boolean isAdmin(){
        TaiKhoan u=Session.getUser();
        return u!=null&&"Admin".equalsIgnoreCase(u.getRole());
    }

    private void logout(){
        int c=JOptionPane.showConfirmDialog(this,"Bạn có chắc chắn muốn đăng xuất?",
            "Đăng xuất",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
        if(c==JOptionPane.YES_OPTION){
            if(notificationTimer!=null) notificationTimer.stop();
            new AuthController().logout();
            dispose();
            SwingUtilities.invokeLater(()->new LoginForm().setVisible(true));
        }
    }

    private String safe(String v){return(v==null||"null".equalsIgnoreCase(v))?"":v;}
    private String str(Object o){return o==null?"":o.toString();}
}