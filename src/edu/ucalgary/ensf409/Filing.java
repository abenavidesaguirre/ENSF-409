package edu.ucalgary.ensf409;

/**
 * @author Alexis Hamrak
 * @version 3.5
 * @since 1.0
 */

/*ENSF 409 Final Project Group 7
Filing.java
Ahmed Waly, Alexis Hamrak, Andrea Benavides Aguirre, Heidi Toews*/

import java.sql.*;
import java.lang.StringBuilder;

/**
 * Filing class used for filing types of furniture
 */
public class Filing {
    /**url of database */
    public final String DBURL;
    /**username of database */
    public final String USERNAME;
    /**password of database */
    public final String PASSWORD;
    private Connection dbConnect = null; //Connection object
    private ResultSet result = null; //ResultSet object

    private String type; //String variable corresponding to the type of item to be used
    private String[][] used = new String[1][2]; //2D String array corresponding to the used items
    private String[][] items; //2D String array corresponding to rows of filing table
    private String[] manufacturers = {"Office Furnishings", "Furniture Goods", "Fine Office Supplies"};
    //String array corresponding to each of the manufacturers for filing objects
    private int toBuy; //int corresponding to number of items to purchase
    private boolean[] found; //boolean array corresponding to whether the pieces were found
    private int itemNum = 0; //The number of items completed
    private int file = 0; //The number of full filing objects found

    /**
     * Constructor for filing object. Establishes connection to the database and
	 * stores all local variables. Also checks if the type is valid and throws an
	 * IllegalArgumentException if invalid
	 * @param connect String argument for database url
	 * @param user String argument for username
	 * @param pass String argument for password
	 * @param type String argument for object type
	 * @param items int argument for the number of items
     */
    public Filing(String connect, String user, String pass, String type, int items) {
        this.DBURL = connect;
        this.USERNAME = user;
        this.PASSWORD = pass;
        type = type.toLowerCase();
        char f = Character.toUpperCase(type.charAt(0)); //convert so input is not case sensitive
        type = String.valueOf(f) + type.substring(1).toLowerCase();

        if (type.equals("Small") || type.equals("Medium") || type.equals("Large")) {
            this.type = type;
        } else {
            throw new IllegalArgumentException("Invalid Filing type, please enter small, medium or large.");
        }
        if (items > 0) {
            this.toBuy= items;
        } else {
            throw new IllegalArgumentException("Invalid item number.");
        }
        this.found = new boolean[toBuy];
        this.initializeConnection();
    }
    
    /**
     * getter for type
     * @return String of type
     */
    public String getType() {
        return type;
    }

    /**
     * getter for toBuy
     * @return int of what number of items we are purchasing
     * */
    public int getToBuy() {
        return toBuy;
    }

    /**
     * getter for found
     * @return boolean array of items found
     * */
    public boolean[] getFound() {
        return found;
    }

    /**
     * getter for used
     * @return 2D String array of used items
     * */
    public String[][] getUsed() {
        return used;
    }

     /**
     * getter for items used at specific index
     * @param index int of the index
     * @return String array of a specific table row
     * */
    public String[] getUsed(int index) {
        return used[index];
    }

    /**
     * getter for items
     * @return 2D String array of items from table
     * */
    public String[][] getItems() {
        return items;
    }

    /**
     * getter for manufacturers
     * @return String array of manufacturers
     * */
    public String[] getManufacturers() {
        return manufacturers;
    }

