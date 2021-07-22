/**
 * @author Andrea Benavides Aguirre
 * @version     3.4               
 * @since       1.0
 */

 /*ENSF 409 Final Project Group 7
	Filing.java
	Ahmed Waly, Alexis Hamrak, Andrea Benavides Aguirre, Heidi Toews*/

package edu.ucalgary.ensf409;
import java.sql.*;
import java.util.*;

/**The Lamp class finds the cheapest combination to make a certain amount of lamps that 
*is requested by the client from class Main*/
public class Lamp{

    //fields
    //database connection attributes
    /**database URL*/
    public final String DBURL;
    /**database username*/
    public final String USERNAME;
    /**database password*/
    public final String PASSWORD;
    private Connection dbConnect;
    private ResultSet results;

    private boolean isValid = true;

    private String type;                    //type of lamp to be constructed
    private int quantity;                   //number of lamps in order

    private String baseID;                  //ID of lamp base to be ordered from DB 
    private String bulbID;                  //ID of lamp bulb to be ordered from DB    
    private int bestPrice;                  //best overall price for lamp being constructed
    private int bestWholePrice;             //best price when looking at whole lamps
    private int bestSinglePrice;            //best price when looking at separate components
    private int totalPrice = 0;             //total order price

    private LampType[] bulbLamps;          //Array of type lamps that contain only working bulbs
    private LampType[] baseLamps;           //Array of type lamps that contain only working bases
    private LampType[] wholeLamps;          //Array of type lamps that contain both a working bulb and working base

    private ArrayList<LampType> lampsToOrder;       //ArrayList LampType that contain the lamps to be purchased

    private String orderString = "";             //String containing all the output information

    /** Lamp Constructor:
     *  - Checks for appropriate lamp type
     *  - Checks for at least one lamp to be made
     *  - Sets user specifications to fields
     *  - If no exceptions are found, begin creating order
     *        
     * @param type              lamp type specified by user
     * @param quantity          number of lamps specified by user
     * @param dbUrl             Database URL
     * @param username          Database Username
     * @param password          Database Password
     */
    public Lamp(String type, int quantity, String dbUrl, String username, String password){
        if(quantity < 0){
            throw new IllegalAccessError("Empty Order");
        }
        if((type.equalsIgnoreCase("desk") || type.equalsIgnoreCase("swing arm") || type.equalsIgnoreCase("study")) && quantity > 0){            //checks for appropriate lamp type and quantity > 0
            if(type.equalsIgnoreCase("swing arm")){
                this.type = "Swing Arm";
            } else {
                this.type = type.substring(0, 1).toUpperCase() + type.substring(1).toLowerCase(); //have the first letter of the type upercase and the rest lowercase
            }
            this.quantity = quantity;
            this.DBURL = dbUrl;
            this.USERNAME = username;
            this.PASSWORD = password;
            createLamps();
        } else {
            throw new IllegalArgumentException("Invalid Lamp type, please enter desk, study, or swing arm.");
        } 
    } 
    //end of constructor  

    //getters
    /**
     * getter for the type of lamp
     * @return String of the type
     */
    public String getType(){
        return this.type;
    }
    /**
     * getter for the quantity required
     * @return int of the quanity
     */
    public int getQuantity(){
        return this.quantity;
    }
    /**
     * getter for the type of the total price
     * @return int of the price
     */
    public int getTotalPrice(){
        return this.totalPrice;
    }
    /**
     * getter for the suggested manufacturers
     * @return String of the manufacturers
     */
    public String getManufacturerString(){
        return "Office Furnishings, Furniture Goods, and Fine Office Supplies";
    }
    /**
     * getter for isValid
     * @return boolean for isValid
     */
    public boolean getIsValid(){
        return this.isValid;
    }
    /**
     * getter for the database connection
     * @return Connection of the database
     */
    public Connection getDbConnect(){
        return this.dbConnect;
    }
    /**
     * getter for the lamps that will be ordered
     * @return ArrayList of LampType
     */
    public ArrayList<LampType> getLampsToOrder(){
        return this.lampsToOrder;
    }

    //end of getters

    /** Method createLamps:                                                                    
     *  - Initalize Database Connection
     *  - If order cannot be fullfilled, list recommended manufacturers and previously selected items are returned to the database
     *  - Find all the lowest prices for each item to be created and add them to the total order cost
     */
    public void createLamps(){
        lampsToOrder = new ArrayList<LampType>(this.quantity * 2);                              //initalize capactiy of arrayList to quantity*2
            initializeConnection();                                                             //initalize DB connection
            for(int i = 0; i < this.quantity; i++){
                if(!creationPossible()){                                                        //if creation of items is not possible, list recommended manufacturers
                    returnItems();                                                              //returns previously selected items if any
                    isValid = false;
                    i = this.quantity;
                } else {
                    getLowestPrice();                                                           //set lowest price for item being created
                    addPriceToTotal();                                                          //add item price to total order price
                }
                lampsToOrder.trimToSize();                                                     //trims unused capacity
            }
    }
    //end of method createLamps

