/**@author Heidi Toews
 * <a>
 * href="mailto:heidi.toews@ucalgary.ca">heidi.toews@ucalgary.ca</a>
 * @version 1.3
 * @since 1.0
 */

/*ENSF 409 Final Project Group 7
OutputTxt.java
Ahmed Waly, Alexis Hamrak, Andrea Benavides Aguirre, Heidi Toews*/

package edu.ucalgary.ensf409;

import java.io.*;

/**Class OutputTxt contains null objects for each type of furniture.<!-- -->
 * The correct object is initialized when the OutputTxt object is constructed.
 */
public class OutputTxt {
    private Chair orderC = null; //variable to use if we require a chair
    private Desk orderD = null; //variable to use if we require a desk
    private Lamp orderL = null; //variable to use if we require a lamp
    private Filing orderF = null; //variable to use if we require a filing object

    /**
     * Overloaded constructor that requires a Chair object.
     * @param orderedChair Chair object
     */
    public OutputTxt(Chair orderedChair) {
        orderC = orderedChair;
    }
    
    /**
     * Overloaded constructor that requires a Desk object.
     * @param orderedDesk Desk object
     */
    public OutputTxt(Desk orderedDesk) {
        orderD = orderedDesk;
    }

    /**
     * Overloaded constructor that requires a Lamp object.
     * @param orderedLamp Lamp object
     */
    public OutputTxt(Lamp orderedLamp) {
        orderL = orderedLamp;
    }

    /**
     * Overloaded constructor that requires a Filing object.
     * @param orderedFiling Filing object
     */
    public OutputTxt(Filing orderedFiling) {
        orderF = orderedFiling;
    }

    /**
     * The outputOrder method creates a text file that has the same name 
     * as the String being passed. This text file contains all the required 
     * information of the transaction.
     * @param fileName String of the name of the file that is being created
     */
    public void outputOrder(String fileName) {
        File order = null;
        FileWriter output = null;
        try {
            order = new File(fileName);
            order.createNewFile();
            output = new FileWriter(order);
            output.write("Furniture Order Form\n\nFaculty Name: \nContact: \nDate: \n\n");
            if (orderC != null) {
                output.write(orderC.getOrderString());
            } else if (orderD != null) {
                output.write(orderD.getOrderString());
            } else if (orderL != null) {
                output.write(orderL.getOrderString());
            } else if (orderF != null) {
                output.write(orderF.getOrderString());
            }
            output.close();
        }
        catch (IOException e) {
            System.err.print("IOException writing to file.");
            System.exit(1);
        }
    }
}
