package edu.ucalgary.ensf409;
import java.util.*;
/**
 * ENSF 409 Final Project Group 7
 * @author Ahmed Waly
 * @author Alexis Hamrak
 * @author Andrea Benavides Aguirre
 * @author Heidi Toews
 * @version     1.5
 * @since       1.0
 */

/**
 * All java files need to be compiled. To run the program, run Main. The program will 
 * prompt the user to input the database username and password, furniture (chair, lamp etc.), 
 * type, and quantity one at a time in that order. Furniture and type are both case insensitive.
 * Make sure to include the mySQL-connector, JUnit, and hamcrest jar files in your command 
 * when compiling and running the program; these jar files are stored inside the lib folder.
 * The unit tests should be compiled and run, and the database should match the one 
 * created by the most recent inventory.sql file on D2L.
 */
public class Main {
    /**
     * The main method takes in the user input by utilizing a scanner 
     * and calls the respective class to calculate the cheapest option. 
     * The scanner will keep promting the user for an input until the 
     * furniture type and quantity are valid.
     * @param args Command Line arguments
     */
    public static void main(String[] args) {

        String fileName = "orderform.txt"; //name of file to output to

        String dburl = "jdbc:mysql://localhost/inventory"; //database URL 
        Scanner scan = null; //scanner object
        String user= new String(); //username for database
        String pass= new String(); //password for database
        String furniture = new String(); //String for type of furniture to use
        String type= new String(); //String for type of that furniture
        int numberItems= 0; //int of the number of items to order
	
        scan = new Scanner(System.in);
        System.out.print("Please enter the username of the local database: ");
        user = scan.nextLine().trim(); //username from the user input
        System.out.print("Please enter the password of the local database: ");
        pass = scan.nextLine().trim(); //password from the user input

        System.out.print("Please enter the furniture type: ");
        furniture = scan.nextLine().trim(); //furniture type from the user input
        
        boolean invalidFurniture =true;
        while(invalidFurniture){ // keep promting the user until the furniture is valid
            if(!(checkFurniture(furniture))){
                System.out.print("Please enter a valid furniture type: ");
                furniture = scan.nextLine().trim(); //furniture type from the user input
            }else{
                invalidFurniture = false;
            }
        }
        
        furniture = furniture.substring(0,1).toUpperCase()+furniture.substring(1).toLowerCase();
        boolean invalidType=true;
        System.out.print("Please enter the type of "+furniture+": ");
        type = scan.nextLine().trim(); //type argument from the user input
        while(invalidType){
            if(!(checkType(furniture, type))){
                System.out.print("Please enter a valid type of "+furniture+": ");
                type = scan.nextLine().trim();
            }else{
                invalidType=false;
            }
        }
        boolean invalidQuantity =true;

        System.out.print("Please enter the quantity required: ");
        numberItems = insertNumberOfItems(scan.nextLine().trim());
        while (invalidQuantity){ // keep prompting the user until the quanity is valid
            if(!checkNumberOfItems(numberItems)){
                System.out.print("Please enter a valid quantity: ");
                numberItems = insertNumberOfItems(scan.nextLine().trim());
            }else{
                invalidQuantity =false;
            }
        }
		if(scan!=null){ //close the scanner if it is open
            try{
                scan.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        
        if(furniture.equalsIgnoreCase("Chair")){
            Chair chair = new Chair(dburl, user, pass, type, numberItems);
            System.out.println(chair.calculateCheapestOption());
            OutputTxt output = new OutputTxt(chair);
            output.outputOrder(fileName);
            chair.close();
        }//case that furniture requested is a chair. Call constructor, output the cheapest option
        //write to output file, close all statements, connections and resultset objects

        else if(furniture.equalsIgnoreCase("Desk")){
            Desk desk = new Desk(dburl, user, pass, type, numberItems);
            desk.findDesks();
            OutputTxt output = new OutputTxt(desk);
            output.outputOrder(fileName);
            desk.closeAll();
        }//case that furniture requested is a desk. Call constructor, output the cheapest option
        //write to output file, close all statements, connections and resultset objects

        else if(furniture.equalsIgnoreCase("Filing")){
            Filing file = new Filing(dburl, user, pass, type, numberItems);
            file.findFiling();
            OutputTxt output = new OutputTxt(file);
            output.outputOrder(fileName);
            file.close();
        }//case that furniture requested is a filing. Call constructor, output the cheapest option
        //write to output file, close all statements, connections and resultset objects

        else if(furniture.equalsIgnoreCase("Lamp")){
            Lamp lamp = new Lamp(type, numberItems, dburl, user, pass);
            System.out.println(lamp.printTransaction());
            OutputTxt output = new OutputTxt(lamp);
            output.outputOrder(fileName);
            lamp.close();
        }//case that furniture requested is a lamp. Call constructor, output the cheapest option
        //write to output file, close all statements, connections and resultset objects

        else{
            throw new IllegalArgumentException("Invalid furniture type. Exiting");
        }//furniture type invalid, throws an IllegalArgumentException
    }
   
    
    /**
     * This Method checks if the furniture type is valid
     * @param furniture String of the furniture typed in by the user
     * @return boolean, which is false if the furnitue type is not valid and true if the 
     * type is valid
     */
    public static boolean checkFurniture(String furniture){
        if(!(furniture.equalsIgnoreCase("Chair"))&&!(furniture.equalsIgnoreCase("Desk"))&&!(furniture.equalsIgnoreCase("Lamp"))&&!(furniture.equalsIgnoreCase("Filing"))){
            System.out.println("Valid entries are chair, desk, lamp, or filing.");
            return false;
        }else{
            return true;
        }
    }
    /**
     * This Method checks if the number is greater than zero.
     * If that case is not met, it will return false. Otherwise return true.
     * @param number number of the int
     * @return boolean of wether the number is valid
     */
    public static boolean checkNumberOfItems(int number){
        if(number<=0){//return false if number of items is less than or equal to 0
            return false;
        }else{
            return true;
        } 
    }
    /**
     * This method attempts to convert the user entry for the quantity into an 
     * int. If that is not possible it will print out a message
     * @param numberAsString The String of the user entry that should be converted to an int
     * @return The number for quantity as an int
     */
    public static int insertNumberOfItems(String numberAsString){
        int number=0;
        try {
            number = Integer.parseInt(numberAsString); //convert user input to integer for number of items
        } catch (IllegalArgumentException e){
            System.out.println("Invalid Quantity Entry"); //print a message to the user if a non integer number is entered
        }
        return number;
    }
    /**
     * This method checks if the type is valid.
     * @param type The type that the user has input
     * @param furniture The furniture that the user has requested
     * @return boolen, which is false if the type is not valid and true if the 
     * type is valid
     */
    public static boolean checkType(String furniture, String type){
        if (furniture.equalsIgnoreCase("chair")) {
            if (type.equalsIgnoreCase("mesh")||type.equalsIgnoreCase("kneeling")
            ||type.equalsIgnoreCase("executive")||type.equalsIgnoreCase("ergonomic")||type.equalsIgnoreCase("task")) {
                return true;
            }else{
                System.out.println("Valid entries are mesh, ergonomic, task, kneeling, or executive.");
            }
        } else if (furniture.equalsIgnoreCase("desk")) {
            if (type.equalsIgnoreCase("standing")||type.equalsIgnoreCase("adjustable")
            ||type.equalsIgnoreCase("traditional")) {
                return true;
            }else{
                System.out.println("Valid entries are traditional, adjustable, or standing.");
            }
        } else if (furniture.equalsIgnoreCase("filing")) {
            if (type.equalsIgnoreCase("small")||type.equalsIgnoreCase("medium")
            ||type.equalsIgnoreCase("large")) {
                return true;
            }else{
                System.out.println("Valid entries are small, medium, or large.");
            }
        } else if (furniture.equalsIgnoreCase("lamp")) {
            if (type.equalsIgnoreCase("desk")||type.equalsIgnoreCase("swing arm")
            ||type.equalsIgnoreCase("study")) {
                return true;
            }else{
                System.out.println("Valid entries are desk, study, or swing arm.");
            }
        }
        return false;
    }
}
