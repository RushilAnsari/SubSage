package subsage;
import java.util.Date;

public class Payment {

    private String subscriptionName;
    private double amountPaid;
    private Date paymentDate;

    public Payment(String subscriptionName, double amountPaid, Date paymentDate) {
        this.subscriptionName = subscriptionName;
        this.amountPaid = amountPaid;
        this.paymentDate = paymentDate;
    }

    public String getSubscriptionName() { return subscriptionName; }
    public void setSubscriptionName(String subscriptionName) { this.subscriptionName = subscriptionName; }

    public double getAmountPaid() { return amountPaid; }
    public void setAmountPaid(double amountPaid) { this.amountPaid = amountPaid; }

    public Date getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Date paymentDate) { this.paymentDate = paymentDate; }

    @Override
    public String toString() {
        return subscriptionName + " | " + amountPaid + " | " + paymentDate;
    }
}
