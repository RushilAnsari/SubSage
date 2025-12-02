package subsage;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class SubSageApp {
    private SubSageManager manager;
    private JFrame mainFrame;
    private JTable table;
    private DefaultTableModel tableModel;
    private JPanel leftPanel;
    private JLabel totalLabel;
    private List<Subscription> currentList; 

    public SubSageApp() {
        manager = new SubSageManager();
        initLoginScreen();
    }

    private void initLoginScreen() {
        JFrame loginFrame = new JFrame("SubSage Login");
        loginFrame.setSize(300, 150);
        loginFrame.setLayout(new FlowLayout());
        loginFrame.setLocationRelativeTo(null);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel lbl = new JLabel("Who are you?");
        JTextField userField = new JTextField(15);
        JButton btnLogin = new JButton("Enter");

        btnLogin.addActionListener(e -> {
            String username = userField.getText().trim();
            if (!username.isEmpty()) {
                manager.setCurrentUser(username);
                loginFrame.dispose();
                initMainDashboard();
            }
        });

        loginFrame.add(lbl);
        loginFrame.add(userField);
        loginFrame.add(btnLogin);
        loginFrame.setVisible(true);
    }

    private void initMainDashboard() {
        mainFrame = new JFrame("SubSage - " + manager.getCurrentUser());
        mainFrame.setSize(1000, 650);
        mainFrame.setLayout(new BorderLayout());
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // --- TOP BAR ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(50, 50, 50));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel(" SubSage – Smart Bill Manager");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        
        JTextField searchField = new JTextField(15);
        searchField.setBorder(BorderFactory.createTitledBorder("Search..."));
        
        topPanel.add(title, BorderLayout.WEST);
        topPanel.add(searchField, BorderLayout.EAST);
        mainFrame.add(topPanel, BorderLayout.NORTH);

        // --- LEFT PANEL ---
        leftPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane leftScroll = new JScrollPane(leftPanel);
        leftScroll.setPreferredSize(new Dimension(220, 0));
        leftScroll.setBorder(null);
        mainFrame.add(leftScroll, BorderLayout.WEST);

        // --- RIGHT PANEL (Table) ---
        String[] columns = {"Service", "Price (AED)", "Cycle", "Due Date", "Auto-Renew", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        
        // Search Logic
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

        // Double Click to Edit
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

        // --- BOTTOM PANEL ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        totalLabel = new JLabel("Total Monthly: 0.00 AED");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalLabel.setForeground(new Color(0, 100, 0));

        JButton btnExport = new JButton("Export to Excel (CSV)");
        btnExport.addActionListener(e -> exportToCSV());

        JButton btnRefresh = new JButton("Refresh Data");
        btnRefresh.addActionListener(e -> refreshUI());

        bottomPanel.add(totalLabel);
        bottomPanel.add(btnExport);
        bottomPanel.add(btnRefresh);
        mainFrame.add(bottomPanel, BorderLayout.SOUTH);

        refreshUI();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }

    private void refreshUI() {
        currentList = manager.getUserSubscriptions();
        tableModel.setRowCount(0);
        leftPanel.removeAll();
        
        double totalMonthly = 0;

        for (Subscription s : currentList) {
            tableModel.addRow(new Object[]{
                s.getServiceName(), String.format("%.2f", s.getPrice()),
                s.getCycle(), s.getNextDueDate(),
                s.isAutoRenew() ? "Yes" : "No", s.getStatus()
            });

            JButton subBtn = new JButton(s.getServiceName());
            if (s.getStatus().equalsIgnoreCase("Active")) {
                subBtn.setForeground(new Color(0, 100, 0)); 
                double cost = s.getPrice();
                if(s.getCycle().equalsIgnoreCase("Yearly")) cost = cost / 12;
                totalMonthly += cost;
            } else {
                subBtn.setForeground(Color.RED); 
            }
            subBtn.addActionListener(e -> openDialog(s, s.getServiceName()));
            leftPanel.add(subBtn);
        }

        JButton btnAdd = new JButton("Add New (+)");
        btnAdd.setFont(new Font("Arial", Font.BOLD, 12));
        btnAdd.addActionListener(e -> openDialog(null, ""));
        leftPanel.add(btnAdd);

        totalLabel.setText(String.format("Total Monthly: %.2f AED", totalMonthly));
        leftPanel.revalidate();
        leftPanel.repaint();
    }

    private void exportToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save as CSV");
        int userSelection = fileChooser.showSaveDialog(mainFrame);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".csv")) filePath += ".csv";

            try (FileWriter fw = new FileWriter(filePath)) {
                fw.write("Service,Price,Cycle,Due Date,Status\n");
                for (Subscription s : currentList) {
                    fw.write(s.getServiceName() + "," + s.getPrice() + "," + s.getCycle() + "," 
                            + s.getNextDueDate() + "," + s.getStatus() + "\n");
                }
                JOptionPane.showMessageDialog(mainFrame, "Export Successful!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainFrame, "Error: " + ex.getMessage());
            }
        }
    }

    // --- UPDATED DIALOG WITH SMART DATE LOGIC ---
    private void openDialog(Subscription existingSub, String defaultName) {
        boolean isEditMode = (existingSub != null);
        JDialog dialog = new JDialog(mainFrame, isEditMode ? "Edit" : "Add", true);
        dialog.setSize(400, 420);
        dialog.setLayout(new GridLayout(8, 2, 10, 10));
        dialog.setLocationRelativeTo(mainFrame);

        // 1. Get Today's Date for Defaults/Validation
        LocalDate today = LocalDate.now();

        JTextField nameField = new JTextField(isEditMode ? existingSub.getServiceName() : defaultName);
        if (isEditMode) nameField.setEditable(false);
        
        JTextField priceField = new JTextField(isEditMode ? String.valueOf(existingSub.getPrice()) : "");
        
        JComboBox<String> cycleBox = new JComboBox<>(new String[]{"Monthly", "Yearly"});
        if (isEditMode) cycleBox.setSelectedItem(existingSub.getCycle());

        // 2. Default Date = Today (if not editing)
        JTextField dateField = new JTextField(isEditMode ? existingSub.getNextDueDate() : today.toString());
        
        JCheckBox renewBox = new JCheckBox("Auto-renew");
        if (isEditMode) renewBox.setSelected(existingSub.isAutoRenew());
        
        JComboBox<String> statusBox = new JComboBox<>(new String[]{"Active", "Expired"});
        if (isEditMode) statusBox.setSelectedItem(existingSub.getStatus());

        // --- NEW: AUTO UPDATE DATE ON CYCLE CHANGE ---
        cycleBox.addActionListener(e -> {
            String selected = (String) cycleBox.getSelectedItem();
            if ("Monthly".equalsIgnoreCase(selected)) {
                dateField.setText(today.plusMonths(1).toString());
            } else {
                dateField.setText(today.plusYears(1).toString());
            }
        });

        dialog.add(new JLabel("  Service Name:")); dialog.add(nameField);
        dialog.add(new JLabel("  Price (AED):")); dialog.add(priceField);
        dialog.add(new JLabel("  Cycle:")); dialog.add(cycleBox);
        dialog.add(new JLabel("  Next Due (YYYY-MM-DD):")); dialog.add(dateField);
        dialog.add(new JLabel("  Auto-Renew:")); dialog.add(renewBox);
        dialog.add(new JLabel("  Status:")); dialog.add(statusBox);

        JButton btnSave = new JButton(isEditMode ? "Update" : "Save");
        btnSave.addActionListener(e -> {
            try {
                // --- VALIDATION LOGIC START ---
                String dateText = dateField.getText().trim();
                LocalDate inputDate = LocalDate.parse(dateText); // Will throw error if format is wrong
                String selectedCycle = (String) cycleBox.getSelectedItem();

                LocalDate limitDate;
                if ("Monthly".equalsIgnoreCase(selectedCycle)) {
                    limitDate = today.plusMonths(1);
                    if (inputDate.isAfter(limitDate)) {
                        JOptionPane.showMessageDialog(dialog, "Error: Monthly cycle cannot be due more than 1 month from today.");
                        return; // Stop saving
                    }
                } else {
                    limitDate = today.plusYears(1);
                    if (inputDate.isAfter(limitDate)) {
                        JOptionPane.showMessageDialog(dialog, "Error: Yearly cycle cannot be due more than 1 year from today.");
                        return; // Stop saving
                    }
                }
                // --- VALIDATION LOGIC END ---

                double price = Double.parseDouble(priceField.getText());
                String name = nameField.getText().trim();
                if(name.isEmpty()) return;

                if (isEditMode) {
                    manager.updateSubscription(existingSub.getId(), name, price, 
                        selectedCycle, dateText, 
                        renewBox.isSelected(), (String)statusBox.getSelectedItem());
                } else {
                    manager.addSubscription(name, price, 
                        selectedCycle, dateText, 
                        renewBox.isSelected(), (String)statusBox.getSelectedItem());
                }
                refreshUI();
                dialog.dispose();
            } catch (DateTimeParseException dtpe) {
                JOptionPane.showMessageDialog(dialog, "Error: Invalid Date. Please use YYYY-MM-DD format (e.g., 2025-01-31)");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: Check price format.");
            }
        });

        JButton btnDelete = new JButton("Delete");
        btnDelete.setForeground(Color.RED);
        btnDelete.setEnabled(isEditMode);
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
