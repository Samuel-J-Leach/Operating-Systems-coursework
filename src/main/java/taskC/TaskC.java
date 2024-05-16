package taskC;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
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
	
	public static int getVirtualPageNumber(String address) {
		return Integer.parseInt(address.substring(2,3));
	}
	
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
	 * 
	 * @param input
	 * @param path
	 */
	public static void writeToFile(String input, String path) {
		try {
			File outputFile = new File(path);
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

    public static void main(String[] args) throws FileNotFoundException {
    	setUpTables();
    	System.out.println("#stream of virtual addresses");
    	System.out.println(addresses.toString());
    	System.out.println("#initial TLB");
    	System.out.print(tlb.toString());
    	System.out.println("#initial Page table");
    	System.out.print(pageTable.toString());
    	System.out.println("\n\n\n");
    	
    	StringBuffer output = new StringBuffer();
    	
    	int vPageNum;
    	ArrayList<Integer> tlbEntry;
    	ArrayList<Integer> ptEntry;// page table entry
    	boolean found;
    	for (String address : addresses) {// for every address
    		found = false;
    		vPageNum = getVirtualPageNumber(address);
    		TLBloop:
    		for (int i = 0; i < tlb.getSize(); i++) {// search TLB for virtual page number
    			tlbEntry = tlb.getEntry(i);
    			if (tlbEntry.get(1) == vPageNum) {// if virtual page number found
    				found = true;
    				if (tlbEntry.get(0) == 1) {// if entry is valid
	    				tlb.updateAllLRU(i);
	    				output.append(generateOutput(address, "Hit"));
	    				break TLBloop;
    				} else if (tlbEntry.get(0) == 0) {// if entry is invalid
    					for (int j = 0; j < pageTable.getSize(); j++) {// search pageTable for virtual page number as index
    						ptEntry = pageTable.getEntry(j);
    						if (ptEntry.get(0) == tlbEntry.get(1)) {// if page table index matches virtual page number
    							if (ptEntry.get(1) != -1) {// if page in memory
    								tlbEntry.set(0, 1);
    								tlbEntry.set(2, ptEntry.get(2));
    								tlb.editEntry(i, tlbEntry);
    			    				tlb.updateAllLRU(i);
    			    				output.append(generateOutput(address, "Miss"));
    			    				break TLBloop;
    							} else if (ptEntry.get(1) == -1) {// if page on disk
    								int newPageNum;
    								try {
    									newPageNum = pageTable.getNextAvailablePageNumber();
									} catch (Exception e) {
										System.out.println("# Memory access " + address + "failed");
										System.out.println(e.getMessage());
										break TLBloop;
									}
									ptEntry.set(2, newPageNum);
    								ptEntry.set(1, 1);
    								pageTable.editEntry(j, ptEntry);
    								tlbEntry.set(0, 1);
    								tlbEntry.set(2, newPageNum);
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
    			for (int i = 0; i < pageTable.getSize(); i++) {// search page table index for virtual page number
    				ptEntry = pageTable.getEntry(i);
    				if (ptEntry.get(0) == vPageNum) {// if page table index matches with virtual page number
    					tlbEntry = new ArrayList<Integer>();// new TLB entry
    					tlbEntry.add(1);// add valid bit
    					tlbEntry.add(vPageNum);// add tag
    					if (ptEntry.get(2) != -1) {
    						tlbEntry.add(ptEntry.get(2));// add physical page #
    						tlbEntry.add(1);
    						tlb.addEntry(tlbEntry);
		    				tlb.updateAllLRU(tlb.getSize()-1);
		    				output.append(generateOutput(address, "Miss"));
		    				break PTloop;
    					} else if (ptEntry.get(2) == -1) {
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
