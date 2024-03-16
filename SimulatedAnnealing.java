import java.util.Random;
import java.util.Collections;

public class SimulatedAnnealing {
    private static final Random random = new Random();

    public static void main(String[] args) {
        String drivingTimesFilePath = "drivingtimes.txt";
        String ordersFilePath = "orders.txt";
        
        int[][] drivingTimes = DataReader.readDrivingTimesFile(drivingTimesFilePath);
        double[][] ordersData = DataReader.readOrdersFile(ordersFilePath);

        Student[] students = initializeStudents(20, ordersData);
        simulateAnnealing(drivingTimes, ordersData, students); 
    }

    private static int getRandomIndex(int length) {
        return random.nextInt(length); 
    }

    public static void simulateAnnealing(int[][] drivingTimes, double[][] ordersData, Student[] students) {
        double temperature = 100000.0;
        double coolingRate = 0.0001;
        int counter = 0;
    
        double[] initialResults = calcNetProfitTime(students, drivingTimes, ordersData);
        double currentProfit = initialResults[1]; 
        double bestProfit = currentProfit; 
    
        Student[] bestStudents = students; 
    
        while (temperature > 1) {
            Student[] newStudents;
            if (counter % 2 == 0) {
                newStudents = moveJobBetweenStudents(students, ordersData);
            } else {
                newStudents = changeOrderWithinStudents(students);
            }
    
            double[] newResults = calcNetProfitTime(newStudents, drivingTimes, ordersData);
            double newProfit = newResults[1]; 
    
            if (acceptanceProbability(currentProfit, newProfit, temperature) > Math.random()) {
                students = newStudents;
                currentProfit = newProfit; 
            }
    
            if (newProfit > bestProfit) {
                bestStudents = newStudents; 
                bestProfit = newProfit; 
            }
    
            temperature *= 1 - coolingRate;
            counter++;
        }
    
        System.out.println("Simulation complete. Best profit: " + bestProfit);
    }

    private static Student[] moveJobBetweenStudents(Student[] students, double[][] ordersData) {
        int fromStudentIndex = getRandomIndex(students.length);
        int toStudentIndex = getRandomIndex(students.length);
        while (toStudentIndex == fromStudentIndex) {
            toStudentIndex = getRandomIndex(students.length);
        }
        
        Student fromStudent = students[fromStudentIndex];
        if (!fromStudent.getAssignedJobs().isEmpty()) {
            int jobIndex = fromStudent.getAssignedJobs().get(0); 
            double jobTime = ordersData[jobIndex][1] * 60;
            double jobProfit = ordersData[jobIndex][2];
            
            fromStudent.removeJob(jobIndex, jobTime, jobProfit);
            students[toStudentIndex].assignJob(jobIndex, jobTime, jobProfit);
        }

        return students;
    }

    private static Student[] changeOrderWithinStudents(Student[] students) {
        for (Student student : students) {
            Collections.shuffle(student.getAssignedJobs());
        }

        return students;
    }    

    public static double[] calcNetProfitTime(Student[] students, int[][] drivingTimes, double[][] ordersData) {
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

    public static Student[] initializeStudents(int numStudents, double[][] ordersData) {
        Student[] students = new Student[numStudents];
        for (int i = 0; i < numStudents; i++) {
            students[i] = new Student();
        }
    
        int totalJobs = ordersData.length;
        Random random = new Random();
    
        for (int jobIndex = 0; jobIndex < totalJobs; jobIndex++) {
            int studentIndex = random.nextInt(numStudents);
            double jobTime = ordersData[jobIndex][1] * 60; 
            double jobProfit = ordersData[jobIndex][2];
            
            students[studentIndex].assignJob(jobIndex, jobTime, jobProfit);
        }
    
        return students;
    }    
}
