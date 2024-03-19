import java.util.List;

public class Order {
    private int orderID;
    private int nodeID;
    private int duration; // in seconds
    private int profit; // Assuming profit is stored as an integer
    private List<Integer> allowedStudents; // List of student IDs allowed to take this order

    public Order(int orderID, int nodeID, int duration, int profit, List<Integer> allowedStudents) {
        this.orderID = orderID;
        this.nodeID = nodeID;
        this.duration = duration;
        this.profit = profit;
        this.allowedStudents = allowedStudents;
    }

    // Getters and possibly setters as needed
    public int getOrderID() { return orderID; }
    public int getNodeID() { return nodeID; }
    public int getDuration() { return duration; }
    public int getProfit() { return profit; }
    public List<Integer> getAllowedStudents() { return allowedStudents; }
}