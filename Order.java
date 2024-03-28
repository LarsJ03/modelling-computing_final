import java.util.List;

public class Order {
    private int orderID;
    private int nodeID;
    private int duration; 
    private int profit; 
    private List<Integer> allowedStudents; 


    public Order(int orderID, int nodeID, int duration, int profit, List<Integer> allowedStudents) {
        this.orderID = orderID;
        this.nodeID = nodeID;
        this.duration = duration;
        this.profit = profit;
        this.allowedStudents = allowedStudents;
    }

    public int getOrderID() { return orderID; }
    public int getNodeID() { return nodeID; }
    public int getDuration() { return duration; }
    public int getProfit() { return profit; }
    public List<Integer> getAllowedStudents() { return allowedStudents; }
}