    /** Method getOrderString
     *  - Is called after all calculations are made
     *  - sets the string to be passed to the output to have all relavent order information
     * @return A String of the output needed in the text file
     */
    public String getOrderString(){ 
        orderString = orderString + "Original Request: " + this.type + " Lamp, " + this.quantity + "\n\n";
        if(isValid){
        orderString = orderString + "Items Ordered\n";
        for(int i = 0; i < lampsToOrder.size(); i++){
            orderString = orderString + "ID: " + lampsToOrder.get(i).getID() + "\n";
        }
        orderString+="\n";
        orderString = orderString + "Total Price: $" + totalPrice;
    }
        else{
            orderString += "Order cannot be fulfilled based on current inventory. Suggested manufacturers are Office Furnishings, Furniture Goods, and Fine Office Supplies.";
        }
        return orderString;
    }
    //end of method setOrderString

    /** Method printTransaction
     *  - called from the main. Outputs relevant transaction information to the terminal
     * @return A String of the transaction information needed in the terminal
     */
    public String printTransaction(){
        if(!isValid){
            return "Order cannot be fulfilled based on current inventory. Suggested manufacturers are Office Furnishings, Furniture Goods, and Fine Office Supplies.";
        } else {
            String outputString = "Purchase ";
            for(int i = 0; i<lampsToOrder.size(); i++){
                outputString += lampsToOrder.get(i).getID() + " ";
                if(i != lampsToOrder.size()-1){
                    outputString += "and ";
                }
            }
            outputString = outputString + "for $" + totalPrice;
            return outputString;
        }
    }
    //end of printTransaction
  
