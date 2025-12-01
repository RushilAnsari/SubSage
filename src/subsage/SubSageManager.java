package subsage;


public class SubSageManager {
    // Constructor - sets up database connection
    public SubSageManager() {
        DBHelper.initDatabase();
    }

    // ========== SUBSCRIPTION MANAGEMENT ==========

    // Add a new subscription
    public boolean addSubscription(Subscription subscription) {
        // Check if subscription with same name already exists
        if (findByName(subscription.getName()) != null) {
            System.out.println("Subscription already exists: " + subscription.getName());
            return false;
        }
        DBHelper.insertSubscription(subscription);
        return true;
    }

    // Get all subscriptions
    public java.util.List<Subscription> getAllSubscriptions() {
        return DBHelper.getAllSubscriptions();
    }

    // Find subscription by name (for button clicks like Netflix)
    public Subscription findByName(String name) {
        for (Subscription sb : getAllSubscriptions()) {
            if (sb.getName().equalsIgnoreCase(name))
                return sb;
        }
        return null;
    }

    // Update an existing subscription
    public boolean updateSubscription(String originalName, Subscription updatedSubscription) {
        DBHelper.updateSubscription(originalName, updatedSubscription);
        return true;
    }

    // Delete a subscription
    public boolean deleteSubscription(String name) {
        DBHelper.deleteSubscriptionByName(name);
        return true;
    }

    // ========== PAYMENT MANAGEMENT ==========

    // Record a payment for a subscription
    /*
     * public boolean markAsPaid(String subscriptionName, String paymentDate, String
     * paymentMethod) {
     * Subscription sub = findByName(subscriptionName);
     * if (sub == null) {
     * System.out.println("Subscription not found: " + subscriptionName);
     * return false;
     * }
     * 
     * return dbHelper.addPayment(
     * subscriptionName,
     * sub.getAmount(),
     * paymentDate,
     * paymentMethod,
     * "Payment recorded"
     * );
     * }
     * 
     * // Get payment history for a subscription
     * public java.util.List<String> getPaymentHistory(String subscriptionName) {
     * return dbHelper.getPaymentHistory(subscriptionName);
     * }
     */
    // ========== STATISTICS ==========

    // Calculate total monthly spending
    public double getTotalMonthlySpending() {
        double total = 0.0;
        for (Subscription sb : getAllSubscriptions()) {
            total += sb.getMonthlyPayment();
        }
        return total;
    }

    // Get count of active subscriptions
    public int getActiveSubscriptionsCount() {
        return getAllSubscriptions().size();
    }

    // Get subscriptions by category
    public java.util.List<Subscription> getSubscriptionsByCategory(String category) {
        java.util.List<Subscription> all = getAllSubscriptions();
        java.util.List<Subscription> filtered = new java.util.ArrayList<>();

        for (Subscription sub : all) {
            if (sub.getCategory().equalsIgnoreCase(category)) {
                filtered.add(sub);
            }
        }

        return filtered;
    }

    // ========== UTILITY METHODS ==========

    // Check if a subscription exists
    public boolean subscriptionExists(String name) {
        return findByName(name) != null;
    }

    // Get upcoming payments (simplified version)
    public java.util.List<Subscription> getUpcomingPayments() {
        // For now, just return all subscriptions
        // You can enhance this later to filter by due date
        return getAllSubscriptions();
    }

    /*
     * // Clear all data (for testing)
     * public void clearAllData() {
     * dbHelper.clearAllData();
     * }
     */ 
}
