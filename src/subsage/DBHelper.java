package subsage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class DBHelper {
    private static final String DB_URL = "jdbc:sqlite:subsage.db";

    //this is the first thing that runs to create the WHOLE THING!
    public static void createNewDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "CRITICAL ERROR: SQLite JAR not found!");
            return;
        }

        String sqlSubs = "CREATE TABLE IF NOT EXISTS subscriptions (\n"
                + " id integer PRIMARY KEY,\n"
                + " username text NOT NULL,\n"
                + " service text NOT NULL,\n"
                + " category text,\n"
                + " price real,\n"
                + " cycle text,\n"
                + " due_date text,\n"
                + " auto_renew integer,\n"
                + " status text\n"
                + ");";

        // budget got added w/ the users..
        String sqlUsers = "CREATE TABLE IF NOT EXISTS users (\n"
                + " username text PRIMARY KEY,\n"
                + " password text NOT NULL,\n"
                + " budget real DEFAULT 0\n"
                + ");";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlSubs);
            stmt.execute(sqlUsers);
        } catch (SQLException e) {
            System.out.println("DB Init Error: " + e.getMessage());
        }
    }

    // --- BUDGET METHODS ---
    public static void updateBudget(String username, double limit) {
        String sql = "UPDATE users SET budget = ? WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, limit);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static double getBudget(String username) {
        String sql = "SELECT budget FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getDouble("budget");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // Default
    }
    // ----------------------
    // we came from SubSageManager to here now!💀
    public static boolean checkUserExists(String username) {
        String sql = "SELECT username FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next(); 
        } catch (SQLException e) { return false; }
    }

    public static boolean validateLogin(String username, String password) {
        String sql = "SELECT password FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("password").equals(password);
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public static void registerUser(String username, String password) {
        String sql = "INSERT INTO users(username, password, budget) VALUES(?,?,0)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void saveSubscription(Subscription sub) {
        String sql = "INSERT INTO subscriptions(username, service, category, price, cycle, due_date, auto_renew, status) VALUES(?,?,?,?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sub.getUsername());
            pstmt.setString(2, sub.getServiceName());
            pstmt.setString(3, sub.getCategory());
            pstmt.setDouble(4, sub.getPrice());
            pstmt.setString(5, sub.getCycle());
            pstmt.setString(6, sub.getNextDueDate());
            pstmt.setInt(7, sub.isAutoRenew() ? 1 : 0);
            pstmt.setString(8, sub.getStatus());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Save Error: " + e.getMessage());
        }
    }

    public static void updateSubscription(Subscription sub) {
        String sql = "UPDATE subscriptions SET price = ?, category = ?, cycle = ?, due_date = ?, auto_renew = ?, status = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, sub.getPrice());
            pstmt.setString(2, sub.getCategory());
            pstmt.setString(3, sub.getCycle());
            pstmt.setString(4, sub.getNextDueDate());
            pstmt.setInt(5, sub.isAutoRenew() ? 1 : 0);
            pstmt.setString(6, sub.getStatus());
            pstmt.setInt(7, sub.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Update Error: " + e.getMessage());
        }
    }

    public static void deleteSubscription(int id) {
        String sql = "DELETE FROM subscriptions WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Delete Error: " + e.getMessage());
        }
    }

    public static List<Subscription> getSubscriptionsByUser(String username) {
        List<Subscription> list = new ArrayList<>();
        String sql = "SELECT * FROM subscriptions WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Subscription(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("service"),
                        rs.getString("category"),
                        rs.getDouble("price"),
                        rs.getString("cycle"),
                        rs.getString("due_date"),
                        rs.getInt("auto_renew") == 1,
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Load Error: " + e.getMessage());
        }
        return list;
    }
}
