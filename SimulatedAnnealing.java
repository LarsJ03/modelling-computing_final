import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The SimulatedAnnealing class implements a simulated annealing algorithm
 * to solve an optimization problem involving assigning jobs to students while
 * maximizing profit and ensuring that no student works more than 8 hours.
 * It uses an annealing schedule to gradually decrease the temperature, 
 * allowing it to explore the solution space and make increasingly selective choices.
 * The algorithm aims to find a near-optimal distribution of jobs among students,
 * considering job durations, profits, and driving times between job locations.
 */
public class SimulatedAnnealing {
    private static final Random random = new Random();
    private static final double MAX_WORKING_SECONDS = 8 * 60 * 60;

    public static void main(String[] args) {
        // Initialize start time and file paths for input data
        long startTime, endTime, duration;
    
        String drivingTimesFilePath = "drivingtimes.txt";
        String ordersFilePath = "orders.txt";
        
        // Load driving times from file and measure the time taken
        startTime = System.currentTimeMillis();
        int[][] drivingTimes = DataReader.readDrivingTimesFile(drivingTimesFilePath);
        endTime = System.currentTimeMillis();
        duration = endTime - startTime;
        System.out.println("Driving times loading time: " + duration + " ms");
    
        // Load orders data from file and measure the time taken
        startTime = System.currentTimeMillis();
        int[][] ordersData = DataReader.readOrdersFile(ordersFilePath);
        endTime = System.currentTimeMillis();
        duration = endTime - startTime;
        System.out.println("Orders data loading time: " + duration + " ms");
    
        // Initialize students with the orders data and driving times, and measure the time taken
        startTime = System.currentTimeMillis();
        Student[] students = initializeStudents(20, ordersData, drivingTimes);
        endTime = System.currentTimeMillis();
        duration = endTime - startTime;
        System.out.println("Initializing students time: " + duration + " ms");

        // Perform the simulated annealing process to optimize the job assignments and measure the time taken
        startTime = System.currentTimeMillis();
        simulateAnnealing(drivingTimes, ordersData, students);
        endTime = System.currentTimeMillis();
        duration = endTime - startTime;
        System.out.println("Simulated annealing process time: " + duration + " ms");
    }

    

    private static int getRandomIndex(int length) {
        return random.nextInt(length); 
    }

    public static Student[] simulateAnnealing(int[][] drivingTimes, int[][] ordersData, Student[] students) {
        double temperature = 10000000.0;
        double coolingRate = 0.9999; // Determines how fast the temperature decreases.
        int counter = 0;
    
        // Calculate the initial profit and time spent for the current assignment of jobs to students.
        double[] initialResults = calcNetProfitTime(students, drivingTimes, ordersData);
        double currentProfit = initialResults[1];
        double bestProfit = currentProfit;
    
        // The main loop of simulated annealing.
        while (temperature > 1) {
            Student[] newStudents;
            // Every 10th iteration, move a job between students; otherwise, reshuffle jobs within students.
            if (counter % 10 == 0) {
                newStudents = moveJobBetweenStudents(students, ordersData);
            } else {
                newStudents = changeOrderWithinStudents(students, ordersData);
            }
    
            // Calculate the new profit after the move or reshuffle.
            double[] newResults = calcNetProfitTime(newStudents, drivingTimes, ordersData);
            double newProfit = newResults[1];
    
            // Determine whether to accept the new assignment based on the acceptance probability.
            if (acceptanceProbability(currentProfit, newProfit, temperature) > Math.random()) {
                students = newStudents;
                currentProfit = newProfit;
            }
    
            // Update the best profit found so far.
            if (currentProfit > bestProfit) {
                bestProfit = currentProfit;
            }
    
            // Decrease the temperature and increment the counter.
            temperature *= coolingRate;
            counter++;
        }
    
        System.out.println("Simulation complete. Best profit: " + bestProfit);
    
        return students;
    }

    private static Student[] moveJobBetweenStudents(Student[] students, int[][] ordersData) {
        int fromStudentIndex = getRandomIndex(students.length);
        int toStudentIndex;
        do {
            toStudentIndex = getRandomIndex(students.length);
        } while (toStudentIndex == fromStudentIndex);
        
        Student fromStudent = students[fromStudentIndex];
        Student toStudent = students[toStudentIndex];
        
        // Only proceed if the fromStudent has jobs assigned.
        if (!fromStudent.getAssignedJobs().isEmpty()) {
            int jobIndex = getRandomIndex(fromStudent.getAssignedJobs().size());
            int jobId = fromStudent.getAssignedJobs().get(jobIndex);
            double jobTime = ordersData[jobId][1] * 60;
            double jobProfit = ordersData[jobId][2];
            
            // Remove the job from the fromStudent.
            fromStudent.removeJob(jobId, jobTime, jobProfit);
            
            // Check if adding the job to toStudent would exceed the max working hours.
            if (toStudent.getTotalWorkingTime() + jobTime <= MAX_WORKING_SECONDS) {
                toStudent.assignJob(jobId, jobTime, jobProfit);
            } else {
                // If it exceeds, simulate the student returning to headquarters before assigning the job.
                toStudent.assignJob(0, 0, 0);
                toStudent.assignJob(jobId, jobTime, jobProfit);
            }
        }
    
        return students;
    }
    

