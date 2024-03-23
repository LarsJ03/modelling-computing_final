import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class Student {
    private int id;
    private List<Integer> assignedOrderIDs; // Stores assigned order IDs
    private int totalWorkingTime; // in seconds

    // Constructor
    public Student(int id) {
        this.id = id;
        this.assignedOrderIDs = new ArrayList<>();
        this.totalWorkingTime = 0;
    }

    // Constructor for deep copying
    public Student(int id, List<Integer> assignedOrderIDs, int totalWorkingTime) {
        this.id = id;
        this.assignedOrderIDs = new ArrayList<>(assignedOrderIDs); // Ensure a deep copy is made
        this.totalWorkingTime = totalWorkingTime;
    }

    public boolean addOrder(Order order, int[][] drivingTimes, HashMap<Integer, Order> orders) {
        int totalTime = 0;
        if (this.getAssignedOrderIDs().size() == 0) {
            int nodeFrom = 251; 
            int nodeTo = order.getNodeID();
            int jobTime = order.getDuration();
            int driveTimeTo = distance(nodeFrom, nodeTo, drivingTimes);
            totalTime = driveTimeTo + jobTime;
        } else {
            int lastOrderID = this.getLastAssignedOrderID();
            Order lastOrder = orders.get(lastOrderID);
            int nodeFrom = lastOrder.getNodeID();
            int nodeTo = order.getNodeID();
            int jobTime = order.getDuration();
            int driveTimeTo = distance(nodeFrom, nodeTo, drivingTimes);
            int driveTimeBack = distance(nodeTo, 251, drivingTimes); // Assuming you need to return to the starting point
            totalTime = driveTimeTo + jobTime + driveTimeBack;

        }

        if (this.totalWorkingTime + totalTime <= 8 * 60 * 60) { // Check if within 8-hour limit
            this.assignedOrderIDs.add(order.getOrderID());
            this.totalWorkingTime += totalTime;
            order.setAssigned(true);
            return true;
        } else {
            return false;
        }
    }


    public int getLastAssignedOrderID() {
        return assignedOrderIDs.get(assignedOrderIDs.size() - 1);
    }

    public int distance(int fromNode, int toNode, int[][] drivingTimes) {
        return drivingTimes[fromNode][toNode];
    }

    // Getter for the student's ID
    public int getId() {
        return id;
    }

    public int getTotalWorkingTime() {
        return totalWorkingTime;
    }

    // Setter for total working time
    public void setTotalWorkingTime(int totalWorkingTime) {
        this.totalWorkingTime = totalWorkingTime;
    }

    // Getter for the list of assigned order IDs
    public List<Integer> getAssignedOrderIDs() {
        return assignedOrderIDs;
    }

    
    
    public void removeOrder(Order randomOrder, int[][] drivingTimes) {
        Integer randomOrderID = randomOrder.getOrderID(); // Use Integer to ensure the correct remove method is called
        this.assignedOrderIDs.remove(randomOrderID); // This now correctly removes by value
        int driveTimeBack = distance(randomOrder.getNodeID(), 251, drivingTimes); // Assuming 251 is the headquarters node ID
        this.totalWorkingTime -= randomOrder.getDuration() + driveTimeBack;
        randomOrder.setAssigned(false);
    }
}