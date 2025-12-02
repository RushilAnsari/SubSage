package subsage;

public class Subscription {
    private int id;
    private String username;
    private String serviceName;
    private String category;
    private double price;
    private String cycle; 
    private String nextDueDate;
    private boolean autoRenew;
    private String status; 

    public Subscription(int id, String username, String serviceName, String category, double price, String cycle, String nextDueDate, boolean autoRenew, String status) {
        this.id = id;
        this.username = username;
        this.serviceName = serviceName;
        this.category = category; 
        this.price = price;
        this.cycle = cycle;
        this.nextDueDate = nextDueDate;
        this.autoRenew = autoRenew;
        this.status = status;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getServiceName() { return serviceName; }
    public String getCategory() { return category; } 
    public double getPrice() { return price; }
    public String getCycle() { return cycle; }
    public String getNextDueDate() { return nextDueDate; }
    public boolean isAutoRenew() { return autoRenew; }
    public String getStatus() { return status; }
}
