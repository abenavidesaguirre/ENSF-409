/**
 * @author Andrea Benavides Aguirre
 * @version     1.1
 * @since       1.0
 */
 /*ENSF 409 Final Project Group 7
	Filing.java
	Ahmed Waly, Alexis Hamrak, Andrea Benavides Aguirre, Heidi Toews*/
package edu.ucalgary.ensf409;
/**Class Lamp Creates a data type called LampType that has all the 
 * parameters a lamp would have in a database */
public class LampType {
        private String id;
        private String type;
        private int price;
        private String base;
        private String bulb;
        private String manuID;
        /**
         * Lamp Constructor:
         *  - Sets the lamp columns from the database to fields
         * @param id String of the lamp ID
         * @param type String of the lamp type
         * @param base String of the lamp base (either "Y" or "N")
         * @param bulb String of the lamp bulb (either "Y" or "N")
         * @param price int of the lamp price
         * @param manuID String of the lamp associated manufacturer ID
         */    
        public LampType(String id, String type, String base, String bulb, int price, String manuID){
            this.id = id;
            this.type = type;
            this.price = price;
            this.base = base;
            this.bulb = bulb;
            this.manuID = manuID;
        }

        //getters
        /**
         * getter for the lamp ID
         * @return String of the lamp ID
         */
        public String getID(){
            return this.id;
        }
        /**
         * getter method for the lamp type
         * @return String of the lamp type
         */
        public String getType(){
            return this.type;
        }
        /**
         * getter method for the lamp price
         * @return int of the lamp price
         */
        public int getPrice(){
            return this.price;
        }
        /**
         * getter method for the lamp base
         * @return String of the lamp base
         */
        public String getBase(){
            return this.base;
        }
        /**
         * getter method for the lamp bulb
         * @return String of the lamp bulb
         */
        public String getBulb(){
            return this.bulb;
        }
        /**
         * getter method for the lamp manufacturer ID
         * @return String of the lamp manufacturer ID
         */
        public String getManuID(){
            return this.manuID;
        }
}
//end of class LampType
