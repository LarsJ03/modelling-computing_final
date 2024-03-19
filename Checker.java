import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.HashMap;

public class Checker {
   public static void main(String[] args) throws FileNotFoundException {
      // Parse driving times
      Scanner reader = new Scanner(new File("drivingtimes.txt"));
      int[][] drivingTime = new int[1099][1099]; // Driving times in seconds
      for(int i = 0; i < 1099; i++)
         for(int j = 0; j < 1099; j++)
            drivingTime[i][j] = reader.nextInt();
      reader.close();
      
      // Parse orders
      reader = new Scanner(new File("orders.txt"));
      int[] profit = new int[1177], duration = new int[1177], nodeID = new int[1177], orderID = new int[1177];
      boolean[][] allowed = new boolean[1177][20];
      boolean[] orderDone = new boolean[1177];
      HashMap<Integer, Integer> orderLookup = new HashMap<Integer, Integer>();
      
      reader.nextLine(); // Skip the first line with the header
      for(int i = 0; i < 1177; i++) {
         String[] orderData = reader.nextLine().split("\t");
         orderID[i] = Integer.parseInt(orderData[0]);
         nodeID[i] = Integer.parseInt(orderData[2]);
         duration[i] = Integer.parseInt(orderData[5]) * 60; // Convert to seconds
         profit[i] = Integer.parseInt(orderData[6]) * 60; // Also multiplied by 60, this way one unit of profit is equal to one unit (second) of time, since one second is worth 1/60 euro
        
        orderLookup.put(orderID[i], i);
        
        // Parse what employees can do what
        for (int j = 0; j < 20; j++)
           if (orderData[7 + j].equals("1"))
              allowed[i][j] = true;
      }
      reader.close();
      
      // Read & check the solution file
      reader = new Scanner(new File("output.txt"));
      
      int claimedProfit = reader.nextInt(); // First line is (supposed) profit of solution
      int realProfit = 0;
      
      boolean valid = true;
      
      for(int i = 0; i < 20; i++) {
         int tasksEmployee = reader.nextInt();
         
         int travelTime = 0, orderProfit = 0, workingTime = 0;
         
         int cur = 251; // Starting location
         for(int j = 0; j < tasksEmployee; j++) {
            int order = reader.nextInt();
           
            // Each order may be performed at most once
            if(orderDone[orderLookup.get(order)]) { System.out.println("ERROR: order is performed multiple times: " + order); valid = false; }
            orderDone[orderLookup.get(order)] = true;
           
            // Check that employee is authorized
            if(!allowed[orderLookup.get(order)][i]) { System.out.println("ERROR: order " + order + " is performed by incapable employee: " + (i + 1)); valid = false; }        
           
            // Accounting
            orderProfit += profit[orderLookup.get(order)];
            workingTime += duration[orderLookup.get(order)];
            
            // Figure out time spent driving
            travelTime += drivingTime[cur][nodeID[orderLookup.get(order)]];
           
            cur = nodeID[orderLookup.get(order)];
         }
         
         travelTime += drivingTime[cur][251]; // Back home
         
         if(travelTime + workingTime > 8 * 60 * 60) { System.out.println("ERROR: employee " + (i + 1) + " works overtime: " + (travelTime + workingTime) + "s"); valid = false; }
         
         System.out.println("Employee " + (i + 1) + " - Profit: " + orderProfit + " - Driving time: " + travelTime + " - Working time: " + workingTime);
         
         realProfit += orderProfit - travelTime - workingTime;
      }
      
      if(realProfit / 60 != claimedProfit)
         System.out.println("ERROR: claimed profit of " + claimedProfit + " does not match actual value achieved of " + (realProfit / 60));
      else if(!valid)
         System.out.println("ERROR: solution is invalid but profit of " + claimedProfit + " does match");
      else
         System.out.println("CORRECT: solution meets all constraints and has profit of " + Math.round(realProfit * 100 / 60.0) / 100.0);
      
      if(claimedProfit < 0)
         System.out.println("WARNING: you should aim for positive profit, value of " + claimedProfit + " is negative");
      
      reader.close();
   }
}