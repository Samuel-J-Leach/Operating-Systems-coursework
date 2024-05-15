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

    public static void main(String[] args) throws FileNotFoundException {
    	setUpTables();
    	System.out.println(addresses.toString());
    	System.out.print(tlb.toString());
    	System.out.print(pageTable.toString());
    	System.out.println("\n\n\n");
    	
    	/*for each address:
    	 * 
    	 * 	get virtual page number from address.
    	 * 
    	 * 	search tlb for virtual page number:
    	 * 
    	 * 		if virtual page number found:
    	 * 			if tlb entry valid:
	     * 				if page in memory:
	     * 					tlb hit.
	     * 					update tlb.
	     * 
	     * 			else:
	     * 				search page table for page number using virtual page number as index:
	     * 					if page number found:
	     * 						if page in memory:
	     * 							tlb hit
	     * 							update tlb.
	     * 						else if page on disk:
	     * 							page fault.
	     * 							update page table.
	     * 							update tlb
	     * 
	     * 		else if virtual page number not found:
	     * 			search page table:
	     * 
	     * 				if new page in memory:
	     * 					tlb miss.
	     * 					update tlb(evict tlb entry if necessary.insert new tlb entry for page.).
	     * 				else if new page on disk:
	     * 					page fault.
	     * 					update page table.
	     * 					update tlb(evict tlb entry if necessary.insert new tlb entry for page.).			
    	 */
    	
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
	    				System.out.println("# After the memory access " + address);
	    				System.out.println("#Address, Result (Hit, Miss, PageFault)");
	    				System.out.println(address + ",Hit");
	    				tlb.updateAllLRU(i);
	    				System.out.println("#updated TLB");
	    				System.out.println(tlb.toString());
	    				System.out.println("#updated Page table");
	    				System.out.println(pageTable.toString());
	    				break TLBloop;
    				} else if (tlbEntry.get(0) == 0) {// if entry is invalid
    					for (int j = 0; j < pageTable.getSize(); j++) {// search pageTable for virtual page number as index
    						ptEntry = pageTable.getEntry(j);
    						if (ptEntry.get(0) == tlbEntry.get(1)) {// if page table index matches virtual page number
    							if (ptEntry.get(1) != -1) {// if page in memory
    								tlbEntry.set(0, 1);
    								tlbEntry.set(2, ptEntry.get(2));
    								tlb.editEntry(i, tlbEntry);
    								System.out.println("# After the memory access " + address);
    			    				System.out.println("#Address, Result (Hit, Miss, PageFault)");
    			    				System.out.println(address + ",Miss");
    			    				tlb.updateAllLRU(i);
    			    				System.out.println("#updated TLB");
    			    				System.out.println(tlb.toString());
    			    				System.out.println("#updated Page table");
    			    				System.out.println(pageTable.toString());
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
    								System.out.println("# After the memory access " + address);
    			    				System.out.println("#Address, Result (Hit, Miss, PageFault)");
    			    				System.out.println(address + ",PageFault");
    			    				tlb.updateAllLRU(i);
    			    				System.out.println("#updated TLB");
    			    				System.out.println(tlb.toString());
    			    				System.out.println("#updated Page table");
    			    				System.out.println(pageTable.toString());
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
    						System.out.println("# After the memory access " + address);
		    				System.out.println("#Address, Result (Hit, Miss, PageFault)");
		    				System.out.println(address + ",Miss");
		    				tlb.updateAllLRU(tlb.getSize()-1);
		    				System.out.println("#updated TLB");
		    				System.out.println(tlb.toString());
		    				System.out.println("#updated Page table");
		    				System.out.println(pageTable.toString());
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
							System.out.println("# After the memory access " + address);
		    				System.out.println("#Address, Result (Hit, Miss, PageFault)");
		    				System.out.println(address + ",PageFault");
		    				tlb.updateAllLRU(tlb.getSize()-1);
		    				System.out.println("#updated TLB");
		    				System.out.println(tlb.toString());
		    				System.out.println("#updated Page table");
		    				System.out.println(pageTable.toString());
		    				break PTloop;
    					}
    				}
    			}
    		}
    	}
    }

}
