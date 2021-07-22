/**@author Heidi Toews
 * <a>
 * href="mailto:heidi.toews@ucalgary.ca">heidi.toews@ucalgary.ca</a>
 * @version 2.4
 * @since 1.0
 */

/**ENSF 409 Final Project Group 7
Desk.java
Ahmed Waly, Alexis Hamrak, Andrea Benavides Aguirre, Heidi Toews*/

package edu.ucalgary.ensf409;

import java.sql.*;
import java.lang.StringBuilder;

/**Class Desk is used to find the cheapest option for a particular type of desk. */
public class Desk {
    private String type;
    private String[][] itemsUsed = new String[1][2]; //itemsUsed[][0] = id, itemsUsed[][1] = price
    private String[][] items;
    private int quantity;
    private boolean[] found;
    private int number = 0; //The number of items purchased
    private int desksFound = 0; //The number of full desks found
    /**database URL*/
    public final String DBURL;
    /**database username*/
    public final String USERNAME;
    /**database password*/
    public final String PASSWORD;
    private Connection connect = null;
    private ResultSet result = null;

    /**Constructor that requires a url, username, and password for the database,
     * the desired desk type, and the number of desks required.
     * @param url String of the database URL
     * @param username String of the database username
     * @param password String of the database password
     * @param type String type of Desk that is required
     * @param quantity int of the number of Desks requested
     */
    public Desk(String url, String username, String password, String type, int quantity) {
        type = type.toLowerCase();
        char firstLetter = Character.toUpperCase(type.charAt(0));
        type = String.valueOf(firstLetter) + type.substring(1);
        if (type.equals("Traditional") || type.equals("Adjustable") || type.equals("Standing")) {
            this.type = type;
        } else {
            throw new IllegalArgumentException("Invalid desk type, please enter traditional, adjustable, or standing.");
        }
        if (quantity > 0) {
            this.quantity = quantity;
        } else {
            throw new IllegalArgumentException("Invalid quantity.");
        }
        found = new boolean[quantity];
        DBURL = url;
        USERNAME = username;
        PASSWORD = password;
        initializeConnection();
    }

