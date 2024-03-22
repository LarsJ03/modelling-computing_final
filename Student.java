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

    public boolean addJob(Order order, int totalDriveTime) {
        int jobTime = order.getDuration() + totalDriveTime;
        if (this.totalWorkingTime + jobTime <= 8 * 60 * 60) { // Check if within 8-hour limit
            this.assignedOrderIDs.add(order.getOrderID());
            this.totalWorkingTime += jobTime;
            return true;
        } else {
            return false;
        }
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

    public boolean addOrder(int orderId, HashMap<Integer, Order> orders, int[][] drivingTimes) {
        Order order = orders.get(orderId);
        int driveTimeTo = drivingTimes[251][order.getNodeID()];
        int driveTimeBack = drivingTimes[order.getNodeID()][251];
        int totalDriveTime = driveTimeTo + driveTimeBack + order.getDuration();
    
        // Check if the addition keeps the total working time within an 8-hour workday limit
        if (this.totalWorkingTime + totalDriveTime <= 8 * 60 * 60) {
            this.assignedOrderIDs.add(orderId);
            this.totalWorkingTime += totalDriveTime;
            return true; // Order was successfully added
        }
        return false; // Order was not added due to exceeding the workday limit
    }
    
    public void removeOrder(int orderId, HashMap<Integer, Order> orders, int[][] drivingTimes) {
        Order order = orders.get(orderId);
        int driveTimeTo = drivingTimes[251][order.getNodeID()];
        int driveTimeBack = drivingTimes[order.getNodeID()][251];
        int totalDriveTime = driveTimeTo + driveTimeBack + order.getDuration();

        this.assignedOrderIDs.remove(Integer.valueOf(orderId)); // Remove the order ID from the list
        this.totalWorkingTime -= totalDriveTime; // Subtract the time taken to complete the order
    }
}