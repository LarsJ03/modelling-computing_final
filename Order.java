public class Order {
    String orderId;
    double profit;
    int duration;
    int nodeId;
    // Assuming the rest of the columns are indicators for which student can handle this order.
    // You can adjust this class to include more fields or handle it differently based on your requirements.

    public Order(String orderId, double profit, int duration, int nodeId) {
        this.orderId = orderId;
        this.profit = profit;
        this.duration = duration;
        this.nodeId = nodeId;
    }
}
