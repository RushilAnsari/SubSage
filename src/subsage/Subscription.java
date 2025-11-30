package subsage;
import java.util.*;

public class Subscription implements Payable {

    private String name;
    private double price;
    private String category;
    private Date nextBillingDate;

    public Subscription(String name, double price, String category, Date nextBillingDate) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.nextBillingDate = nextBillingDate;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Date getNextBillingDate() { return nextBillingDate; }
    public void setNextBillingDate(Date nextBillingDate) { this.nextBillingDate = nextBillingDate; }

    @Override
    public double getMonthlyPayment() {
        return price;
    }

    @Override
    public String toString() {
        return name + " | " + price + " | " + nextBillingDate;
    }
}
