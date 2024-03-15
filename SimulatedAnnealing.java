import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class SimulatedAnnealing {
    private static final Random random = new Random();
    
    public static void main(String[] args) {
        String drivingTimesFilePath = "drivingtimes.txt";
        String ordersFilePath = "orders.txt";
        
        int[][] drivingTimes = DataReader.readDrivingTimesFile(drivingTimesFilePath);
        double[][] ordersData = DataReader.readOrdersFile(ordersFilePath);

        // Perform simulated annealing to find the best route
        int[] bestRoute = simulatedAnnealing(drivingTimes, ordersData);
        
        // Calculate the net profit and total time for the best route found
        double[] profitAndTime = calculateProfitAndTime(drivingTimes, ordersData, bestRoute);
        double netProfit = profitAndTime[1];
        double totalTime = profitAndTime[0];

        // Print the best route, net profit, and total time
        System.out.println("Best route: " + Arrays.toString(bestRoute));
        System.out.println("Net profit: " + netProfit);
        System.out.println("Total time (hours): " + totalTime / 3600);
    }


    private static int getRandomIndex(int length) {
        return random.nextInt(length); // Use the pre-initialized Random instance
    }

    public static int[] simulatedAnnealing(int[][] drivingTimes, double[][] ordersData) {
        int[] currentRoute = generateInitialRoute(ordersData.length);
        int[] bestRoute = currentRoute.clone();
        double[] currentProfitAndTime = calculateProfitAndTime(drivingTimes, ordersData, currentRoute);
        double currentProfit = currentProfitAndTime[1];
        double bestProfit = currentProfit;
        double temperature = 10000.0; // Starting temperature
        double coolingRate = 0.003; // Cooling rate
        int counter = 0;

        while (temperature > 1) {
            System.out.println("Temperature: " + temperature + " | Best profit: " + bestProfit + "| counter" + counter);
            int[] newRoute = generateNeighbor(currentRoute);
            double[] newProfitAndTime = calculateProfitAndTime(drivingTimes, ordersData, newRoute);

            if (acceptanceProbability(currentProfit, newProfitAndTime[1], temperature) > Math.random()) {
                currentRoute = newRoute;
                currentProfit = newProfitAndTime[1];
            }

            if (newProfitAndTime[1] > bestProfit) {
                bestRoute = newRoute.clone();
                bestProfit = newProfitAndTime[1];
            }

            temperature *= 1 - coolingRate; // Cool down
            counter++;
        }

        return bestRoute;
    }

    public static double[] calculateProfitAndTime(int[][] drivingTimes, double[][] ordersData, int[] route) {
        double totalTime = 0; // Time in seconds
        double totalProfit = 0;
        int currentNode = 0; // Starting at headquarters, assuming index 0 is the headquarters

        for (int jobIndex : route) {
            int nodeID = (int)ordersData[jobIndex][0];
            double driveTime = drivingTimes[currentNode][nodeID]; // Drive time in seconds
            double jobDuration = ordersData[jobIndex][1] * 60; // Job duration in seconds
            double jobProfit = ordersData[jobIndex][2]; // Profit for this job
            
            totalTime += driveTime + jobDuration; // Add drive time and job duration to total time
            totalProfit += jobProfit; // Add profit for this job to total profit
            currentNode = nodeID; // Move to the next node
        }

        // Optionally, add the time to return to headquarters if needed
        totalTime += drivingTimes[currentNode][0]; // Add drive time back to headquarters

        return new double[]{totalTime, totalProfit};
    }

    private static double acceptanceProbability(double currentProfit, double newProfit, double temperature) {
        if (newProfit > currentProfit) {
            return 1.0;
        }
        return Math.exp((newProfit - currentProfit) / temperature);
    }

    private static int[] generateNeighbor(int[] route) {
        int[] newRoute = route.clone();
        int a = getRandomIndex(newRoute.length);
        int b = getRandomIndex(newRoute.length);
        // Swap two nodes
        int temp = newRoute[a];
        newRoute[a] = newRoute[b];
        newRoute[b] = temp;
        return newRoute;
    }

    private static int[] generateInitialRoute(int numberOfOrders) {
        Integer[] route = new Integer[numberOfOrders]; // Use Integer[] to use with Collections.shuffle
        for (int i = 0; i < numberOfOrders; i++) {
            route[i] = i; // Initialize with the order index
        }
        Collections.shuffle(Arrays.asList(route));
        return Arrays.stream(route).mapToInt(Integer::intValue).toArray();
    }
    
}
