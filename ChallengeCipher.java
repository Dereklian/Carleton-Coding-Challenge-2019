import java.awt.Point;
import java.util.Scanner;

public class ChallengeCipher {

    private static char[][] table;
    private static Point[] positions;

    public ChallengeCipher() {
        table = new char[5][5];
        positions = new Point[26];
    }

    //Function to call the generate table method, and then checks if there is any missing information
    public String solve(String key, String message) {
        generateTable(key);
        printTable();
        if (message != null && key != null) {
            return codec(new StringBuilder(message), 4);
        } else {
            System.err.println("Missing key/message information! Make sure to run Step 2 first if you haven't already.");
            return "";
        }
    }

    //Function to generate the table of chars that will be used to encrypt
    private void generateTable(String key) {
        for (int i = 0; i < key.length(); i++) {
            //Place the chars in the key, at the beginning of the table
            char c = key.charAt(i);
            table[i / 5][i % 5] = c;
            positions[c - 'A'] = new Point(i % 5, i / 5);
        }
    }

    //Function to print out the table of chars that will be used to encrypt
    private void printTable(){
        System.out.println("");
        //Loop through 2D array and print out each element
    	for(int i = 0; i < 5; i++){
    		for(int j = 0; j < 5; j++){
    			System.out.print(table[i][j] + " ");
        	}
    		System.out.println("");
    	}
    }

    //Codec function to encrypt and decrypt
    private String codec(StringBuilder text, int direction) {
        //Get length of message
        int len = text.length();
        for (int i = 0; i < len; i += 2) {
            //Break down message to be encrypted
            char a = text.charAt(i);
            char b = text.charAt(i + 1);

            //Shift the chars and then get the new integer (x and y's)
            int row1 = positions[a - 'A'].y;
            int row2 = positions[b - 'A'].y;
            int col1 = positions[a - 'A'].x;
            int col2 = positions[b - 'A'].x;

            //Check if two are the same
            if (row1 == row2) {
                col1 = (col1 + direction) % 5;
                col2 = (col2 + direction) % 5;

            } else if (col1 == col2) {
                row1 = (row1 + direction) % 5;
                row2 = (row2 + direction) % 5;

            } else {
                int tmp = col1;
                col1 = col2;
                col2 = tmp;
            }

            //Set chars of the output string
            text.setCharAt(i, table[row1][col1]);
            text.setCharAt(i + 1, table[row2][col2]);
        }
        //Return encoded/decoded message
        return text.toString();
    }
}
