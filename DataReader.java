import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataReader {
    public static double[][] readOrdersFile(String filePath) {
        List<double[]> rows = new ArrayList<>();
        int[] selectedColumns = {2, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26}; // Specify the indexes of the columns you're interested in
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = reader.readLine(); // Skip the header row
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                double[] selectedData = new double[selectedColumns.length];
                for (int i = 0; i < selectedColumns.length; i++) {
                    selectedData[i] = Double.parseDouble(parts[selectedColumns[i]]);
                }
                rows.add(selectedData);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new double[0][0]; // Return an empty array in case of an error
        }

        // Convert the list to a 2D array
        double[][] ordersData = new double[rows.size()][];
        ordersData = rows.toArray(ordersData);
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
}
