import java.util.ArrayList;
import java.util.Arrays;
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

    public static void removeRandomOrder(Student[] students, HashMap<Integer, Order> orders, int[][] drivingTimes) {
        int randomStudentIndex = random.nextInt(students.length);
        Student randomStudent = students[randomStudentIndex];
        if (randomStudent.getAssignedOrderIDs().size() > 0) {
            int randomOrderIndex = random.nextInt(randomStudent.getAssignedOrderIDs().size());
            Order randomOrder = orders.get(randomStudent.getAssignedOrderIDs().get(randomOrderIndex));
            randomStudent.removeOrder(randomOrder, drivingTimes);
        }
        

    }
    
    public static void addOrderToAllowedStudent(Student[] students, HashMap<Integer, Order> orders, int[][] drivingTimes) {
        for (Order order : orders.values()) {
            if (!order.isAssigned()) {
                for (Student student : students) {
                    if (order.getAllowedStudents().contains(student.getId())) {
                        if (student.addOrder(order, drivingTimes, orders)) {
                            order.setAssigned(true); 
                            break; 
                        }
                    }
                }
            }
        }
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
