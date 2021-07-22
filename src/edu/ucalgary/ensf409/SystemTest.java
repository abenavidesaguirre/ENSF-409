package edu.ucalgary.ensf409;

import static org.junit.Assert.*;
import org.junit.*;
import java.io.*;
import org.junit.runners.MethodSorters;
/**
 * ENSF 409 Final Project Group 7
 * @author Ahmed Waly
 * @author Alexis Hamrak
 * @author Andrea Benavides Aguirre
 * @author Heidi Toews
 * @version     1.6                
 * @since       1.0
 */
/**This class holds the junit tests to test multiple functunalities 
 * in the program. Make sure to refresh the database before use, and 
 * that the database matches the one created by the most recent 
 * inventory.sql file on D2L. 
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING) //The tests are run in alphabetical order
public class SystemTest {
    /**The name of the text file */
    public final static String FILE = "orderform.txt";
    /**Database URL */
    public final static String DB = "jdbc:mysql://localhost/inventory";
    /**Database username */
    public final static String USERNAME = "scm";
    /**Database password */
    public final static String PASSWORD = "ensf409";

    //Class Chair Tests

    /**
     * Calling the Chair constructor with a chair type that does not exist.
     * The expected result is an IllegalArgumentException should be thrown.
    */
    @Test
    public void testAAWrongChairType(){
        boolean exceptionThrown = false; //indicator if an IllegalArgumentException is thrown or not
        try{
            new Chair(DB, USERNAME, PASSWORD ,"BadType",1);
        }catch(IllegalArgumentException e){ //An IllegalArgumentException should be Caught
            exceptionThrown =true; //exceptionThrown becomes true
        }
        assertTrue("Unable to properly thow an IllegalArgumentException in class Chair when a wrong chair type is passed into the Chair Constructor", exceptionThrown);
    }
    /**
     * Constructor of Chair is created, testing to see if the type of chair has 
     * been successfully stored and the getType method works
     */
    @Test
    public void testABChairGetType(){
        //correct inputs has been passed to the Chair Constructor
        var testChair = new Chair(DB,USERNAME,PASSWORD, "Task",1);
        assertEquals("Storing the type of chair and retrieving the String using getType method failed","Task", testChair.getType());
    }
    /**
     * Constructor of Chair is created, testing to see if the numberOfChairsRequired 
     * has 
     * been successfully stored and the getNumberOfChairsRequired method works
     */
    @Test
    public void testACChairGetNumberOfChairsRequired(){
        //correct inputs has been passed to the Chair Constructor
        var testChair = new Chair(DB,USERNAME,PASSWORD, "Task",5);
        assertEquals("Storing the number of Chairs required and retrieving the value using getNumberOfChairsRequired method failed",5, testChair.getNumberOfChairsRequired());
    }
    /**
     * Checking if the database connection has been succesfuly made
     * @throws Exception SQLException
     */
    @Test
    public void testADChairDatabaseConnection() throws Exception{
        var testChair = new Chair(DB,USERNAME,PASSWORD, "Mesh",1);
        assertTrue("Unable to connect to the local database proberly using class Chair", testChair.getDbConnect().isValid(0)); //checking to see if the DataBase connection is valid
    }
    /**
     * Testing to see that closing the database connection is successful
     * @throws Exception SQLException
     */
    @Test
    public void testAEChairDatabaseClose() throws Exception{
        var testChair = new Chair(DB,USERNAME,PASSWORD, "Mesh",1);
        testChair.close(); //calling the close method
        assertFalse("Unable to close the local database proberly using class Chair", testChair.getDbConnect().isValid(0));//checking to see if the DataBase connection is valid
    }
    /**
     * Testing to to see if the cheapest option has beeen calculated correctly
     */
    @Test
    public void testAFMeshChairTotalPrice(){
        var testChair = new Chair(DB,USERNAME,PASSWORD, "Mesh",1);
        testChair.calculateCheapestOption(); //calculating the cheapest option
        assertEquals("Calculating the cheapest option for creating the requested chair Failed",200, testChair.getTotalPrice()); //testing to see if the total price is equal to the expected price
    }
    /**
     * Testing to to see if the cheapest option has beeen calculated correctly and 
     * the ouput is in the correct format
     */
    @Test
    public void testAGErgonomicChairOutput(){
        var testChair = new Chair(DB,USERNAME,PASSWORD, "Ergonomic",1);
        assertEquals("Calculating the cheapest option for creating the requested chair Failed","Purchase C4839 and C5409 for $250.", testChair.calculateCheapestOption()); //testing to see if the total price is equal to the expected price
    }
    /**
     * Testing to see if the database has been updated, by counting the number of 
     * rows for the chair type in the database before and after 
     * calculating the cheapest option. The number of rows should have decreased 
     * after calculating the cheapest option since some furniture items will be 
     * purchased.
     */
    @Test
    public void testAHChairUpdatedDatabase(){
        var testChair = new Chair(DB,USERNAME,PASSWORD, "Executive",1);
        int before = testChair.getRowCount(); //get the number of rows
        testChair.calculateCheapestOption(); // calculate the cheapest option
        int after = testChair.getRowCount(); //get the number of rows after the calculation
        assertTrue("Updating the Database inside the Chair class after calculating the cheapest option failed", after<before);//the number of rows should have decreased
    }
    /**
     * Testing to see if the chair class is able to identify the cheapest IDs 
     * combination to create a Task Chair.
     */
    @Test
    public void testAIChairGetItemsID(){
        var testChair = new Chair(DB,USERNAME,PASSWORD, "Task",1);
        testChair.calculateCheapestOption();
        assertEquals("Finding the IDs for the Cheapest combination for creating the Task chair failed.","C0914\nC3405", testChair.getItemsID());
    }
    /**
     * Testing to see if the chair class is able to return the suggested 
     * manufacturers when an invalid transaction occurs.
     */
    @Test
    public void testAJSuggestedManufacturers(){
        var testChair = new Chair(DB,USERNAME,PASSWORD, "Task",100);
        assertEquals("Class Chair failed to determine that a transaction couldn't be fulfilled and return the suggested manufacturers.","Order cannot be fulfilled based on current inventory. Suggested manufacturers are Office Furnishings, Chairs R Us, Furniture Goods, and Fine Office Supplies.", testChair.calculateCheapestOption());
    }

    //Class Desk Tests
        /**Tests that the constructor creates an object. */
    @Test
    public void testAKConstructor() {
        Desk desk = new Desk(DB, USERNAME  , PASSWORD, "Standing", 1);
        assertNotNull("The desk class didn't properly assemble the constructor",desk);
    }

    /**Tests that the constructor stores the type correctly. */
    @Test
    public void testALConstructorTypeA() {
        Desk desk = new Desk(DB, USERNAME, PASSWORD, "Traditional", 1);
        assertEquals("The Desk Class has not stored the type of desk correctly","Traditional", desk.getType());
    }

    /**Tests that the constructor throws an IllegalArgumentException if the type is invalid. */
    @Test
    public void testAMConstructorTypeD() {
        boolean exception =false;
        try{
            new Desk(DB, USERNAME, PASSWORD, "aninvalidtype", 1);
        }catch(IllegalArgumentException e){
            exception=true;
        }
        assertTrue("Desk Class failed to throw an illegal argument exception when an invalid type is passed", exception);
    }

    /**Tests that the quantity is stored correctly. */
    @Test
    public void testANConstructorValidQuantity() {
        Desk desk = new Desk(DB, USERNAME, PASSWORD, "Standing", 1);
        assertEquals("The Desk Class has not stored the quantity of desks requested properly",1, desk.getQuantity());
    }

    /**Tests that the constructor throws an IllegalArgumentException if the quantity is invalid. */
    @Test
    public void testAOConstructorInvalidQuantity() {
        boolean exception =false;
        try{
            new Desk(DB, USERNAME, PASSWORD, "Standing", -2);
        }catch(IllegalArgumentException e){
            exception=true;
        }
        assertTrue("Desk Class failed to throw an illegal argument exception when a negative number of desks is requested", exception);
    }

    /**First test for correct cheapest price; ordering one traditional desk. */
    @Test
    public void testAPCheapestPriceTraditional1() {
        Desk desk = new Desk(DB, USERNAME, PASSWORD, "Traditional", 1);
        desk.findDesks();
        assertEquals("The Desk Class has not calculated the cheapest traditional desk correctly",100, desk.getTotal());
    }
    
    /**Second test for correct cheapest price; ordering 2 standing desks. */
    @Test
    public void testAQCheapestPriceStanding2() {
        Desk desk = new Desk(DB, USERNAME, PASSWORD, "Standing", 2);
        desk.findDesks();
        assertEquals("The Desk Class has not calculated the cheapest standing desk correctly",600, desk.getTotal());
    }
    
     /**Tests that getOrderString returns the correct value for a completable order. */
     @Test
     public void testAROrderString() {
         Desk desk = new Desk(DB, USERNAME, PASSWORD, "Adjustable", 1);
         desk.findDesks();
         String expected = "Original Request: Adjustable Desk, 1\n\n";
         expected = expected + "Items Ordered\n";
         expected = expected + "ID: D1030\nID: D2746";
         expected = expected + "\n\nTotal Price: $400";
         String actual = desk.getOrderString();
         assertEquals("The Desk Class has not found the cheapest IDs for adjustable desk and formatted them correctly",expected, actual);
     }

    /**Tests that getOrderString returns the correct value for an impossible order. */
    @Test
    public void testASOrderNotPossible() {
        Desk desk = new Desk(DB, USERNAME, PASSWORD, "Adjustable", 30);
        desk.findDesks();
        String expected = "Original Request: Adjustable Desk, 30\n\n";
        expected = expected + "Order cannot be fulfilled based on current inventory. ";
        expected = expected + "Suggested manufacturers are ";
        expected = expected + "Academic Desks, Office Furnishings, Furniture Goods, and Fine Office Supplies.";
        String actual = desk.getOrderString();
        assertEquals("The Desk Class has not found the suggested manufacturers for adjustable desk and formatted them correctly",expected, actual);
    }

    //Class Lamp Tests

    /** Tests for successful connection to the database inside class Lamp.
     *  Checks for connection using the third, fourth, and fifth parameters passed into the lamp constructor, respectively
     *  the database URL, the database username, and the database password
     * @throws Exception SQL Database Exception
     */
    @Test
    public void testATLampDatabaseConnection() throws Exception{
        var testLamp = new Lamp("Desk", 1, DB, USERNAME, PASSWORD);                    
        assertTrue("Failed to connect to database inside class Lamp", testLamp.getDbConnect().isValid(0));
    }

    /** Tests for a database connection that has been established to be successfully close inside the class Lamp.
     * @throws Exception SQL Database Exception
     */
    @Test
    public void testAULampDatabaseClosedConnection() throws Exception{
        var testLamp = new Lamp("Desk", 1, DB, USERNAME, PASSWORD);                   
        testLamp.close();
        assertEquals("Failed to close connection to database inside class Lamp", false, testLamp.getDbConnect().isValid(0));
    }

    /** Tests for the creation of a lamp with an invalid type. The error is caught in the lamp constructor and throws an Illegal Argument Exception
     */
    @Test
    public void testAVInvalidLampType(){
        boolean exceptionThrown = false;
        try{
            new Lamp("invalidLampType", 1, DB, USERNAME, PASSWORD);
        } catch(IllegalArgumentException e){
            exceptionThrown = true;
        }
        assertEquals("Failed to throw IllegalArgumentException for invalid lamp type", true, exceptionThrown);
    }
    
    /** Tests for the creation of 1000 study lamps. There will not be enough materials in the database to construct this order. The order will not complete. Instead a list
     * of suggested manufacturers will be output in the output file.
     */
    @Test
    public void testAWDeskLampOrderQuantityTooLarge(){
        var testLamp = new Lamp("Study", 1000, DB, USERNAME, PASSWORD);
        assertEquals("Failed to recognize that the number of items placed in the order cannot be made with the available database resources", false, testLamp.getIsValid());
    }

    /** Tests the creation of 1 desk lamp and finds the total lowest cost for the order based on available lamp parts in the database
     */
    @Test
    public void testAXGetPriceFor1DeskLamp(){
        var testLamp = new Lamp("Desk", 1, DB, USERNAME, PASSWORD);
        assertEquals("Failed to calculate the lowest price for an order of 1 desk lamp", 20, testLamp.getTotalPrice());
    }
    
    /** Tests the creation of 2 study lamps and finds the toatal lowest cost for the order based on availalbe lamp parts in the database
     */
    @Test
    public void testAYGetPriceFor2StudyLamps(){
        var testLamp = new Lamp("Study",2,DB,USERNAME,PASSWORD);
        assertEquals("Failed to calculate the lowest price for an order of 2 study lamps", 20, testLamp.getTotalPrice());
    }

    /** Tests for the creationg of 2 swing arm lamps and finds the ID's of the lamps to be purchased from the database to fulfil the order.
     */
    @Test
    public void testAZGetPurchaseIDsFor2SwingArmLamps(){
        var testLamp = new Lamp("Swing arm", 2,DB, USERNAME, PASSWORD);
        String idString = "";
        for(int i = 0; i < testLamp.getLampsToOrder().size(); i++){
            idString =  idString + testLamp.getLampsToOrder().get(i).getID() + "\n";
        }
        assertEquals("Failed to find the correct purchase IDs for an order of 2 swing arm lamps", "L053\nL096\nL487\nL879\n", idString);
    }

    /** Tests to get the complete list of lamp manufacturers
     */
    @Test
    public void testBAGetLampManufacturers(){
        var testLamp = new Lamp("desk",1,DB,USERNAME,PASSWORD);
        assertEquals("Failed to obtain correct lamp manufacturers", "Office Furnishings, Furniture Goods, and Fine Office Supplies", testLamp.getManufacturerString());
    }

    //Class Filing tests

    /**
     * Calling the Filing constructor with a file type that is invalid. 
     * An IllegalArgumentException should be thrown as a result
    */
    @Test
    public void testBBIncorrectFilingType(){
        boolean exception = false; //indicator if an IllegalArgumentException is thrown or not
        try{
            new Filing(DB,USERNAME,PASSWORD, "Invalid" , 1);
        }catch(IllegalArgumentException e){ //An IllegalArgumentException should be Caught
            exception =true; //exceptionThrown becomes true
        }
        assertTrue("IllegalArgumentException not thrown from Filing constructor when invalid type passed in.", exception);
    }
    /**
     * Object of Filing created, test used to see if the correct number of items is stored 
     * and its getter works correctly
     */
    @Test
    public void testBCItemNumFiling(){
        //correct data has been passed to the Chair Constructor
        var file = new Filing(DB, USERNAME, PASSWORD, "Small" , 3);
        assertEquals("Reading in number of desired filing items of small type failed in testBDItemNumFiling.", 3, file.getToBuy());
    }
    /**
     * Test for a string containing manufacturers available if we are unable to create a
     * Filing object
     */
    @Test
    public void testBDManufacturersFiling(){
        var file = new Filing(DB, USERNAME, PASSWORD, "Large", 10);
        String[] manu = file.getManufacturers();
        String expected = "Office Furnishings Furniture Goods Fine Office Supplies";
        String received = "";
        for(int i=0; i < manu.length; i++){
            received= received + manu[i] + " ";
        }
        received = received.trim();
        assertEquals("Manufacturer String from Filing returned incorrect, testBEmanufacturers failed in testBEManufacturersFiling", expected, received);
    }
    
     /**
     * Test for the output of a medium filing object.
     * Should return the cheapest price ($200)
     */
    @Test
    public void testBEMediumFilingPriceOne(){
        var file = new Filing(DB, USERNAME, PASSWORD, "Medium", 1);
        file.findFiling();
        assertEquals("Calculating the cheapest option for a medium object of filing failed in testBFMediumFilingPriceOne.", 200 , file.getCost());
    }

    /**
     * Test for the output of a medium filing object.
     * Should return 2 ID's: F007 and F008 along with the correct formatting and their price total: $200
     */
    @Test
    public void testBFGetOrderString(){
        var file = new Filing(DB, USERNAME, PASSWORD, "Large", 1);
        file.findFiling();
        String expected = "Original Request: Large Filing, 1" + '\n' + '\n' + "Items Ordered" + '\n' + "ID: F010" + '\n' + "ID: F012" + '\n' +'\n' + "Total Price: $300"; 
        assertEquals("Calculating the cheapest option for a large object of filing failed in testBFGetOrderString.", expected , file.getOrderString());
    }

    /**
     * Test for the output of a medium filing object when the number of items exceeds
     * the table size. Should return the manufacturers in a string with no items.
     */
    @Test
    public void testBGTooManyFiling(){
        var file = new Filing(DB, USERNAME, PASSWORD, "Medium", 10);
        String expected = "Original Request: Medium Filing, 10\n\nOrder cannot be fulfilled based on current inventory. Suggested manufacturers are Office Furnishings, Furniture Goods, and Fine Office Supplies.";
        assertEquals("Test for a request of more objects than possible of type filing failed in testBGTooManyFiling.", expected , file.getOrderString());
    }

    /**
     * Test for the output of a medium filing object when the number of items exceeds
     * the table size. Should return the manufacturers in a string with no items.
     */
    @Test
    public void testBHGetItemsFiling(){
        var file = new Filing(DB, USERNAME, PASSWORD, "Small", 2);
        file.findFiling();
        assertEquals("Calculating the cheapest option for two small object of filing from test BHGetItemsFiling failed.", 200, file.getCost());
    }

    /**
     * Object of Filing created, tested to see if the type is case insensitive
     * (all uppercase test)
     * */
    @Test
    public void testBIConstructorFiling() {
        Filing file= new Filing(DB, USERNAME, PASSWORD, "LARGE", 1);
        assertEquals("Testing the constructor for case sensitivity with lrage object of filing failed.","Large", file.getType());
    }

    /**
     * Test for the output of a small filing object.
     * Should return 4 ID's: F004, F005, F006 and F013 along with the correct formatting and their price total: $225
     */
    @Test
    public void testBJGetOrderString(){
        var file = new Filing(DB, USERNAME, PASSWORD, "Small", 1);
        file.findFiling();
        String expected = "Original Request: Small Filing, 1" + '\n' + '\n' + "Items Ordered" + '\n' + "ID: F006" + '\n' + "ID: F013" + '\n' +'\n' + "Total Price: $100"; 
        assertEquals("Calculating the cheapest option for a small object of filing failed in testBJGetOrderString.", expected , file.getOrderString());
    }



    //General Tests
    /**Testing to see if the checkNumberOfItems method in main 
     * returns a false when a negative number is passed.
     */
   @Test
    public void testBKTransactionWithNegativeNumber(){
        assertFalse("Unable to properly thow an IllegalArgumentException in class Main when a negative number is passed", Main.checkNumberOfItems(-1));
    }
    /**
     * Testing to see if a text file has been created when the user purchases a 
     * chair.
     */
   @Test
    public void testBLFileCreation(){
      var myChair = new Chair (DB, USERNAME, PASSWORD, "Ergonomic", 50);
      myChair.calculateCheapestOption();
      OutputTxt output = new OutputTxt(myChair);
      output.outputOrder(FILE);
      myChair.close();
      assertTrue("File stored was not created succefully for class Chair", new File(FILE).exists());
    }
    /**
     * Testing to see if a text file has been created when the user purchases a 
     * desk.
     */
    @Test
    public void testBMFileCreation2(){
        Desk desk = new Desk(DB, USERNAME, PASSWORD, "Standing", 30);
        desk.findDesks();
        OutputTxt output = new OutputTxt(desk);
        output.outputOrder(FILE);
        desk.closeAll();
        assertTrue("File stored was not created succefully for class Desk", new File(FILE).exists());
    }
    /**
     * Testing to see if a text file has been created when the user purchases a 
     * filing.
     */
    @Test
    public void testBNFileCreation3(){
        Filing file = new Filing(DB, USERNAME, PASSWORD, "small", 30);
        file.findFiling();
        OutputTxt output = new OutputTxt(file);
        output.outputOrder(FILE);
        file.close();
        assertTrue("File stored was not created succefully for class Filing", new File(FILE).exists());
    }
    /**
     * Testing to see if a file has been created when the user purchases a 
     * lamp.
     */
    @Test
    public void testBOFileCreation4(){
        Lamp lamp = new Lamp("Desk", 20, DB, USERNAME, PASSWORD);
        System.out.println(lamp.printTransaction());
        OutputTxt output = new OutputTxt(lamp);
        output.outputOrder(FILE);
        lamp.close();
        assertTrue("File stored was not created succefully for class Lamp", new File(FILE).exists());
    }
    /**Testing to see the contents within the file for class Chair is the 
     * same as the expected
     */
    @Test
    public void testBPFileContent(){
      var myChair = new Chair (DB, USERNAME, PASSWORD, "Ergonomic", 5);
      myChair.calculateCheapestOption();
      OutputTxt output = new OutputTxt(myChair);
      output.outputOrder(FILE);
      myChair.close();
      String line;
      String content = new String(); //stores the content in the file
      String expected = "Furniture Order Form\nFaculty Name: Contact: Date: \nOriginal Request: Ergonomic Chair, 5\nOrder cannot be fulfilled based on current inventory. Suggested manufacturers are Office Furnishings, Chairs R Us, Furniture Goods, and Fine Office Supplies."; //the expected content within the file
      try{
          BufferedReader readContent = new BufferedReader(new FileReader(FILE));
          while ((line = readContent.readLine()) != null){
              if(line.trim().length() ==0){
                  content+="\n"; //if a line is empty
              }
            content+= line;
          }
          assertEquals("File stored was not created succefully", expected, content);
          readContent.close();
      }catch(Exception e){
        assertTrue("OutputForm file cannot be read from testBKFileContent", false);//if the connot be read, the test failed
      }
      
    }
    /**Testing to recognize an invalid type for lamp furniture
    */
    @Test
    public void testBQInvalidLampType(){
        assertFalse("Failed to recognize invalid Lamp type", Main.checkType("Lamp", "standing"));
      
    }
    /**Testing to recognize an invalid type for filing furniture.
    */
    @Test
    public void testBRInvalidFilingType(){
        assertFalse("Failed to recognize valid Filing type", Main.checkType("Filing", "Mini"));
      
    }
    /**Testing to recognize valid types for desk and chair furniture
    */
    @Test
    public void testBSValidTypes(){
        boolean validTypes = true;
        if(!Main.checkType("Desk","ADJUstable")){
            validTypes = false;
        }
        if(!Main.checkType("Chair", "ErGONoMic")){
            validTypes = false;
        }
        assertTrue("Failed to recognize valid Desk and Chair types", validTypes);
      
    }
    /** Tests an invalid furniture item. Expects a false */
    @Test
    public void testBTInvalidFurniture(){
        assertFalse("Failed to recognize invalid furniture item", Main.checkFurniture("Bed"));
    }

    /** Tests for an invalid quantity argument in the form of a non-integer. 
     * Expects a integer value, else the returned number will equal zero.
    */
    @Test
    public void testBUInvalidQuantityNonInteger(){
        assertEquals("Failed to recognize user input of quantity was non-numeric", 0, Main.insertNumberOfItems("invalidQuantity"));
    }
    
    /**Remove all the text files before the start of the testing*/
    @Before
    public void start() {
      removeAllData();
    }
    /**Remove all the text files after finishing the testing*/
    @After
    public void end() {
      removeAllData();
    }
    /**Delete the outputform text file */
    public void removeAllData(){
      File myFile = new File(FILE);
      myFile.delete();
    }
}
