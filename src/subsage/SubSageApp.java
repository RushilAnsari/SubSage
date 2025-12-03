package subsage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubSageApp {
    private SubSageManager manager;
    private JFrame mainFrame;
    private JTable table;
    private DefaultTableModel tableModel;
    private JPanel leftPanel;
    private JLabel totalLabel;
    private JProgressBar budgetBar;
    private JLabel budgetLabel;
    private List<Subscription> currentList; 

    private static final double USD_TO_AED_RATE = 3.6725;// this is the current currency conversion rate from USD TO AED
    private static final Color PURPLE_COLOR = new Color(108, 99, 255); //hex code for the purple theme: #6C63FF
    private static final Color TEXT_FIELD_BG = new Color(245, 246, 250);// hex code for the light gray background: #F5F6FA
    private static final Color TEXT_COLOR = new Color(51, 51, 51);//hex code for dark text: #333333

    //Runs first when the app starts
    public SubSageApp() {
        manager = new SubSageManager();// SubSageManager is called!
        setUIFont(new javax.swing.plaf.FontUIResource("SansSerif", Font.PLAIN, 14));
        initLoginScreen();
    }

    // This helper function sets a global font for the entire application
    private static void setUIFont(javax.swing.plaf.FontUIResource f) {
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, f);
            }
        }
    }

    // Login Screen window
    private void initLoginScreen() {
        JFrame loginFrame = new JFrame("SubSage - Login");
        loginFrame.setSize(450, 750);
        loginFrame.setLayout(new BorderLayout());
        loginFrame.setLocationRelativeTo(null);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.getContentPane().setBackground(Color.WHITE);

        try {
            ImageIcon logoIcon = new ImageIcon("lib/logo.jpg");
            loginFrame.setIconImage(logoIcon.getImage());
        } catch (Exception e) { }

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        //This adds the big logo image to the center of the screen
        try {
            ImageIcon logoIcon = new ImageIcon("lib/logo.jpg"); 
            Image image = logoIcon.getImage();
            Image newimg = image.getScaledInstance(140, 140,  java.awt.Image.SCALE_SMOOTH); 
            logoIcon = new ImageIcon(newimg);
            JLabel logoLabel = new JLabel(logoIcon);
            logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            mainPanel.add(logoLabel);
        } catch (Exception e) {
            JLabel logoLabel = new JLabel("SubSage");
            logoLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
            logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            mainPanel.add(logoLabel);
        }

        mainPanel.add(Box.createVerticalStrut(20));

        JLabel welcomeLabel = new JLabel("Welcome Back!");
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        welcomeLabel.setForeground(TEXT_COLOR);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(welcomeLabel);

        mainPanel.add(Box.createVerticalStrut(5));

        JLabel subtitleLabel = new JLabel("Login to continue");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        subtitleLabel.setForeground(Color.GRAY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(subtitleLabel);

        mainPanel.add(Box.createVerticalStrut(30));

        JTextField userField = createStyledTextField();
        mainPanel.add(createLabeledPanel("Username", userField));

        mainPanel.add(Box.createVerticalStrut(20));

        JPasswordField passField = createStyledPasswordField();
        mainPanel.add(createLabeledPanel("Password", passField));

        mainPanel.add(Box.createVerticalStrut(10));

        JLabel forgotPassLabel = new JLabel("Forgot Password?");
        forgotPassLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        forgotPassLabel.setForeground(Color.GRAY);
        JPanel forgotPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        forgotPanel.setBackground(Color.WHITE);
        forgotPanel.add(forgotPassLabel);
        mainPanel.add(forgotPanel);

        mainPanel.add(Box.createVerticalStrut(20));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);

        JButton btnLogin = new JButton("LOGIN");
        styleButton(btnLogin);
        btnLogin.setPreferredSize(new Dimension(300, 50));
        
        // This is the logic for the Login Button click
        btnLogin.addActionListener(e -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword()).trim();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(loginFrame, "Please enter username and password.");
                return;
            }

            //SubSageManager is called again over here!😲
            if (manager.userExists(username)) {
                if (manager.login(username, password)) {
                    loginFrame.dispose();
                    initMainDashboard();
                } else {
                    JOptionPane.showMessageDialog(loginFrame, "Incorrect Password!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(loginFrame, "User not found. Please Sign Up.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(btnLogin);
        mainPanel.add(buttonPanel);

        mainPanel.add(Box.createVerticalStrut(20));

        JPanel signUpPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        signUpPanel.setBackground(Color.WHITE);
        JLabel dontHaveAccountLabel = new JLabel("Don't have an account? ");
        
        JLabel signUpLabel = new JLabel("Sign Up");
        signUpLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        signUpLabel.setForeground(PURPLE_COLOR);
        signUpLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // This logic handles clicking the 'Sign Up' text
        signUpLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String username = userField.getText().trim();
                String password = new String(passField.getPassword()).trim();
                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(loginFrame, "Enter a username and password to sign up.");
                    return;
                }
                if (manager.userExists(username)) {
                    JOptionPane.showMessageDialog(loginFrame, "User already exists. Please login.");
                } else {
                    manager.register(username, password);
                    JOptionPane.showMessageDialog(loginFrame, "Account Created! Logging in...");
                    loginFrame.dispose();
                    initMainDashboard();
                }
            }
        });

        signUpPanel.add(dontHaveAccountLabel);
        signUpPanel.add(signUpLabel);
        mainPanel.add(signUpPanel);

        loginFrame.add(mainPanel, BorderLayout.CENTER);
        loginFrame.setVisible(true);
    }

    // This helper creates a text field with custom styling (rounded border, background color)
    private JTextField createStyledTextField() {
        JTextField field = new JTextField(20); 
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBackground(TEXT_FIELD_BG);
        field.setForeground(TEXT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true), 
                new EmptyBorder(5, 10, 5, 10) 
        ));
        field.setPreferredSize(new Dimension(300, 40)); 
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        return field;
    }

    // This helper creates a password field with custom styling
    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField(20);
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBackground(TEXT_FIELD_BG);
        field.setForeground(TEXT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(5, 10, 5, 10)
        ));
        field.setPreferredSize(new Dimension(300, 40)); 
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        return field;
    }

    // This helper creates a panel that groups a Label and an Input Field together
    private JPanel createLabeledPanel(String labelText, JComponent field) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(TEXT_COLOR);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(5));
        panel.add(field);
        return panel;
    }

    // This helper applies the Purple Theme styling to any button passed to it
    private void styleButton(JButton button) {
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(PURPLE_COLOR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // This function builds the Main Dashboard (Table, Side Panel, Budget Bar)
    private void initMainDashboard() {
        mainFrame = new JFrame("SubSage - " + manager.getCurrentUser());
        mainFrame.setSize(1200, 700);
        mainFrame.setLayout(new BorderLayout());
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        try {
            ImageIcon logoIcon = new ImageIcon("lib/logo.jpg");
            mainFrame.setIconImage(logoIcon.getImage());
        } catch (Exception e) { }

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(50, 50, 50));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel(" SubSage – Smart Bill Manager");
        title.setForeground(PURPLE_COLOR); 
        title.setFont(new Font("Arial", Font.BOLD, 20));
        
        JTextField searchField = new JTextField(15);
        searchField.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(PURPLE_COLOR), "Search...", 0, 0, null, PURPLE_COLOR));
        
        topPanel.add(title, BorderLayout.WEST);
        topPanel.add(searchField, BorderLayout.EAST);
        mainFrame.add(topPanel, BorderLayout.NORTH);

        leftPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane leftScroll = new JScrollPane(leftPanel);
        leftScroll.setPreferredSize(new Dimension(220, 0));
        leftScroll.setBorder(null);
        mainFrame.add(leftScroll, BorderLayout.WEST);

        String[] columns = {"Service", "Category", "Price (AED)", "Cycle", "Due Date", "Auto-Renew", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        
        // This custom renderer handles the Visual Alerts (Red/Yellow rows)
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                int modelRow = table.convertRowIndexToModel(row);
                String dateStr = (String) tableModel.getValueAt(modelRow, 4); 
                String status = (String) tableModel.getValueAt(modelRow, 6);  

                if (!isSelected) {
                    try {
                        LocalDate due = LocalDate.parse(dateStr);
                        LocalDate today = LocalDate.now();
                        long daysBetween = ChronoUnit.DAYS.between(today, due);

                        if ("Expired".equalsIgnoreCase(status)) c.setBackground(new Color(255, 200, 200)); 
                        else if (daysBetween >= 0 && daysBetween <= 7) c.setBackground(new Color(255, 255, 200)); 
                        else c.setBackground(Color.WHITE);
                    } catch (Exception e) { c.setBackground(Color.WHITE); }
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(table.getSelectionBackground());
                    c.setForeground(table.getSelectionForeground());
                }
                return c;
            }
        });

        // This enables the Search Bar filtering logic
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                String text = searchField.getText();
                if (text.trim().length() == 0) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });

        // This listener handles Double-Clicking a row to Edit it
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int viewRow = table.getSelectedRow();
                    if (viewRow != -1) {
                        int modelRow = table.convertRowIndexToModel(viewRow);
                        String serviceName = (String) tableModel.getValueAt(modelRow, 0);
                        Subscription found = null;
                        for(Subscription s : currentList) {
                            if(s.getServiceName().equals(serviceName)) found = s;
                        }
                        if(found != null) openDialog(found, serviceName);
                    }
                }
            }
        });
        mainFrame.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JPanel progressPanel = new JPanel(new BorderLayout());
        budgetLabel = new JLabel("Budget Usage: 0%");
        budgetLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        
        budgetBar = new JProgressBar(0, 100);
        budgetBar.setStringPainted(true);
        budgetBar.setPreferredSize(new Dimension(100, 20));
        
        JButton btnSetBudget = new JButton("⚙");
        btnSetBudget.setToolTipText("Set Monthly Budget Limit");
        btnSetBudget.addActionListener(e -> setBudgetDialog());
        btnSetBudget.setPreferredSize(new Dimension(30, 20));

        progressPanel.add(budgetLabel, BorderLayout.WEST);
        progressPanel.add(budgetBar, BorderLayout.CENTER);
        progressPanel.add(btnSetBudget, BorderLayout.EAST);
        
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        totalLabel = new JLabel("Total: 0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalLabel.setForeground(PURPLE_COLOR); 

        JButton btnStats = new JButton("View Breakdown 📊");
        btnStats.addActionListener(e -> showBreakdown());
        styleButton(btnStats);

        JButton btnExport = new JButton("Export CSV");
        btnExport.addActionListener(e -> exportToCSV());
        styleButton(btnExport);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> refreshUI());
        styleButton(btnRefresh);

        controlPanel.add(totalLabel);
        controlPanel.add(btnStats);
        controlPanel.add(btnExport);
        controlPanel.add(btnRefresh);

        bottomPanel.add(progressPanel);
        bottomPanel.add(Box.createVerticalStrut(5));
        bottomPanel.add(controlPanel);

        mainFrame.add(bottomPanel, BorderLayout.SOUTH);

        refreshUI();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }

    // This function opens the popup to set the Monthly Budget limit
    private void setBudgetDialog() {
        String input = JOptionPane.showInputDialog(mainFrame, "Enter your monthly budget limit (AED):", manager.getUserBudget());
        if (input != null) {
            try {
                double limit = Double.parseDouble(input);
                manager.setUserBudget(limit);
                refreshUI();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(mainFrame, "Invalid number");
            }
        }
    }

    // This is the core logic function: It fetches data, updates the table, buttons, and budget bar!!
    private void refreshUI() {
        currentList = manager.getUserSubscriptions(); // calling the SubSageManager to get the subscriptions
        tableModel.setRowCount(0);
        leftPanel.removeAll();
        
        double totalMonthly = 0;

        for (Subscription s : currentList) {
            tableModel.addRow(new Object[]{
                s.getServiceName(), s.getCategory(), String.format("%.2f", s.getPrice()),
                s.getCycle(), s.getNextDueDate(), s.isAutoRenew() ? "Yes" : "No", s.getStatus()
            });

            JButton subBtn = new JButton(s.getServiceName());
            styleButton(subBtn); 
            
            if (s.getStatus().equalsIgnoreCase("Active")) {
                subBtn.setForeground(Color.WHITE); 
                subBtn.setBackground(PURPLE_COLOR); 
                
                double cost = s.getPrice();
                if(s.getCycle().equalsIgnoreCase("Yearly")) cost = cost / 12;
                totalMonthly += cost;
            } else {
                subBtn.setForeground(Color.WHITE); 
                subBtn.setBackground(Color.RED); 
            }
            subBtn.addActionListener(e -> openDialog(s, s.getServiceName()));
            leftPanel.add(subBtn);
        }

        JButton btnAdd = new JButton("Add New (+)");
        styleButton(btnAdd); 
        btnAdd.addActionListener(e -> openDialog(null, ""));
        leftPanel.add(btnAdd);

        totalLabel.setText(String.format("Total Monthly: %.2f AED", totalMonthly));
        
        double budgetLimit = manager.getUserBudget();
        if (budgetLimit > 0) {
            int percent = (int) ((totalMonthly / budgetLimit) * 100);
            budgetBar.setValue(percent);
            budgetLabel.setText(String.format("Budget: %.0f / %.0f AED", totalMonthly, budgetLimit));
            
            if (percent < 80) budgetBar.setForeground(new Color(0, 180, 0)); 
            else if (percent < 100) budgetBar.setForeground(Color.ORANGE);
            else budgetBar.setForeground(Color.RED);
        } else {
            budgetBar.setValue(0);
            budgetLabel.setText("No Budget Set");
            budgetBar.setForeground(Color.GRAY);
        }

        leftPanel.revalidate();
        leftPanel.repaint();
    }

    // This function calculates spending per category and shows the Breakdown Popup
    private void showBreakdown() {
        Map<String, Double> breakdown = new HashMap<>();
        double total = 0;
        for (Subscription s : currentList) {
            if (s.getStatus().equalsIgnoreCase("Active")) {
                double cost = s.getPrice();
                if (s.getCycle().equalsIgnoreCase("Yearly")) cost /= 12;
                String cat = s.getCategory() == null ? "Uncategorized" : s.getCategory();
                breakdown.put(cat, breakdown.getOrDefault(cat, 0.0) + cost);
                total += cost;
            }
        }
        StringBuilder msg = new StringBuilder("Monthly Spending Breakdown:\n\n");
        for (String cat : breakdown.keySet()) {
            msg.append(String.format("- %s: %.2f AED\n", cat, breakdown.get(cat)));
        }
        msg.append(String.format("\nTotal: %.2f AED", total));
        JOptionPane.showMessageDialog(mainFrame, msg.toString(), "Financial Breakdown", JOptionPane.INFORMATION_MESSAGE);
    }

    // This function handles exporting the data table to a CSV (Excel) file
    private void exportToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save as CSV");
        int userSelection = fileChooser.showSaveDialog(mainFrame);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".csv")) filePath += ".csv";
            try (FileWriter fw = new FileWriter(filePath)) {
                fw.write("Service,Category,Price,Cycle,Due Date,Status\n");
                for (Subscription s : currentList) {
                    fw.write(s.getServiceName() + "," + s.getCategory() + "," + s.getPrice() + "," + s.getCycle() + "," 
                            + s.getNextDueDate() + "," + s.getStatus() + "\n");
                }
                JOptionPane.showMessageDialog(mainFrame, "Export Successful!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainFrame, "Error: " + ex.getMessage());
            }
        }
    }

    // This function creates the Add/Edit Dialog window logic and UI
    private void openDialog(Subscription existingSub, String defaultName) {
        boolean isEditMode = (existingSub != null);
        JDialog dialog = new JDialog(mainFrame, isEditMode ? "Edit" : "Add", true);
        dialog.setSize(420, 480);
        dialog.setLayout(new GridLayout(9, 2, 10, 10));
        dialog.setLocationRelativeTo(mainFrame);

        LocalDate today = LocalDate.now();

        JTextField nameField = new JTextField(isEditMode ? existingSub.getServiceName() : defaultName);
        if (isEditMode) nameField.setEditable(false);
        
        String[] cats = {"Entertainment", "Utilities", "Work/Software", "Groceries", "Personal", "Other"};
        JComboBox<String> categoryBox = new JComboBox<>(cats);
        if (isEditMode && existingSub.getCategory() != null) {
            categoryBox.setSelectedItem(existingSub.getCategory());
        }

        JPanel pricePanel = new JPanel(new BorderLayout());
        JTextField priceField = new JTextField(isEditMode ? String.valueOf(existingSub.getPrice()) : "");
        String[] currencies = {"AED", "USD"};
        JComboBox<String> currencyBox = new JComboBox<>(currencies);
        pricePanel.add(priceField, BorderLayout.CENTER);
        pricePanel.add(currencyBox, BorderLayout.EAST);

        JComboBox<String> cycleBox = new JComboBox<>(new String[]{"Monthly", "Yearly"});
        if (isEditMode) cycleBox.setSelectedItem(existingSub.getCycle());

        JTextField dateField = new JTextField(isEditMode ? existingSub.getNextDueDate() : today.toString());
        JCheckBox renewBox = new JCheckBox("Auto-renew");
        if (isEditMode) renewBox.setSelected(existingSub.isAutoRenew());
        JComboBox<String> statusBox = new JComboBox<>(new String[]{"Active", "Expired"});
        if (isEditMode) statusBox.setSelectedItem(existingSub.getStatus());

        cycleBox.addActionListener(e -> {
            String selected = (String) cycleBox.getSelectedItem();
            if ("Monthly".equalsIgnoreCase(selected)) dateField.setText(today.plusMonths(1).toString());
            else dateField.setText(today.plusYears(1).toString());
        });

        dialog.add(new JLabel("  Service Name:")); dialog.add(nameField);
        dialog.add(new JLabel("  Category:")); dialog.add(categoryBox);
        dialog.add(new JLabel("  Price:")); dialog.add(pricePanel); 
        dialog.add(new JLabel("  Cycle:")); dialog.add(cycleBox);
        dialog.add(new JLabel("  Next Due (YYYY-MM-DD):")); dialog.add(dateField);
        dialog.add(new JLabel("  Auto-Renew:")); dialog.add(renewBox);
        dialog.add(new JLabel("  Status:")); dialog.add(statusBox);

        JButton btnSave = new JButton(isEditMode ? "Update" : "Save");
        styleButton(btnSave); 
        
        // This is the logic for the Save/Update Button inside the dialog
        btnSave.addActionListener(e -> {
            try {
                String dateText = dateField.getText().trim();
                LocalDate inputDate = LocalDate.parse(dateText);
                String selectedCycle = (String) cycleBox.getSelectedItem();

                if (inputDate.isBefore(today)) {
                    JOptionPane.showMessageDialog(dialog, "Error: The date cannot be in the past.");
                    return;
                }

                LocalDate limitDate;
                if ("Monthly".equalsIgnoreCase(selectedCycle)) {
                    limitDate = today.plusMonths(1);
                    if (inputDate.isAfter(limitDate)) {
                        JOptionPane.showMessageDialog(dialog, "Error: Monthly cycle cannot be due more than 1 month from today.");
                        return; 
                    }
                } else {
                    limitDate = today.plusYears(1);
                    if (inputDate.isAfter(limitDate)) {
                        JOptionPane.showMessageDialog(dialog, "Error: Yearly cycle cannot be due more than 1 year from today.");
                        return; 
                    }
                }

                double inputPrice = Double.parseDouble(priceField.getText());
                
                String selectedCurrency = (String) currencyBox.getSelectedItem();
                double finalPriceAED = inputPrice;
                if ("USD".equals(selectedCurrency)) {
                    finalPriceAED = inputPrice * USD_TO_AED_RATE;
                }

                // Logic to check if adding this bill will exceed the Budget
                double budgetLimit = manager.getUserBudget();
                if (budgetLimit > 0) {
                    double currentTotal = 0;
                    for (Subscription s : manager.getUserSubscriptions()) {
                        if (isEditMode && s.getId() == existingSub.getId()) continue; 
                        if ("Active".equalsIgnoreCase(s.getStatus())) {
                            double cost = s.getPrice();
                            if ("Yearly".equalsIgnoreCase(s.getCycle())) cost /= 12;
                            currentTotal += cost;
                        }
                    }
                    
                    String newStatus = (String) statusBox.getSelectedItem();
                    if ("Active".equalsIgnoreCase(newStatus)) {
                        double newItemMonthlyCost = finalPriceAED;
                        if ("Yearly".equalsIgnoreCase(selectedCycle)) newItemMonthlyCost /= 12;
                        
                        if (currentTotal + newItemMonthlyCost > budgetLimit) {
                            JOptionPane.showMessageDialog(dialog, 
                                String.format("Budget Exceeded! Total would be %.2f AED, but limit is %.2f AED.", 
                                (currentTotal + newItemMonthlyCost), budgetLimit), 
                                "Budget Warning", JOptionPane.WARNING_MESSAGE);
                            return; // Stop saving if over budget
                        }
                    }
                }

                String name = nameField.getText().trim();
                String category = (String) categoryBox.getSelectedItem();

                if(name.isEmpty()) return;

                if (isEditMode) {
                    manager.updateSubscription(existingSub.getId(), name, category, finalPriceAED, 
                        selectedCycle, dateText, 
                        renewBox.isSelected(), (String)statusBox.getSelectedItem());
                } else {
                    manager.addSubscription(name, category, finalPriceAED, 
                        selectedCycle, dateText, 
                        renewBox.isSelected(), (String)statusBox.getSelectedItem());
                }
                refreshUI();
                dialog.dispose();
            } catch (DateTimeParseException dtpe) {
                JOptionPane.showMessageDialog(dialog, "Error: Invalid Date. Please use YYYY-MM-DD format.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: Check price format.");
            }
        });

        JButton btnDelete = new JButton("Delete");
        btnDelete.setForeground(Color.RED);
        btnDelete.setEnabled(isEditMode);
        
        // This is the logic for the Delete Button inside the dialog
        btnDelete.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(dialog, "Delete this?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                manager.deleteSubscription(existingSub.getId());
                refreshUI();
                dialog.dispose();
            }
        });

        dialog.add(btnDelete);
        dialog.add(btnSave);
        dialog.setVisible(true);
    }
}
