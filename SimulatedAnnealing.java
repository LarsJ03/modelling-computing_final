import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimulatedAnnealing {
    private static final Random random = new Random();
    private static final double MAX_WORKING_SECONDS = 8 * 60 * 60;

    public static void main(String[] args) {
        long startTime, endTime, duration;
    
        String drivingTimesFilePath = "drivingtimes.txt";
        String ordersFilePath = "orders.txt";
        
        startTime = System.currentTimeMillis();
        int[][] drivingTimes = DataReader.readDrivingTimesFile(drivingTimesFilePath);
        endTime = System.currentTimeMillis();
        duration = endTime - startTime;
        System.out.println("Driving times loading time: " + duration + " ms");
    
        startTime = System.currentTimeMillis();
        int[][] ordersData = DataReader.readOrdersFile(ordersFilePath);
        endTime = System.currentTimeMillis();
        duration = endTime - startTime;
        System.out.println("Orders data loading time: " + duration + " ms");
    
        startTime = System.currentTimeMillis();
        Student[] students = initializeStudents(20, ordersData, drivingTimes);
        endTime = System.currentTimeMillis();
        duration = endTime - startTime;
        System.out.println("Initializing students time: " + duration + " ms");

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
        double coolingRate = 0.9999; // Example of geometric cooling
        int counter = 0;
    
        double[] initialResults = calcNetProfitTime(students, drivingTimes, ordersData);
        double currentProfit = initialResults[1];
        double bestProfit = currentProfit;
    
        while (temperature > 1) {
            Student[] newStudents;
            if (counter % 10 == 0) {
                newStudents = moveJobBetweenStudents(students, ordersData);
            } else {
                newStudents = changeOrderWithinStudents(students, ordersData);
            }
    
            double[] newResults = calcNetProfitTime(newStudents, drivingTimes, ordersData);
            double newProfit = newResults[1];
    
            if (acceptanceProbability(currentProfit, newProfit, temperature) > Math.random()) {
                students = newStudents;
                currentProfit = newProfit;
            }
    
            if (currentProfit > bestProfit) {
                bestProfit = currentProfit;
            }
    
            temperature *= coolingRate; // Adjusted cooling rate application
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
        
        if (!fromStudent.getAssignedJobs().isEmpty()) {
            int jobIndex = getRandomIndex(fromStudent.getAssignedJobs().size());
            int jobId = fromStudent.getAssignedJobs().get(jobIndex);
            double jobTime = ordersData[jobId][1] * 60;
            double jobProfit = ordersData[jobId][2];
            
            fromStudent.removeJob(jobId, jobTime, jobProfit); // Assume this method updates totalWorkingTime and totalProfit correctly
            
            if (toStudent.getTotalWorkingTime() + jobTime <= MAX_WORKING_SECONDS) {
                toStudent.assignJob(jobId, jobTime, jobProfit);
            } else {
                toStudent.assignJob(0, 0, 0); // Indicate return to headquarters
                toStudent.assignJob(jobId, jobTime, jobProfit); // Then assign the new job
            }
        }
    
        return students;
    }
    

    private static Student[] changeOrderWithinStudents(Student[] students, int[][] ordersData) {
    for (Student student : students) {
        List<Integer> jobs = new ArrayList<>(student.getAssignedJobs()); // Copy current jobs
        student.removeAllJobs(); // Clear current jobs
        
        Collections.shuffle(jobs); // Shuffle job order
        
        for (int jobId : jobs) {
            double jobTime = ordersData[jobId][1] * 60;
            double jobProfit = ordersData[jobId][2];
            
            if (student.getTotalWorkingTime() + jobTime <= MAX_WORKING_SECONDS) {
                student.assignJob(jobId, jobTime, jobProfit);
            } else {
                student.assignJob(0, 0, 0); 
            }
        }
    }

    return students;
}   

    public static double[] calcNetProfitTime(Student[] students, int[][] drivingTimes, int[][] ordersData) {
        double totalNetProfit = 0;
        double totalTimeSpent = 0;
        double hourlyCost = 60.0; 
    
        for (Student student : students) {
            double studentProfit = 0;
            double studentTime = 0;
            int currentNode = 251; 
    
            for (int jobIndex : student.getAssignedJobs()) {
                int nextNode = (int)ordersData[jobIndex][0];
                double driveTime = drivingTimes[currentNode][nextNode]; 
                double jobDuration = ordersData[jobIndex][1] * 60; 
                double jobProfit = ordersData[jobIndex][2];
    
                studentTime += driveTime + jobDuration;
                studentProfit += jobProfit;
                currentNode = nextNode; 
            }
    
            studentTime += drivingTimes[currentNode][251]; 
            
            double studentCost = (studentTime / 3600) * hourlyCost; 
            studentProfit -= studentCost; 
    
            totalTimeSpent += studentTime;
            totalNetProfit += studentProfit;
        }

        return new double[]{totalTimeSpent, totalNetProfit};
    }
    
    private static double acceptanceProbability(double currentProfit, double newProfit, double temperature) {
        if (newProfit > currentProfit) {
            return 1.0;
        }
        return Math.exp((newProfit - currentProfit) / temperature);
    }

    public static Student[] initializeStudents(int numStudents, int[][] ordersData, int[][] drivingTimes) {
        Student[] students = new Student[numStudents];
        for (int i = 0; i < numStudents; i++) {
            students[i] = new Student();
        }
    
        Random random = new Random();
    
        for (int jobIndex = 0; jobIndex < ordersData.length; jobIndex++) {
            int studentIndex = random.nextInt(numStudents);
            Student student = students[studentIndex];
    
            double jobDuration = ordersData[jobIndex][1] * 60; 
            double jobProfit = ordersData[jobIndex][2];
            int jobNode = ordersData[jobIndex][0]; 

            int lastJobNode = student.getLastJobNode(ordersData);
            double drivingTimeToJob = drivingTimes[lastJobNode][jobNode]; 
    
            double drivingTimeBack = drivingTimes[jobNode][251];
    
            double totalTimeForJob = drivingTimeToJob + jobDuration + drivingTimeBack;

            if (student.getTotalWorkingTime() + totalTimeForJob <= 8 * 60 * 60) {
                student.assignJob(ordersData[jobIndex][0], jobDuration, jobProfit);
            } else {
                student.stopWork();
            }
        }
    
        return students;
    }
    
}
