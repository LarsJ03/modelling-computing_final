import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;

public class Student {
    private int id;
    private List<Integer> assignedOrderIDs; 
    private int totalWorkingTime; 

    // Constructor
    public Student(int id) {
        this.id = id;
        this.assignedOrderIDs = new ArrayList<>();
        this.totalWorkingTime = 0;
    }

    // Constructor for deep copying
    public Student(int id, List<Integer> assignedOrderIDs, int totalWorkingTime) {
        this.id = id;
        this.assignedOrderIDs = new ArrayList<>(assignedOrderIDs); 
        this.totalWorkingTime = totalWorkingTime;
    }

    public boolean addOrder(Order order, HashMap<Integer, Order> orders, int[][] drivingTimes) {
        int oldWorkingTime = this.getTotalWorkingTime();
        
        // Generate a random index to insert the new order. The +1 ensures that the new order can also be placed at the end of the list.
        int randomIndex = new Random().nextInt(this.assignedOrderIDs.size() + 1);
        
        // Insert the new order at the random index
        this.assignedOrderIDs.add(randomIndex, order.getOrderID());
        
        // Recalculate the total working time with the new order included
        int workingTime = AnnealingStrategies.recalculateWorkingTime(this, orders, drivingTimes);
        
        // Check if the new total working time exceeds the maximum allowed time
        if (workingTime > 28800) {
            // If it does, remove the newly added order and reset the working time
            this.assignedOrderIDs.remove(Integer.valueOf(order.getOrderID())); // Use Integer.valueOf to ensure removal of the object, not the index
            this.setTotalWorkingTime(oldWorkingTime);
            return false;
        } else {
            return true; 
        }
    }


    public int getLastAssignedOrderID() {
        return assignedOrderIDs.get(assignedOrderIDs.size() - 1);
    }

    public int distance(int fromNode, int toNode, int[][] drivingTimes) {
        return drivingTimes[fromNode][toNode];
    }

    public int getId() {
        return id;
    }

    public int getTotalWorkingTime() {
        return totalWorkingTime;
    }

    public void setTotalWorkingTime(int totalWorkingTime) {
        this.totalWorkingTime = totalWorkingTime;
    }
    public List<Integer> getAssignedOrderIDs() {
        return assignedOrderIDs;
    }
    
    public void removeOrder(Order randomOrder, int[][] drivingTimes) {
        Integer randomOrderID = randomOrder.getOrderID(); 
        this.assignedOrderIDs.remove(randomOrderID);
    }
}