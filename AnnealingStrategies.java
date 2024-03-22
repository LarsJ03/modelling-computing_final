import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class AnnealingStrategies {
    private static final Random random = new Random();

    public static Student[] swapTwoOrders(Student[] students, HashMap<Integer, Order> orders, int[][] drivingTimes) {
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

        return students; // Return the original array with the student updated
    }

    public static Student[] reassignOrder(Student[] students, HashMap<Integer, Order> orders, int[][] drivingTimes) {
        Student[] newStudents = deepCopyStudents(students);
    
        // Filter students with orders to ensure there's an order to reassign
        List<Student> studentsWithOrders = Arrays.stream(newStudents)
                                                 .filter(s -> !s.getAssignedOrderIDs().isEmpty())
                                                 .collect(Collectors.toList());
    
        if (!studentsWithOrders.isEmpty()) {
            // Select a random student with at least one order
            Student studentFrom = studentsWithOrders.get(random.nextInt(studentsWithOrders.size()));
    
            // Get a random order from this student
            int orderIndex = random.nextInt(studentFrom.getAssignedOrderIDs().size());
            Integer orderIdToReassign = studentFrom.getAssignedOrderIDs().get(orderIndex);
            Order orderToReassign = orders.get(orderIdToReassign);
    
            // Remove the order from the first student
            studentFrom.getAssignedOrderIDs().remove(orderIndex);
            recalculateWorkingTime(studentFrom, orders, drivingTimes);
    
            // Try to assign the order to another student who is allowed to take it and won't exceed max working hours
            for (Student studentTo : newStudents) {
                if (!studentTo.equals(studentFrom) && orderToReassign.getAllowedStudents().contains(studentTo.getId())) {
                    // Temporarily add the order to calculate the new working time
                    studentTo.getAssignedOrderIDs().add(orderIdToReassign);
                    recalculateWorkingTime(studentTo, orders, drivingTimes);
    
                    // Check if the new working time exceeds the maximum allowed working time
                    if (studentTo.getTotalWorkingTime() > 8*3600) {
                        // If it exceeds, remove the order and try the next student
                        studentTo.getAssignedOrderIDs().remove(orderIdToReassign);
                        recalculateWorkingTime(studentTo, orders, drivingTimes);
                    } else {
                        // Successfully reassigned the order without exceeding max working hours
                        break; // Stop looking for a student to reassign the order to
                    }
                }
            }
        }

        
    
        // Update the original students array with the modified students
        for (int i = 0; i < students.length; i++) {
            students[i] = newStudents[i];
        }
    
        return students; // Return the updated original array
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
}
