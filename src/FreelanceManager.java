import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.plaf.basic.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.font.*;
import java.text.*;
import java.util.*;
import java.util.List;
import java.sql.*;

public class FreelanceManager extends JFrame {

    // ── Color Palette ──────────────────────────────────────────────
    static final Color BG_DARK      = new Color(0x0D0F14);
    static final Color BG_CARD      = new Color(0x161B25);
    static final Color BG_HOVER     = new Color(0x1E2535);
    static final Color ACCENT       = new Color(0x00D4FF);
    static final Color ACCENT2      = new Color(0x7B61FF);
    static final Color SUCCESS      = new Color(0x00E5A0);
    static final Color WARNING      = new Color(0xFFB800);
    static final Color DANGER       = new Color(0xFF4D6A);
    static final Color TEXT_PRIMARY = new Color(0xF0F4FF);
    static final Color TEXT_MUTED   = new Color(0x6B7A99);
    static final Color BORDER_COLOR = new Color(0x252D3D);
    static final Color TABLE_ALT    = new Color(0x131720);

    // ── Fonts ──────────────────────────────────────────────────────
    static Font FONT_TITLE, FONT_HEADER, FONT_BODY, FONT_MONO, FONT_SMALL;

    // ── Data (kept only for Dashboard KPIs — loaded from DB) ──────
    static List<Client>  clients  = new ArrayList<>();
    static List<Project> projects = new ArrayList<>();
    static List<Payment> payments = new ArrayList<>();
    static int clientIdSeq = 1, projectIdSeq = 1, paymentIdSeq = 1;

    JPanel mainContent;
    JPanel activeNavBtn = null;

