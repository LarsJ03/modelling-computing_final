import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AnnealingStrategies {
    private static final Random random = new Random();

    public static OrderAssignment swapTwoOrders(Student[] students, HashMap<Integer, Order> orders, int[][] drivingTimes, ArrayList<Integer> notAssignedOrders) {
    // No need for deep copying all students upfront

    // Randomly select a student with more than one order
    List<Integer> eligibleStudentIndices = IntStream.range(0, students.length)
            .filter(i -> students[i].getAssignedOrderIDs().size() > 1)
            .boxed()
            .collect(Collectors.toList());

    if (!eligibleStudentIndices.isEmpty()) {
        int selectedStudentIndex = eligibleStudentIndices.get(random.nextInt(eligibleStudentIndices.size()));
        Student selectedStudent = students[selectedStudentIndex]; 

        // Get two random indices to swap
        int size = selectedStudent.getAssignedOrderIDs().size();
        int index1 = random.nextInt(size);
        int index2;
        do {
            index2 = random.nextInt(size);
        } while (index2 == index1);

        // Perform the swap
        Integer temp = selectedStudent.getAssignedOrderIDs().get(index1);
        selectedStudent.getAssignedOrderIDs().set(index1, selectedStudent.getAssignedOrderIDs().get(index2));
        selectedStudent.getAssignedOrderIDs().set(index2, temp);

        if (AnnealingStrategies.recalculateWorkingTime(selectedStudent, orders, drivingTimes) > 28800) {
            return new OrderAssignment(students, notAssignedOrders); 
        } else {
            // Update the student at the saved index if the swap is successful and does not need to be reverted
            students[selectedStudentIndex] = selectedStudent; // This line is actually redundant in this approach since we're modifying directly
        }
    }    

    return new OrderAssignment(students, notAssignedOrders);
}

    public static OrderAssignment removeRandomOrder(Student[] students, HashMap<Integer, Order> orders, int[][] drivingTimes, ArrayList<Integer> notAssignedOrders) {
        if (students.length == 0) {
            return new OrderAssignment(students, notAssignedOrders);
        }
        
        int randomStudentIndex = random.nextInt(students.length);
        Student randomStudent = students[randomStudentIndex];
        if (!randomStudent.getAssignedOrderIDs().isEmpty()) {
            int randomOrderIndex = random.nextInt(randomStudent.getAssignedOrderIDs().size());
            Integer randomOrderID = randomStudent.getAssignedOrderIDs().get(randomOrderIndex);
            Order randomOrder = orders.get(randomOrderID);
            if (randomOrder != null) {
                randomStudent.removeOrder(randomOrder, drivingTimes); 
                notAssignedOrders.add(Integer.valueOf(randomOrderID)); 
                recalculateWorkingTime(randomStudent, orders, drivingTimes);
            } 
        }

        return new OrderAssignment(students, notAssignedOrders);
    }
    
    public static OrderAssignment addOrder(Student[] students, HashMap<Integer, Order> orders, int[][] drivingTimes, ArrayList<Integer> notAssignedOrders) {
        if (notAssignedOrders.isEmpty()) {
            return new OrderAssignment(students, notAssignedOrders);
        }
    
        int randomIndex = random.nextInt(notAssignedOrders.size());
        Integer randomOrderID = notAssignedOrders.get(randomIndex); 
        Order randomOrder = orders.get(randomOrderID); 

        for (int studentID : randomOrder.getAllowedStudents()) {
            Student student = students[studentID];
            if (student.addOrder(randomOrder, orders, drivingTimes)) {
                notAssignedOrders.remove(randomOrderID); 
                break;
            }
        }
        
        return new OrderAssignment(students, notAssignedOrders);
    }
    


    public static Student[] deepCopyStudents(Student[] students) {
        Student[] copiedStudents = new Student[students.length];
        for (int i = 0; i < students.length; i++) {
            copiedStudents[i] = new Student(students[i].getId(), students[i].getAssignedOrderIDs(), students[i].getTotalWorkingTime());
        }
        return copiedStudents;
    }

    public static int recalculateWorkingTime(Student student, HashMap<Integer, Order> orders, int[][] drivingTimes) {

        if (student.getAssignedOrderIDs().isEmpty()) {
            student.setTotalWorkingTime(0);
            return 0;
        }
    
        int totalWorkingTime = 0;
        int headquartersNodeID = 251;
    
        Order firstOrder = orders.get(student.getAssignedOrderIDs().get(0));
        totalWorkingTime += drivingTimes[headquartersNodeID][firstOrder.getNodeID()];
    
        for (int i = 0; i < student.getAssignedOrderIDs().size() - 1; i++) {
            Order currentOrder = orders.get(student.getAssignedOrderIDs().get(i));
            Order nextOrder = orders.get(student.getAssignedOrderIDs().get(i + 1));
            totalWorkingTime += currentOrder.getDuration();
            totalWorkingTime += drivingTimes[currentOrder.getNodeID()][nextOrder.getNodeID()]; 
        }
    
        Order lastOrder = orders.get(student.getAssignedOrderIDs().get(student.getAssignedOrderIDs().size() - 1));
        totalWorkingTime += lastOrder.getDuration();
    
        totalWorkingTime += drivingTimes[lastOrder.getNodeID()][headquartersNodeID];
    
        student.setTotalWorkingTime(totalWorkingTime);

        return totalWorkingTime;
    }
}
