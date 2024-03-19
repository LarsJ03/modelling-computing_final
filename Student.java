import java.util.List;
import java.util.ArrayList;

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
}