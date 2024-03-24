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
        double temperature = 100; // Starting temperature
        double coolingRate = 0.00001; // Cooling rate
        double startTemperature = temperature; // Save start temperature for probability calculations
    
        ArrayList<Integer> notAssignedOrders = new ArrayList<>(orders.keySet());
        ArrayList<Integer> changes = new ArrayList<>();
    
        int bestProfit = 0;
        int currentProfit = 0;
        int newProfit = 0;
    
        int counter = 0;
        while (temperature > 0.5) {
            // Dynamically calculate strategy probabilities based on current temperature
            double addProbability = 0.1;
            double removeProbability = 0.1; 
    
            double rand = Math.random();
            String strategyUsed = "";
            AnnealingStrategies.checkForOverlap(students, notAssignedOrders, "Before new iteration order");
    
            Student[] newStudents = AnnealingStrategies.deepCopyStudents(students);
            ArrayList<Integer> tempChanges = new ArrayList<>(notAssignedOrders); // Temporary changes holder
            OrderAssignment orderAssignmentResult;

            // Assume other parts of the method are above this
            if (rand < addProbability) {
                strategyUsed = "Add Order";
                orderAssignmentResult = AnnealingStrategies.addOrder(newStudents, orders, drivingTimes, tempChanges);
                AnnealingStrategies.checkForOverlap(orderAssignmentResult.getUpdatedStudents(), orderAssignmentResult.getUpdatedNotAssignedOrders(), "after add");
            } else if (rand < addProbability + removeProbability) {
                strategyUsed = "Remove Order";
                orderAssignmentResult = AnnealingStrategies.removeRandomOrder(newStudents, orders, drivingTimes, tempChanges);
                AnnealingStrategies.checkForOverlap(orderAssignmentResult.getUpdatedStudents(), orderAssignmentResult.getUpdatedNotAssignedOrders(), "after remove");
            } else {
                strategyUsed = "Swap Orders";
                orderAssignmentResult = AnnealingStrategies.swapTwoOrders(newStudents, orders, drivingTimes, tempChanges);
                AnnealingStrategies.checkForOverlap(orderAssignmentResult.getUpdatedStudents(), orderAssignmentResult.getUpdatedNotAssignedOrders(), "After swap");
            }

            // Now, unpack the OrderAssignmentResult to get the updated students and notAssignedOrders
            newStudents = orderAssignmentResult.getUpdatedStudents();
            tempChanges = orderAssignmentResult.getUpdatedNotAssignedOrders();

            AnnealingStrategies.checkForOverlap(newStudents, tempChanges, "After operation");
    
            currentProfit = currentProfit(students, orders);
            newProfit = currentProfit(newStudents, orders);

            AnnealingStrategies.checkForOverlap(newStudents, tempChanges, "after profit calculation");

            if (newProfit > bestProfit) {
                students = newStudents;
                bestProfit = newProfit; // Update best profit
                notAssignedOrders = new ArrayList<>(tempChanges); // Apply changes if this is a new best
            }
    
            // Calculate acceptance probability
            if (acceptanceProbability(currentProfit, newProfit, temperature) > Math.random()) {
                students = newStudents; // Accept new solution
                bestProfit = newProfit; // Update best profit
                notAssignedOrders = new ArrayList<>(tempChanges); // Apply changes if this is a new best

            }

            AnnealingStrategies.checkForOverlap(newStudents, tempChanges, "after accept");
    
            if (counter % 1000 == 0) {
                System.out.println("Current profit: " + currentProfit + " | Best profit: " + bestProfit + " | Temperature: " + temperature + " | Counter: " + counter);
                System.out.println("Not assigned orders: " + notAssignedOrders.size());
            }

            

            // Debugging code to detect duplicate orders
            Set<Integer> assignedOrdersDebug = new HashSet<>();
            boolean duplicatesDetected = false;
            for (Student student : newStudents) {
                for (Integer orderId : student.getAssignedOrderIDs()) {
                    if (!assignedOrdersDebug.add(orderId)) {
                        duplicatesDetected = true;
                        break;
                    }
                }
            }

            if (duplicatesDetected && strategyUsed.equals("Swap Orders")) {
                System.out.println("Duplicate order assignments detected at counter " + counter + " using strategy " + strategyUsed);
                // Optionally, print more information about the duplicates or the state of assignments
                break;
            }

            AnnealingStrategies.checkForOverlap(students, notAssignedOrders, "end iteration");

            // Cool system
            temperature *= 1 - coolingRate;
            counter++;
        }
    
        System.out.println("Final not assigned orders: " + notAssignedOrders.size());
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
