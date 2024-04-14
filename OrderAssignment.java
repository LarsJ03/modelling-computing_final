import java.util.ArrayList;

// Class to package updated students and unassigned orders together
public class OrderAssignment {
    private Student[] updatedStudents; // Array of updated students
    private ArrayList<Integer> updatedNotAssignedOrders; // List of orders not currently assigned

    // Constructor initializes the updated students and unassigned orders
    public OrderAssignment(Student[] updatedStudents, ArrayList<Integer> updatedNotAssignedOrders) {
        this.updatedStudents = updatedStudents;
        this.updatedNotAssignedOrders = updatedNotAssignedOrders;
    }

    // Returns the updated array of students
    public Student[] getUpdatedStudents() {
        return updatedStudents;
    }

    // Returns the list of unassigned order IDs
    public ArrayList<Integer> getUpdatedNotAssignedOrders() {
        return updatedNotAssignedOrders;
    }
}