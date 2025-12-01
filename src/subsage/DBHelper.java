package subsage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DBHelper {
    private static final String DB_url = "jdbc:sqlite:subsage.db";
    private static final SimpleDateFormat date_format = new SimpleDateFormat("dd-MM-yyyy");

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_url);
    }

    public static void initDatabase() {
        String createSubscriptionsTable = "CREATE TABLE IF NOT EXISTS subscriptions ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT," + "name TEXT NOT NULL," + "category TEXT,"
                + "price REAL NOT NULL, " + "next_billing_date TEXT" + ");";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(createSubscriptionsTable);
            System.out.println("Database ready (Subscriptions table).");
        } catch (SQLException e) {
            System.out.println("Error initialising database: ");
            e.printStackTrace();
        }
    }

    public static void insertSubscription(Subscription s) {
        String sql = "INSERT INTO subscriptions " + "(name, category, price, next_billing_date)"
                + "VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getCategory());
            ps.setDouble(3, s.getPrice());
            Date d = s.getNextBillingDate();
            String Datestr = (d != null) ? date_format.format(d) : null;
            ps.setString(4, Datestr);
            ps.executeUpdate();
            System.out.println("Saved Subscriptions in the DataBase: " + s.getName());
        } catch (SQLException e) {
            System.out.println("Error Inserting subscription: ");
            e.printStackTrace();
        }
    }

    public static List<Subscription> getAllSubscriptions() {
        List<Subscription> result = new ArrayList<>();
        String sql = "SELECT name, category, price, next_billing_date FROM subscriptions";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();) {
            while (rs.next()) {
                String name = rs.getString("name");
                String category = rs.getString("category");
                double price = rs.getDouble("price");
                String dateString = rs.getString("next_billing_date");
                Date nextDate = null;
                if (dateString != null) {
                    try {
                        nextDate = date_format.parse(dateString);
                    } catch (ParseException e) {
                        System.out.println("Could not parse date: " + dateString);
                    }
                }
                Subscription s = new Subscription(name, price, category, nextDate);
                result.add(s);
            }
        } catch (SQLException e) {
            System.out.println("Error loading subscriptions: ");
        }
        return result;
    }

    public static void deleteSubscriptionByName(String name) {
        String sql = "DELETE FROM subscriptions WHERE name = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.executeUpdate();
            System.out.println("Deleted Subscription from Database: " + name);
        } catch (SQLException e) {
            System.out.println("Error deleting the subscription: ");
            e.printStackTrace();
        }
    }

    public static void updateSubscription(String originalName, Subscription s) {
        String sql = "UPDATE subscriptions " + "SET name = ?, category = ?, price = ?, next_billing_date = ?"
                + "WHERE name = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getCategory());
            ps.setDouble(3, s.getPrice());
            Date d = s.getNextBillingDate();
            String dateString = (d != null) ? date_format.format(d) : null;
            ps.setString(4, dateString);
            ps.setString(5, originalName);
            ps.executeUpdate();
            System.out.println("Update subscription in Database: " + originalName);
        } catch (SQLException e) {
            System.out.println("Error Updating Subscription:");
            e.printStackTrace();
        }
    }
}