    /**Initializes the connection to the database with the stored url, username, and password. */
    public void initializeConnection() {
        try {
            connect = DriverManager.getConnection(DBURL, USERNAME, PASSWORD);
        }
        catch (SQLException e) {
            closeAll();
            System.err.print("Failed to connect to database with url " + DBURL);
            System.err.print(", username " + USERNAME + ", and password " + PASSWORD + "\n");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**Updates the database to reflect the items that have been purchased by 
     * deleting the rows of the items that have been purchased.
    */
    public void updateDatabase() {
        PreparedStatement stmt = null;
        try {
            String query = "DELETE FROM Desk WHERE ID = ?";
            stmt = connect.prepareStatement(query);
            for (int i = 0; i < itemsUsed.length; i++) {
                for (int j = 0; j < itemsUsed[i].length; j++) {
                    stmt.setString(1, itemsUsed[i][j]);
                    stmt.executeUpdate();
                }
            }
            stmt.close();
        }
        catch (SQLException e) {
            closeAll();
            System.err.println("SQLException in updateDatabase.");
            System.exit(1);
        }
    }

    /**Finds the cheapest way to make a dest of the desired type.<!-- --> Updates the
     * itemsUsed and found data members.
     */
    public void findDesks() {
        //This method finds all the desks of the desire type that are in the
        //database and stores them in items. Then it calls findOptions as
        //many times as there are needed desks.
        result = null;
        items = new String[1][5];
        //items[][0] = id; items[][1] = legs; items[][2] = top; items[][3] = drawer; items[][4] = price
        Statement stmt = null;
        try {
            stmt = connect.createStatement();
            result = stmt.executeQuery("SELECT * FROM DESK");
            int i = 0;
            while(result.next()) {
                if (type.equals(result.getString("Type"))) {
                    if (i >= items.length) {
                        String copy[][] = new String[items.length + 1][5];
                        for (int j = 0; j < items.length; j++) {
                            copy[j] = items[j];
                        }
                        items = copy;
                    }
                    items[i] = new String[5];
                    items[i][0] = result.getString("ID");
                    items[i][1] = result.getString("Legs");
                    items[i][2] = result.getString("Top");
                    items[i][3] = result.getString("Drawer");
                    items[i][4] = String.valueOf(result.getInt("Price"));
                    i++;
                } //Get all items of the desired type from the database.
            }
            stmt.close();
        }
        catch (SQLException e) {
            closeAll();
            System.err.println("SQLException in getDesk.");
            System.exit(1);
        }
        for (int i = 0; i < quantity; i++) {
            //Call findOptions once for every desk needed.
            String op[][] = findOptions();
            if (op[0][0] == null) {
                //If no options were found, the order can't be fulfilled.
                for (int j = 0; j < found.length; j++) {
                    found[j] = false;
                }
                break;
            }
        }
        if (found[found.length-1]) {
            //Only update the database if all needed desks could be found.
            updateDatabase();
        }
        printString();
    }

    /**Finds all the ways the available items can be put together to make
     * a full desk and calls findCheapest to find the cheapest option;
     * updates the found data member.
     * @return A 2D String array of the combinations to make Desk items
     */
    private String[][] findOptions() {
        String[][] options = new String[1][4];
        //options[][0] = first item; options[][1] = second item; options[][2] = third item; options[][3] = price
        int total = 0; //Total number of options found
        for (int i = 0; i < items.length; i++) {
            if (makesDesk(items[i])) {
                //If a desk can be found with one item, add it to the options array.
                if (total >= options.length) {
                    //If the options array isn't big enough, expand it.
                    String copy[][] = new String[options.length + 1][4];
                    for (int m = 0; m < options.length; m++) {
                        copy[m] = options[m];
                    }
                    options = copy;
                }
                options[total][0] = items[i][0];
                options[total][3] = items[i][4];
                total++;
            } else {
                //Otherwise try with a second item.
                for (int j = i+1; j < items.length; j++) {
                    //If a desk can be made with two items, add them to the options array.
                    if (makesDesk(items[i], items[j])) {
                        if (total >= options.length) {
                            //If the options array isn't big enough, expand it.
                            String copy[][] = new String[options.length + 1][4];
                            for (int m = 0; m < options.length; m++) {
                                copy[m] = options[m];
                            }
                            options = copy;
                        }
                        options[total][0] = items[i][0];
                        options[total][1] = items[j][0];
                        //If this option was made using parts from a desk that
                        //has already been chosen, don't include it's price.
                        int p = 0;
                        if (alreadyBought(items[i][0])) {
                            p += 0;
                        } else {
                            p += Integer.parseInt(items[i][4]);
                        }
                        if (alreadyBought(items[j][0])) {
                            p += 0;
                        } else {
                            p += Integer.parseInt(items[j][4]);
                        }
                        options[total][3] = String.valueOf(p);
                        total++;
                    } else {
                        //Otherwise try with a third item.
                        for (int k = j+1; k < items.length; k++) {
                            if (makesDesk(items[i], items[j], items[k])) {
                                if (total >= options.length) {
                                    //If the options array isn't big enough, expand it.
                                    String copy[][] = new String[options.length + 1][4];
                                    for (int m = 0; m < options.length; m++) {
                                        copy[m] = options[m];
                                    }
                                    options = copy;
                                }
                                options[total][0] = items[i][0];
                                options[total][1] = items[j][0];
                                options[total][2] = items[k][0];
                                //If this option was made using parts from a desk that
                                //has already been chosen, don't include it's price.
                                int p = 0;
                                if (alreadyBought(items[i][0])) {
                                    p += 0;
                                } else {
                                    p += Integer.parseInt(items[i][4]);
                                }
                                if (alreadyBought(items[j][0])) {
                                    p += 0;
                                } else {
                                    p += Integer.parseInt(items[j][4]);
                                }
                                if (alreadyBought(items[k][4])) {
                                    p += 0;
                                } else {
                                    p += Integer.parseInt(items[k][4]);
                                }
                                options[total][3] = String.valueOf(p);
                                total++;
                            }
                        }
                    }
                }
            }
        }

        if (total == 0) {
            //If no options were found, indicate that in the found array.
            found[desksFound] = false;
            desksFound++;
        } else {
            //If options were found, indicate that in the found array and call
            //findCheapest to find the cheapest one.
            found[desksFound] = true;
            findCheapest(options);
            desksFound++;
        }
        return options;
    }
    /**Checks if an ID has been used or not.
     * @param id String of the ID
     * @return Boolean, true if the ID has been used and false if it hasn't
     */
    private boolean alreadyBought(String id) {
        for (int i = 0; i < itemsUsed.length; i++) {
            if (id.equals(itemsUsed[i][0])) {
                return true;
            }
        }
        return false;
    }

    /**Takes a 2D array of the different ways the available items can make
     * a desk, and finds the cheapest one. Puts the ids of the chosen items
     * in itemsUsed and updates the items array.
     * @param options A 2D array of the different combinations the available 
     * items can make a desk
     */
    private void findCheapest(String[][] options) {
        int lowest = Integer.parseInt(options[0][3]); //Start with the first option
        int index = 0;
        for (int i = 1; i < options.length && options[i][0] != null; i++) {
            if (Integer.parseInt(options[i][3]) < lowest) {
                //If a cheaper option is found, use that instead
                lowest = Integer.parseInt(options[i][3]);
                index = i;
            }
        }
        if (options[index][0] != null && !alreadyBought(options[index][0])) {
            if (number >= itemsUsed.length) {
                String copy[][] = new String[itemsUsed.length + 1][2];
                for (int i = 0; i < itemsUsed.length; i++) {
                    copy[i][0] = itemsUsed[i][0];
                    copy[i][1] = itemsUsed[i][1];
                }
                itemsUsed = copy;
            }
            itemsUsed[number][0] = options[index][0];
            itemsUsed[number][1] = findPrice(options[index][0]);
            number++;
        }
        if (options[index][1] != null && !alreadyBought(options[index][1])) {
            if (number >= itemsUsed.length) {
                String copy[][] = new String[itemsUsed.length + 1][2];
                for (int i = 0; i < itemsUsed.length; i++) {
                    copy[i][0] = itemsUsed[i][0];
                    copy[i][1] = itemsUsed[i][1];
                }
                itemsUsed = copy;
            }
            itemsUsed[number][0] = options[index][1];
            itemsUsed[number][1] = findPrice(options[index][1]);
            number++;
        }
        if (options[index][2] != null && !alreadyBought(options[index][2])) {
            if (number >= itemsUsed.length) {
                String copy[][] = new String[itemsUsed.length + 1][2];
                for (int i = 0; i < itemsUsed.length; i++) {
                    copy[i][0] = itemsUsed[i][0];
                    copy[i][1] = itemsUsed[i][1];
                }
                itemsUsed = copy;
            }
            itemsUsed[number][0] = options[index][2];
            itemsUsed[number][1] = findPrice(options[index][2]);
            number++;
        }
        //Mark the necessary parts as U in the items array.
        items = markUsed(items, options[index][0], options[index][1], options[index][2]);
    }

    /**Checks if the provided item is a whole desk.
     * @param one String array of one row in the Desk Table
     * @return true if the provided items can be used to make a whole desk,
     * returns false otherwise
     */
    private boolean makesDesk(String[] one) {
        for (int i = 0; i < one.length; i++) {
            if (one[i] == null) {
                return false;
            }
        }
        if (one[1].equals("Y") && one[2].equals("Y") && one[3].equals("Y")) {
            return true;
        }
        return false;
    }

    /**Checks if the provided items can be used to make a whole desk.
     * @param one String array of one row in the Desk Table
     * @param two String array of another row in the Desk Table
     * @return true if the provided items can be used to make a whole desk,
     * returns false otherwise
     */
    private boolean makesDesk(String[] one, String[] two) {
        if (one[1].equals("Y") || two[1].equals("Y")) {
            if (one[2].equals("Y") || two[2].equals("Y")) {
                if (one[3].equals("Y") || two[3].equals("Y")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**Checks if the provided items can be used to make a whole desk.
     * @param one String array of one row in the Desk Table
     * @param two String array of another row in the Desk Table
     * @param three String array of a third row in the Desk Table
     * @return true if the provided items can be used to make a whole desk,
     * returns false otherwise
     */
    private boolean makesDesk(String[] one, String[] two, String[] three) {
        if (one[1].equals("Y") || two[1].equals("Y") || three[1].equals("Y")) {
            if (one[2].equals("Y") || two[2].equals("Y") || three[2].equals("Y")) {
                if (one[3].equals("Y") || two[3].equals("Y") || three[3].equals("Y")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**Finds the price of an item given its ID. 
     * @param id String of the ID that is needed to find the price
     * @return A String of the price
     */
    private String findPrice(String id) {
        for (int i = 0; i < items.length; i++) {
            if (id.equals(items[i][0])) {
                return items[i][4];
            }
        }
        return "";
    }

    /**Marks the parts necessary to make a full desk as U (used/unavailable) within 
     * a 2D array.
     * @param old A 2D String array that hasn't been marked
     * @param item1 The id of the first item that might have been used
     * @param item2 The id of the second item that might have been used
     * @param item3 The id of the third item that might have been used
     * @return A 2D String array that has been marked
     */
    private String[][] markUsed(String[][] old, String item1, String item2, String item3) {
        int index1 = -1;
        int index2 = -1;
        int index3 = -1;
        if (item1 == null) {
            item1 = new String("notAnID");
        } else {
            //Find the location of item 1
            for (int i = 0; i < items.length; i++) {
                if (item1.equals(items[i][0])) {
                    index1 = i;
                }
            }
        }
        if (item2 == null) {
            item2 = new String("notAnID");
        } else {
            //Find the location of item 2
            for (int i = 0; i < items.length; i++) {
                if (item2.equals(items[i][0])) {
                    index2 = i;
                }
            }
        }
        if (item3 == null) {
            item3 = new String("notAnID");
        } else {
            //Find the location of item 3
            for (int i = 0; i < items.length; i++) {
                if (item3.equals(items[i][0])) {
                    index3 = i;
                }
            }
        }
        boolean legs = false;
        boolean top = false;
        boolean drawer = false;
        if (index1 != -1) {
            if (items[index1][1].equals("Y")) {
                items[index1][1] = "U";
                legs = true;
            }
            if (items[index1][2].equals("Y")) {
                items[index1][2] = "U";
                top = true;
            }
            if (items[index1][3].equals("Y")) {
                items[index1][3] = "U";
                drawer = true;
            }
        }
        if (index2 != -1) {
            if (items[index2][1].equals("Y") && !legs) {
                items[index2][1] = "U";
                legs = true;
            }
            if (items[index2][2].equals("Y") && !top) {
                items[index2][2] = "U";
                top = true;
            }
            if (items[index2][3].equals("Y") && !drawer) {
                items[index2][3] = "U";
                drawer = true;
            }
        }
        if (index3 != -1) {
            if (items[index3][1].equals("Y") && !legs) {
                items[index3][1] = "U";
                legs = true;
            }
            if (items[index3][2].equals("Y") && !top) {
                items[index3][2] = "U";
                top = true;
            }
            if (items[index3][3].equals("Y") && !drawer) {
                items[index3][3] = "U";
                drawer = true;
            }
        }
        return old;
    }

    /**Attempts to close the Connection object and the ResultSet object. */
    public void closeAll() {
        if (connect != null) {
            try {
                connect.close();
            }
            catch (SQLException e) {
                System.err.print("Failed to close connection to database.");
                System.exit(1);
            }
        }
        if (result != null) {
            try {
                result.close();
            }
            catch (SQLException e) {
                System.err.print("Failed to close ResultSet object.");
                System.exit(1);
            }
        }
    }

    /**Returns a String that will be outputed in the text file
     * @return A String of the formatted items used and the total price or the 
     * suggested manufacturers
     */
    public String getOrderString() {
        StringBuilder order = new StringBuilder("Original Request: ");
        order.append(type + " Desk, " + quantity + "\n\n");

        if (found[found.length-1]) {
            order.append("Items Ordered\n");
            for (int i = 0; i < itemsUsed.length; i++) {
                order.append("ID: " + itemsUsed[i][0] + "\n");
            }
            order.append("\nTotal Price: $" + getTotal());
        } else {
            order.append("Order cannot be fulfilled based on current inventory. ");
            order.append("Suggested manufacturers are Academic Desks, Office Furnishings, ");
            order.append("Furniture Goods, and Fine Office Supplies.");
        }
        return order.toString();
    }

    /**Prints a summary of the order to the command line. */
    public void printString() {
        StringBuilder order = new StringBuilder();
        if (found[found.length-1]) {
            order.append("Purchase ");
            for (int i = 0; i < itemsUsed.length; i++) {
                order.append(itemsUsed[i][0] + " and ");
            }
            order.append("for $" + getTotal() + ".");
        } else {
            order.append("Order cannot be fulfilled based on current inventory. ");
            order.append("Suggested manufacturers are Academic Desks, Office Furnishings, ");
            order.append("Furniture Goods, and Fine Office Supplies.");
        }
        System.out.println(order);
    }
    /**Getter method for the total price of the order
     * @return int of the total price
     */
    public int getTotal() {
        int total = 0;
        for (int i = 0; i < itemsUsed.length; i++) {
            total += Integer.parseInt(itemsUsed[i][1]);
        }
        return total;
    }

    /**Getter method for the type of Desk that was requested
     * @return String of the Desk type
     */
    public String getType() {
        return type;
    }

    /**Getter method for the quantity of Desks that was requested
     * @return int of the quantity
     */
    public int getQuantity() {
        return quantity;
    }

    /**Getter method for the found array
     * @return boolean array
     */
    public boolean[] getFound() {
        return found;
    }

    /**Getter method for the items used for Desks
     * @return 2D String array of the Desk items that were used
     */
    public String[][] getItemsUsed() {
        return itemsUsed;
    }

    /**Getter method for a row in the itemsUsed array at the specified index.
     * @param index int of the index needed
     * @return A String array of the row for the Desk item that was used
     */
    public String[] getItemsUsed(int index) {
        return itemsUsed[index];
    }

    /**Getter method for the items in the Desk table
     * @return 2D String array of the Desk items
     */
    public String[][] getItems() {
        return items;
    }
}
