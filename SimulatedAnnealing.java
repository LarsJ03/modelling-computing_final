import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;


public class SimulatedAnnealing {
    private static final int NUM_STUDENTS = 20;
    private static final Random random = new Random();

    public static void main(String[] args) throws FileNotFoundException {
        // Load driving times and orders as before
        int[][] drivingTimes = DataReader.readDrivingTimesFile();

        // Adjust DataReader to return the correct structure
        HashMap<Integer, Order> orders = DataReader.readOrdersFile(); 

        // Initialize students
        Student[] students = new Student[NUM_STUDENTS];
        for (int i = 0; i < NUM_STUDENTS; i++) {
            students[i] = new Student(i);
        }    

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


    public static Student[] simulatedAnnealing(Student[] students, HashMap<Integer, Order> orders, int[][] drivingTimes) {
        double temperature = 10; // Starting temperature
        double coolingRate = 0.000003; // Cooling rate
    
        ArrayList<Integer> notAssignedOrders = new ArrayList<>(orders.keySet());
    
        int bestProfit = 0;
        int newProfit;
    
        // Time tracking variables
        long totalTimeAddOrder = 0;
        long totalTimeRemoveOrder = 0;
        long totalTimeSwapOrders = 0;
        long totalTimeProfitCalculation = 0;
        long totalTimeAcceptance = 0;
    
        int countAddOrder = 0;
        int countRemoveOrder = 0;
        int countSwapOrders = 0;
        int counter = 0;
    
        while (temperature > 0.5) {
            double rand = Math.random();
    
            Student[] newStudents = AnnealingStrategies.deepCopyStudents(students);
            ArrayList<Integer> tempChanges = new ArrayList<>(notAssignedOrders);
    
            long startTime, endTime;
    
            // Dynamically calculate strategy probabilities based on current temperature
            double addProbability = 0.1;
            double removeProbability = 0.001;
    
            if (rand < addProbability) {
                startTime = System.currentTimeMillis();
                AnnealingStrategies.addOrder(newStudents, orders, drivingTimes, tempChanges);
                endTime = System.currentTimeMillis();
                totalTimeAddOrder += (endTime - startTime);
                countAddOrder++;
            } else if (rand < addProbability + removeProbability) {
                startTime = System.currentTimeMillis();
                AnnealingStrategies.removeRandomOrder(newStudents, orders, drivingTimes, tempChanges);
                endTime = System.currentTimeMillis();
                totalTimeRemoveOrder += (endTime - startTime);
                countRemoveOrder++;
            } else {
                startTime = System.currentTimeMillis();
                AnnealingStrategies.swapTwoOrders(newStudents, orders, drivingTimes, tempChanges);
                endTime = System.currentTimeMillis();
                totalTimeSwapOrders += (endTime - startTime);
                countSwapOrders++;
            }
    
            startTime = System.currentTimeMillis();
            int currentProfit = currentProfit(students, orders);
            newProfit = currentProfit(newStudents, orders);
            endTime = System.currentTimeMillis();
            totalTimeProfitCalculation += (endTime - startTime);
    
            startTime = System.currentTimeMillis();
            if (acceptanceProbability(currentProfit, newProfit, temperature) > Math.random()) {
                students = newStudents; // Accept new solution
                bestProfit = newProfit; // Update best profit
                notAssignedOrders = new ArrayList<>(tempChanges); // Apply changes
            }
            endTime = System.currentTimeMillis();
            totalTimeAcceptance += (endTime - startTime);
            if (counter % 10000 == 0) {
                System.out.println("Iteration: " + counter + ", Profit: " + bestProfit + ", Temperature: " + temperature);
            }

            temperature *= 1 - coolingRate;
            counter++;
        }
    
        // Print average times
        System.out.println("Average time for Add Order: " + (totalTimeAddOrder / (double) countAddOrder) + " ms.");
        System.out.println("Average time for Remove Order: " + (totalTimeRemoveOrder / (double) countRemoveOrder) + " ms.");
        System.out.println("Average time for Swap Orders: " + (totalTimeSwapOrders / (double) countSwapOrders) + " ms.");
        System.out.println("Average time for Profit Calculation: " + (totalTimeProfitCalculation / (double) counter) + " ms.");
        System.out.println("Average time for Acceptance Condition: " + (totalTimeAcceptance / (double) counter) + " ms.");
    
        System.out.println("Final not assigned orders: " + notAssignedOrders.size() + ". Total iterations: " + counter);
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
