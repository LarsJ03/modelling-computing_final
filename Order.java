import java.util.List;

public class Order {
    private int orderID;
    private int nodeID;
    private int duration; // in seconds
    private int profit; // Assuming profit is stored as an integer
    private List<Integer> allowedStudents; // List of student IDs allowed to take this order
    private boolean assigned; // Indicates whether the order has been assigned

    // Existing constructors and methods remain unchanged

    public Order(int orderID, int nodeID, int duration, int profit, List<Integer> allowedStudents, boolean assigned) {
        this.orderID = orderID;
        this.nodeID = nodeID;
        this.duration = duration;
        this.profit = profit;
        this.allowedStudents = allowedStudents;
        this.assigned = assigned; // Initialize as not assigned
    }

    // Method to set the assigned status of the order
    public void setAssigned(boolean assigned) {
        this.assigned = assigned;
    }

    // Method to check if the order is assigned
    public boolean isAssigned() {
        return assigned;
    }

    public int getOrderID() { return orderID; }
    public int getNodeID() { return nodeID; }
    public int getDuration() { return duration; }
    public int getProfit() { return profit; }
    public List<Integer> getAllowedStudents() { return allowedStudents; }
}