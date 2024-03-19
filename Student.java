import java.util.ArrayList;
import java.util.List;

public class Student {
    private int id; 
    private List<Integer> assignedJobs;
    private List<Double> timeRequired; 
    private double totalWorkingTime;
    private double totalProfit;

    public Student(int id) {
        this.id = id;
        this.assignedJobs = new ArrayList<>();
        this.timeRequired = new ArrayList<>(); 
        this.totalWorkingTime = 0;
        this.totalProfit = 0;
    }

    public int getId() {
        return id;
    }

    public void assignJob(int jobNode, double jobTime, double jobProfit) {
        assignedJobs.add(jobNode);
        timeRequired.add(jobTime); // Add jobTime to timeRequired list
        totalWorkingTime += jobTime;
        totalProfit += jobProfit;
    }

    public void removeJob(int jobNode, double jobTime, double jobProfit) {
        int index = assignedJobs.indexOf(jobNode);
        if(index != -1) {
            assignedJobs.remove(index);
            timeRequired.remove(index); // Remove the corresponding time
            totalWorkingTime -= jobTime; 
            totalProfit -= jobProfit; 
        }
    }

    public void removeAllJobs() {
        assignedJobs.clear();
        timeRequired.clear(); // Clear the timeRequired list
        totalWorkingTime = 0;
        totalProfit = 0;
    }

    public void stopWork() {
        assignedJobs.add(0); 
    }

    public void removeLongestJob() {
        if(timeRequired.isEmpty()) {
            return;
        }
        double longestTime = -1;
        int longestJobIndex = -1;
        for(int i = 0; i < timeRequired.size(); i++) {
            if(timeRequired.get(i) > longestTime) {
                longestTime = timeRequired.get(i);
                longestJobIndex = i;
            }
        }
        if(longestJobIndex != -1) {
            assignedJobs.remove(longestJobIndex);
            totalWorkingTime -= timeRequired.get(longestJobIndex);
            timeRequired.remove(longestJobIndex);
        }
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