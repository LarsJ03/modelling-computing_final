import java.util.ArrayList;

public class OrderAssignment {
    private Student[] updatedStudents;
    private ArrayList<Integer> updatedNotAssignedOrders;

    public OrderAssignment(Student[] updatedStudents, ArrayList<Integer> updatedNotAssignedOrders) {
        this.updatedStudents = updatedStudents;
        this.updatedNotAssignedOrders = updatedNotAssignedOrders;
    }

    public Student[] getUpdatedStudents() {
        return updatedStudents;
    }

    public ArrayList<Integer> getUpdatedNotAssignedOrders() {
        return updatedNotAssignedOrders;
    }
}