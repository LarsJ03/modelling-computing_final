import java.util.List;

public class Order {
    private int orderID; // Unique identifier for the order
    private int nodeID; // Geographic node ID associated with the order
    private int duration; // Duration of the order in seconds
    private int profit; // Profit gained from completing the order
    private List<Integer> allowedStudents; // List of student IDs that are allowed to take this order

    // Constructor to initialize an Order object with all attributes
    public Order(int orderID, int nodeID, int duration, int profit, List<Integer> allowedStudents) {
        this.orderID = orderID;
        this.nodeID = nodeID;
        this.duration = duration;
        this.profit = profit;
        this.allowedStudents = allowedStudents;
    }

    // Getter method for the order ID
    public int getOrderID() {
        return orderID;
    }

    // Getter method for the node ID
    public int getNodeID() {
        return nodeID;
    }

    // Getter method for the duration of the order
    public int getDuration() {
        return duration;
    }

    // Getter method for the profit associated with the order
    public int getProfit() {
        return profit;
    }

    // Getter method to retrieve the list of student IDs allowed to take the order
    public List<Integer> getAllowedStudents() {
        return allowedStudents;
    }
}