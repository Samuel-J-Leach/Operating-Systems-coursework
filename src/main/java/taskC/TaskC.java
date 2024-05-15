package taskC;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/*import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;*/

public class TaskC {
	
	private static TLB tlb = new TLB();
	private static PageTable pageTable = new PageTable();
	private static ArrayList<String> addresses = new ArrayList<String>();
	
	public static ArrayList<Integer> splitAndConvertToIntegerList(String strNum) {
		ArrayList<Integer> numbers = new ArrayList<Integer>();
		for (String num : strNum.split(",")) {
			numbers.add(Integer.parseInt(num));
		}
		return numbers;
	}
	
	public static void setUpTables() throws FileNotFoundException {
    	File file = new File("taskC.txt");
    	Scanner reader = new Scanner(file);
    	String line;
    	int[] values;
    	String table = null;
    	while (reader.hasNextLine()) {
    		line = reader.nextLine();
    		if (line.equals("#Address")) {
    			table = "addresses";
    		} else if (line.equals("#Initial TLB")) {
    			table = "tlb";
    		} else if (line.equals("#Initial Page table")) {
    			table = "pageTable";
    		} else if (!line.substring(0).equals("#")){
    			switch (table) {
    			case "addresses":
    				addresses.add(line);
    				break;
    			case "tlb":
    				tlb.addEntry(splitAndConvertToIntegerList(line));
    				break;
    			case "pageTable":
    				pageTable.addEntry(splitAndConvertToIntegerList(line));
    				break;
    			}
    		}
    	}
    	reader.close();
	}

    public static void main(String[] args) throws FileNotFoundException {
    	setUpTables();
    }

}
