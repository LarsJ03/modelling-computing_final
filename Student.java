import java.util.ArrayList;
import java.util.List;

public class Student {
    private List<Integer> assignedJobs; 
    private double totalWorkingTime; 
    private double totalProfit;

    public Student() {
        this.assignedJobs = new ArrayList<>();
        this.totalWorkingTime = 0;
        this.totalProfit = 0;
    }

    public void assignJob(int jobNode, double jobTime, double jobProfit) {
        assignedJobs.add(jobNode);
        totalWorkingTime += jobTime;
        totalProfit += jobProfit;
    }

    public void stopWork() {
        assignedJobs.add(0);
        totalWorkingTime = 0;
    }

    public void removeJob(int jobNode, double jobTime, double jobProfit) {
        if(assignedJobs.contains(jobNode)) {
            assignedJobs.remove(Integer.valueOf(jobNode));
            totalWorkingTime -= jobTime; 
            totalProfit -= jobProfit; 
        }
    }

    public void removeAllJobs() {
        assignedJobs.clear(); // Clears the list of assigned jobs
        totalWorkingTime = 0; // Resets the total working time
        totalProfit = 0; // Resets the total profit
    }

    public List<Integer> getAssignedJobs() {
        return assignedJobs;
    }

    public double getTotalWorkingTime() {
        return totalWorkingTime;
    }

    public double getTotalProfit() {
        return totalProfit;
    }

    public int getLastJobNode(int[][] ordersData) {
        // Check if there are any jobs assigned
        if (!assignedJobs.isEmpty()) {
            // Return the node of the last job
            // Assuming the jobNode is stored or can be inferred from the last element of assignedJobs
            // For example, if assignedJobs stores job indices, and you need to fetch the node from those indices
            // You might need additional logic here to return the actual node ID based on your data structure
            return assignedJobs.get(assignedJobs.size() - 1); // Corrected to size() - 1
        } else {
            // Return a default value or handle the case where there are no jobs
            return 0; // Example default value, adjust as necessary
        }
    }

    
}