    /** Method initalizeConnection:
     * - tries to create a connection with the database using the user provided credentials
     */
    public void initializeConnection(){
        try{
            dbConnect = DriverManager.getConnection(DBURL, USERNAME, PASSWORD);
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    // end of method initalizeConnection

    /** Method creationPossible
     *  - initalizes LampType arrays to hold a capacity equal to the number of items with the respective criteria from the DB
     *  - checks if a lamp item can be made based on the number of inventory parts found in the DB
     * @return Boolean if the lamp item can be made or not
     */
    public boolean creationPossible(){
        initializeArrays();                                 //initalizes LampType arrays
        int numOfBases = baseLamps.length;                  
        int numOfBulbs = bulbLamps.length;
        int numOfWholes = wholeLamps.length;

        if(numOfBases < 1 && numOfWholes < 1){              //if there are no whole lamps found in the DB and no lamps with working bases a lamp cannot be created
            return false;
        }
        if(numOfBulbs < 1 && numOfWholes < 1){              //if there are no whole lamps found in the DB and no lamps with working bulbs a lamp cannot be created
            return false;
        }

        return true;
    }
    //end of method creationPossible

    /** Method initializeArrays
     * - sends all the LampType arrays to be initalized to hold the number of lamps which are found in the DB that have the respective query criteria
     */
    public void initializeArrays() {    
        baseLamps = new LampType[countQuery('Y','N')];              //initalize baseLamps where the base is true and the bulb is false
        bulbLamps = new LampType[countQuery('N','Y')];              //initalize bulbLamps where the base if false and the bulb is true
        wholeLamps = new LampType[countQuery('Y','Y')];             //initalize wholeLamps where the base is true and the bulb is true
    }
    //end of method initalizeArrays

    /** Method countQuery
     *  - Makes a database query based on the parameters passed in from initalizeArrays
     *  - returns the number of items found from the query
     * 
     * @param baseStatus indicates if we are looking for a working base or not
     * @param bulbStatus indicates if we are looking for a working bulb or not
     * @return int of the number of items found from the uery
     */
    public int countQuery(char baseStatus, char bulbStatus){
        try{
            Statement myStmt = dbConnect.createStatement();
            results = myStmt.executeQuery("SELECT COUNT(*) FROM lamp WHERE type=\"" + type + "\" AND base=\"" + baseStatus + "\" AND bulb=\"" + bulbStatus + "\"");     //searches the lamp DB to find objects matching the given type, base, and bulb
            results.next();
            int theSize = results.getInt("COUNT(*)");               //saves the number of items found from the query
            myStmt.close();
            return theSize;                                         //returns the total number of items found from the query
        } catch (SQLException e){
            e.printStackTrace();
            return -1;
        }
    }
    //end of method countQuery

    /** Method returnItems
     * - if the creation of an object fails due to insufficient items in the database at any moment we return the items that have already been selected to purchase (stored in
     * lampsToOrder) to the database
     */
    public void returnItems(){
        LampType temp;
        try{
            for(int i = 0; i < lampsToOrder.size(); i++){
                temp = lampsToOrder.get(i);                                     //saves LampTypes from the ArrayList lampsToOrder to then return information to DB
                Statement myStmt = dbConnect.createStatement();
                myStmt.execute("INSERT INTO lamp (ID, Type, Base, Bulb, Price, ManuID) VALUES (\"" + temp.getID() + "\", \"" + temp.getType() + "\", \"" + temp.getBase() +"\", \""
                + temp.getBulb() + "\", \"" + temp.getPrice() + "\", \"" + temp.getManuID() + "\")");               //query inserts the LampType in temp back into the DB
                myStmt.close();
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    /** Method getLowestPrice
     *  - add lamp elements to each LampType array
     *  - get the lowest price if we were to chose a whole lamp
     *  - get the lowest price is we were to assemble using different pieces from different lamps 
     *  - add lowest price lamp(s) to lampsToOrder and remove them from the DB
     */
    public void getLowestPrice(){
        fetchLamps();                       //fill arrays with corresponding lamps that fulfill given criteria
        compareWholePrice();                //get the lowest price for a whole lamp if there are any
        compareSinglePrice();               //get the lowest price for mixing and matchng lamps if there are any
        if(baseID != bulbID){                               
            addOrRemoveFromDB("SELECT *",baseID);               //Save the lamp with the maching ID to lampsToOrder. Only happens if two lamps are bought instead of one -- base lamp
            addOrRemoveFromDB("DELETE",baseID);                 //Remove the lamp with the matching ID from the DB
            }
            addOrRemoveFromDB("SELECT *",bulbID);               //Save the lamp with the matching ID to lampsToOrder -- bulb Lamp
            addOrRemoveFromDB("DELETE",bulbID);                 //Remove the lamp with the matching ID form the DB
    }
    //end of method getLowestPrice

    /** Method fetchLamps 
     *  - fills each array with the corresponding lamps that fullfill the conditions passed in
     */
    public void fetchLamps(){
            lampQuery('Y', 'N');
            lampQuery('N','Y');
            lampQuery('Y','Y');
            
    }
    // end of method fethcLamps

    /** Method lampQuery
     *  - creates a query that searches for lamps with or without bases and bulbs
     *  - depending on the parameters passed either baseLamps, bulbLamps, or wholeLamps are filled in with the generated query results
     * 
     * @param baseStatus  indicates if we are looking for a lamp with a working base or not
     * @param bulbStatus  indicates if we are looking for a lmap with a working bulb or not
     */
    public void lampQuery(char baseStatus, char bulbStatus){
        try{
            Statement myStmt = dbConnect.createStatement();
            ResultSet results = myStmt.executeQuery("SELECT * FROM lamp where type=\"" + type + "\" AND base=\"" + baseStatus + "\" AND bulb=\"" + bulbStatus + "\"");      //searches for lamps with the give criteria in the DB
            int i = 0;
            while(results.next()){
                LampType temp = new LampType(results.getString("Id"), results.getString("Type"), results.getString("Base"), results.getString("Bulb"), results.getInt("Price"), results.getString("ManuID"));
                if(baseStatus == 'Y' && bulbStatus == 'N') {            
                    baseLamps[i] = temp;                                //update baseLamps if the parameters are fullfilled
                } else if (baseStatus == 'N' && bulbStatus == 'Y'){
                    bulbLamps[i] = temp;                                //update bulbLamps if the parameters are fullfilled
                } else if(baseStatus == 'Y' && bulbStatus == 'Y'){
                    wholeLamps[i] = temp;                               //update wholeLamps if the parameters are fullfilled
                }
                i++;
            }
            myStmt.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }
    //end of method lampQuery

    /** Method addOrRemoveFromDB
     *  - if the keyword passed in is "SELECT *" then a query is generated to find a lamp that has a matching ID. The lamp is returned as a resultSet and added to arrayList lampsToOrder
     *  - if the keyword is "DELETE" then a query is generated to find and delete a lamp with the matching ID
     * 
     * @param keyword   String that indicates intructions for query and differentiates what to do with results
     * @param id        lamp ID that must be matched to add or remove lamps from the DB
     */
    public void addOrRemoveFromDB(String keyword, String id){
        try{
            Statement myStmt = dbConnect.createStatement();
            String query = keyword + " FROM lamp WHERE ID=\"" + id + "\"";
            if(keyword.equals("DELETE")){
                myStmt.execute(query);                          //removes lamp with matching ID from DB
            }
            if(keyword.equals("SELECT *")){
                results = myStmt.executeQuery(query);
                results.next();
                LampType temp = new LampType(results.getString("Id"), results.getString("Type"), results.getString("Base"), results.getString("Bulb"), results.getInt("Price"), results.getString("ManuID"));
                lampsToOrder.add(temp);                         //adds lamp with matching ID to lampsToOrder
            }
            myStmt.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    //end of method addOrRemoveFromDB

    /** Method compareWholePrice 
     *  - If the array wholeLamps is not empty, we go through all the lamps stored in wholeLamps and find the lamp with the lowest price.
     *  - The lamp with the lowest price from wholeLamps has its price saed to bestWholePrice and it's ID saved to baseID and bulbID
     */
    public void compareWholePrice(){
        if(wholeLamps.length == 0){             //checks if array is empty
            return;
        }
        
        bestWholePrice = wholeLamps[0].getPrice();          //sets the price of the first lamp in the array as the first comparator
        baseID = wholeLamps[0].getID();                     
        bulbID = wholeLamps[0].getID();
        for(int i = 1; i < wholeLamps.length; i++){
            if(wholeLamps[i].getPrice() < bestWholePrice){              //if the next lamp in the array has a lower price than the comparator, that lamp becomes the comparator
                bestWholePrice = wholeLamps[i].getPrice();              
                baseID = wholeLamps[i].getID();                         //Update the baseID and bulbID to reflect the new lowest price
                bulbID = wholeLamps[i].getID();
            }
        }           
    }
    //end of method compareWholePrice

    /** Method compareSinglePrice
     *  - If both bulbLamps and baseLamps arrays are not empty, we will go through each respective array and find the lamps with the lowest prices in both arrays. The lamps with the
     *  lowest prices from each array are added together and compared with the lowest price from the wholeLamps array (if applicable). If the separate components of two lamps have a 
     *  lower combined price than that of a whole lamp, the sum of the two arrays becomes the best overall price and the ID's of the two lamps are used as the lamps to be purchased.
     *  If the whole lamp has a lower price, then that becomes the best overall price.
     */
    public void compareSinglePrice(){
        if(bulbLamps.length == 0 || baseLamps.length == 0){                 //checks if bulbLamps or baseLamps are empty, if so the best Whole lamp price becomes the best price
            bestPrice = bestWholePrice;
            return;
        }
        
        int tempBasePrice = baseLamps[0].getPrice();                        //sets the first Lamp in baseLamp as the comparator price
        String tempBaseID = baseLamps[0].getID();                          
        for(int i = 1; i < baseLamps.length; i++) {                         //loops through the subsequent lamps in baseLamp
            if(baseLamps[i].getPrice() < tempBasePrice){                    //if the price of the lamp at index i is less than the comparator price, that lamp becomes the comparator
                tempBasePrice = baseLamps[i].getPrice();
                tempBaseID = baseLamps[i].getID();
            }
        }

        int tempBulbPrice = bulbLamps[0].getPrice();                        //sets the first lamp in bublLamp as the comparator price
        String tempBulbID = bulbLamps[0].getID();
        for(int i = 1; i < bulbLamps.length; i++){                          //loops through the subsequent lamps in bulbLamp
            if(bulbLamps[i].getPrice() < tempBulbPrice){                    //if the price of the lamp at index i is less than the comparator price, that lamp becomes the comparator
                tempBulbPrice = bulbLamps[i].getPrice();    
                tempBulbID = bulbLamps[i].getID();
            }
        }

        bestSinglePrice = tempBasePrice + tempBulbPrice;                        //add together the lowest prices from baseLamp and bulbLamp
        if(wholeLamps.length == 0 || (bestSinglePrice < bestWholePrice)){       //if there were no lamps in wholeLamps or the price of the single components is lower than the best whole price, then the bestPrice becomes the sum of the two lamps 
            bestPrice = bestSinglePrice;                                        //update the bestPrice to reflect the sum of the two lamps
            baseID = tempBaseID;                                                //update the baseID to reflect the correct lamp    
            bulbID = tempBulbID;                                                //update the bulbID to reflect the correct lamp
            return;
        }
        bestPrice = bestWholePrice;                                             //if the price of the whole lamp is less than the price of the two lamps then the price of the whole lamp becomes the best price
        return;               
    }
    //end of method compareSinglePrice

       /** Method addPriceToTotal
     *  - updates total price after the best price for each item has been found
     */
    public void addPriceToTotal(){
        totalPrice += bestPrice;
    }
    //end of method addPriceToTotal

    /** Method close
	 *  - closes DB connection
	 */
	public void close() {
		try {
			results.close();
			dbConnect.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
//end of class Lamp
    

  
 
