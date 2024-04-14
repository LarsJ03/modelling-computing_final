import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AnnealingStrategies {
    // Static random object for generating random numbers throughout the class
    private static final Random random = new Random();

    // Method to swap two orders assigned to a student if possible
    public static OrderAssignment swapTwoOrders(Student[] students, HashMap<Integer, Order> orders, int[][] drivingTimes, ArrayList<Integer> notAssignedOrders) {
        // Find students who are eligible for swapping (having more than one order assigned)
        List<Integer> eligibleStudentIndices = IntStream.range(0, students.length)
                .filter(i -> students[i].getAssignedOrderIDs().size() > 1)
                .boxed()
                .collect(Collectors.toList());

        if (!eligibleStudentIndices.isEmpty()) {
            // Randomly select one of the eligible students
            int selectedStudentIndex = eligibleStudentIndices.get(random.nextInt(eligibleStudentIndices.size()));
            Student selectedStudent = students[selectedStudentIndex];

            // Get two distinct random indices for the orders to be swapped
            int size = selectedStudent.getAssignedOrderIDs().size();
            int index1 = random.nextInt(size);
            int index2;
            do {
                index2 = random.nextInt(size);
            } while (index2 == index1);

            // Swap the orders at index1 and index2
            Integer temp = selectedStudent.getAssignedOrderIDs().get(index1);
            selectedStudent.getAssignedOrderIDs().set(index1, selectedStudent.getAssignedOrderIDs().get(index2));
            selectedStudent.getAssignedOrderIDs().set(index2, temp);

            // Check if working time exceeds limit after swap, if so, revert
            if (AnnealingStrategies.recalculateWorkingTime(selectedStudent, orders, drivingTimes) > 28800) {
                return new OrderAssignment(students, notAssignedOrders);
            } else {
                // Otherwise, update the student with the successful swap
                students[selectedStudentIndex] = selectedStudent;
            }
        }

        return new OrderAssignment(students, notAssignedOrders);
    }

    // Method to remove a random order from a random student
    public static OrderAssignment removeRandomOrder(Student[] students, HashMap<Integer, Order> orders, int[][] drivingTimes, ArrayList<Integer> notAssignedOrders) {
        if (students.length == 0) {
            return new OrderAssignment(students, notAssignedOrders);
        }

        // Select a random student
        int randomStudentIndex = random.nextInt(students.length);
        Student randomStudent = students[randomStudentIndex];

        // Ensure the selected student has orders assigned
        if (!randomStudent.getAssignedOrderIDs().isEmpty()) {
            // Select a random order from the student and remove it
            int randomOrderIndex = random.nextInt(randomStudent.getAssignedOrderIDs().size());
            Integer randomOrderID = randomStudent.getAssignedOrderIDs().remove(randomOrderIndex);
            Order randomOrder = orders.get(randomOrderID);
            if (randomOrder != null) {
                randomStudent.removeOrder(randomOrder, drivingTimes);
                notAssignedOrders.add(randomOrderID);
                recalculateWorkingTime(randomStudent, orders, drivingTimes);
            }
        }

        return new OrderAssignment(students, notAssignedOrders);
    }

    // Method to assign a random unassigned order to a suitable student
    public static OrderAssignment addOrder(Student[] students, HashMap<Integer, Order> orders, int[][] drivingTimes, ArrayList<Integer> notAssignedOrders) {
        if (notAssignedOrders.isEmpty()) {
            return new OrderAssignment(students, notAssignedOrders);
        }

        // Select a random order from unassigned orders
        int randomIndex = random.nextInt(notAssignedOrders.size());
        Integer randomOrderID = notAssignedOrders.get(randomIndex);
        Order randomOrder = orders.get(randomOrderID);

        // Attempt to add the order to a suitable student
        for (int studentID : randomOrder.getAllowedStudents()) {
            Student student = students[studentID];
            if (student.addOrder(randomOrder, orders, drivingTimes)) {
                notAssignedOrders.remove(randomOrderID);
                break;
            }
        }

        return new OrderAssignment(students, notAssignedOrders);
    }

    // Helper method to deep copy student objects to prevent original data modification
    public static Student[] deepCopyStudents(Student[] students) {
        Student[] copiedStudents = new Student[students.length];
        for (int i = 0; i < students.length; i++) {
            copiedStudents[i] = new Student(students[i].getId(), students[i].getAssignedOrderIDs(), students[i].getTotalWorkingTime());
        }
        return copiedStudents;
    }

    // Recalculate the working time of a student after modifying their assigned orders
    public static int recalculateWorkingTime(Student student, HashMap<Integer, Order> orders, int[][] drivingTimes) {
        if (student.getAssignedOrderIDs().isEmpty()) {
            student.setTotalWorkingTime(0);
            return 0;
        }

        int totalWorkingTime = 0;
        int headquartersNodeID = 251;

        // Calculate total working time starting from headquarters
        Order firstOrder = orders.get(student.getAssignedOrderIDs().get(0));
        totalWorkingTime += drivingTimes[headquartersNodeID][firstOrder.getNodeID()];

        // Add driving and service times for each order in sequence
        for (int i = 0; i < student.getAssignedOrderIDs().size() - 1; i++) {
            Order currentOrder = orders.get(student.getAssignedOrderIDs().get(i));
            Order nextOrder = orders.get(student.getAssignedOrderIDs().get(i + 1));
            totalWorkingTime += currentOrder.getDuration();
            totalWorkingTime += drivingTimes[currentOrder.getNodeID()][nextOrder.getNodeID()];
        }

        // Add the last order's duration and return time to headquarters
        Order lastOrder = orders.get(student.getAssignedOrderIDs().get(student.getAssignedOrderIDs().size() - 1));
        totalWorkingTime += lastOrder.getDuration();
        totalWorkingTime += drivingTimes[lastOrder.getNodeID()][headquartersNodeID];

        student.setTotalWorkingTime(totalWorkingTime);

        return totalWorkingTime;
    }
}
