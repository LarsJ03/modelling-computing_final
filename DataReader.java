import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.io.FileNotFoundException;

public class DataReader {
    public static int[][] readDrivingTimesFile() throws FileNotFoundException {
        // Parse driving times
        Scanner reader = new Scanner(new File("drivingtimes.txt"));
        int[][] drivingTime = new int[1099][1099]; 
        for(int i = 0; i < 1099; i++)
           for(int j = 0; j < 1099; j++)
              drivingTime[i][j] = reader.nextInt();
        reader.close();

        return drivingTime;
    }

    public static HashMap<Integer, Order> readOrdersFile() throws FileNotFoundException {
        Scanner reader = new Scanner(new File("orders.txt"));
        HashMap<Integer, Order> orders = new HashMap<>();

        reader.nextLine(); // Skip the header
        while (reader.hasNextLine()) {
            String[] orderData = reader.nextLine().split("\t");
            int orderID = Integer.parseInt(orderData[0].trim());
            int nodeID = Integer.parseInt(orderData[2].trim());
            int duration = Integer.parseInt(orderData[5].trim()) * 60; // Convert to seconds
            int profit = Integer.parseInt(orderData[6].trim()); // Assuming profit doesn't need conversion

            List<Integer> allowedStudents = new ArrayList<>();
            for (int j = 0; j < 20; j++) { // Assuming a fixed number of students for simplicity
                if (orderData[7 + j].equals("1")) {
                    allowedStudents.add(j); // Assuming student IDs are simply their index for simplicity
                    orders.put(orderID, new Order(orderID, nodeID, duration, profit, allowedStudents));
                }
            }

            
        }
        reader.close();

        return orders;
    }
}

