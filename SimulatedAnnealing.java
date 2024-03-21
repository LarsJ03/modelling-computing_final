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
        double temperature = 10000000; // Starting temperature
        double coolingRate = 0.000001; // Cooling rate
        int bestProfit = currentProfit(students, orders);
        System.out.println("start profit = " + bestProfit);

        int counter = 0;
        while (temperature > 1) {
            
            Student[] newStudents = students;
            // Create a new neighbor solution by slightly altering the current solution
            if (counter % 10 == 0) {
                newStudents = alterAssignments(students, orders, drivingTimes);
            } else if (counter % 5 == 0) {
                newStudents = removeAndReassignJobs(students, orders, drivingTimes);
            } else {
                newStudents = reshuffleOrdersOfAStudent(students, orders, drivingTimes);
            }
            
            int currentProfit = currentProfit(students, orders);
            int newProfit = currentProfit(newStudents, orders);
        
            // Calculate acceptance probability
            if (acceptanceProbability(currentProfit, newProfit, temperature) > Math.random()) {
                students = newStudents; // Accept new solution
                if (newProfit > bestProfit) {
                    System.out.println("New best profit: " + newProfit);
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
        double totalProfit = 0;
        double costPerSecond = 60.0 / (60 * 60); // Correctly calculate cost per second (€60/hour)
        for (Student student : students) {
            // Add profit from each order
            for (Integer orderID : student.getAssignedOrderIDs()) {
                Order order = orders.get(orderID);
                totalProfit += order.getProfit();
            }
            // Subtract cost of student's working time, doing it outside the inner loop
            totalProfit -= student.getTotalWorkingTime() * costPerSecond;
        }
    
        return (int) Math.round(totalProfit);
    }

    public static double acceptanceProbability(int currentProfit, int newProfit, double temperature) {
        if (newProfit > currentProfit) {
            return 1.0;
        }
        // Ensure the difference is appropriately scaled by the temperature
        return Math.exp((newProfit - currentProfit) / temperature);
    }

    public static Student[] alterAssignments(Student[] students, HashMap<Integer, Order> orders, int[][] drivingTimes) {
        Student[] newStudents = deepCopyStudents(students);

        // Randomly select a student with at least one order
        List<Student> studentsWithOrders = Arrays.stream(newStudents)
                                                .filter(s -> !s.getAssignedOrderIDs().isEmpty())
                                                .collect(Collectors.toList());
        if (!studentsWithOrders.isEmpty()) {
            Student studentFrom = studentsWithOrders.get(random.nextInt(studentsWithOrders.size()));
            int orderIndex = random.nextInt(studentFrom.getAssignedOrderIDs().size());
            Integer orderId = studentFrom.getAssignedOrderIDs().get(orderIndex);
            Order orderToReassign = orders.get(orderId);

            // Find a new student that is allowed to take this order and does not currently have it
            for (Student studentTo : newStudents) {
                if (orderToReassign.getAllowedStudents().contains(studentTo.getId()) && !studentTo.getAssignedOrderIDs().contains(orderId)) {
                    // Reassign the order
                    studentFrom.removeOrder(orderId, orders, drivingTimes); // Assume this method updates the student's assigned orders and total working time
                    studentTo.addOrder(orderId, orders, drivingTimes); // Assume this method does the same
                    
                    // Recalculate working times for both students involved
                    recalculateWorkingTime(studentFrom, orders, drivingTimes);
                    recalculateWorkingTime(studentTo, orders, drivingTimes);
                    
                    break; // Stop after reassigning to ensure only slight changes are made
                }
            }
        }

        return newStudents;
    } 

    public static Student[] reshuffleOrdersOfAStudent(Student[] students, HashMap<Integer, Order> orders, int[][] drivingTimes) {
        Student[] newStudents = deepCopyStudents(students);

        // Filter students with more than one order to ensure reshuffling has an effect
        List<Student> studentsWithMultipleOrders = Arrays.stream(newStudents)
            .filter(s -> s.getAssignedOrderIDs().size() > 1)
            .collect(Collectors.toList());

        if (!studentsWithMultipleOrders.isEmpty()) {
            // Select a random student with multiple orders
            Student selectedStudent = studentsWithMultipleOrders.get(random.nextInt(studentsWithMultipleOrders.size()));
            
            // Reshuffle their assigned orders
            Collections.shuffle(selectedStudent.getAssignedOrderIDs());
            
            // Recalculate the total working time for the selected student
            recalculateWorkingTime(selectedStudent, orders, drivingTimes);
        }

        return newStudents; // Return the modified array with potentially reshuffled orders
    }

    public static Student[] removeAndReassignJobs(Student[] students, HashMap<Integer, Order> orders, int[][] drivingTimes) {
        Student[] newStudents = deepCopyStudents(students);
    
        // Select a random student with at least one order for job removal
        List<Student> studentsWithOrders = Arrays.stream(newStudents)
                                                  .filter(s -> !s.getAssignedOrderIDs().isEmpty())
                                                  .collect(Collectors.toList());
        if (!studentsWithOrders.isEmpty()) {
            Student selectedStudent = studentsWithOrders.get(random.nextInt(studentsWithOrders.size()));
    
            // Randomly select a job to remove
            List<Integer> assignedOrders = new ArrayList<>(selectedStudent.getAssignedOrderIDs());
            if (!assignedOrders.isEmpty()) {
                Integer orderIdToRemove = assignedOrders.get(random.nextInt(assignedOrders.size()));
                selectedStudent.removeOrder(orderIdToRemove, orders, drivingTimes); // Remove the selected order
    
                // Attempt to reassign the removed job to a different student
                Order orderToReassign = orders.get(orderIdToRemove);
                for (Student studentTo : newStudents) {
                    // Check if this student can take the order and does not currently have it
                    if (orderToReassign.getAllowedStudents().contains(studentTo.getId()) &&
                        !studentTo.getAssignedOrderIDs().contains(orderIdToRemove)) {
                        studentTo.addOrder(orderIdToRemove, orders, drivingTimes);
                        
                        }
                    }
                }
    
                // Recalculate working times for both the student who lost the job and the one who may have gained it
                recalculateWorkingTime(selectedStudent, orders, drivingTimes);
                // Note: If the order was successfully reassigned, the recipient's working time is already recalculated in addOrder
            }
        
    
        return newStudents;
    }

    private static Student[] deepCopyStudents(Student[] students) {
        Student[] copiedStudents = new Student[students.length];
        for (int i = 0; i < students.length; i++) {
            copiedStudents[i] = new Student(students[i].getId(), students[i].getAssignedOrderIDs(), students[i].getTotalWorkingTime());
        }
        return copiedStudents;
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
