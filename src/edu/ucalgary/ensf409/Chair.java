package edu.ucalgary.ensf409;
import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 @author Ahmed Waly <a href="mailto:ahmed.waly@ucalgary.ca"> ahmed.waly@ucalgary.ca</a>
 @version    1.7
 @since      1.0
 */
/**ENSF 409 Final Project Group 7
 * Chair.java
 * Ahmed Waly, Alexis Hamrak, Andrea Benavides Aguirre, Heidi Toews
 * */
/**class Chair is used to find the cheapest combination to make a certain amount of chairs that 
*is requested by the user from class Main*/
public class Chair {
    /** Represents the database URL*/
    public final String DBURL;
    /**Represents the database Username */
    public final String USERNAME;
    /** Represents the database Password*/
    public final String PASSWORD;
    private String type; //stores the type that is needed for the transaction
    private int numberOfChairsRequired; //stores the number of chairs required for the transaction
    private int numberOfChairsAssembled; //stores the number of chairs that have been assembled, 
    //if the number of chairs assembled equals the number of chairs required then the transaction is successful.
    private String[][] chairData; //2D array that stores the information of the chair data of the type required from the database
    private int totalPrice=0; //the total price of the transaction
    private Connection dbConnect;
    private ResultSet results;
    private ArrayList<Integer> listOfPrices = new ArrayList<Integer>(); //stores all the possible prices that can assemble a chair
    private ArrayList<String> listOfID = new ArrayList<String>(); //stores all possible combinations of IDs that can assemble a chair
    private int[] prices; //stores the prices from the ArrayList, listOfPrices, into an array
    private int minIndex; //stores the index of prices that contains the lowest price
    private StringBuffer itemsID = new StringBuffer(); //stores the IDs that were used in the transaction
    private String[] leftover = {"leftover","N","N","N","N","0"}; //keeps track of the leftover parts
    
    /**
     * The user defined constructor assigns values for the URL, username, and 
     * password of the database that should be accessed. This constructor also 
     * calls the checkType() and selectTypeChairs() methods.
     * @param url String of the database URL
     * @param username String of the database username
     * @param password String of the database password
     * @param type String type of chair that is required
     * @param numberOfChairsRequired int of the number of chairs requested
     */
    public Chair(String url, String username, String password, String type, int numberOfChairsRequired){
        this.DBURL = url;
        this.USERNAME = username;
        this.PASSWORD = password;
        this.type = type.substring(0, 1).toUpperCase() + type.substring(1).toLowerCase(); //have the first letter of the type upercase and the rest lowercase
        this.checkType(); // makes sure the type is valid
        this.numberOfChairsRequired = numberOfChairsRequired;
        this.initializeConnection(); //initialize connection to the database
        this.selectTypeChairs(); //initializes chairData based on the type that was passed
    }
    /**
     * getter method for the type of chair that was required
     * @return Returns a String of the type of chair
     */
    public String getType() {
        return this.type;
    }
    /**
     * getter method for the number of chairs that was required
     * @return Returns an int of the number
     */
    public int getNumberOfChairsRequired() {
        return this.numberOfChairsRequired;
    }

