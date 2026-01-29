package com.Parthiban;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/atm_db";
    private static final String USER = "root";
    private static final String PASS = "Parthiban2006@06";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}

public class ATMGUI extends JFrame {

    private Connection conn;
    private int loggedAccount;
    JTextField accField;
    JPasswordField passField;

    // Pleasant theme colors
    Color background = new Color(235, 250, 247);
    Color buttonColor = new Color(0, 153, 153);
    Color textColor = Color.WHITE;
    Color inputColor = new Color(255, 255, 255);

    public ATMGUI() {

        try {
            conn = DBConnection.getConnection();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Connection Failed!");
        }

        // LOGIN WINDOW UI
        setTitle("ATM Login");
        setSize(400, 250);
        setLayout(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(background);

        JLabel lblAcc = new JLabel("Account No:");
        lblAcc.setBounds(50, 50, 100, 30);
        lblAcc.setFont(new Font("Arial", Font.BOLD, 14));
        add(lblAcc);

        accField = new JTextField();
        accField.setBounds(150, 50, 150, 30);
        accField.setBackground(inputColor);
        add(accField);

        JLabel lblPass = new JLabel("Password:");
        lblPass.setBounds(50, 100, 100, 30);
        lblPass.setFont(new Font("Arial", Font.BOLD, 14));
        add(lblPass);

        passField = new JPasswordField();
        passField.setBounds(150, 100, 150, 30);
        passField.setBackground(inputColor);
        add(passField);

        JButton loginBtn = new JButton("Login");
        styleButton(loginBtn);
        loginBtn.setBounds(140, 150, 100, 40);
        add(loginBtn);

        loginBtn.addActionListener(e -> login());
        setVisible(true);
    }


    private void styleButton(JButton btn) {
        btn.setBackground(buttonColor);
        btn.setForeground(textColor);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
    }


    public void login() {
        try {
            int acc = Integer.parseInt(accField.getText());
            String pass = new String(passField.getPassword());

            String sql = "SELECT * FROM accounts WHERE account_no=? AND password=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, acc);
            ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                loggedAccount = acc;
                JOptionPane.showMessageDialog(this, "Login Successful!");
                showATMMenu();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Credentials!");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }


    public void showATMMenu() {

        JFrame menu = new JFrame("ATM Menu");
        menu.setSize(500, 500);
        menu.setLayout(new BorderLayout());
        menu.setDefaultCloseOperation(EXIT_ON_CLOSE);
        menu.getContentPane().setBackground(background);

        JLabel title = new JLabel("ATM Dashboard", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        menu.add(title, BorderLayout.NORTH);

        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(3, 3, 10, 10));
        gridPanel.setBackground(background);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JButton deposit = new JButton("Deposit");
        JButton withdraw = new JButton("Withdraw");
        JButton balance = new JButton("Balance");
        JButton statement = new JButton("Statement");
        JButton transfer = new JButton("Transfer");
        JButton logout = new JButton("Logout");

        JButton blank1 = new JButton();
        JButton blank2 = new JButton();
        JButton blank3 = new JButton();

        blank1.setVisible(false);
        blank2.setVisible(false);
        blank3.setVisible(false);

        JButton[] buttons = {deposit, withdraw, balance, statement, transfer, logout};

        for (JButton b : buttons) styleButton(b);

        gridPanel.add(deposit);
        gridPanel.add(withdraw);
        gridPanel.add(balance);
        gridPanel.add(statement);
        gridPanel.add(transfer);
        gridPanel.add(logout);
        gridPanel.add(blank1);
        gridPanel.add(blank2);
        gridPanel.add(blank3);

        menu.add(gridPanel, BorderLayout.CENTER);

        JTextArea display = new JTextArea();
        display.setEditable(false);
        display.setFont(new Font("Monospaced", Font.PLAIN, 14));
        display.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(display);
        scrollPane.setPreferredSize(new Dimension(400, 120));
        scrollPane.setBorder(BorderFactory.createLineBorder(buttonColor, 2));
        menu.add(scrollPane, BorderLayout.SOUTH);

        menu.setVisible(true);


        deposit.addActionListener(e -> {
            String amt = JOptionPane.showInputDialog("Enter Deposit Amount:");
            if (amt != null) {
                depositMoney(Double.parseDouble(amt));
                display.setText("Deposited: ₹" + amt);
            }
        });

        withdraw.addActionListener(e -> {
            String amt = JOptionPane.showInputDialog("Enter Withdrawal Amount:");
            if (amt != null) {
                withdrawMoney(Double.parseDouble(amt));
                display.setText("Withdrawn: ₹" + amt);
            }
        });

        balance.addActionListener(e -> display.setText("Your Balance: ₹" + getBalance()));
        statement.addActionListener(e -> display.setText(getMiniStatement()));

        transfer.addActionListener(e -> {
            String toAcc = JOptionPane.showInputDialog("Enter Receiver Account:");
            String amt = JOptionPane.showInputDialog("Enter Amount:");
            if (toAcc != null && amt != null) {
                moneyTransfer(Integer.parseInt(toAcc), Double.parseDouble(amt));
                display.setText("Transferred ₹" + amt + " to A/C: " + toAcc);
            }
        });

        logout.addActionListener(e -> {
            JOptionPane.showMessageDialog(menu, "Logged Out Successfully!");
            menu.dispose();
        });
    }


    public void depositMoney(double amount) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE accounts SET balance=balance+? WHERE account_no=?");
            ps.setDouble(1, amount);
            ps.setInt(2, loggedAccount);
            ps.executeUpdate();
        } catch (Exception ignored) {}
    }

    public void withdrawMoney(double amount) {
        if (getBalance() >= amount) {
            try {
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE accounts SET balance=balance-? WHERE account_no=?");
                ps.setDouble(1, amount);
                ps.setInt(2, loggedAccount);
                ps.executeUpdate();
            } catch (Exception ignored) {}
        } else {
            JOptionPane.showMessageDialog(this, "Insufficient Balance!");
        }
    }

    public double getBalance() {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT balance FROM accounts WHERE account_no=?");
            ps.setInt(1, loggedAccount);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (Exception ignored) {}
        return 0;
    }

    public String getMiniStatement() {
        StringBuilder history = new StringBuilder("\n---- Mini Statement ----\n");
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM transactions WHERE account_no=? ORDER BY timestamp DESC LIMIT 5");
            ps.setInt(1, loggedAccount);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                history.append(rs.getString("type")).append(" | ₹")
                        .append(rs.getDouble("amount")).append(" | ")
                        .append(rs.getTimestamp("timestamp")).append("\n");
            }
        } catch (Exception ignored){}
        return history.toString();
    }

    public void moneyTransfer(int toAcc, double amount) {
        if (getBalance() >= amount) {
            withdrawMoney(amount);

            try {
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE accounts SET balance=balance+? WHERE account_no=?");
                ps.setDouble(1, amount);
                ps.setInt(2, toAcc);
                ps.executeUpdate();
            } catch (Exception ignored) {}
        }
    }

    public static void main(String[] args) {
        new ATMGUI();
    }
}
