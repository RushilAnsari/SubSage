package subsage;

import java.util.List;

public class SubSageManager {
    private String currentUser;

    public SubSageManager() {
        DBHelper.createNewDatabase();
    }

    public boolean userExists(String username) {
        return DBHelper.checkUserExists(username);
    }

    public boolean login(String username, String password) {
        if (DBHelper.validateLogin(username, password)) {
            this.currentUser = username;
            return true;
        }
        return false;
    }

    public void register(String username, String password) {
        DBHelper.registerUser(username, password);
        this.currentUser = username;
    }

    // --- BUDGET METHODS ---
    public void setUserBudget(double limit) {
        DBHelper.updateBudget(currentUser, limit);
    }

    public double getUserBudget() {
        return DBHelper.getBudget(currentUser);
    }
    // --------------------------

    public String getCurrentUser() {
        return currentUser;
    }

    public List<Subscription> getUserSubscriptions() {
        return DBHelper.getSubscriptionsByUser(currentUser);
    }

    public void addSubscription(String service, String category, double price, String cycle, String date, boolean renew, String status) {
        Subscription newSub = new Subscription(0, currentUser, service, category, price, cycle, date, renew, status);
        DBHelper.saveSubscription(newSub);
    }

    public void updateSubscription(int id, String service, String category, double price, String cycle, String date, boolean renew, String status) {
        Subscription sub = new Subscription(id, currentUser, service, category, price, cycle, date, renew, status);
        DBHelper.updateSubscription(sub);
    }

    public void deleteSubscription(int id) {
        DBHelper.deleteSubscription(id);
    }
}
