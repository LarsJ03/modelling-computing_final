import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;


public class SimulatedAnnealing {
    private static final Random random = new Random();
    private static final int NUM_STUDENTS = 20;

    public static void main(String[] args) throws FileNotFoundException {
        // Load driving times and orders as before
        int[][] drivingTimes = DataReader.readDrivingTimesFile();

        // Adjust DataReader to return the correct structure
        HashMap<Integer, Order> orders = DataReader.readOrdersFile(); 

        // Initialize students
        Student[] students = initializeStudents(drivingTimes, orders);

        // Perform simulated annealing to optimize the assignments
        simulatedAnnealing(students, orders, drivingTimes);

        // Display the final best profit
        int finalProfit = currentProfit(students, orders);
        System.out.println("Final best profit: " + finalProfit);

        try {
            writeOutput(students, "output.txt");
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

    public static void simulatedAnnealing(Student[] students, HashMap<Integer, Order> orders, int[][] drivingTimes) {
        double temperature = 10000; // Starting temperature
        double coolingRate = 0.003; // Cooling rate
        int bestProfit = currentProfit(students, orders);
        System.out.println("start profit = " + bestProfit);

        int counter = 0;
        while (temperature > 1) {
            // Create a new neighbor solution by slightly altering the current solution
            Student[] newStudents = alterAssignments(students, orders, drivingTimes);
    
            int currentProfit = currentProfit(students, orders);
            int newProfit = currentProfit(newStudents, orders);

            // Calculate acceptance probability
            if (acceptanceProbability(currentProfit, newProfit, temperature) > Math.random()) {
                students = newStudents; // Accept new solution
                if (newProfit > bestProfit) {
                    bestProfit = newProfit; // Update best profit
                }
            }
            if (counter % 100 == 0) {
                System.out.println("Current profit: " + currentProfit);
            }
            // Cool system
            temperature *= 1 - coolingRate;
            counter++; 
        }
    
        System.out.println("Final best profit: " + bestProfit);
    }

    public static int currentProfit(Student[] students, HashMap<Integer, Order> orders) {
        int totalProfit = 0;
        int costPerSecond = 1 / 60; // Assuming cost is 1 unit per second for simplicity.
    
        for (Student student : students) {
            for (Integer orderID : student.getAssignedOrderIDs()) {
                Order order = orders.get(orderID);
                totalProfit += order.getProfit(); // Add profit from each order
            }
            // Subtract cost of student's working time
            totalProfit -= student.getTotalWorkingTime() * costPerSecond;
        }
    
        return totalProfit;
    }

    public static double acceptanceProbability(int currentProfit, int newProfit, double temperature) {
        if (newProfit > currentProfit) {
            return 1.0;
        }
        // Ensure the difference is appropriately scaled by the temperature
        return Math.exp((newProfit - currentProfit) / temperature);
    }

    public static Student[] alterAssignments(Student[] students, HashMap<Integer, Order> orders, int[][] drivingTimes) {
    System.out.println("started");
    Random rand = new Random();
    // Deep copy the students array to avoid altering the original assignments
    Student[] newStudents = new Student[students.length];
    for (int i = 0; i < students.length; i++) {
        // Clone each student to preserve the integrity of the original assignments
        newStudents[i] = new Student(students[i].getId(), new ArrayList<>(students[i].getAssignedOrderIDs()), students[i].getTotalWorkingTime());
    }

    // Select two random students
    int studentIndex1 = rand.nextInt(newStudents.length);
    int studentIndex2 = rand.nextInt(newStudents.length);
    while (studentIndex1 == studentIndex2) { // Ensure they are not the same
        studentIndex2 = rand.nextInt(newStudents.length);
    }

    Student student1 = newStudents[studentIndex1];
    Student student2 = newStudents[studentIndex2];

    // Ensure both students have orders to swap
    if (!student1.getAssignedOrderIDs().isEmpty() && !student2.getAssignedOrderIDs().isEmpty()) {
        // Randomly select orders to swap
        int orderIndex1 = rand.nextInt(student1.getAssignedOrderIDs().size());
        int orderIndex2 = rand.nextInt(student2.getAssignedOrderIDs().size());
        int orderID1 = student1.getAssignedOrderIDs().get(orderIndex1);
        int orderID2 = student2.getAssignedOrderIDs().get(orderIndex2);

        // Check if swap is allowed
        if (orders.get(orderID1).getAllowedStudents().contains(student2.getId()) &&
            orders.get(orderID2).getAllowedStudents().contains(student1.getId())) {
            System.out.println("swapping");
            // Perform swap
            student1.getAssignedOrderIDs().set(orderIndex1, orderID2);
            student2.getAssignedOrderIDs().set(orderIndex2, orderID1);

            // Recalculate total working time for both students
            recalculateWorkingTime(student1, orders, drivingTimes);
            recalculateWorkingTime(student2, orders, drivingTimes);
        }
    }

    return newStudents;
}

    private static void recalculateWorkingTime(Student student, HashMap<Integer, Order> orders, int[][] drivingTimes) {
        int totalWorkingTime = 0;
        for (int orderID : student.getAssignedOrderIDs()) {
            Order order = orders.get(orderID);
            int driveTimeTo = drivingTimes[251][order.getNodeID()];
            int driveTimeBack = drivingTimes[order.getNodeID()][251];
            totalWorkingTime += order.getDuration() + driveTimeTo + driveTimeBack;
        }
        student.setTotalWorkingTime(totalWorkingTime);
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
