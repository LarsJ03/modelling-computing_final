import java.util.HashMap;
import java.util.List;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

public class SimulatedAnnealing {
    private static final int NUM_STUDENTS = 20; // Constant for the number of students
    private static final Random random = new Random();

    public static void main(String[] args) throws FileNotFoundException {
        // Load driving times and orders using custom DataReader class
        int[][] drivingTimes = DataReader.readDrivingTimesFile();
        HashMap<Integer, Order> orders = DataReader.readOrdersFile();

        // Initialize students array with specified number of student objects
        Student[] students = new Student[NUM_STUDENTS];
        for (int i = 0; i < NUM_STUDENTS; i++) {
            students[i] = new Student(i);
        }

        // Measure start time for performance tracking
        long startTime = System.currentTimeMillis();

        // Perform the simulated annealing process
        Student[] final_students = simulatedAnnealing(students, orders, drivingTimes);

        // Measure end time and calculate total duration
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        System.out.println("Simulated annealing completed in " + duration + " ms.");

        // Calculate and display the final profit
        int finalProfit = currentProfit(final_students, orders);
        System.out.println("Final best profit: " + finalProfit);

        // Write results to output file
        try {
            writeOutput(final_students, "output.txt");
        } catch (IOException e) {
            System.out.println("An error occurred while writing to the file: " + e.getMessage());
        }
    }

    public static Student[] simulatedAnnealing(Student[] students, HashMap<Integer, Order> orders, int[][] drivingTimes) {
        double temperature = 7; // Starting temperature for annealing
        double coolingRate = 0.999995; // Rate at which temperature decreases exponentially
    
        ArrayList<Integer> notAssignedOrders = new ArrayList<>(orders.keySet()); // Orders not assigned to any student
        Student[] bestStudents = AnnealingStrategies.deepCopyStudents(students); // Best solution found so far
    
        int bestProfit = 0; // Track the best profit found
        int lastProfit = 0; // Track profit from earlier to determine stagnation
        int newProfit; // Track new profit calculations
    
        // Performance metrics initialization
        long totalTimeAddOrder = 0;
        long totalTimeRemoveOrder = 0;
        long totalTimeSwapOrders = 0;
        long totalTimeProfitCalculation = 0;
        long totalTimeAcceptance = 0;
    
        // Counters for the number of operations performed
        int countAddOrder = 0;
        int countRemoveOrder = 0;
        int countSwapOrders = 0;
        int counter = 0; // Overall counter for iterations
        int profitCheckInterval = 10000; // Interval to check for profit improvement
    
        double addProbability = 0.40; // Probability of adding an order
        double removeProbability = 0.15; // Probability of removing an order
    
        while (temperature > 0.01) { // Continue until the system has cooled
            double rand = Math.random(); // Random number for decision making
    
            Student[] newStudents = AnnealingStrategies.deepCopyStudents(students); // Create a copy of current solution
            ArrayList<Integer> tempChanges = new ArrayList<>(notAssignedOrders); // Temporary changes
    
            long startTime, endTime; // For timing operations
    
            // Choose operation based on probability
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
    
            // Profit calculation for decision making
            startTime = System.currentTimeMillis();
            int currentProfit = currentProfit(students, orders);
            newProfit = currentProfit(newStudents, orders);
            endTime = System.currentTimeMillis();
            totalTimeProfitCalculation += (endTime - startTime);
    
            // Decide whether to accept the new solution
            startTime = System.currentTimeMillis();
            if (acceptanceProbability(currentProfit, newProfit, temperature) > Math.random()) {
                students = newStudents; // Accept new solution
                if (newProfit > bestProfit) {
                    bestProfit = newProfit; // Update best profit
                    bestStudents = AnnealingStrategies.deepCopyStudents(newStudents); // Update best solution
                }
                notAssignedOrders = new ArrayList<>(tempChanges); // Update order list
            }
            endTime = System.currentTimeMillis();
            totalTimeAcceptance += (endTime - startTime);

            if (counter % 10000 == 0 && counter != 0){
                System.out.println("Iteration: " + counter + ", Profit: " + bestProfit + ", Temperature: " + temperature);
            }

            if (counter % profitCheckInterval == 0 && counter != 0 && counter < 3000000) {
                // Check if profit improvement is minimal over a large number of iterations and temperature is low
                if (bestProfit - lastProfit < 1 && temperature < 0.5) {
                    temperature += random.nextGaussian() * Math.sqrt(2) + 2; // Bump up temperature
                }
                lastProfit = bestProfit; // Update last checked profit
            }

            // Dynamic temperature adjustment using linear cooling
            // if (temperature < 1.0) {
            //     temperature -= 0.000002; // Slow cooling when the temperature is below 1.0
            // } else if (temperature < 0.3) {
            //     temperature -= 0.000001; // Slower cooling when the temperature is below 0.3
            // } else {
            //     temperature -= 0.00001; // Standard cooling rate
            // }

            // Check if profit improvement is minimal over a large number of iterations and temperature is low
            // if (bestProfit - lastProfit < 1 && temperature < 0.5) {
            //     temperature += random.nextGaussian() * Math.sqrt(2) + 2; // Bump up temperature
            // }

            
    
            temperature *= coolingRate; // Cooling down
            counter++;
        }
    
        // Output average times for each operation
        System.out.println("Average time for Add Order: " + (totalTimeAddOrder / (double) countAddOrder) + " ms.");
        System.out.println("Average time for Remove Order: " + (totalTimeRemoveOrder / (double) countRemoveOrder) + " ms.");
        System.out.println("Average time for Swap Orders: " + (totalTimeSwapOrders / (double) countSwapOrders) + " ms.");
        System.out.println("Average time for Profit Calculation: " + (totalTimeProfitCalculation / (double) counter) + " ms.");
        System.out.println("Average time for Acceptance Condition: " + (totalTimeAcceptance / (double) counter) + " ms.");
    
        System.out.println("Final not assigned orders: " + notAssignedOrders.size() + ". Total iterations: " + counter);
        return bestStudents;
    }
    


    

    // Calculate the profit from the assigned orders
    public static int currentProfit(Student[] students, HashMap<Integer, Order> orders) {
        double totalRevenue = 0;
        double totalCost = 0;
        double costPerSecond = 60.0 / (60 * 60); // Cost per second calculation

        // Calculate total revenue and cost for each student
        for (Student student : students) {
            for (Integer orderID : student.getAssignedOrderIDs()) {
                Order order = orders.get(orderID);
                totalRevenue += order.getProfit(); // Sum up profit from each order
            }
        }
        for (Student student : students) {
            totalCost += student.getTotalWorkingTime() * costPerSecond; // Compute cost based on working time
        }

        double totalProfit = totalRevenue - totalCost; // Calculate net profit
        return (int) Math.round(totalProfit);
    }

    // Calculate the probability of accepting a new solution based on profit and temperature
    public static double acceptanceProbability(int currentProfit, int newProfit, double temperature) {
        if (newProfit > currentProfit) {
            return 1.0; // Always accept better profit
        }
        return Math.exp((newProfit - currentProfit) / temperature); // Calculate probability based on exponential function
    }

    // Method to write the output to a file
    private static void writeOutput(Student[] students, String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(filename, "UTF-8")) {
            int netProfit = currentProfit(students, DataReader.readOrdersFile());
            writer.println(netProfit + 1); // Output the net profit

            for (Student student : students) {
                List<Integer> orders = student.getAssignedOrderIDs();
                writer.println(orders.size()); // Write number of orders for each student
                for (int orderId : orders) {
                    writer.println(orderId); // Write each order ID
                }
            }
        }
    }
}