    private static Student[] changeOrderWithinStudents(Student[] students, int[][] ordersData) {
        for (Student student : students) {
            // Make a copy of the current jobs assigned to the student
            List<Integer> jobs = new ArrayList<>(student.getAssignedJobs()); 
            // Remove all current job assignments to reset the student's working hours and profit
            student.removeAllJobs(); 
            
            // Randomly shuffle the list of jobs to create a new order
            Collections.shuffle(jobs);
            
            // Reassign jobs in the new order, respecting the maximum working hours constraint
            for (int jobId : jobs) {
                double jobTime = ordersData[jobId][1] * 60; // Convert job duration to seconds
                double jobProfit = ordersData[jobId][2];
                
                // Assign the job if it does not lead to exceeding the maximum working hours
                if (student.getTotalWorkingTime() + jobTime <= MAX_WORKING_SECONDS) {
                    student.assignJob(jobId, jobTime, jobProfit);
                } else {
                    // Indicate the student must return to headquarters if max working hours are reached
                    student.assignJob(0, 0, 0); 
                }
            }
        }
    
        return students;
    }   

    public static double[] calcNetProfitTime(Student[] students, int[][] drivingTimes, int[][] ordersData) {
        double totalNetProfit = 0;
        double totalTimeSpent = 0;
        double hourlyCost = 60.0; // Hourly cost for a student's work
        
        for (Student student : students) {
            double studentProfit = 0;
            double studentTime = 0;
            int currentNode = 251; // Assuming 251 is the headquarters node
        
            // Calculate the total time and profit for each student
            for (int jobIndex : student.getAssignedJobs()) {
                int nextNode = ordersData[jobIndex][0];
                double driveTime = drivingTimes[currentNode][nextNode];
                double jobDuration = ordersData[jobIndex][1] * 60; 
                double jobProfit = ordersData[jobIndex][2];
        
                studentTime += driveTime + jobDuration;
                studentProfit += jobProfit;
                currentNode = nextNode; 
            }
        
            // Include the drive back to headquarters in the total time
            studentTime += drivingTimes[currentNode][251];
            
            // Calculate and subtract the cost of the student's time from their profit
            double studentCost = (studentTime / 3600) * hourlyCost;
            studentProfit -= studentCost;
        
            totalTimeSpent += studentTime;
            totalNetProfit += studentProfit;
        }
    
        return new double[]{totalTimeSpent, totalNetProfit};
    }
    
    private static double acceptanceProbability(double currentProfit, double newProfit, double temperature) {
        // Always accept the new solution if it's better than the current one
        if (newProfit > currentProfit) {
            return 1.0;
        }
        // Calculate the acceptance probability for a worse solution based on the temperature
        return Math.exp((newProfit - currentProfit) / temperature);
    }

    public static Student[] initializeStudents(int numStudents, int[][] ordersData, int[][] drivingTimes) {
        Student[] students = new Student[numStudents];
        for (int i = 0; i < numStudents; i++) {
            students[i] = new Student();
        }
        
        Random random = new Random();
        
        // Assign jobs to students, respecting the maximum working hours
        for (int jobIndex = 0; jobIndex < ordersData.length; jobIndex++) {
            int studentIndex = random.nextInt(numStudents);
            Student student = students[studentIndex];
        
            double jobDuration = ordersData[jobIndex][1] * 60;
            double jobProfit = ordersData[jobIndex][2];
            int jobNode = ordersData[jobIndex][0];
    
            // Calculate driving times to and from the job
            int lastJobNode = student.getLastJobNode(ordersData);
            double drivingTimeToJob = drivingTimes[lastJobNode][jobNode];
            double drivingTimeBack = drivingTimes[jobNode][251];
        
            double totalTimeForJob = drivingTimeToJob + jobDuration + drivingTimeBack;
    
            // Assign the job if it doesn't lead to exceeding the maximum working hours
            if (student.getTotalWorkingTime() + totalTimeForJob <= 8 * 60 * 60) {
                student.assignJob(ordersData[jobIndex][0], jobDuration, jobProfit);
            } else {
                // Indicate the student must return to headquarters if they cannot take on more work
                student.stopWork();
            }
        }
        
        return students;
    }
    
}
