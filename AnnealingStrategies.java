import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class AnnealingStrategies {
    private static final Random random = new Random();

    public static OrderAssignment swapTwoOrders(Student[] students, HashMap<Integer, Order> orders, int[][] drivingTimes, ArrayList<Integer> notAssignedOrders) {
        checkForOverlap(students, notAssignedOrders, "Before swapping order");
        Student[] newStudents = deepCopyStudents(students);
        // Filter students with more than one order to ensure swapping has an effect
        List<Student> studentsWithMultipleOrders = Arrays.stream(newStudents)
            .filter(s -> s.getAssignedOrderIDs().size() > 1)
            .collect(Collectors.toList());
    
        if (!studentsWithMultipleOrders.isEmpty()) {
            // Select a random student with multiple orders
            Student selectedStudent = studentsWithMultipleOrders.get(random.nextInt(studentsWithMultipleOrders.size()));
            
            // Get two random indices to swap
            int size = selectedStudent.getAssignedOrderIDs().size();
            int index1 = random.nextInt(size);
            int index2 = index1;
            // Ensure index2 is different from index1
            while (index2 == index1) {
                index2 = random.nextInt(size);
            }
            // Perform the swap
            Integer temp = selectedStudent.getAssignedOrderIDs().get(index1);
            selectedStudent.getAssignedOrderIDs().set(index1, selectedStudent.getAssignedOrderIDs().get(index2));
            selectedStudent.getAssignedOrderIDs().set(index2, temp);
            
            // Recalculate the total working time for the selected student
            recalculateWorkingTime(selectedStudent, orders, drivingTimes);
        }
    
        // Update the original students array with the modified student
        for (int i = 0; i < students.length; i++) {
            if (students[i].getId() == newStudents[i].getId()) {
                students[i] = newStudents[i];
            }
        }
        checkForOverlap(students, notAssignedOrders, "After swapping order");
        return new OrderAssignment(students, notAssignedOrders); // Return the original array with the student updated
    }

    public static OrderAssignment removeRandomOrder(Student[] students, HashMap<Integer, Order> orders, int[][] drivingTimes, ArrayList<Integer> notAssignedOrders) {
        if (students.length == 0) {
            return new OrderAssignment(students, notAssignedOrders);
        }

        checkForOverlap(students, notAssignedOrders, "Before removing order");
        
        int randomStudentIndex = random.nextInt(students.length);
        Student randomStudent = students[randomStudentIndex];
        if (!randomStudent.getAssignedOrderIDs().isEmpty()) {
            int randomOrderIndex = random.nextInt(randomStudent.getAssignedOrderIDs().size());
            Integer randomOrderID = randomStudent.getAssignedOrderIDs().get(randomOrderIndex); // Use Integer to get the order ID
            Order randomOrder = orders.get(randomOrderID);
            if (randomOrder != null) {
                randomStudent.removeOrder(randomOrder, drivingTimes); // Assuming your removeOrder method is corrected as per previous suggestions
                notAssignedOrders.add(Integer.valueOf(randomOrderID)); // Correctly remove by object
            } 
        }
        checkForOverlap(students, notAssignedOrders, "after removing order");

        return new OrderAssignment(students, notAssignedOrders);
    }
    
    public static OrderAssignment addOrder(Student[] students, HashMap<Integer, Order> orders, int[][] drivingTimes, ArrayList<Integer> notAssignedOrders) {
        if (notAssignedOrders.isEmpty()) {
            return new OrderAssignment(students, notAssignedOrders);
        }
        checkForOverlap(students, notAssignedOrders, "Before adding order");
    
        // Use the random index to retrieve an actual Order ID from the notAssignedOrders list
        int randomIndex = random.nextInt(notAssignedOrders.size());
        Integer randomOrderID = notAssignedOrders.get(randomIndex); // Correctly get the Order ID
        Order randomOrder = orders.get(randomOrderID); // Now get the Order object using the ID
        // Attempt to add the order to one of the allowed students
        for (int studentID : randomOrder.getAllowedStudents()) {
            Student student = students[studentID];
            if (student.addOrder(randomOrder, drivingTimes, orders)) {
                notAssignedOrders.remove(randomOrderID); // If successfully added, remove from notAssignedOrders
                break;
            }
        }
        checkForOverlap(students, notAssignedOrders, "after adding order");
        return new OrderAssignment(students, notAssignedOrders);
    }
    


    public static Student[] deepCopyStudents(Student[] students) {
        Student[] copiedStudents = new Student[students.length];
        for (int i = 0; i < students.length; i++) {
            copiedStudents[i] = new Student(students[i].getId(), students[i].getAssignedOrderIDs(), students[i].getTotalWorkingTime());
        }
        return copiedStudents;
    }

    private static void recalculateWorkingTime(Student student, HashMap<Integer, Order> orders, int[][] drivingTimes) {

        if (student.getAssignedOrderIDs().isEmpty()) {
            student.setTotalWorkingTime(0);
            return;
        }
    
        int totalWorkingTime = 0;
    
        // Assume 251 is the ID for headquarters
        int headquartersNodeID = 251;
    
        // Initialize with the drive time from headquarters to the first order
        Order firstOrder = orders.get(student.getAssignedOrderIDs().get(0));
        totalWorkingTime += drivingTimes[headquartersNodeID][firstOrder.getNodeID()];
    
        // Loop through the orders in sequence, not including the last one because it's handled separately for the drive back
        for (int i = 0; i < student.getAssignedOrderIDs().size() - 1; i++) {
            Order currentOrder = orders.get(student.getAssignedOrderIDs().get(i));
            Order nextOrder = orders.get(student.getAssignedOrderIDs().get(i + 1));
            totalWorkingTime += currentOrder.getDuration(); // Add duration of current order
            totalWorkingTime += drivingTimes[currentOrder.getNodeID()][nextOrder.getNodeID()]; // Add driving time to next order
        }
    
        // Add the duration of the last order
        Order lastOrder = orders.get(student.getAssignedOrderIDs().get(student.getAssignedOrderIDs().size() - 1));
        totalWorkingTime += lastOrder.getDuration();
    
        // Add driving time from the last order back to the headquarters
        totalWorkingTime += drivingTimes[lastOrder.getNodeID()][headquartersNodeID];
    
        student.setTotalWorkingTime(totalWorkingTime);
    }

    public static void checkForOverlap(Student[] students, ArrayList<Integer> notAssignedOrders, String message) {
        Set<Integer> assignedOrderIds = new HashSet<>();
        for (Student student : students) {
            assignedOrderIds.addAll(student.getAssignedOrderIDs());
        }
    
        // Check for overlap: An order is in both notAssignedOrders and assigned
        for (Integer orderId : notAssignedOrders) {
            if (assignedOrderIds.contains(orderId)) {
                throw new RuntimeException("Overlap detected " + message + ": Order " + orderId + " is both in notAssignedOrders and assigned to a student.");
            }
        }
    
        // The total number of unique orders either assigned or not assigned
        int totalUniqueOrders = assignedOrderIds.size() + notAssignedOrders.size();
        int totalOrders = 1177; // Hardcoded based on your information
    
        // If the total unique orders does not equal the total known orders, some are missing
        if (totalUniqueOrders != totalOrders) {
            throw new RuntimeException("Order inconsistency detected " + message + ": The total of assigned and not assigned orders (" + totalUniqueOrders + ") does not match the known total of " + totalOrders + ".");
        }
    }
}