    /**
     * getter method for itemsID in String form.
     * @return Returns a String of the chair IDs that were used in the transaction 
     * seperated by a newline.
     */
    public String getItemsID(){
        return this.itemsID.toString();
    }
    /**
     * getter method for total price in int form.
     * @return Returns an int of the price
     */
    public int getTotalPrice(){
        return this.totalPrice;
    }
     /**
     * getter method for the the database connection
     * @return Returns a Connection of the database
     */
    public Connection getDbConnect() {
        return this.dbConnect;
    }
    /**
     * This private method updates itemsID by adding any IDs that have been used in the 
     * transaction. This method doesn't take in any argument and returns a void.
     */
    private void updateItemsID(){
        itemsID.append(listOfID.get(minIndex).replace("+","\n"));
        itemsID.append("\n");
    }
    /**
     * This private method is named initializeConnection and it 
     * creates a connection between the Inventory Class 
     * and the local host that has database URL, username, and password that 
     * And if the connection wasn't successfully made, the method will catch the 
     * SQLException and print the stack trace. 
     * This method doesn't take in any arguments and also returns a void.
     */
    private void initializeConnection(){       
        try{
            dbConnect = DriverManager.getConnection(DBURL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /**
     * This private method is named checkType and it checks if the type 
     * that the user inputs is valid, this method is sensitive to capitalizaton.
     * If it is not valid the method throws an IllegalArgumentException.
     */
    private void checkType(){
        if (!(type.equals("Mesh"))&&!(type.equals("Executive"))&&!(type.equals("Task"))&&!(type.equals("Kneeling"))&&!(type.equals("Ergonomic"))){
            throw new IllegalArgumentException("Invalid Chair type, please enter mesh, ergonomic, task, kneeling, or executive.");
        }
    }
    /**
     * This public method is named calculateCheapestOption and it calculates 
     * the cheapest price to assemble the required number of chairs using the 
     * chair components within the invenory database. This method doesn't take 
     * in any arguments and returns a String of the suggested manufacturers or 
     * the purchased items along with the price.
     * @return A String of the purchased items along with the price of the 
     * suggested manufacturers
     */
    public String calculateCheapestOption(){
        //check if their are enough pieces to assemble the number of chairs requested
        if (!checkPieces(1)||!checkPieces(2)||!checkPieces(3)||!checkPieces(4)){
            //if there are not enough pieces
            itemsID.setLength(0); //remove the IDs
            numberOfChairsAssembled =0; //reset the number of chairs assembled
            totalPrice=0;
            return("Order cannot be fulfilled based on current inventory. Suggested manufacturers are Office Furnishings, Chairs R Us, Furniture Goods, and Fine Office Supplies.");
        }
        for(int i=0;i<numberOfChairsRequired;i++){ //call the methods depending on how many chairs are required
            allPossiblePrices(); //get all the possible prices
            pricesToArray(); //convert the arraylist of possible prices to an array
            totalPrice+=getLowestPrice(); //increment the lowest price in the array to the total price
            
            if(prices.length==0){ //if there isn't any possible prices break from the loop
                break;
            }

            updateItemsID(); //add the IDs in itemID
            updateTable(); //temporarily update the dable
            listOfID.clear(); // clear arrayLists of prices and ID
            listOfPrices.clear();
            numberOfChairsAssembled++; //increment the numberOfchairs assembled by 1
        }
        if(numberOfChairsAssembled < numberOfChairsRequired){
            //if the chairs assembled is less than the chairs required, return the manufacturers and reset the number namuber of chairs assembled.
            itemsID.setLength(0); //remove the IDs
            numberOfChairsAssembled =0; //reset the number of chairs assembled
            totalPrice=0;
            return("Order cannot be fulfilled based on current inventory. Suggested manufacturers are Office Furnishings, Chairs R Us, Furniture Goods, and Fine Office Supplies.");
        }else{ //if there are enough chairs assembled, update the database and return the purchased items along with the price
            updateDatabase();
            String[] id = getItemsID().split(Pattern.quote("\n"));
            StringBuffer idOutput = new StringBuffer();
            for(int i=0; i<id.length;i++){
                if(i==0 && !(id[i].equals("leftover"))){
                    idOutput.append(id[i]);
                }
                if(i>0 &&!(id[i].equals("leftover"))){
                    idOutput.append(" and "+ id[i]);
                }
               
            }
            
            return ("Purchase "+ idOutput+" for $"+getTotalPrice());
        }
    }
    /**
     * This private method is named selectTypeChairs and it initializes the 
     * 2D array, chairData, with all the columns within the chair table of the 
     * local database. If an SQLexception happened, the method will print a 
     * comment and then print the stack trace.
     */
    private void selectTypeChairs(){
        chairData= new String[this.getRowCount()][7];//construct the chairData array with the number of rows that match the type needed and 7 columns
        try {                    
            Statement myStmt = dbConnect.createStatement();
            results = myStmt.executeQuery("SELECT * FROM chair");
            int i=0;
            while (results.next()){
                String chairType = results.getString("Type");
                if(chairType.equals(type)){ //if the chair type in the database matches the type needed, add the columns in the database table into chairData
                    chairData[i][0]=results.getString("ID");
                    chairData[i][1]=results.getString("Legs");
                    chairData[i][2]=results.getString("Arms");
                    chairData[i][3]=results.getString("Seat");
                    chairData[i][4]=results.getString("Cushion");
                    chairData[i][5]=results.getString("Price");
                    chairData[i][6]=results.getString("ManuID");
                    i++;
                }
            }
            myStmt.close();
        }catch (SQLException e) {
            System.out.println("Couldn't Read the columns of the chair");
            e.printStackTrace();
        }
    }
    /**
     * This private method is named checkPieces and it ensures there are enough 
     * chair peices to fulfill the number of chairs being requested.
     * If there are not enough pieces, the method will return false; otherwise 
     * it will return true.
     * @param column the column index of the chair peice being checked in the 
     * array chairData.
     * @return Returns true if there are enough pieces to fulfill the request, 
     * false otherwise.
     */
    private boolean checkPieces(int column){
        int numberOfPieces=0;//number of chair pieces in the database, the type of piece 
        //depends on the column being passed (eg. if column=1, the number represents how many legs are available)*/
        for (int i=0; i< chairData.length;i++){
            if (chairData[i][column].equals("Y")){ //if the type is available, increment number
                numberOfPieces++;
            }
        }
        if(numberOfPieces<numberOfChairsRequired){// if the number of available pieces is less than the number of chairs required return false
            return false;
        }
        return true; 
    }
    /**
     * This public method is named getRowCount and it counts the number of 
     * rows within the chair table that have the same type that was specified
     * by the user. This row count is then returned as an int.
     * @return The number of rows that have the same type that was in the user input.
     */
    public int getRowCount(){
        int numberOfRows=0; // the number of rows in the database that match the type needed
        try {                    
            Statement myStmt = dbConnect.createStatement();
            results = myStmt.executeQuery("SELECT * FROM chair"); //select all from table chair
            while(results.next()){ //iterate throw the rows of table chair
                String chairType = results.getString("Type"); //get the chair type of the row
                if(chairType.equals(type)){ //if the chair type matches the type needed increment the number of rows
                    numberOfRows++;
                }
            }
        }catch (SQLException e) { //if an SQLException occurs print a message and the stack Trace
            System.out.println("Couldn't count rows");
            e.printStackTrace();
        }
        return numberOfRows;
    }
    /**
     * This private method is named allPossiblePrices and it stores all the 
     * possible prices within an ArrayList called listOfPrices and all the 
     * possible combinations of id that could be made to generate a 
     * single chair into listOfID by calling addPricesAndID method. 
     * This method gets called multiple times depending on how many 
     * chairs are needed. This method doesn't take in any arguments and returns 
     * a void.
     */
    private void allPossiblePrices(){
            for(int i=0;i<chairData.length-1;i++){ //loop through each row in chairData
                //call addPricesAndID with all the different combinations of components
                //pass in the combination that matches the row along with the row index of chairData
                if(chairData[i][1].equals("Y") && chairData[i][2].equals("Y") &&chairData[i][3].equals("Y") &&chairData[i][4].equals("Y")){
                    addPricesAndID("Y", "Y", "Y", "Y", i); 
                }else if(chairData[i][1].equals("N") && chairData[i][2].equals("Y") &&chairData[i][3].equals("Y")&&chairData[i][4].equals("Y")){
                    addPricesAndID("N", "Y", "Y", "Y", i);
                }else if(chairData[i][1].equals("Y") && chairData[i][2].equals("N") &&chairData[i][3].equals("Y")&&chairData[i][4].equals("Y")){
                    addPricesAndID("Y", "N", "Y", "Y", i);
                }else if(chairData[i][1].equals("Y") && chairData[i][2].equals("Y")&&chairData[i][3].equals("N")&&chairData[i][4].equals("Y")){
                    addPricesAndID("Y", "Y", "N", "Y", i);
                }else if(chairData[i][1].equals("Y") && chairData[i][2].equals("Y") &&chairData[i][3].equals("Y")&&chairData[i][4].equals("N")){
                    addPricesAndID("Y", "Y", "Y", "N", i);
                }else if(chairData[i][1].equals("N") && chairData[i][2].equals("N") &&chairData[i][3].equals("Y")&&chairData[i][4].equals("Y")){
                    addPricesAndID("N", "N", "Y", "Y", i);
                }else if(chairData[i][1].equals("N") && chairData[i][2].equals("Y") &&chairData[i][3].equals("N")&&chairData[i][4].equals("Y")){
                    addPricesAndID("N", "Y", "N", "Y", i);
                }else if(chairData[i][1].equals("N") && chairData[i][2].equals("Y") &&chairData[i][3].equals("Y")&&chairData[i][4].equals("N")){
                    addPricesAndID("N", "Y", "Y", "N", i);
                }else if(chairData[i][1].equals("Y") && chairData[i][2].equals("N") &&chairData[i][3].equals("N")&&chairData[i][4].equals("Y")){
                    addPricesAndID("Y", "N", "N", "Y", i);
                }else if(chairData[i][1].equals("Y") && chairData[i][2].equals("N") &&chairData[i][3].equals("Y")&&chairData[i][4].equals("N")){
                    addPricesAndID("Y", "N", "Y", "N", i);
                }else if(chairData[i][1].equals("Y") && chairData[i][2].equals("Y") &&chairData[i][3].equals("N")&&chairData[i][4].equals("N")){
                    addPricesAndID("Y", "Y", "N", "N", i);
                }else if(chairData[i][1].equals("Y") && chairData[i][2].equals("N") &&chairData[i][3].equals("N")&&chairData[i][4].equals("N")){
                    addPricesAndID("Y", "N", "N", "N", i);
                }else if(chairData[i][1].equals("N") && chairData[i][2].equals("Y") &&chairData[i][3].equals("N")&&chairData[i][4].equals("N")){
                    addPricesAndID("N", "Y", "N", "N", i);
                }else if(chairData[i][1].equals("N") && chairData[i][2].equals("N") &&chairData[i][3].equals("Y")&&chairData[i][4].equals("N")){
                    addPricesAndID("N", "N", "Y", "N", i);
                }else if(chairData[i][1].equals("N") && chairData[i][2].equals("N") &&chairData[i][3].equals("N")&&chairData[i][4].equals("Y")){
                    addPricesAndID("N", "N", "N", "Y", i);
                }else{
                    continue;
                }
            }
    }
    /**
     * This private method is called addPricesAndID and it finds all the possible 
     * combinations that can make a complete chair from a single row in chairData.
     * This method stores the possible prices of the combination in the arrayList 
     * listOfPrices and and the possible ID combinations in listOfID.
     * @param legs The status of legs in a certain row of chairData ("Y" or "N")
     * @param arms The status of arms in a certain row of chairData ("Y" or "N")
     * @param seat The status of seat in a certain row of chairData ("Y" or "N")
     * @param cushion The status of cushion in a certain row of chairData ("Y" or "N")
     * @param row The row index of the chairData
     */
    private void addPricesAndID(String legs, String arms, String seat, String cushion, int row){
        int numberOfN=0; //count the number of components that have "N"
        int column1=0; //the index of the column with the first "N" if it exists
        int column2=0; //the index of the column with the second "N" if it exists
        int column3=0; //the index of the column with the third "N" if it exists
        if(legs.equals("N")){ //if the status of legs is "N"
            numberOfN++;
            column1=1;
        }
        if(arms.equals("N")){ //if the status of arms is "N"
            numberOfN++;
            if(column1==0){//if column1 hasn't been set to an index
                column1=2;
            }else{ //else set column2
                column2 =2;
            }
        }
        if(seat.equals("N")){ //if the status of seat is "N"
            numberOfN++;
            if(column1==0){//if column1 hasn't been set to an index
                column1=3;
            }else if(column2==0){ //else if column2 hasn't been set to an index
                column2 =3;
            }else{ //else set column 3
                column3 =3;
            }
        }
        if(cushion.equals("N")){ //if the status of cushion is "N"
            numberOfN++;
            if(column1==0){ //if column1 hasn't been set to an index
                column1=4;
            }else if(column2==0){ //else if column2 hasn't been set to an index
                column2 =4;
            }else{ //else set column 3
                column3 =4;
            }
        }
        int temperaryPrice;
        String temperaryID= new String();
        if(numberOfN==0){ //if the row doesn't have any "N"
            listOfPrices.add(Integer.parseInt(chairData[row][5])); //add the price of the row to the listOfPrices
            listOfID.add(chairData[row][0]); //add the ID of the row to the listOfPrices
        }else if(numberOfN==1){ //if the row has one "N"
                for(int j=row+1;j<chairData.length;j++){ //loop through the other rows with int j
                    if(chairData[j][column1].equals("Y")){ //if a row that has "Y" of column1 is reached
                        //add the price of both rows to the listOfPrices
                        temperaryPrice=Integer.parseInt(chairData[j][5]);
                        temperaryPrice+=Integer.parseInt(chairData[row][5]);
                        listOfPrices.add(temperaryPrice); 
                        //add the IDs of both rows to the listOfID seperated by "+"
                        temperaryID =chairData[row][0]+"+"+chairData[j][0];
                        listOfID.add(temperaryID); 
                    }
                }
        }else if(numberOfN==2){ //if the row has two "N"s
            for(int j=row+1;j<chairData.length;j++){ //loop through the other rows with int j
                if(chairData[j][column1].equals("Y")){ //if a row that has "Y" of column1 is reached
                    for(int k=row+1;k<chairData.length;k++){ //loop again through the other rows with int k
                        if(chairData[k][column2].equals("Y")){ //if a row that has "Y" of column2 is reached
                            if(k!=j){ //if the rows of k and j are different
                                //add the prices of the two rows along with the row argument that was passed to the listOfPrices
                                temperaryPrice=Integer.parseInt(chairData[j][5]);
                                temperaryPrice+=Integer.parseInt(chairData[row][5]);
                                temperaryPrice+=Integer.parseInt(chairData[k][5]);
                                listOfPrices.add(temperaryPrice);
                                //add the IDs of the three rows to the listOfID seperated by "+"
                                temperaryID =chairData[row][0]+"+"+chairData[j][0]+"+"+chairData[k][0];
                                listOfID.add(temperaryID);
                            }else{ // if rows k and j are the same
                                //add the price of the j row with the price of the row that was passed to the listOfPrices
                                temperaryPrice=Integer.parseInt(chairData[j][5]);
                                temperaryPrice+=Integer.parseInt(chairData[row][5]);
                                listOfPrices.add(temperaryPrice);
                                //add the IDs of the two rows to the listOfID seperated by "+"
                                temperaryID =chairData[row][0]+"+"+chairData[j][0];
                                listOfID.add(temperaryID);
                            }
                        }
                    }
                }
            }

        }else if(numberOfN==3){ // if the row has three "N"s
            for(int j=row+1;j<chairData.length;j++){//loop through the other rows with int j
                if(chairData[j][column1].equals("Y")){ //if a row that has "Y" of column1 is reached
                    for(int k=row+1;k<chairData.length;k++){//loop again through the other rows with int k
                        if(chairData[k][column2].equals("Y")){//if a row that has "Y" of column2 is reached
                            for(int z=row+1;z<chairData.length;z++){//loop again through the other rows with int z
                                if(chairData[z][column3].equals("Y")){//if a row that has "Y" of column3 is reached
                                    if(k==j && k==z){ //if k, j, and z are the same row
                                        //add the price of the j row with the price of the row that was passed to the listOfPrices
                                        temperaryPrice=Integer.parseInt(chairData[j][5]);
                                        temperaryPrice+=Integer.parseInt(chairData[row][5]);
                                        listOfPrices.add(temperaryPrice);
                                        //add the IDs of the two rows to the listOfID seperated by "+"
                                        temperaryID =chairData[row][0]+"+"+chairData[j][0];
                                        listOfID.add(temperaryID);
                                    }else if (k==j || k==z){ //if k and j or k and z are the same row
                                        //add the prices of the j and z rows along with the row argument that was passed to the listOfPrices
                                        temperaryPrice=Integer.parseInt(chairData[j][5]);
                                        temperaryPrice+=Integer.parseInt(chairData[row][5]);
                                        temperaryPrice+=Integer.parseInt(chairData[z][5]);
                                        listOfPrices.add(temperaryPrice);
                                        //add the IDs of the j, z, and row argument rows to the listOfID seperated by "+"
                                        temperaryID =chairData[row][0]+"+"+chairData[j][0]+"+"+chairData[z][0];
                                        listOfID.add(temperaryID);
                                    }else if (j==z){ // if j and z are the same row
                                         //add the prices of the k and z along with the row argument that was passed to the listOfPrices
                                        temperaryPrice=Integer.parseInt(chairData[row][5]);
                                        temperaryPrice+=Integer.parseInt(chairData[k][5]);
                                        temperaryPrice+=Integer.parseInt(chairData[z][5]);
                                        listOfPrices.add(temperaryPrice);
                                        //add the IDs of the k, z, and row argument to the listOfID seperated by "+"
                                        temperaryID =chairData[row][0]+"+"+chairData[k][0]+"+"+chairData[z][0];
                                        listOfID.add(temperaryID);
                                    }else{// if all rows are different
                                        //add the prices of the j, k, and z along with the row argument that was passed to the listOfPrices
                                        temperaryPrice=Integer.parseInt(chairData[row][5]);
                                        temperaryPrice+=Integer.parseInt(chairData[k][5]);
                                        temperaryPrice+=Integer.parseInt(chairData[z][5]);
                                        temperaryPrice+=Integer.parseInt(chairData[j][5]);
                                        listOfPrices.add(temperaryPrice);
                                        //add the IDs of the j, k, z, and row argument to the listOfID seperated by "+"
                                        temperaryID=chairData[row][0]+"+"+chairData[k][0]+"+"+chairData[z][0];
                                        temperaryID+="+";
                                        temperaryID+=chairData[j][0];
                                        listOfID.add(temperaryID);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    /**
     * This private method is named updateDatabase and it updates the local 
     * database by deleting all the chair items that have been purchased.
     * This method doesn't take in any arguments and returns a void.
     */
    private void updateDatabase(){
        String[] id = getItemsID().split(Pattern.quote("\n")); //convert all the itemsID to an array of String
        
        for(int i=0;i<id.length;i++){// loop through the id array
            try {
                String query = "DELETE FROM chair WHERE ID = ?";
                PreparedStatement myStmt = dbConnect.prepareStatement(query);
                myStmt.setString(1, id[i]); // delete each row in the database that have IDs that match the ones in the id array
                myStmt.executeUpdate(); //update the database
                myStmt.close();
            }catch (SQLException ex) { //if an SQLException occurs, print the stack trace
                ex.printStackTrace();
            }
        }
    }
    /**
     * This private method is named updateTable and it updates the 2D array 
     * of the chairData, by deleting the rows of the chair items that are 
     * temporarily being used. The purpose of this method is to avoid updating 
     * the database before knowing that there are enough chair items to 
     * complete the transaction. This method doesn't take in any arguments 
     * and returns a void.
     */
    private void updateTable(){
        String[] id = listOfID.get(minIndex).split(Pattern.quote("+")); //convert the listOfID at a certain index to a String array ID
        //create a new 2D array of chairData having the IDs that are temporarily being used, removed from the array
        String[][] newChairData = new String[(chairData.length-(id.length))+1][7]; 
        int x=0; //represents the index of the new array
        int n=0; //this is an indicator to know if the row in chairData contains one of the IDs that are being temporarily used
    
        int numberOfYInColumn1=0;
        int numberOfYInColumn2=0;
        int numberOfYInColumn3=0;
        int numberOfYInColumn4=0;
        for(int i=0; i<chairData.length;i++){ //loop through the rows of chairData
            for(int j=0;j<id.length;j++){ //loop through the id array
                if(chairData[i][0].equals(id[j])){
                     //count the number of Y's for each column when combining the cheapest rows
                    if(chairData[i][1].equals("Y")){
                        numberOfYInColumn1++;
                    }
                    if(chairData[i][2].equals("Y")){
                        numberOfYInColumn2++;
                    }
                    if(chairData[i][3].equals("Y")){
                        numberOfYInColumn3++;
                    }
                    if(chairData[i][4].equals("Y")){
                        numberOfYInColumn4++;
                    }
                }
            }
        }
        //if there is more than one y in the column mark it as leftover
        if(numberOfYInColumn1>1){ 
            leftover[1]="Y";
        }else{
            leftover[1]="N";
        }
        if(numberOfYInColumn2>1){
            leftover[2]="Y";
        }else{
            leftover[2]="N";
        }
        if(numberOfYInColumn3>1){
            leftover[3]="Y";
        }else{
            leftover[3]="N";
        }
        if(numberOfYInColumn4>1){
            leftover[4]="Y";
        }else{
            leftover[4]="N";
        }       
        for(int i=0; i<chairData.length;i++){ //loop through the rows of chairData
            for(int j=0;j<id.length;j++){ //loop through the id array
                if(chairData[i][0].equals(id[j])){ // if a row in chairData contains an id being used, set n=1
                    n=1;
                }
            }
            if(n==0){ //if n=0, copy the row into the new array in index x
                newChairData[x] = chairData[i];
                x++; //increment the index of the new array
            }
            n=0; //set n=0
        }
        newChairData[newChairData.length-1] =leftover;
        chairData=newChairData; //at the end of the loop update the chairData with the newChairData
    }
    /**
     * This private method is named pricesToArray and it converts the ArrayList 
     * listOfPrices into an array of type int named prices. This method takes 
     * in no arguments and returns a void.
     */
    private void pricesToArray(){
        Object [] obj = listOfPrices.toArray();
        prices = new int [obj.length]; // create a new array of prices
        for(int i=0;i<prices.length;i++){ //loop throw the prices array
            prices[i]= (Integer) obj[i]; // copy each element in the listOfPrices
        }
    }
    /**
     * This private method is named getLowestPrice and returns the lowest price 
     * within array of prices. This method doesn't take in any arguments.
     * @return Returns an int of the lowest price.
     */
    private int getLowestPrice(){
        minIndex=0;
        if(prices.length ==0){ //if there are no possible prices, return 0
            return 0;
        }
        int minPrice=prices[0];
        for(int i=0;i<prices.length;i++){ //loop through the prices array
            if(prices[i]<minPrice){ //if the price of the index is lower than minPrice change the minIndex and minPrice of the current index
                minPrice=prices[i];
                minIndex=i;
            }
        }
        return minPrice;
    }
    /**
     * This private method is named formatItemsOfID and it returns a String for 
     * the list of IDs used in the transaction with a format of "ID: IDnumber" 
     * while having each ID on a newline. This method doesn't take in any arguments
     * @return A string of the formated IDs used in the transaction
     */
    private String formatItemsOfID(){
        StringBuffer formattedID = new StringBuffer(); //create a new StringBuffer called formattedID
        String[] id = getItemsID().split(Pattern.quote("\n")); //convert itemsID to a String array
        for(int i=0;i<id.length;i++){ //iterate through the id array
            if(getItemsID().isEmpty()){ //if there aren't any IDs break
                break;
            }
            if(!(id[i].equals("leftover"))){ //if the id isn't the leftover append it to the formattedIDs
                formattedID.append("ID: "+id[i]+"\n"); //append each formatted ID to the StringBuffer 
            }   
        }
        if(formattedID.length()>0){
            formattedID.deleteCharAt(formattedID.length()-1); //delete the last new line
        }
        return formattedID.toString(); //return the formattedID
    }
    /**
     * This public method returns the required information 
     * to make the output file in OutputTxt class.
     * @return A String of the information
     */
    public String getOrderString(){
        StringBuffer order =  new StringBuffer();
        if(numberOfChairsAssembled == 0){
            order.append("Original Request: "+ getType()+" Chair, "+ getNumberOfChairsRequired());
            order.append("\n\n");
            order.append("Order cannot be fulfilled based on current inventory. Suggested manufacturers are Office Furnishings, Chairs R Us, Furniture Goods, and Fine Office Supplies.");
        }else{
            order.append("Original Request: "+ getType()+" Chair, "+ getNumberOfChairsRequired());
            order.append("\n\n");
            order.append("Items Ordered\n");
            order.append(formatItemsOfID());
            order.append("\n\nTotal Price: $"+getTotalPrice());
        }
        return order.toString();
    }
    /**
     * This public method is named close and it ensures that the 
     * database connection and the ResultSet object are properly closed after 
     * all the functions in main. This method doesn't take in any arguments and 
     * returns a void.
     */
    public void close() {
        try {
            results.close(); //close the ResultSet
            dbConnect.close(); //close the database connection
        } catch (SQLException e) {
            e.printStackTrace(); //if an SQLException occurs print the stack trace
        }
    }
}
