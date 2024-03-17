import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataReader {
    public static int[][] readOrdersFile(String filePath) {
        List<int[]> rows = new ArrayList<>();
        int[] selectedColumns = {2, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26}; // Specify the indexes of the columns you're interested in
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = reader.readLine(); // Skip the header row
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                int[] selectedData = new int[selectedColumns.length];
                for (int i = 0; i < selectedColumns.length; i++) {
                    selectedData[i] = Integer.parseInt(parts[selectedColumns[i]].trim()); // Trim spaces before parsing
                }
                rows.add(selectedData);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new int[0][0]; // Return an empty array in case of an error
        }

        // Convert the list to a 2D array
        int[][] ordersData = new int[rows.size()][];
        ordersData = rows.toArray(ordersData);
        ordersData = addHeadquartersEntry(ordersData);
        return ordersData;
    }

    public static int[][] readDrivingTimesFile(String filePath) {
        ArrayList<String[]> rows = new ArrayList<>();

        // First, read all lines to determine the size and collect data
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                rows.add(parts);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new int[0][0]; // Return an empty array in case of an error
        }

        // Now that we know the size, initialize the 2D array
        int size = rows.size();
        int[][] drivingTimes = new int[size][size];

        // Fill the 2D array with driving times
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < rows.get(i).length; j++) {
                drivingTimes[i][j] = Integer.parseInt(rows.get(i)[j]);
            }
        }

        return drivingTimes;
    }

    public static int[][] addHeadquartersEntry(int[][] ordersData) {
        // Create a new array that is one entry larger than ordersData
        int[][] updatedOrdersData = new int[ordersData.length + 1][ordersData[0].length];
    
        // Add a new entry for the headquarters as the first row
        updatedOrdersData[0][0] = 251; // Node ID for the headquarters
        updatedOrdersData[0][1] = 0; // Duration
        updatedOrdersData[0][2] = 0; // Profit
        for (int i = 3; i < updatedOrdersData[0].length; i++) {
            updatedOrdersData[0][i] = 1; // Assuming columns [3] to [22] indicate student eligibility, set to 1
        }
    
        // Copy the original ordersData into the new array, starting from the second row
        for (int i = 0; i < ordersData.length; i++) {
            System.arraycopy(ordersData[i], 0, updatedOrdersData[i + 1], 0, ordersData[i].length);
        }
    
        // Save to file
        saveArrayToFile(updatedOrdersData, "updatedOrdersData.txt");
    
        return updatedOrdersData;
    }

    private static void saveArrayToFile(int[][] array, String fileName) {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
        for (int[] row : array) {
            for (int i = 0; i < row.length; i++) {
                writer.write(Integer.toString(row[i]));
                if (i < row.length - 1) {
                    writer.write("\t"); // Tab-separated values
                }
            }
            writer.newLine(); // New line for each row
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}
}