    /**
	 * initializeConnection() establishes a connection with the existing database.
	 * Doesn't return anything. Doesn't take in any arguments
	 */
    public void initializeConnection() {
         try {
            dbConnect = DriverManager.getConnection(DBURL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Calculates the cheapest way to make a filing system of the given type.
     * This updates which items have been used.
     */
    public void findFiling() {
        result = null;
        items = new String[1][5];
        try {
            Statement stmt = dbConnect.createStatement();
            result = stmt.executeQuery("SELECT * FROM Filing"); //select filing table from database
            int i = 0;
            while(result.next()) {
                if (type.equals(result.getString("Type"))) {
                    if (i >= items.length) {
                        String arr[][] = new String[items.length + 1][5];
                        for (int j = 0; j < items.length; j++) {
                            arr[j] = items[j];
                        }
                        items = arr;
                    }
                    items[i] = new String[5]; //read in all tables and store them locally into items array
                    items[i][0] = result.getString("ID");
                    items[i][1] = result.getString("Rails");
                    items[i][2] = result.getString("Drawers");
                    items[i][3] = result.getString("Cabinet");
                    items[i][4] = String.valueOf(result.getInt("Price"));
                    i++;
                } //read items from the table from the database
            }
            stmt.close();
        }
        catch (SQLException e) {
            close();
            System.err.println("SQLException when reading Filing table.");
            System.exit(1);
        }
        for (int n = 0; n < toBuy; n++) {
            String op[][] = findOptions(); //call findOptions to make our order
            if (op[0][0] == null) {
                //no options available- no filing system is available
                for (int i = 0; i < found.length; i++) {
                    found[i] = false;
                }
                break;
            }
        }
        if (found[found.length-1]) {
            updateDatabase();
          // update database if all combinations were possible, don't update otherwise
        }
        printString();
    }

    /**
     * findOptions finds all possible combinations to make filing items.
     * Calls findCheapest to find the cheapest price of a full file.
     * @return A 2D String array of the options to make filing items
     */
    private String[][] findOptions() {
        String[][] options = new String[1][4];
        int total = 0;
        for (int i = 0; i < items.length; i++) {
            if (makesFile(items[i])) { //If a filing system can be found with one item, add it to the array of possibilities
                if (total >= options.length) {
                    //change array length if needed
                    String copy[][] = new String[options.length + 1][4];
                    for (int j = 0; j < options.length; j++) {
                        copy[j] = options[j];
                    }
                    options = copy;
                }
                options[total][0] = items[i][0];
                options[total][3] = items[i][4];
                total++;
            } else {//not possible, try with multiple items
                for (int j = i+1; j < items.length; j++) {
                    if (makesFile(items[i], items[j])) { //add to array if we can make a complete filing object
                        if (total >= options.length){
                            //change array length if needed
                            String copy[][] = new String[options.length + 1][4];
                            for (int k = 0; k < options.length; k++) {
                                copy[k] = options[k];
                            }
                            options = copy;
                        }
                        options[total][0] = items[i][0];
                        options[total][1] = items[j][0];
                        //If using parts of a filing system that has already been used, exclude its price
                        int d = 0;
                        if (alreadyBought(items[i][0])) {
                            d += 0;
                        } else {
                            d += Integer.parseInt(items[i][4]);
                        }
                        if (alreadyBought(items[j][0])) {
                            d += 0;
                        } else {
                            d += Integer.parseInt(items[j][4]);
                        }
                        options[total][3] = String.valueOf(d);
                        total++;
                    } else {//try using another item
                        for (int k = j+1; k < items.length; k++) {//add to array if we can make a complete filing object
                            if (makesFile(items[i], items[j], items[k])) {
                                if (total >= options.length) {
                               
                                    String copy[][] = new String[options.length + 1][4];
                                    for (int m = 0; m < options.length; m++) {
                                        copy[m] = options[m];
                                    }
                                    options = copy;
                                }
                                options[total][0] = items[i][0];
                                options[total][1] = items[j][0];
                                options[total][2] = items[k][0];
                                int p = 0;
                                if (alreadyBought(items[i][0])) { //check to see if the ID is already in our array of items to be purchased
                                    p += 0;
                                } else {
                                    p += Integer.parseInt(items[i][4]);//if not in the array, add it in
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
            //no possible options found, write to array saying it was false
            found[file] = false;
            file++;
        } else {
            //options were available, call findCheapest to find the cheapest one.
            found[file] = true;
            findCheapest(options);
            file++;
        }
        return options;
    }

    /**
     * alreadyPurchased checks to see if the item with the corresponding ID has been used already
     * @param id String corresponding to the id of a given item
     * @return returns boolean value of true if used, false if not used
     */
    private boolean alreadyBought(String id) {
        for (int i = 0; i < used.length; i++) {
            if (id.equals(used[i][0])) {
                return true;
            }
        }
        return false;
    }

    /**
     * findCheapest uses a 2D array of the many ways the given items can make a complete 
     * filing object, and finds the cheapest one. Puts each ID used
     * from the chosen items in used variable and updates the items array.
     * @param options A 2D array of all the options that can make a filing object
     */
    private void findCheapest(String[][] options) {
        int lowest = Integer.parseInt(options[0][3]); //Start with the first option
        int index = 0;
        for (int i = 1; i < options.length && options[i][0] != null; i++) {
            if (Integer.parseInt(options[i][3]) < lowest) { //cheaper option found, use instead
                lowest = Integer.parseInt(options[i][3]);
                index = i;
            }
        }
        if (options[index][0] != null && !alreadyBought(options[index][0])) {
            if (itemNum >=used.length) {
                String copy[][] = new String[used.length + 1][2];
                for (int i = 0; i < used.length; i++) {
                    copy[i][0] = used[i][0];
                    copy[i][1] = used[i][1];
                }
                used = copy;
            }
            used[itemNum][0] = options[index][0];
            used[itemNum][1] = findPrice(options[index][0]);
            itemNum ++;
        }
        if (options[index][1] != null && !alreadyBought(options[index][1])) {
            if (itemNum >=used.length) {
                String copy[][] = new String[used.length + 1][2];
                for (int i = 0; i < used.length; i++) {
                    copy[i][0] = used[i][0];
                    copy[i][1] = used[i][1];
                }
                used = copy;
            }
            used[itemNum][0] = options[index][1];
            used[itemNum][1] = findPrice(options[index][1]);
            itemNum ++;
        }
        if (options[index][2] != null && !alreadyBought(options[index][2])) {
            if (itemNum >=used.length) {
                String copy[][] = new String[used.length + 1][2];
                for (int i = 0; i < used.length; i++) {
                    copy[i][0] = used[i][0];
                    copy[i][1] = used[i][1];
                }
                used = copy;
            }
            used[itemNum][0] = options[index][2];
            used[itemNum][1] = findPrice(options[index][2]);
            itemNum ++;
        }
        items = markIfUsed(items, options[index][0], options[index][1], options[index][2]); //Mark the used items in the items array.
    }

    /**
     * makesFile returns a boolean corresponding to whether the items can make a full filing object.
     * Returns false otherwise. Overloaded; can be called with one, two or
     * three items.
     * @param one String array corresponding to one item
     * @return returns boolean
     */
    private boolean makesFile(String[] one) {
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

    /**
     * makesFile returns a boolean corresponding to whether the items can make a full filing object.
     * Returns false otherwise.
     * @param one String array corresponding to one item
     * @param two String array corresponding to second item
     * @return returns boolean
     */
    private boolean makesFile(String[] one, String[] two) {
        if (one[1].equals("Y") || two[1].equals("Y")) {
            if (one[2].equals("Y") || two[2].equals("Y")) {
                if (one[3].equals("Y") || two[3].equals("Y")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * makesFile returns a boolean corresponding to whether the items can make a full filing object.
     * Returns false otherwise
     * @param one String array corresponding to one item
     * @param two String array corresponding to second item
     * @param three String array corresponding to third item
     * @return returns boolean
     */
    private boolean makesFile(String[] one, String[] two, String[] three) {
        if (one[1].equals("Y") || two[1].equals("Y") || three[1].equals("Y")) {
            if (one[2].equals("Y") || two[2].equals("Y") || three[2].equals("Y")) {
                if (one[3].equals("Y") || two[3].equals("Y") || three[3].equals("Y")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * findPrice finds the price of the item at a given String ID
     * @param id String argument for ID
     * @return returns String of price
     */
    private String findPrice(String id) {
        for (int i = 0; i < items.length; i++) {
            if (id.equals(items[i][0])) {
                return items[i][4];
            }
        }
        return "";
    }

    /**
     * Marks the parts necessary to make a full filing system as "U" (used item). 
     * @param old The old 2D array String that has't marked the used items yet
     * @param first The id of the first item that potentially has been used
     * @param second The id of the second item that potentially has been used
     * @param third The id of the third item that potentially has been used
     * @return A String 2D array after the used items have been marked
     */
    private String[][] markIfUsed(String[][] old, String first, String second, String third) {
        int index1 = -1; //first piece
        int index2 = -1; //second piece
        int index3 = -1; //third piece
        if (first == null) {
            first = new String("notAnID");
        } else {
            for (int i = 0; i < items.length; i++) {
                if (first.equals(items[i][0])) {
                    index1 = i;
                }
            }//Find the location of item 1
        }
        if (second == null) {
            second = new String("notAnID");
        } else {
            for (int i = 0; i < items.length; i++) {
                if (second.equals(items[i][0])) {
                    index2 = i;
                }
            }//Find the location of item 2
        }
        if (third == null) {
            third = new String("notAnID");
        } else {
            for (int i = 0; i < items.length; i++) {
                if (third.equals(items[i][0])) {
                    index3 = i;
                }
            } //Find the location of item 3
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

    /**
     * getOrderString creates the String to be sent for the given order
     * @return returns String consisting of correct formatting
     */
    public String getOrderString() {
        StringBuilder toOrder = new StringBuilder("Original Request: ");
        toOrder.append(type + " Filing, " + toBuy + "\n\n");

        if (found[found.length-1]) { //items have been found, send this string as output
            toOrder.append("Items Ordered\n");
            for (int i = 0; i < used.length; i++) {
                toOrder.append("ID: " + used[i][0] + "\n");
            }
            toOrder.append("\nTotal Price: $" + getCost());
        } else { //items not found, send the manufacturers instead
            toOrder.append("Order cannot be fulfilled based on current inventory. ");
            toOrder.append("Suggested manufacturers are Office Furnishings, ");
            toOrder.append("Furniture Goods, and Fine Office Supplies.");
        }
        return toOrder.toString();
    }

    /**
     * Prints the IDs used and the total price or the suggested manufacturers 
     * depending on if the transaction is successful.
     */
    public void printString() {
        StringBuilder order = new StringBuilder();
        if (found[found.length-1]) {
            order.append("Purchase ");
            for (int i = 0; i < used.length; i++) {
                order.append(used[i][0]);
                if (i == used.length - 2) {
                    order.append(" and ");
                } else if (i < used.length - 2) {
                    order.append(" and ");
                }
            }
            order.append(" for $" + getCost() + ".");
        } else {
            order.append("Order cannot be fulfilled based on current inventory. ");
            order.append("Suggested manufacturers are Office Furnishings, ");
            order.append("Furniture Goods, and Fine Office Supplies.");
        }
        System.out.println(order);
    }

    /**
     * getter for the total order price
     * @return returns int of the total price
     */
    public int getCost() {
        int total = 0;
        for (int i = 0; i < used.length; i++) {
            total += Integer.parseInt(used[i][1]);
        }
        return total;
    }

    
    /**
	 * updateDatabase updates the given database to remove the used items.
	 * This method is only called if there is success in creating an order.
	 */
    public void updateDatabase() {
        PreparedStatement stmt = null;
        try {
            String query = "DELETE FROM Filing WHERE ID = ?";
            stmt = dbConnect.prepareStatement(query);
            for (int i = 0; i < used.length; i++) {
                for (int j = 0; j < used[i].length; j++) {
                    stmt.setString(1, used[i][j]);
                    stmt.executeUpdate();
                }
            }
            stmt.close();
        }
        catch (SQLException e) {
            close();
            System.err.println("SQLException when updating Filing.");
            System.exit(1);
        }
    }
    /**
     * close closes the connection and ResultSet objects. 
     */
    public void close() {
    	try {
			result.close();
			dbConnect.close();
		} catch (SQLException e) {
			System.out.println("Unable to close");
		}
    }
}
