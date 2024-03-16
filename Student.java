import java.util.ArrayList;
import java.util.List;

public class Student {
    private List<Integer> assignedJobs; // Stores indices of jobs assigned to this student
    private double totalWorkingTime; // Total time spent on jobs and driving
    private double totalProfit; // Total profit from jobs assigned to this student

    public Student() {
        this.assignedJobs = new ArrayList<>();
        this.totalWorkingTime = 0;
        this.totalProfit = 0;
    }

    // Assign a job to this student
    public void assignJob(int jobIndex, double jobTime, double jobProfit) {
        assignedJobs.add(jobIndex);
        totalWorkingTime += jobTime;
        totalProfit += jobProfit;
    }

    public void removeJob(int jobIndex, double jobTime, double jobProfit) {
        // Check if the job exists in the list
        if(assignedJobs.contains(jobIndex)) {
            assignedJobs.remove(Integer.valueOf(jobIndex)); // Remove the job by its Integer value (not index)
            totalWorkingTime -= jobTime; // Subtract the job's time from the total
            totalProfit -= jobProfit; // Subtract the job's profit from the total
        }
    }


    // Getters and Setters
    public List<Integer> getAssignedJobs() {
        return assignedJobs;
    }

    public double getTotalWorkingTime() {
        return totalWorkingTime;
    }

    public double getTotalProfit() {
        return totalProfit;
    }

    
}