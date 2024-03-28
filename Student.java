import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;

public class Student {
    private int id;
    private List<Integer> assignedOrderIDs; 
    private int totalWorkingTime; 

    public Student(int id) {
        this.id = id;
        this.assignedOrderIDs = new ArrayList<>();
        this.totalWorkingTime = 0;
    }

    public Student(int id, List<Integer> assignedOrderIDs, int totalWorkingTime) {
        this.id = id;
        this.assignedOrderIDs = new ArrayList<>(assignedOrderIDs); 
        this.totalWorkingTime = totalWorkingTime;
    }

    public boolean addOrder(Order order, HashMap<Integer, Order> orders, int[][] drivingTimes) {
        int oldWorkingTime = this.getTotalWorkingTime();
        
        int randomIndex = new Random().nextInt(this.assignedOrderIDs.size() + 1);
        
        this.assignedOrderIDs.add(randomIndex, order.getOrderID());
        
        int workingTime = AnnealingStrategies.recalculateWorkingTime(this, orders, drivingTimes);
        
        if (workingTime > 28800) {
            this.assignedOrderIDs.remove(Integer.valueOf(order.getOrderID())); 
            this.setTotalWorkingTime(oldWorkingTime);
            return false;
        } else {
            return true; 
        }
    }

    public void removeOrder(Order randomOrder, int[][] drivingTimes) {
        Integer randomOrderID = randomOrder.getOrderID(); 
        this.assignedOrderIDs.remove(randomOrderID);
    }


    public int getLastAssignedOrderID() {
        return assignedOrderIDs.get(assignedOrderIDs.size() - 1);
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
    
}