    // ─────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        initFonts();
        SwingUtilities.invokeLater(() -> new FreelanceManager().setVisible(true));
    }

    static void initFonts() {
        try {
            FONT_TITLE  = new Font("SansSerif", Font.BOLD,  22);
            FONT_HEADER = new Font("SansSerif", Font.BOLD,  13);
            FONT_BODY   = new Font("SansSerif", Font.PLAIN, 13);
            FONT_MONO   = new Font("Monospaced",Font.PLAIN, 12);
            FONT_SMALL  = new Font("SansSerif", Font.PLAIN, 11);
        } catch (Exception e) {
            FONT_TITLE  = new Font(Font.SANS_SERIF, Font.BOLD,  22);
            FONT_HEADER = new Font(Font.SANS_SERIF, Font.BOLD,  13);
            FONT_BODY   = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
            FONT_MONO   = new Font(Font.MONOSPACED, Font.PLAIN, 12);
            FONT_SMALL  = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
        }
    }

    // ─────────────────────────────────────────────────────────────
    public FreelanceManager() {
        setTitle("FreelanceHub — Dashboard");
        setSize(1280, 820);
        setMinimumSize(new Dimension(1100, 700));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout());

        add(buildSidebar(), BorderLayout.WEST);

        mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(BG_DARK);
        add(mainContent, BorderLayout.CENTER);

        showDashboard();
    }

    // ── SIDEBAR ─────────────────────────────────────────────────
    JPanel buildSidebar() {
        JPanel side = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(BORDER_COLOR);
                g2.drawLine(getWidth()-1, 0, getWidth()-1, getHeight());
            }
        };
        side.setPreferredSize(new Dimension(230, 0));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setOpaque(false);

        JPanel logo = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        logo.setOpaque(false);
        logo.setPreferredSize(new Dimension(230, 72));
        logo.setMaximumSize(new Dimension(230, 72));
        JLabel icon = new JLabel("◈") {{ setFont(new Font("SansSerif", Font.BOLD, 26)); setForeground(ACCENT); }};
        JPanel logoText = new JPanel(new GridLayout(2,1,0,0));
        logoText.setOpaque(false);
        logoText.add(label("FreelanceHub", FONT_TITLE, TEXT_PRIMARY));
        logoText.add(label("Pro Dashboard", FONT_SMALL, TEXT_MUTED));
        logo.add(icon); logo.add(logoText);
        side.add(logo);
        side.add(separator());

        String[][] navItems = {
            {"⬛ Dashboard","dashboard"},{"◉ Clients","clients"},
            {"◈ Projects","projects"},{"$ Payments","payments"},{"≡ Reports","reports"}
        };
        for (String[] item : navItems) side.add(navButton(item[0], item[1]));

        side.add(Box.createVerticalGlue());
        side.add(separator());

        JPanel profile = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        profile.setOpaque(false);
        profile.setPreferredSize(new Dimension(230, 64));
        profile.setMaximumSize(new Dimension(230, 64));
        JLabel av = new JLabel("AP") {{
            setFont(FONT_HEADER); setForeground(BG_DARK); setHorizontalAlignment(CENTER);
            setOpaque(true); setBackground(ACCENT); setPreferredSize(new Dimension(36,36));
        }};
        JPanel pInfo = new JPanel(new GridLayout(2,1,0,1));
        pInfo.setOpaque(false);
        pInfo.add(label("Aarav Patel", FONT_HEADER, TEXT_PRIMARY));
        pInfo.add(label("Freelancer", FONT_SMALL, TEXT_MUTED));
        profile.add(av); profile.add(pInfo);
        side.add(profile);
        return side;
    }

    JPanel navButton(String text, String view) {
        JPanel btn = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 10)) {
            boolean hovered = false;
            { setOpaque(false);
              addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hovered=true; repaint(); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
                public void mouseExited (MouseEvent e) { hovered=false; repaint(); setCursor(Cursor.getDefaultCursor()); }
                public void mouseClicked(MouseEvent e) {
                    if (activeNavBtn != null) activeNavBtn.repaint();
                    activeNavBtn = (JPanel) e.getSource();
                    switch(view) {
                        case "dashboard": showDashboard(); break;
                        case "clients":   showClients();   break;
                        case "projects":  showProjects();  break;
                        case "payments":  showPayments();  break;
                        case "reports":   showReports();   break;
                    }
                }
              });
            }
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean active = (this == activeNavBtn);
                if (active) {
                    g2.setColor(new Color(0,212,255,30));
                    g2.fillRoundRect(10,4,getWidth()-20,getHeight()-8,8,8);
                    g2.setColor(ACCENT);
                    g2.fillRoundRect(0,4,4,getHeight()-8,4,4);
                } else if (hovered) {
                    g2.setColor(BG_HOVER);
                    g2.fillRoundRect(10,4,getWidth()-20,getHeight()-8,8,8);
                }
                super.paintComponent(g);
            }
        };
        btn.setPreferredSize(new Dimension(230, 44));
        btn.setMaximumSize(new Dimension(230, 44));
        boolean active = view.equals("dashboard");
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_BODY);
        lbl.setForeground(active ? ACCENT : TEXT_MUTED);
        btn.add(lbl);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { lbl.setForeground(TEXT_PRIMARY); }
            public void mouseExited (MouseEvent e) { lbl.setForeground(btn==activeNavBtn ? ACCENT : TEXT_MUTED); }
        });
        if (active) activeNavBtn = btn;
        return btn;
    }

    Component separator() {
        JPanel sep = new JPanel() {
            protected void paintComponent(Graphics g) {
                g.setColor(BORDER_COLOR);
                g.drawLine(14, getHeight()/2, getWidth()-14, getHeight()/2);
            }
        };
        sep.setOpaque(false);
        sep.setPreferredSize(new Dimension(230, 14));
        sep.setMaximumSize(new Dimension(230, 14));
        return sep;
    }

    // ─────────────────────────────────────────────────────────────
    // DASHBOARD
    // ─────────────────────────────────────────────────────────────
    void showDashboard() {
        mainContent.removeAll();
        JPanel p = darkPanel();
        p.setLayout(new BorderLayout());
        p.add(pageHeader("Dashboard", "Welcome back, PRASANNA! Here's your overview."), BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(buildDashboardBody());
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_DARK);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        styleScrollBar(scroll);
        p.add(scroll, BorderLayout.CENTER);
        mainContent.add(p, BorderLayout.CENTER);
        mainContent.revalidate();
        mainContent.repaint();
    }

    JPanel buildDashboardBody() {
        // Load summary data from DB
        double totalRevenue = 0, pendingAmt = 0;
        long activeProj = 0, activeClnt = 0, totalProj = 0, totalClnt = 0;
        List<Object[]> recentProjects = new ArrayList<>();
        List<Object[]> recentPayments = new ArrayList<>();
        List<Object[]> allProjects    = new ArrayList<>();

        try {
            Connection con = DBConnection.getConnection();

            // KPIs
            ResultSet rs = con.createStatement().executeQuery(
                "SELECT COALESCE(SUM(amount),0) FROM payments WHERE status='Received'");
            if (rs.next()) totalRevenue = rs.getDouble(1);

            rs = con.createStatement().executeQuery(
                "SELECT COALESCE(SUM(amount),0) FROM payments WHERE status='Pending'");
            if (rs.next()) pendingAmt = rs.getDouble(1);

            rs = con.createStatement().executeQuery(
                "SELECT COUNT(*) FROM projects WHERE status='In Progress'");
            if (rs.next()) activeProj = rs.getLong(1);

            rs = con.createStatement().executeQuery("SELECT COUNT(*) FROM projects");
            if (rs.next()) totalProj = rs.getLong(1);

            rs = con.createStatement().executeQuery(
                "SELECT COUNT(*) FROM clients WHERE status='Active'");
            if (rs.next()) activeClnt = rs.getLong(1);

            rs = con.createStatement().executeQuery("SELECT COUNT(*) FROM clients");
            if (rs.next()) totalClnt = rs.getLong(1);

            // Recent Projects (top 5)
            PreparedStatement ps = con.prepareStatement(
                "SELECT p.name, c.name, p.status, p.value FROM projects p " +
                "JOIN clients c ON p.client_id=c.id ORDER BY p.id DESC LIMIT 5");
            rs = ps.executeQuery();
            while (rs.next())
                recentProjects.add(new Object[]{rs.getString(1),rs.getString(2),rs.getString(3),"₹"+fmt(rs.getDouble(4))});

            // Recent Payments (top 5)
            ps = con.prepareStatement(
                "SELECT description, amount, status, date FROM payments ORDER BY id DESC LIMIT 5");
            rs = ps.executeQuery();
            while (rs.next())
                recentPayments.add(new Object[]{rs.getString(1),"₹"+fmt(rs.getDouble(2)),rs.getString(3),rs.getString(4)});

            // All projects for progress bars
            ps = con.prepareStatement(
                "SELECT p.name, p.paid, p.value, p.status FROM projects p ORDER BY p.id");
            rs = ps.executeQuery();
            while (rs.next())
                allProjects.add(new Object[]{rs.getString(1),rs.getDouble(2),rs.getDouble(3),rs.getString(4)});

            con.close();
        } catch (Exception e) { e.printStackTrace(); }

        JPanel body = darkPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(0,28,28,28));

        // KPI Cards
        JPanel kpis = new JPanel(new GridLayout(1,4,16,0));
        kpis.setOpaque(false);
        kpis.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        kpis.add(kpiCard("Total Revenue",   "₹"+fmt(totalRevenue), "+18% this month",           SUCCESS, "▲"));
        kpis.add(kpiCard("Pending Payments","₹"+fmt(pendingAmt),   "Awaiting payment",           WARNING, "⌛"));
        kpis.add(kpiCard("Active Projects", String.valueOf(activeProj), "of "+totalProj+" total", ACCENT,  "◈"));
        kpis.add(kpiCard("Active Clients",  String.valueOf(activeClnt), "of "+totalClnt+" total", ACCENT2, "◉"));
        body.add(kpis);
        body.add(Box.createVerticalStrut(24));

        // Two-column: recent projects + recent payments
        JPanel cols = new JPanel(new GridLayout(1,2,20,0));
        cols.setOpaque(false);
        cols.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        JPanel rProj = card("Recent Projects");
        DefaultTableModel projModel = new DefaultTableModel(new String[]{"Project","Client","Status","Value"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Object[] row : recentProjects) projModel.addRow(row);
        rProj.add(styledTable(projModel, new int[]{200,110,90,80}), BorderLayout.CENTER);

        JPanel rPay = card("Recent Payments");
        DefaultTableModel payModel = new DefaultTableModel(new String[]{"Description","Amount","Status","Date"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Object[] row : recentPayments) payModel.addRow(row);
        rPay.add(styledTable(payModel, new int[]{160,90,80,90}), BorderLayout.CENTER);

        cols.add(rProj); cols.add(rPay);
        body.add(cols);
        body.add(Box.createVerticalStrut(24));

        // Project progress bars
        JPanel progCard = card("Project Progress");
        JPanel progBody = darkPanel();
        progBody.setLayout(new BoxLayout(progBody, BoxLayout.Y_AXIS));
        for (Object[] row : allProjects) {
            String name   = (String)  row[0];
            double paid   = (Double)  row[1];
            double value  = (Double)  row[2];
            String status = (String)  row[3];
            int pct = value > 0 ? (int)((paid / value) * 100) : 0;
            progBody.add(progressRow(name, pct, status));
            progBody.add(Box.createVerticalStrut(10));
        }
        JScrollPane ps = new JScrollPane(progBody);
        ps.setBorder(null);
        ps.getViewport().setBackground(BG_CARD);
        ps.setPreferredSize(new Dimension(0, 200));
        styleScrollBar(ps);
        progCard.add(ps, BorderLayout.CENTER);
        progCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
        body.add(progCard);

        return body;
    }

    JPanel kpiCard(String title, String value, String sub, Color accent, String icon) {
        JPanel c = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.setColor(accent);
                g2.fillRoundRect(0,0,4,getHeight(),4,4);
                GradientPaint gp = new GradientPaint(getWidth()-60,0,new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),40),getWidth(),getHeight(),new Color(0,0,0,0));
                g2.setPaint(gp);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                super.paintComponent(g);
            }
        };
        c.setOpaque(false);
        c.setBorder(new EmptyBorder(16,20,16,16));
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(label(title, FONT_SMALL, TEXT_MUTED), BorderLayout.WEST);
        top.add(label(icon, new Font("SansSerif", Font.PLAIN,18), accent), BorderLayout.EAST);
        JLabel val = new JLabel(value);
        val.setFont(new Font("SansSerif", Font.BOLD, 24));
        val.setForeground(TEXT_PRIMARY);
        c.add(top, BorderLayout.NORTH);
        c.add(val,  BorderLayout.CENTER);
        c.add(label(sub, FONT_SMALL, TEXT_MUTED), BorderLayout.SOUTH);
        return c;
    }

    JPanel progressRow(String name, int pct, String status) {
        JPanel row = new JPanel(new BorderLayout(10,0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(4,12,4,12));
        JLabel nameLbl = label(name, FONT_BODY, TEXT_PRIMARY);
        nameLbl.setPreferredSize(new Dimension(180,20));
        JPanel barOuter = new JPanel(null) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_DARK);
                g2.fillRoundRect(0,6,getWidth(),8,8,8);
                Color c = pct==100 ? SUCCESS : pct>50 ? ACCENT : WARNING;
                int w = (int)(getWidth()*pct/100.0);
                if (w > 0) {
                    GradientPaint gp = new GradientPaint(0,0,c,w,0,c.brighter());
                    g2.setPaint(gp);
                    g2.fillRoundRect(0,6,w,8,8,8);
                }
            }
        };
        barOuter.setOpaque(false);
        barOuter.setPreferredSize(new Dimension(0,20));
        Color sc = status.equals("Completed") ? SUCCESS : status.equals("In Progress") ? ACCENT : WARNING;
        JLabel pctLbl = label(pct+"%", FONT_SMALL, sc);
        pctLbl.setPreferredSize(new Dimension(42,20));
        pctLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        JLabel statLbl = statusBadge(status);
        statLbl.setPreferredSize(new Dimension(90,20));
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));
        right.setOpaque(false);
        right.add(pctLbl); right.add(statLbl);
        row.add(nameLbl,  BorderLayout.WEST);
        row.add(barOuter, BorderLayout.CENTER);
        row.add(right,    BorderLayout.EAST);
        return row;
    }

    // ─────────────────────────────────────────────────────────────
    // CLIENTS  (unchanged — already DB-driven)
    // ─────────────────────────────────────────────────────────────
    void showClients() {
        mainContent.removeAll();
        JPanel p = darkPanel();
        p.setLayout(new BorderLayout());
        p.add(pageHeader("Clients", "Manage your client relationships"), BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID","Name","Company","Email","Phone","Status","Action"}, 0) {
            public boolean isCellEditable(int r, int c) { return c == 6; }
        };
        refreshClientModel(model);

        JTable table = new JTable(model);
        styleTableFull(table);
        table.getColumnModel().getColumn(6).setCellRenderer(new ActionButtonRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new ActionButtonEditor(new JCheckBox(), "client", model, table));
        setColumnWidths(table, new int[]{50,130,140,190,130,80,100});

        JPanel content = darkPanel();
        content.setLayout(new BorderLayout());
        content.setBorder(new EmptyBorder(0,28,28,28));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(0,0,14,0));

        JTextField search = searchField("Search clients...");
        JButton addBtn = accentButton("+ Add Client");
        addBtn.addActionListener(e -> showAddClientDialog());
        topBar.add(search, BorderLayout.WEST);
        topBar.add(addBtn, BorderLayout.EAST);
        content.add(topBar, BorderLayout.NORTH);

        JPanel tableCard = card(null);
        tableCard.setLayout(new BorderLayout());
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(null);
        sp.getViewport().setBackground(BG_CARD);
        styleScrollBar(sp);
        tableCard.add(sp, BorderLayout.CENTER);
        content.add(tableCard, BorderLayout.CENTER);

        search.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String q = search.getText().toLowerCase();
                model.setRowCount(0);
                try {
                    Connection con = DBConnection.getConnection();
                    PreparedStatement ps = con.prepareStatement(
                        "SELECT * FROM clients WHERE LOWER(name) LIKE ? OR LOWER(company) LIKE ? OR LOWER(email) LIKE ?");
                    ps.setString(1, "%"+q+"%"); ps.setString(2, "%"+q+"%"); ps.setString(3, "%"+q+"%");
                    ResultSet rs = ps.executeQuery();
                    while (rs.next())
                        model.addRow(new Object[]{rs.getInt("id"),rs.getString("name"),rs.getString("company"),
                            rs.getString("email"),rs.getString("phone"),rs.getString("status"),"Edit | Delete"});
                    con.close();
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });

        p.add(content, BorderLayout.CENTER);
        mainContent.add(p, BorderLayout.CENTER);
        mainContent.revalidate();
        mainContent.repaint();
    }

    void refreshClientModel(DefaultTableModel model) {
        model.setRowCount(0);
        try {
            Connection con = DBConnection.getConnection();
            ResultSet rs = con.prepareStatement("SELECT * FROM clients").executeQuery();
            while (rs.next())
                model.addRow(new Object[]{rs.getInt("id"),rs.getString("name"),rs.getString("company"),
                    rs.getString("email"),rs.getString("phone"),rs.getString("status"),"Edit | Delete"});
            con.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    void showAddClientDialog() { showClientDialog(null, -1); }

    // editId = DB id of row being edited; -1 for new
    void showClientDialog(Object[] rowData, int editId) {
        JDialog dlg = styledDialog(editId == -1 ? "Add Client" : "Edit Client", 500, 480);
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BG_CARD);
        form.setBorder(new EmptyBorder(24,28,24,28));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL; gc.insets = new Insets(6,0,6,0);

        JTextField fName    = formField(rowData!=null ? (String)rowData[1] : "");
        JTextField fCompany = formField(rowData!=null ? (String)rowData[2] : "");
        JTextField fEmail   = formField(rowData!=null ? (String)rowData[3] : "");
        JTextField fPhone   = formField(rowData!=null ? (String)rowData[4] : "");
        JComboBox<String> fStatus = styledCombo(new String[]{"Active","Inactive"});
        if (rowData != null) fStatus.setSelectedItem(rowData[5]);

        addFormRow(form,gc,"Full Name *",  fName,    0);
        addFormRow(form,gc,"Company",      fCompany, 1);
        addFormRow(form,gc,"Email *",      fEmail,   2);
        addFormRow(form,gc,"Phone",        fPhone,   3);
        addFormRow(form,gc,"Status",       fStatus,  4);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));
        btns.setBackground(BG_CARD);
        JButton cancel = ghostButton("Cancel");
        JButton save   = accentButton(editId == -1 ? "Add Client" : "Save Changes");
        cancel.addActionListener(e -> dlg.dispose());
        save.addActionListener(e -> {
            if (fName.getText().trim().isEmpty() || fEmail.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dlg,"Name and Email are required.","Validation",JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                Connection con = DBConnection.getConnection();
                if (editId == -1) {
                    PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO clients(name,company,email,phone,status) VALUES(?,?,?,?,?)");
                    ps.setString(1,fName.getText().trim()); ps.setString(2,fCompany.getText().trim());
                    ps.setString(3,fEmail.getText().trim()); ps.setString(4,fPhone.getText().trim());
                    ps.setString(5,(String)fStatus.getSelectedItem());
                    ps.executeUpdate();
                } else {
                    PreparedStatement ps = con.prepareStatement(
                        "UPDATE clients SET name=?,company=?,email=?,phone=?,status=? WHERE id=?");
                    ps.setString(1,fName.getText().trim()); ps.setString(2,fCompany.getText().trim());
                    ps.setString(3,fEmail.getText().trim()); ps.setString(4,fPhone.getText().trim());
                    ps.setString(5,(String)fStatus.getSelectedItem()); ps.setInt(6,editId);
                    ps.executeUpdate();
                }
                con.close();
            } catch (Exception ex) { ex.printStackTrace(); }
            dlg.dispose();
            showClients();
        });  btns.add(cancel); btns.add(save);
        gc.gridx=0; gc.gridy=5; gc.gridwidth=2; gc.insets=new Insets(18,0,0,0);
        form.add(btns,gc);
        dlg.add(form);
        dlg.setVisible(true);
    }


    void showProjects() {
        mainContent.removeAll();
        JPanel p = darkPanel();
        p.setLayout(new BorderLayout());
        p.add(pageHeader("Projects", "Track your project pipeline"), BorderLayout.NORTH);
 
       
        DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID","Project Name","Client","Category","Status","Start","End","Value","Paid","Action"}, 0) {
            public boolean isCellEditable(int r, int c) { return c == 9; }
        };
        refreshProjectModel(model);

        JTable table = new JTable(model);
        styleTableFull(table);
        table.getColumnModel().getColumn(9).setCellRenderer(new ActionButtonRenderer());
        table.getColumnModel().getColumn(9).setCellEditor(new ActionButtonEditor(new JCheckBox(),"project",model,table));
        setColumnWidths(table, new int[]{40,160,120,110,90,85,85,80,70,100});

        JPanel content = darkPanel();
        content.setLayout(new BorderLayout());
        content.setBorder(new EmptyBorder(0,28,28,28));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(0,0,14,0));
        JTextField search = searchField("Search projects...");
        JButton addBtn = accentButton("+ New Project");
        addBtn.addActionListener(e -> showProjectDialog(null, -1));
        topBar.add(search, BorderLayout.WEST);
        topBar.add(addBtn, BorderLayout.EAST);

        search.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String q = search.getText().toLowerCase();
                model.setRowCount(0);
                try {
                    Connection con = DBConnection.getConnection();
                    PreparedStatement ps = con.prepareStatement(
                        "SELECT p.id, p.name, c.name, p.category, p.status, " +
                        "p.start_date, p.end_date, p.value, p.paid " +
                        "FROM projects p JOIN clients c ON p.client_id=c.id " +
                        "WHERE LOWER(p.name) LIKE ? OR LOWER(c.name) LIKE ? OR LOWER(p.category) LIKE ?");
                    ps.setString(1,"%"+q+"%"); ps.setString(2,"%"+q+"%"); ps.setString(3,"%"+q+"%");
                    ResultSet rs = ps.executeQuery();
                    while (rs.next())
                        model.addRow(new Object[]{
                            rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),
                            rs.getString(6),rs.getString(7),
                            "₹"+fmt(rs.getDouble(8)),"₹"+fmt(rs.getDouble(9)),"Edit | Delete"});
                    con.close();
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });

        JPanel tableCard = card(null);
        tableCard.setLayout(new BorderLayout());
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(null);
        sp.getViewport().setBackground(BG_CARD);
        styleScrollBar(sp);
        tableCard.add(sp, BorderLayout.CENTER);

        content.add(topBar,    BorderLayout.NORTH);
        content.add(tableCard, BorderLayout.CENTER);
        p.add(content, BorderLayout.CENTER);
        mainContent.add(p, BorderLayout.CENTER);
        mainContent.revalidate();
        mainContent.repaint();
    }

    void refreshProjectModel(DefaultTableModel model) {
        model.setRowCount(0);
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                "SELECT p.id, p.name, c.name, p.category, p.status, " +
                "p.start_date, p.end_date, p.value, p.paid " +
                "FROM projects p JOIN clients c ON p.client_id=c.id ORDER BY p.id");
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                model.addRow(new Object[]{
                    rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),
                    rs.getString(6),rs.getString(7),
                    "₹"+fmt(rs.getDouble(8)),"₹"+fmt(rs.getDouble(9)),"Edit | Delete"});
            con.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // rowData = table row values (for edit pre-fill); editId = DB id, -1 for new
    void showProjectDialog(Object[] rowData, int editId) {
        JDialog dlg = styledDialog(editId == -1 ? "New Project" : "Edit Project", 540, 560);
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BG_CARD);
        form.setBorder(new EmptyBorder(24,28,24,28));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL; gc.insets = new Insets(6,0,6,0);

        // Load clients from DB as "id: name" strings — parse id back on save, no index mismatch possible
        List<String> clientEntries = new ArrayList<>();
        try {
            Connection con = DBConnection.getConnection();
            ResultSet rs = con.prepareStatement("SELECT id, name FROM clients ORDER BY name").executeQuery();
            while (rs.next())
                clientEntries.add(rs.getInt(1) + ": " + rs.getString(2));
            con.close();
        } catch (Exception e) { e.printStackTrace(); }

        if (clientEntries.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No clients found. Please add a client before creating a project.",
                "No Clients", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JComboBox<String> fClient = styledCombo(clientEntries.toArray(new String[0]));
        JTextField fName   = formField(rowData != null ? (String)rowData[1] : "");
        JComboBox<String> fCat = styledCombo(new String[]{
            "Web Development","Mobile Dev","Design","Backend Dev","Marketing","Consulting","Other"});
        JComboBox<String> fStatus = styledCombo(new String[]{
            "In Progress","Completed","On Hold","Not Started"});
        JTextField fStart  = formField(rowData != null ? (String)rowData[5] : "2025-01-01");
        JTextField fEnd    = formField(rowData != null ? (String)rowData[6] : "2025-12-31");
        JTextField fValue  = formField("0");
        JTextField fPaid   = formField("0");

        // Pre-select combos and fetch exact numeric values when editing
        if (editId != -1) {
            // Pre-select client by matching "id: name" entry
            if (rowData != null) {
                String clientName = (String) rowData[2];
                for (int i = 0; i < clientEntries.size(); i++)
                    if (clientEntries.get(i).contains(": " + clientName)) { fClient.setSelectedIndex(i); break; }
                fCat.setSelectedItem(rowData[3]);
                fStatus.setSelectedItem(rowData[4]);
            }
            // Always re-fetch value/paid from DB to avoid fmt() rounding
            try {
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(
                    "SELECT client_id, category, status, value, paid FROM projects WHERE id=?");
                ps.setInt(1, editId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int dbClientId = rs.getInt(1);
                    // Match client id in combo entries
                    for (int i = 0; i < clientEntries.size(); i++)
                        if (clientEntries.get(i).startsWith(dbClientId + ":")) { fClient.setSelectedIndex(i); break; }
                    fCat.setSelectedItem(rs.getString(2));
                    fStatus.setSelectedItem(rs.getString(3));
                    fValue.setText(String.valueOf((int)rs.getDouble(4)));
                    fPaid.setText(String.valueOf((int)rs.getDouble(5)));
                }
                con.close();
            } catch (Exception e) { e.printStackTrace(); }
        }

        addFormRow(form,gc,"Client *",        fClient, 0);
        addFormRow(form,gc,"Project Name *",  fName,   1);
        addFormRow(form,gc,"Category",        fCat,    2);
        addFormRow(form,gc,"Status",          fStatus, 3);
        addFormRow(form,gc,"Start Date",      fStart,  4);
        addFormRow(form,gc,"End Date",        fEnd,    5);
        addFormRow(form,gc,"Total Value (₹)", fValue,  6);
        addFormRow(form,gc,"Paid So Far (₹)", fPaid,   7);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));
        btns.setBackground(BG_CARD);
        JButton cancel = ghostButton("Cancel");
        JButton save   = accentButton(editId == -1 ? "Add Project" : "Save Changes");
        cancel.addActionListener(e -> dlg.dispose());
        save.addActionListener(e -> {
            if (fName.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dlg,"Project Name is required.","Validation",JOptionPane.WARNING_MESSAGE);
                return;
            }
            String selectedEntry = (String) fClient.getSelectedItem();
            if (selectedEntry == null || !selectedEntry.contains(":")) {
                JOptionPane.showMessageDialog(dlg,"Please select a valid client.","Validation",JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Parse client id directly from "id: name" — no index arithmetic needed
            int cid;
            try {
                cid = Integer.parseInt(selectedEntry.split(":")[0].trim());
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(dlg,"Invalid client selection.","Error",JOptionPane.ERROR_MESSAGE);
                return;
            }
            double val  = 0, paid = 0;
            try { val  = Double.parseDouble(fValue.getText().trim()); } catch (Exception ex) {}
            try { paid = Double.parseDouble(fPaid.getText().trim());  } catch (Exception ex) {}
            try {
                Connection con = DBConnection.getConnection();
                if (editId == -1) {
                    PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO projects(client_id,name,category,status,start_date,end_date,value,paid) VALUES(?,?,?,?,?,?,?,?)");
                    ps.setInt(1,cid); ps.setString(2,fName.getText().trim());
                    ps.setString(3,(String)fCat.getSelectedItem()); ps.setString(4,(String)fStatus.getSelectedItem());
                    ps.setString(5,fStart.getText().trim()); ps.setString(6,fEnd.getText().trim());
                    ps.setDouble(7,val); ps.setDouble(8,paid);
                    ps.executeUpdate();
                } else {
                    PreparedStatement ps = con.prepareStatement(
                        "UPDATE projects SET client_id=?,name=?,category=?,status=?,start_date=?,end_date=?,value=?,paid=? WHERE id=?");
                    ps.setInt(1,cid); ps.setString(2,fName.getText().trim());
                    ps.setString(3,(String)fCat.getSelectedItem()); ps.setString(4,(String)fStatus.getSelectedItem());
                    ps.setString(5,fStart.getText().trim()); ps.setString(6,fEnd.getText().trim());
                    ps.setDouble(7,val); ps.setDouble(8,paid); ps.setInt(9,editId);
                    ps.executeUpdate();
                }
                con.close();
            } catch (Exception ex) { ex.printStackTrace(); }
            dlg.dispose();
            showProjects();
        });
        btns.add(cancel); btns.add(save);
        gc.gridx=0; gc.gridy=8; gc.gridwidth=2; gc.insets=new Insets(18,0,0,0);
        form.add(btns,gc);
        dlg.add(form);
        dlg.setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────
    // PAYMENTS  — fully DB-driven
    // ─────────────────────────────────────────────────────────────
    void showPayments() {
        mainContent.removeAll();
        JPanel p = darkPanel();
        p.setLayout(new BorderLayout());
        p.add(pageHeader("Payments", "Track invoices and payment status"), BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID","Description","Client","Project","Amount","Date","Method","Status","Action"}, 0) {
            public boolean isCellEditable(int r, int c) { return c == 8; }
        };
        refreshPaymentModel(model);

        JTable table = new JTable(model);
        styleTableFull(table);
        table.getColumnModel().getColumn(8).setCellRenderer(new ActionButtonRenderer());
        table.getColumnModel().getColumn(8).setCellEditor(new ActionButtonEditor(new JCheckBox(),"payment",model,table));
        setColumnWidths(table, new int[]{40,150,120,140,90,90,110,80,100});

        JPanel content = darkPanel();
        content.setLayout(new BorderLayout());
        content.setBorder(new EmptyBorder(0,28,28,28));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(0,0,14,0));

        JPanel leftBar = new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));
        leftBar.setOpaque(false);
        JTextField search = searchField("Search payments...");
        JComboBox<String> filter = styledCombo(new String[]{"All Status","Received","Pending","Overdue"});
        leftBar.add(search); leftBar.add(filter);

        JButton addBtn = accentButton("+ Record Payment");
        addBtn.addActionListener(e -> showPaymentDialog(null, -1));
        topBar.add(leftBar, BorderLayout.WEST);
        topBar.add(addBtn,  BorderLayout.EAST);

        // Filter combo
        filter.addActionListener(e -> {
            String f = (String) filter.getSelectedItem();
            model.setRowCount(0);
            try {
                Connection con = DBConnection.getConnection();
                PreparedStatement ps;
                if ("All Status".equals(f)) {
                    ps = con.prepareStatement(
                        "SELECT py.id, py.description, c.name, pr.name, py.amount, py.`date`, py.method, py.status " +
                        "FROM payments py JOIN clients c ON py.client_id=c.id JOIN projects pr ON py.project_id=pr.id " +
                        "ORDER BY py.id DESC");
                } else {
                    ps = con.prepareStatement(
                        "SELECT py.id, py.description, c.name, pr.name, py.amount, py.`date`, py.method, py.status " +
                        "FROM payments py JOIN clients c ON py.client_id=c.id JOIN projects pr ON py.project_id=pr.id " +
                        "WHERE py.status=? ORDER BY py.id DESC");
                    ps.setString(1, f);
                }
                ResultSet rs = ps.executeQuery();
                while (rs.next())
                    model.addRow(new Object[]{
                        rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4),
                        "₹"+fmt(rs.getDouble(5)),rs.getString(6),rs.getString(7),rs.getString(8),"Edit | Delete"});
                con.close();
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        // Search field
        search.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String q = search.getText().toLowerCase();
                model.setRowCount(0);
                try {
                    Connection con = DBConnection.getConnection();
                    PreparedStatement ps = con.prepareStatement(
                        "SELECT py.id, py.description, c.name, pr.name, py.amount, py.`date`, py.method, py.status " +
                        "FROM payments py JOIN clients c ON py.client_id=c.id JOIN projects pr ON py.project_id=pr.id " +
                        "WHERE LOWER(py.description) LIKE ? OR LOWER(c.name) LIKE ? OR LOWER(pr.name) LIKE ? " +
                        "ORDER BY py.id DESC");
                    ps.setString(1,"%"+q+"%"); ps.setString(2,"%"+q+"%"); ps.setString(3,"%"+q+"%");
                    ResultSet rs = ps.executeQuery();
                    while (rs.next())
                        model.addRow(new Object[]{
                            rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4),
                            "₹"+fmt(rs.getDouble(5)),rs.getString(6),rs.getString(7),rs.getString(8),"Edit | Delete"});
                    con.close();
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });

        JPanel tableCard = card(null);
        tableCard.setLayout(new BorderLayout());
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(null);
        sp.getViewport().setBackground(BG_CARD);
        styleScrollBar(sp);
        tableCard.add(sp, BorderLayout.CENTER);

        content.add(topBar,    BorderLayout.NORTH);
        content.add(tableCard, BorderLayout.CENTER);
        p.add(content, BorderLayout.CENTER);
        mainContent.add(p, BorderLayout.CENTER);
        mainContent.revalidate();
        mainContent.repaint();
    }

    void refreshPaymentModel(DefaultTableModel model) {
        model.setRowCount(0);
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                "SELECT py.id, py.description, c.name, pr.name, py.amount, py.date, py.method, py.status " +
                "FROM payments py JOIN clients c ON py.client_id=c.id JOIN projects pr ON py.project_id=pr.id " +
                "ORDER BY py.id DESC");
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                model.addRow(new Object[]{
                    rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4),
                    "₹"+fmt(rs.getDouble(5)),rs.getString(6),rs.getString(7),rs.getString(8),"Edit | Delete"});
            con.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // rowData = table row; editId = DB id, -1 for new
    void showPaymentDialog(Object[] rowData, int editId) {
        JDialog dlg = styledDialog(editId == -1 ? "Record Payment" : "Edit Payment", 500, 500);
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BG_CARD);
        form.setBorder(new EmptyBorder(24,28,24,28));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL; gc.insets = new Insets(6,0,6,0);

        // Load clients & projects as "id: name" — parse id on save, no index arithmetic needed
        List<String> clientEntries  = new ArrayList<>();
        List<String> projectEntries = new ArrayList<>();
        try {
            Connection con = DBConnection.getConnection();
            ResultSet rs = con.prepareStatement("SELECT id,name FROM clients ORDER BY name").executeQuery();
            while (rs.next()) clientEntries.add(rs.getInt(1) + ": " + rs.getString(2));
            rs = con.prepareStatement("SELECT id,name FROM projects ORDER BY name").executeQuery();
            while (rs.next()) projectEntries.add(rs.getInt(1) + ": " + rs.getString(2));
            con.close();
        } catch (Exception e) { e.printStackTrace(); }

        if (clientEntries.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No clients found. Please add a client first.", "No Clients", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (projectEntries.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No projects found. Please add a project first.", "No Projects", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JComboBox<String> fClient  = styledCombo(clientEntries.toArray(new String[0]));
        JComboBox<String> fProject = styledCombo(projectEntries.toArray(new String[0]));
        JTextField fDesc  = formField("");
        JTextField fAmt   = formField("0");
        JTextField fDate  = formField("2025-01-01");
        JComboBox<String> fMethod = styledCombo(new String[]{"Bank Transfer","UPI","Wire Transfer","Cash","Cheque","PayPal","Other"});
        JComboBox<String> fStatus = styledCombo(new String[]{"Pending","Received","Overdue"});

        // Pre-fill when editing — fetch all fields fresh from DB
        if (editId != -1) {
            try {
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(
                    "SELECT client_id,project_id,description,amount,date,method,status FROM payments WHERE id=?");
                ps.setInt(1, editId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int dbCid = rs.getInt(1);
                    int dbPid = rs.getInt(2);
                    fDesc.setText(rs.getString(3));
                    fAmt.setText(String.valueOf((int)rs.getDouble(4)));
                    fDate.setText(rs.getString(5));
                    fMethod.setSelectedItem(rs.getString(6));
                    fStatus.setSelectedItem(rs.getString(7));
                    // Select matching "id: name" entry by checking the id prefix
                    for (int i = 0; i < clientEntries.size(); i++)
                        if (clientEntries.get(i).startsWith(dbCid + ":")) { fClient.setSelectedIndex(i); break; }
                    for (int i = 0; i < projectEntries.size(); i++)
                        if (projectEntries.get(i).startsWith(dbPid + ":")) { fProject.setSelectedIndex(i); break; }
                }
                con.close();
            } catch (Exception e) { e.printStackTrace(); }
        }

        addFormRow(form,gc,"Client *",       fClient,  0);
        addFormRow(form,gc,"Project *",      fProject, 1);
        addFormRow(form,gc,"Description *",  fDesc,    2);
        addFormRow(form,gc,"Amount (₹) *",   fAmt,     3);
        addFormRow(form,gc,"Date",           fDate,    4);
        addFormRow(form,gc,"Payment Method", fMethod,  5);
        addFormRow(form,gc,"Status",         fStatus,  6);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));
        btns.setBackground(BG_CARD);
        JButton cancel = ghostButton("Cancel");
        JButton save   = accentButton(editId == -1 ? "Record" : "Save Changes");
        cancel.addActionListener(e -> dlg.dispose());
        save.addActionListener(e -> {
            if (fDesc.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dlg,"Description is required.","Validation",JOptionPane.WARNING_MESSAGE);
                return;
            }
            String clientSel  = (String) fClient.getSelectedItem();
            String projectSel = (String) fProject.getSelectedItem();
            if (clientSel == null || !clientSel.contains(":") ||
                projectSel == null || !projectSel.contains(":")) {
                JOptionPane.showMessageDialog(dlg,"Please select a valid client and project.","Validation",JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Parse IDs directly from "id: name" — no index lookup, no crash
            int cid, pid;
            try {
                cid = Integer.parseInt(clientSel.split(":")[0].trim());
                pid = Integer.parseInt(projectSel.split(":")[0].trim());
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(dlg,"Invalid selection.","Error",JOptionPane.ERROR_MESSAGE);
                return;
            }
            double amt = 0;
            try { amt = Double.parseDouble(fAmt.getText().trim()); } catch (Exception ex) {}
            try {
                Connection con = DBConnection.getConnection();
                if (editId == -1) {
                    PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO payments(client_id,project_id,description,amount,date,method,status) VALUES(?,?,?,?,?,?,?)");
                    ps.setInt(1,cid); ps.setInt(2,pid); ps.setString(3,fDesc.getText().trim());
                    ps.setDouble(4,amt); ps.setString(5,fDate.getText().trim());
                    ps.setString(6,(String)fMethod.getSelectedItem());
                    ps.setString(7,(String)fStatus.getSelectedItem());
                    ps.executeUpdate();
                } else {
                    PreparedStatement ps = con.prepareStatement(
                        "UPDATE payments SET client_id=?,project_id=?,description=?,amount=?,date=?,method=?,status=? WHERE id=?");
                    ps.setInt(1,cid); ps.setInt(2,pid); ps.setString(3,fDesc.getText().trim());
                    ps.setDouble(4,amt); ps.setString(5,fDate.getText().trim());
                    ps.setString(6,(String)fMethod.getSelectedItem());
                    ps.setString(7,(String)fStatus.getSelectedItem());
                    ps.setInt(8,editId);
                    ps.executeUpdate();
                }
                con.close();
                dlg.dispose();
                showPayments();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dlg,
                    "Error saving payment:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        btns.add(cancel); btns.add(save);
        gc.gridx=0; gc.gridy=7; gc.gridwidth=2; gc.insets=new Insets(18,0,0,0);
        form.add(btns,gc);
        dlg.add(form);
        dlg.setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────
    // REPORTS  — DB-driven
    // ─────────────────────────────────────────────────────────────
    void showReports() {
        mainContent.removeAll();
        JPanel p = darkPanel();
        p.setLayout(new BorderLayout());
        p.add(pageHeader("Reports", "Financial overview and analytics"), BorderLayout.NORTH);

        JPanel content = darkPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(0,28,28,28));

        double totalRevenue=0, pendingAmt=0, totalBilled=0;
        List<Object[]> clientRevRows  = new ArrayList<>();
        List<Object[]> statusRows     = new ArrayList<>();
        List<Object[]> methodRows     = new ArrayList<>();

        try {
            Connection con = DBConnection.getConnection();

            ResultSet rs = con.createStatement().executeQuery(
                "SELECT COALESCE(SUM(amount),0) FROM payments WHERE status='Received'");
            if(rs.next()) totalRevenue=rs.getDouble(1);

            rs = con.createStatement().executeQuery(
                "SELECT COALESCE(SUM(amount),0) FROM payments WHERE status='Pending'");
            if(rs.next()) pendingAmt=rs.getDouble(1);

            rs = con.createStatement().executeQuery("SELECT COALESCE(SUM(value),0) FROM projects");
            if(rs.next()) totalBilled=rs.getDouble(1);

            // Revenue by client
            PreparedStatement ps = con.prepareStatement(
                "SELECT c.name, COUNT(DISTINCT pr.id), " +
                "COALESCE(SUM(CASE WHEN py.status='Received' THEN py.amount ELSE 0 END),0), " +
                "COALESCE(SUM(CASE WHEN py.status='Pending'  THEN py.amount ELSE 0 END),0) " +
                "FROM clients c " +
                "LEFT JOIN projects pr ON pr.client_id=c.id " +
                "LEFT JOIN payments py ON py.client_id=c.id " +
                "GROUP BY c.id, c.name ORDER BY c.name");
            rs = ps.executeQuery();
            while(rs.next())
                clientRevRows.add(new Object[]{rs.getString(1),rs.getLong(2),"₹"+fmt(rs.getDouble(3)),"₹"+fmt(rs.getDouble(4))});

            // Projects by status
            String[] statuses={"In Progress","Completed","On Hold","Not Started"};
            for(String st:statuses){
                PreparedStatement sp2 = con.prepareStatement(
                    "SELECT COUNT(*),COALESCE(SUM(value),0),COALESCE(SUM(paid),0) FROM projects WHERE status=?");
                sp2.setString(1,st);
                ResultSet rs2 = sp2.executeQuery();
                if(rs2.next())
                    statusRows.add(new Object[]{st,rs2.getLong(1),"₹"+fmt(rs2.getDouble(2)),"₹"+fmt(rs2.getDouble(3))});
            }

            // Payment method breakdown
            ps = con.prepareStatement(
                "SELECT method, COUNT(*), SUM(amount) FROM payments WHERE status='Received' GROUP BY method ORDER BY SUM(amount) DESC");
            rs = ps.executeQuery();
            final double tr = totalRevenue;
            while(rs.next()){
                double amt=rs.getDouble(3);
                methodRows.add(new Object[]{rs.getString(1),rs.getLong(2),"₹"+fmt(amt),
                    String.format("%.1f%%", tr>0 ? amt/tr*100 : 0)});
            }
            con.close();
        } catch(Exception e){ e.printStackTrace(); }

        double collectionRate = totalBilled>0 ? (totalRevenue/totalBilled)*100 : 0;

        JPanel kpis = new JPanel(new GridLayout(1,4,16,0));
        kpis.setOpaque(false);
        kpis.setMaximumSize(new Dimension(Integer.MAX_VALUE,120));
        kpis.add(kpiCard("Total Revenue",  "₹"+fmt(totalRevenue), "All time received",    SUCCESS, "▲"));
        kpis.add(kpiCard("Pending",        "₹"+fmt(pendingAmt),   "Awaiting payment",     WARNING, "⌛"));
        kpis.add(kpiCard("Total Billed",   "₹"+fmt(totalBilled),  "Across all projects",  ACCENT,  "⊕"));
        kpis.add(kpiCard("Collection Rate",String.format("%.1f%%",collectionRate),"Revenue / Billed",ACCENT2,"◉"));
        content.add(kpis);
        content.add(Box.createVerticalStrut(24));

        JPanel cols = new JPanel(new GridLayout(1,2,20,0));
        cols.setOpaque(false);
        cols.setMaximumSize(new Dimension(Integer.MAX_VALUE,320));

        JPanel clientRev = card("Revenue by Client");
        DefaultTableModel crModel = new DefaultTableModel(new String[]{"Client","Projects","Received","Pending"},0){
            public boolean isCellEditable(int r,int c){return false;}};
        for(Object[] row:clientRevRows) crModel.addRow(row);
        clientRev.add(styledTable(crModel,new int[]{150,70,100,100}),BorderLayout.CENTER);

        JPanel projStatus = card("Projects by Status");
        DefaultTableModel psModel = new DefaultTableModel(new String[]{"Status","Count","Total Value","Paid"},0){
            public boolean isCellEditable(int r,int c){return false;}};
        for(Object[] row:statusRows) psModel.addRow(row);
        projStatus.add(styledTable(psModel,new int[]{110,60,110,100}),BorderLayout.CENTER);

        cols.add(clientRev); cols.add(projStatus);
        content.add(cols);
        content.add(Box.createVerticalStrut(24));

        JPanel pmCard = card("Payment Method Breakdown");
        pmCard.setMaximumSize(new Dimension(Integer.MAX_VALUE,250));
        DefaultTableModel pmModel = new DefaultTableModel(new String[]{"Method","Transactions","Total Amount","% of Revenue"},0){
            public boolean isCellEditable(int r,int c){return false;}};
        for(Object[] row:methodRows) pmModel.addRow(row);
        pmCard.add(styledTable(pmModel,new int[]{140,110,130,110}),BorderLayout.CENTER);
        content.add(pmCard);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_DARK);
        styleScrollBar(scroll);
        p.add(scroll, BorderLayout.CENTER);
        mainContent.add(p, BorderLayout.CENTER);
        mainContent.revalidate();
        mainContent.repaint();
    }

    // ─────────────────────────────────────────────────────────────
    // TABLE RENDERER — status badge + striped rows
    // ─────────────────────────────────────────────────────────────
    void styleTableFull(JTable table) {
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_PRIMARY);
        table.setFont(FONT_BODY);
        table.setRowHeight(38);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0,0));
        table.setBorder(null);
        table.setSelectionBackground(BG_HOVER);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setFocusable(false);
        JTableHeader header = table.getTableHeader();
        header.setBackground(BG_DARK);
        header.setForeground(TEXT_MUTED);
        header.setFont(FONT_HEADER);
        header.setBorder(BorderFactory.createMatteBorder(0,0,1,0,BORDER_COLOR));
        header.setPreferredSize(new Dimension(0,40));
        ((DefaultTableCellRenderer)header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t,Object val,boolean sel,boolean focus,int row,int col){
                JLabel lbl=(JLabel)super.getTableCellRendererComponent(t,val,sel,focus,row,col);
                lbl.setFont(FONT_BODY);
                lbl.setBorder(new EmptyBorder(0,14,0,14));
                lbl.setBackground(sel?BG_HOVER:(row%2==0?BG_CARD:TABLE_ALT));
                lbl.setForeground(TEXT_PRIMARY);
                String s=val!=null?val.toString():"";
                if(s.equals("Active")||s.equals("Received")||s.equals("Completed")) lbl.setForeground(SUCCESS);
                else if(s.equals("Inactive")||s.equals("Overdue"))                  lbl.setForeground(DANGER);
                else if(s.equals("Pending")||s.equals("On Hold"))                   lbl.setForeground(WARNING);
                else if(s.equals("In Progress"))                                     lbl.setForeground(ACCENT);
                return lbl;
            }
        });
    }

    JScrollPane styledTable(DefaultTableModel model, int[] widths) {
        JTable t = new JTable(model);
        styleTableFull(t);
        setColumnWidths(t, widths);
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(null);
        sp.getViewport().setBackground(BG_CARD);
        styleScrollBar(sp);
        return sp;
    }

    void setColumnWidths(JTable t, int[] widths) {
        for(int i=0;i<widths.length&&i<t.getColumnCount();i++)
            t.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
    }

    // ─────────────────────────────────────────────────────────────
    // Action Button Renderer / Editor  — fully DB-driven
    // ─────────────────────────────────────────────────────────────
    class ActionButtonRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable t,Object val,boolean sel,boolean focus,int row,int col){
            JPanel p=new JPanel(new FlowLayout(FlowLayout.CENTER,6,4));
            p.setBackground(sel?BG_HOVER:(row%2==0?BG_CARD:TABLE_ALT));
            p.add(miniBtn("Edit",ACCENT)); p.add(miniBtn("Delete",DANGER));
            return p;
        }
    }

    class ActionButtonEditor extends DefaultCellEditor {
        JPanel panel;
        JButton editBtn, delBtn;
        String entityType;
        DefaultTableModel model;
        JTable table;
        int editingRow;

        ActionButtonEditor(JCheckBox cb, String entityType, DefaultTableModel model, JTable table) {
            super(cb);
            this.entityType=entityType; this.model=model; this.table=table;
            panel=new JPanel(new FlowLayout(FlowLayout.CENTER,6,4));
            panel.setBackground(BG_CARD);
            editBtn=miniBtn("Edit",ACCENT);
            delBtn =miniBtn("Delete",DANGER);
            panel.add(editBtn); panel.add(delBtn);

            editBtn.addActionListener(e -> {
                fireEditingStopped();
                int id = (int) model.getValueAt(editingRow, 0);
                // Collect entire row as Object[]
                int colCount = model.getColumnCount();
                Object[] rowData = new Object[colCount];
                for (int i=0;i<colCount;i++) rowData[i] = model.getValueAt(editingRow,i);

                switch (entityType) {
                    case "client":  showClientDialog(rowData, id);  break;
                    case "project": showProjectDialog(rowData, id); break;
                    case "payment": showPaymentDialog(rowData, id); break;
                }
            });

            delBtn.addActionListener(e -> {
                fireEditingStopped();
                int id = (int) model.getValueAt(editingRow, 0);
                int res = JOptionPane.showConfirmDialog(table,"Delete this record?","Confirm Delete",JOptionPane.YES_NO_OPTION);
                if (res == JOptionPane.YES_OPTION) {
                    try {
                        Connection con = DBConnection.getConnection();
                        String sql;
                        switch (entityType) {
                            case "client":  sql="DELETE FROM clients WHERE id=?";  break;
                            case "project": sql="DELETE FROM projects WHERE id=?"; break;
                            default:        sql="DELETE FROM payments WHERE id=?"; break;
                        }
                        PreparedStatement ps = con.prepareStatement(sql);
                        ps.setInt(1, id);
                        ps.executeUpdate();
                        con.close();
                    } catch (Exception ex) { ex.printStackTrace(); }
                    switch (entityType) {
                        case "client":  showClients();  break;
                        case "project": showProjects(); break;
                        default:        showPayments(); break;
                    }
                }
            });
        }

        public Component getTableCellEditorComponent(JTable t,Object val,boolean sel,int row,int col){
            editingRow=row; panel.setBackground(BG_HOVER); return panel;
        }
        public Object getCellEditorValue() { return ""; }
    }

    JButton miniBtn(String text, Color color) {
        JButton b = new JButton(text);
        b.setFont(FONT_SMALL);
        b.setForeground(color);
        b.setBackground(new Color(color.getRed(),color.getGreen(),color.getBlue(),20));
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(color.getRed(),color.getGreen(),color.getBlue(),80),1,true),
            new EmptyBorder(2,8,2,8)));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter(){
            Color origBg=b.getBackground();
            public void mouseEntered(MouseEvent e){b.setBackground(new Color(color.getRed(),color.getGreen(),color.getBlue(),40));}
            public void mouseExited (MouseEvent e){b.setBackground(origBg);}
        });
        return b;
    }

    // ─────────────────────────────────────────────────────────────
    // UI HELPERS
    // ─────────────────────────────────────────────────────────────
    JPanel darkPanel() {
        JPanel p=new JPanel(); p.setBackground(BG_DARK); return p;
    }

    JPanel card(String title) {
        JPanel c = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,14,14);
                super.paintComponent(g);
            }
        };
        c.setOpaque(false);
        c.setBorder(new EmptyBorder(16,16,16,16));
        if(title!=null){
            JLabel lbl=new JLabel(title);
            lbl.setFont(FONT_HEADER);
            lbl.setForeground(TEXT_PRIMARY);
            lbl.setBorder(new EmptyBorder(0,0,12,0));
            c.add(lbl,BorderLayout.NORTH);
        }
        return c;
    }

    JPanel pageHeader(String title, String sub){
        JPanel h=new JPanel(new BorderLayout());
        h.setOpaque(false);
        h.setBorder(new EmptyBorder(28,28,20,28));
        JLabel t=new JLabel(title);
        t.setFont(new Font("SansSerif",Font.BOLD,28));
        t.setForeground(TEXT_PRIMARY);
        JLabel s=new JLabel(sub);
        s.setFont(FONT_BODY);
        s.setForeground(TEXT_MUTED);
        JPanel text=new JPanel(new GridLayout(2,1,0,4));
        text.setOpaque(false);
        text.add(t); text.add(s);
        h.add(text,BorderLayout.WEST);
        return h;
    }

    JPanel toolbar(){
        JPanel tb=new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));
        tb.setOpaque(false);
        tb.setBorder(new EmptyBorder(0,28,14,28));
        return tb;
    }

    JLabel label(String text,Font font,Color color){
        JLabel l=new JLabel(text); l.setFont(font); l.setForeground(color); return l;
    }

    JLabel statusBadge(String status){
        JLabel l=new JLabel(status);
        l.setFont(FONT_SMALL);
        Color c=status.equals("Completed")||status.equals("Active")||status.equals("Received") ? SUCCESS
               :status.equals("In Progress") ? ACCENT
               :status.equals("On Hold")||status.equals("Pending") ? WARNING : DANGER;
        l.setForeground(c);
        l.setHorizontalAlignment(SwingConstants.CENTER);
        return l;
    }

    JTextField searchField(String placeholder){
        JTextField f=new JTextField(18){
            protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                super.paintComponent(g);
                if(getText().isEmpty()&&!isFocusOwner()){
                    g2.setColor(TEXT_MUTED);
                    g2.setFont(FONT_BODY);
                    g2.drawString(placeholder,10,(getHeight()+g2.getFontMetrics().getAscent())/2-2);
                }
            }
        };
        f.setFont(FONT_BODY); f.setForeground(TEXT_PRIMARY); f.setCaretColor(ACCENT);
        f.setBackground(BG_CARD); f.setOpaque(false);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR,1,true),new EmptyBorder(6,10,6,10)));
        f.setPreferredSize(new Dimension(240,36));
        return f;
    }

    JTextField formField(String value){
        JTextField f=new JTextField(value);
        f.setFont(FONT_BODY); f.setForeground(TEXT_PRIMARY); f.setCaretColor(ACCENT);
        f.setBackground(BG_DARK);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR,1,true),new EmptyBorder(8,10,8,10)));
        f.setPreferredSize(new Dimension(240,38));
        f.addFocusListener(new FocusAdapter(){
            public void focusGained(FocusEvent e){f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(ACCENT,1,true),new EmptyBorder(8,10,8,10)));}
            public void focusLost (FocusEvent e){f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_COLOR,1,true),new EmptyBorder(8,10,8,10)));}
        });
        return f;
    }

    JComboBox<String> styledCombo(String[] items){
        JComboBox<String> cb=new JComboBox<>(items);
        cb.setFont(FONT_BODY); cb.setForeground(TEXT_PRIMARY); cb.setBackground(BG_DARK);
        cb.setBorder(BorderFactory.createLineBorder(BORDER_COLOR,1,true));
        cb.setPreferredSize(new Dimension(240,38));
        cb.setRenderer(new DefaultListCellRenderer(){
            public Component getListCellRendererComponent(JList<?> l,Object val,int idx,boolean sel,boolean focus){
                JLabel lbl=(JLabel)super.getListCellRendererComponent(l,val,idx,sel,focus);
                lbl.setBackground(sel?BG_HOVER:BG_DARK);
                lbl.setForeground(TEXT_PRIMARY);
                lbl.setBorder(new EmptyBorder(6,10,6,10));
                return lbl;
            }
        });
        return cb;
    }

    void addFormRow(JPanel form,GridBagConstraints gc,String labelText,JComponent field,int row){
        gc.gridx=0; gc.gridy=row; gc.gridwidth=1; gc.weightx=0.3; gc.insets=new Insets(6,0,6,12);
        JLabel lbl=new JLabel(labelText); lbl.setFont(FONT_BODY); lbl.setForeground(TEXT_MUTED);
        form.add(lbl,gc);
        gc.gridx=1; gc.weightx=0.7; gc.insets=new Insets(6,0,6,0);
        form.add(field,gc);
    }

    JButton accentButton(String text){
        JButton b=new JButton(text){
            boolean hovered=false;
            { addMouseListener(new MouseAdapter(){
                public void mouseEntered(MouseEvent e){hovered=true; repaint();}
                public void mouseExited (MouseEvent e){hovered=false;repaint();}
            }); }
            protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp=new GradientPaint(0,0,hovered?ACCENT.brighter():ACCENT,getWidth(),0,hovered?ACCENT2.brighter():ACCENT2);
                g2.setPaint(gp); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                super.paintComponent(g);
            }
        };
        b.setFont(FONT_HEADER); b.setForeground(BG_DARK);
        b.setBorder(new EmptyBorder(8,18,8,18));
        b.setFocusPainted(false); b.setContentAreaFilled(false); b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    JButton ghostButton(String text){
        JButton b=new JButton(text);
        b.setFont(FONT_BODY); b.setForeground(TEXT_MUTED); b.setBackground(BG_DARK);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR,1,true),new EmptyBorder(7,16,7,16)));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter(){
            public void mouseEntered(MouseEvent e){b.setForeground(TEXT_PRIMARY);b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(TEXT_MUTED,1,true),new EmptyBorder(7,16,7,16)));}
            public void mouseExited (MouseEvent e){b.setForeground(TEXT_MUTED);   b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_COLOR,1,true),new EmptyBorder(7,16,7,16)));}
        });
        return b;
    }

    JDialog styledDialog(String title,int w,int h){
        JDialog dlg=new JDialog(this,title,true);
        dlg.setSize(w,h); dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(BG_CARD);
        dlg.setBackground(BG_CARD);
        dlg.setLayout(new BorderLayout());
        JLabel header=new JLabel("  "+title);
        header.setFont(new Font("SansSerif",Font.BOLD,16));
        header.setForeground(TEXT_PRIMARY);
        header.setBackground(BG_DARK); header.setOpaque(true);
        header.setPreferredSize(new Dimension(0,52));
        header.setBorder(BorderFactory.createMatteBorder(0,0,1,0,BORDER_COLOR));
        dlg.add(header,BorderLayout.NORTH);
        return dlg;
    }

    void styleScrollBar(JScrollPane sp){
        sp.getVerticalScrollBar().setBackground(BG_DARK);
        sp.getVerticalScrollBar().setUI(new BasicScrollBarUI(){
            protected void configureScrollBarColors(){thumbColor=BORDER_COLOR;trackColor=BG_DARK;}
            protected JButton createDecreaseButton(int o){return zeroBtn();}
            protected JButton createIncreaseButton(int o){return zeroBtn();}
            JButton zeroBtn(){JButton b=new JButton();b.setPreferredSize(new Dimension(0,0));return b;}
        });
        sp.getHorizontalScrollBar().setBackground(BG_DARK);
        sp.getHorizontalScrollBar().setUI(new BasicScrollBarUI(){
            protected void configureScrollBarColors(){thumbColor=BORDER_COLOR;trackColor=BG_DARK;}
            protected JButton createDecreaseButton(int o){return zeroBtn();}
            protected JButton createIncreaseButton(int o){return zeroBtn();}
            JButton zeroBtn(){JButton b=new JButton();b.setPreferredSize(new Dimension(0,0));return b;}
        });
    }

    static String fmt(double v){
        if(v>=100000) return String.format("%.1fL",v/100000);
        if(v>=1000)   return String.format("%.1fK",v/1000);
        return String.valueOf((int)v);
    }

    // ─────────────────────────────────────────────────────────────
    // DATA MODELS  (kept for compatibility; not used for DB ops)
    // ─────────────────────────────────────────────────────────────
    static class Client {
        int id; String name,email,phone,company,status;
        Client(int id,String name,String email,String phone,String company,String status){
            this.id=id;this.name=name;this.email=email;this.phone=phone;this.company=company;this.status=status;}
    }
    static class Project {
        int id,clientId; String name,category,status,startDate,endDate; double value,paid;
        Project(int id,int clientId,String name,String category,String status,String startDate,String endDate,double value,double paid){
            this.id=id;this.clientId=clientId;this.name=name;this.category=category;this.status=status;
            this.startDate=startDate;this.endDate=endDate;this.value=value;this.paid=paid;}
    }
    static class Payment {
        int id,clientId,projectId; String description,date,method,status; double amount;
        Payment(int id,int clientId,int projectId,String description,double amount,String date,String method,String status){
            this.id=id;this.clientId=clientId;this.projectId=projectId;this.description=description;
            this.amount=amount;this.date=date;this.method=method;this.status=status;}
    }
}
