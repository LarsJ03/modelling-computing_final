import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;

public class Student {
    private int id; // Unique identifier for the student
    private List<Integer> assignedOrderIDs; // List to store the IDs of orders assigned to the student
    private int totalWorkingTime; // Total working time of the student in seconds

    // Constructor for creating a new student with no orders assigned
    public Student(int id) {
        this.id = id;
        this.assignedOrderIDs = new ArrayList<>(); // Initialize with empty list of orders
        this.totalWorkingTime = 0; // Initial working time is zero
    }

    // Constructor for creating a student with existing orders and specified working time
    public Student(int id, List<Integer> assignedOrderIDs, int totalWorkingTime) {
        this.id = id;
        this.assignedOrderIDs = new ArrayList<>(assignedOrderIDs); // Make a copy of the order IDs list
        this.totalWorkingTime = totalWorkingTime; // Set the total working time
    }

    // Method to add an order to the student's list of orders
    public boolean addOrder(Order order, HashMap<Integer, Order> orders, int[][] drivingTimes) {
        int oldWorkingTime = this.getTotalWorkingTime(); // Store the old working time

        // Generate a random index to insert the order into the list
        int randomIndex = new Random().nextInt(this.assignedOrderIDs.size() + 1);
        
        this.assignedOrderIDs.add(randomIndex, order.getOrderID()); // Add order at the random position
        
        // Recalculate the working time after adding the order
        int workingTime = AnnealingStrategies.recalculateWorkingTime(this, orders, drivingTimes);
        
        // Check if the working time exceeds the maximum allowed (28800 seconds = 8 hours)
        if (workingTime > 28800) {
            // If it exceeds, remove the order and revert to the old working time
            this.assignedOrderIDs.remove(Integer.valueOf(order.getOrderID()));
            this.setTotalWorkingTime(oldWorkingTime);
            return false; // Indicate that the order was not successfully added
        } else {
            return true; // Indicate successful addition
        }
    }

    // Method to remove a specified order from the student's list
    public void removeOrder(Order randomOrder, int[][] drivingTimes) {
        Integer randomOrderID = randomOrder.getOrderID(); // Get the ID of the order to be removed
        this.assignedOrderIDs.remove(randomOrderID); // Remove the order ID from the list
    }

    // Getter method to retrieve the ID of the last order assigned to the student
    public int getLastAssignedOrderID() {
        return assignedOrderIDs.get(assignedOrderIDs.size() - 1);
    }

    // Getter method for the student's ID
    public int getId() {
        return id;
    }

    // Getter method for the student's total working time
    public int getTotalWorkingTime() {
        return totalWorkingTime;
    }

    // Setter method to update the student's total working time
    public void setTotalWorkingTime(int totalWorkingTime) {
        this.totalWorkingTime = totalWorkingTime;
    }

    // Getter method to retrieve the list of order IDs assigned to the student
    public List<Integer> getAssignedOrderIDs() {
        return assignedOrderIDs;
    }
    
}
