package taskC;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

/**
 * java implementation of a translation-lookaside buffer
 */
public class TLB {

	//holds all of the TLB data
	private Dictionary<String, ArrayList<Integer>> table = new Hashtable<String, ArrayList<Integer>>();

	private int size;
	
	public TLB () {
		this.table.put("Valid", new ArrayList<Integer>());
		this.table.put("Tag", new ArrayList<Integer>());
		this.table.put("Physical Page #", new ArrayList<Integer>());
		this.table.put("LRU", new ArrayList<Integer>());
		this.size = 0;
	}
	
	public int getSize() {
		return this.size;
	}
	
	public ArrayList<Integer> getEntry(int i) {
		ArrayList<Integer> entry = new ArrayList<Integer>();
		if (i < this.size && i >= 0) {
			entry.add(this.table.get("Valid").get(i));
			entry.add(this.table.get("Tag").get(i));
			entry.add(this.table.get("Physical Page #").get(i));
			entry.add(this.table.get("LRU").get(i));
		}
		return entry;
	}
	
	public void evictEntry() {
		for (int i = 0; i < this.size; i++) {
			if (this.table.get("LRU").get(i) == 1) {
				this.table.get("Valid").remove(i);
				this.table.get("Tag").remove(i);
				this.table.get("Physical Page #").remove(i);
				this.table.get("LRU").remove(i);
				this.size--;
				break;
			}
		}
	}
	
	public void updateAllLRU(int x) {
		int LRU;
		for (int i = 0; i < this.size; i++) {
			LRU = this.table.get("LRU").get(i);
			if (LRU >= this.table.get("LRU").get(x) && x != i) {
				this.table.get("LRU").set(i, LRU - 1);
			}
		}
		this.table.get("LRU").set(x, 4);
	}
	
	public void addEntry(ArrayList<Integer> entry) {
		if (this.size == 4) this.evictEntry();
		this.table.get("Valid").add(entry.get(0));
		this.table.get("Tag").add(entry.get(1));
		this.table.get("Physical Page #").add(entry.get(2));
		this.table.get("LRU").add(entry.get(3));
		this.size++;
	}
	
	public void editEntry(int i, ArrayList<Integer> entry) {
		if (i < this.size && i >= 0) {
			this.table.get("Valid").set(i, entry.get(0));
			this.table.get("Tag").set(i, entry.get(1));
			this.table.get("Physical Page #").set(i, entry.get(2));
			this.table.get("LRU").set(i, entry.get(3));
		}
	}
	
	@Override
	public String toString() {
		StringBuffer output = new StringBuffer();
		output.append("#Valid, Tag, Physical Page #, LRU\n");
		for (int i = 0; i < this.size; i++) {
			for (int j = 0; j < 3; j++) {
				output.append(this.getEntry(i).get(j));
				output.append(",");
			}
			output.append(this.getEntry(i).get(3));
			output.append("\n");
		}
		return output.toString();
	}
}
