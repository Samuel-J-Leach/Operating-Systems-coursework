package taskC;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class TaskC {
	
	private static TLB tlb = new TLB();// translation-lookaside buffer
	private static PageTable pageTable = new PageTable();
	private static ArrayList<String> addresses = new ArrayList<String>();// virtual addresses
	
	/**
	 * takes a string containing integers separated by commas and converts it to a list of integers
	 * 
	 * @param strNum - the string containing integers separated by commas
	 * @return numbers - the list of integers
	 */
	public static ArrayList<Integer> splitAndConvertToIntegerList(String strNum) {
		ArrayList<Integer> numbers = new ArrayList<Integer>();
		for (String num : strNum.split(",")) {
			numbers.add(Integer.parseInt(num));
		}
		return numbers;
	}
	
	/**
	 * gets the virtual page number from the virtual address given
	 * 
	 * @param address - virtual address to be accessed
	 * @return virtual page number from the virtual address
	 */
	public static int getVirtualPageNumber(String address) {
		return Integer.parseInt(address.substring(2,3));
	}
	
	/**
	 * reads the data from taskC.txt and inserts it into the list of addresses, TLB, and Page table
	 * 
	 * @throws FileNotFoundException
	 */
	public static void setUpTables() throws FileNotFoundException {
    	File file = new File("taskC.txt");
    	Scanner reader = new Scanner(file);
    	String line;
    	String table = null;
    	while (reader.hasNextLine()) {
    		line = reader.nextLine();
    		if (line.equals("#Address")) {
    			table = "addresses";
    		} else if (line.equals("#Initial TLB")) {
    			table = "tlb";
    		} else if (line.equals("#Initial Page table")) {
    			table = "pageTable";
    		} else if (!line.substring(0,1).equals("#")){
    			switch (table) {
    			case "addresses":
    				addresses.add(line.strip());
    				break;
    			case "tlb":
    				tlb.addEntry(splitAndConvertToIntegerList(line.strip()));
    				break;
    			case "pageTable":
    				//"Disk" is replaced by "-1" to simplify the implementation of the pageTable object
    				pageTable.addEntry(splitAndConvertToIntegerList(line.strip().replaceAll("Disk", "-1")));
    				break;
    			}
    		}
    	}
    	reader.close();
	}
	
	/**
	 * generates a string to be added to the output file
	 * 
	 * @param address - virtual address accessed
	 * @param result - result of memory access
	 * @return string containing virtual address accessed, result of the memory access,
	 * 		   the updated TLB and the updated page table
	 */
	public static String generateOutput(String address, String result) {
		StringBuffer output = new StringBuffer();
		output.append("# After the memory access " + address + "\n");
		output.append("#Address, Result (Hit, Miss, PageFault)\n");
		output.append(address + "," + result + "\n");
		output.append("#updated TLB\n");
		output.append(tlb.toString());
		output.append("#updated Page table\n");
		output.append(pageTable.toString());
		return output.toString();
	}
	
	/**
	 * writes text to a file
	 * 
	 * @param input - the string to be written to the file
	 * @param path - the file path to be written to
	 */
	public static void writeToFile(String input, String path) {
		try {
			File outputFile = new File(path);
			//try to create new file
			//if file already exists then delete it and create a new empty file
			if (!outputFile.createNewFile()) {
				outputFile.delete();
				outputFile.createNewFile();
			}
			FileWriter writer = new FileWriter(path);
			writer.write(input);
			writer.close();
			System.out.println("\n\n-----output successfully written to " + path + "-----\n\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * main program - handles finding/updating/adding TLB and
	 * 				  page table entries as virtual addresses are accessed
	 * 
	 * @param args
	 * @throws FileNotFoundException
	 */
    public static void main(String[] args) throws FileNotFoundException {

    	setUpTables();

    	//prints the data from taskC.txt
    	System.out.println("#stream of virtual addresses");
    	System.out.println(addresses.toString());
    	System.out.println("#initial TLB");
    	System.out.print(tlb.toString());
    	System.out.println("#initial Page table");
    	System.out.print(pageTable.toString());
    	System.out.println("\n\n\n");

    	StringBuffer output = new StringBuffer();

    	int vPageNum;
    	ArrayList<Integer> tlbEntry;// translation-lookaside buffer entry
    	ArrayList<Integer> ptEntry;// page table entry
    	boolean found;// shows whether the virtual page number was found in the TLB or not
    	for (String address : addresses) {
    		found = false;
    		vPageNum = getVirtualPageNumber(address);

    		TLBloop:
    		for (int i = 0; i < tlb.getSize(); i++) {// search TLB for virtual page number

    			tlbEntry = tlb.getEntry(i);

    			if (tlbEntry.get(1) == vPageNum) {// if virtual page number found

    				found = true;

    				if (tlbEntry.get(0) == 1) {// if TLB entry is valid

	    				tlb.updateAllLRU(i);
	    				output.append(generateOutput(address, "Hit"));
	    				break TLBloop;

    				} else if (tlbEntry.get(0) == 0) {// if entry is invalid

    					// search pageTable for virtual page number
    					for (int j = 0; j < pageTable.getSize(); j++) {

    						ptEntry = pageTable.getEntry(j);

    						// if virtual page number found in page table
    						if (ptEntry.get(0) == tlbEntry.get(1)) {

    							if (ptEntry.get(2) != -1) {// if page in memory

    								tlbEntry.set(0, 1);// set to valid
    								tlbEntry.set(2, ptEntry.get(2));// insert physical page number
    								tlb.editEntry(i, tlbEntry);
    			    				tlb.updateAllLRU(i);

    			    				output.append(generateOutput(address, "Miss"));
    			    				break TLBloop;

    							} else if (ptEntry.get(2) == -1) {// if page on disk

    								int newPageNum;
    								try {
    									newPageNum = pageTable.getNextAvailablePageNumber();
									} catch (Exception e) {
										System.out.println("# Memory access " + address + "failed");
										System.out.println(e.getMessage());
										break TLBloop;
									}
									ptEntry.set(2, newPageNum);// insert new physical page number
    								ptEntry.set(1, 1);// set to valid
    								pageTable.editEntry(j, ptEntry);
    								
    								tlbEntry.set(0, 1);// set to valid
    								tlbEntry.set(2, newPageNum);// insert new physical page number
    								tlb.editEntry(i, tlbEntry);
    								
    								tlb.updateAllLRU(i);
    								output.append(generateOutput(address, "PageFault"));
    			    				break TLBloop;
    							}
    						}
    					}
    				}
    			}
    		}

    		if (!found) {// if virtual page number not found

    			PTloop:
    			for (int i = 0; i < pageTable.getSize(); i++) {// search page table for virtual page number

    				ptEntry = pageTable.getEntry(i);

    				if (ptEntry.get(0) == vPageNum) {// if virtual page number found in page table

    					tlbEntry = new ArrayList<Integer>();// new TLB entry
    					tlbEntry.add(1);// add valid bit
    					tlbEntry.add(vPageNum);// add tag(virtual page number)

    					if (ptEntry.get(2) != -1) {// if page is in memory

    						tlbEntry.add(ptEntry.get(2));// add physical page #
    						tlbEntry.add(1);
    						tlb.addEntry(tlbEntry);

		    				tlb.updateAllLRU(tlb.getSize()-1);
		    				output.append(generateOutput(address, "Miss"));
		    				break PTloop;

    					} else if (ptEntry.get(2) == -1) {// if page is on disk

    						int newPageNum;
							try {
								newPageNum = pageTable.getNextAvailablePageNumber();
							} catch (Exception e) {
								System.out.println("# Memory access " + address + "failed");
								System.out.println(e.getMessage());
								break PTloop;
							}
							ptEntry.set(2, newPageNum);// set physical page
							ptEntry.set(1, 1);// set valid bit
							pageTable.editEntry(i, ptEntry);

							tlbEntry.add(newPageNum);// add physical page #
    						tlbEntry.add(1);// add LRU
							tlb.addEntry(tlbEntry);

		    				tlb.updateAllLRU(tlb.getSize()-1);
		    				output.append(generateOutput(address, "PageFault"));
		    				break PTloop;
    					}
    				}
    			}
    		}
    	}

    	System.out.println(output.toString());
    	writeToFile(output.toString(), "taskc-sampleoutput.txt");
    }

}
