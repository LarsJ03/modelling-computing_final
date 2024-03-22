import java.util.Random;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;


public class SimulatedAnnealing {
    private static final int NUM_STUDENTS = 20;

    public static void main(String[] args) throws FileNotFoundException {
        // Load driving times and orders as before
        int[][] drivingTimes = DataReader.readDrivingTimesFile();

        // Adjust DataReader to return the correct structure
        HashMap<Integer, Order> orders = DataReader.readOrdersFile(); 

        // Initialize students
        Student[] students = initializeStudents(drivingTimes, orders);

        // Perform simulated annealing to optimize the assignments
        long startTime = System.currentTimeMillis(); // Start time

        Student[] final_students = simulatedAnnealing(students, orders, drivingTimes);

        long endTime = System.currentTimeMillis(); // End time
        long duration = endTime - startTime; // Calculate duration

        System.out.println("Simulated annealing completed in " + duration + " ms.");


        // Display the final best profit
        int finalProfit = currentProfit(final_students, orders);
        System.out.println("Final best profit: " + finalProfit);

        try {
            writeOutput(final_students, "output.txt");
        } catch (IOException e) {
            System.out.println("An error occurred while writing to the file: " + e.getMessage());
        }
    }

    public static Student[] initializeStudents(int[][] drivingTimes, HashMap<Integer, Order> orders) {
        Student[] students = new Student[NUM_STUDENTS];
        for (int i = 0; i < NUM_STUDENTS; i++) {
            students[i] = new Student(i);
        }

        for (Order order : orders.values()) {
            for (Integer studentID : order.getAllowedStudents()) {
                Student student = students[studentID];
                int driveTimeTo = drivingTimes[251][order.getNodeID()]; 
                int driveTimeBack = drivingTimes[order.getNodeID()][251]; 
                int totalDriveTime = driveTimeTo + driveTimeBack;
                
                if (student.addJob(order, totalDriveTime)) {
                    break; 
                }
            }
        }

        return students;
    }

    public static Student[] simulatedAnnealing(Student[] students, HashMap<Integer, Order> orders, int[][] drivingTimes) {
        double temperature = 1000000; // Starting temperature
        double coolingRate = 0.00001; // Cooling rate
        int bestProfit = currentProfit(students, orders);
        System.out.println("start profit = " + bestProfit);

        int counter = 0;
        while (temperature > 1) {
            
            Student[] newStudents = AnnealingStrategies.deepCopyStudents(students);
            // Create a new neighbor solution by slightly altering the current solution
            newStudents = AnnealingStrategies.swapTwoOrders(students, orders, drivingTimes);

            if (counter % 100 == 0) {
                newStudents = AnnealingStrategies.reassignOrder(students, orders, drivingTimes);
            }
            
            
            int currentProfit = currentProfit(students, orders);
            int newProfit = currentProfit(newStudents, orders);
       
            // Calculate acceptance probability
            if (acceptanceProbability(currentProfit, newProfit, temperature) > Math.random()) {
                students = newStudents; // Accept new solution
                if (newProfit > bestProfit) {
                    bestProfit = newProfit; // Update best profit
                }
            }
            if (counter % 100000 == 0) {
                System.out.println("Current profit: " + currentProfit + " | Best profit: " + bestProfit + " | Temperature: " + temperature + " | Counter: " + counter);
            }
            // Cool system
            temperature *= 1 - coolingRate;
            counter++; 
        }

        return students;
    
    }

    public static int currentProfit(Student[] students, HashMap<Integer, Order> orders) {
        double totalRevenue = 0;
        double totalCost = 0;
        double costPerSecond = 60.0 / (60 * 60); // Cost per second (€60/hour)
    
        // Calculate total revenue from all orders assigned to students
        for (Student student : students) {
            for (Integer orderID : student.getAssignedOrderIDs()) {
                Order order = orders.get(orderID);
                totalRevenue += order.getProfit(); // Add profit from each order to total revenue
            }
        }
    
        // Calculate total cost based on all students' working time
        for (Student student : students) {
            totalCost += student.getTotalWorkingTime() * costPerSecond; // Calculate and add cost for each student
        }
    
        double totalProfit = totalRevenue - totalCost; // Final profit is revenue minus cost
        return (int) Math.round(totalProfit); // Return rounded profit value
    }

    public static double acceptanceProbability(int currentProfit, int newProfit, double temperature) {
        if (newProfit > currentProfit) {
            return 1.0;
        }
        // Ensure the difference is appropriately scaled by the temperature
        return Math.exp((newProfit - currentProfit) / temperature);
    }

    private static void writeOutput(Student[] students, String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(filename, "UTF-8")) {
            int netProfit = currentProfit(students, DataReader.readOrdersFile());
            writer.println(netProfit);

            for (Student student : students) {
                // Write the number of customers (orders) served by the student
                List<Integer> orders = student.getAssignedOrderIDs();
                writer.println(orders.size());

                // Write the order IDs handled by the student
                for (int orderId : orders) {
                    writer.println(orderId);
                }
            }
        }
    }
}
