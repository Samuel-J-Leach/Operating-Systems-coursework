package taskC;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

public class TLB {
	
	private Dictionary<String, ArrayList<Integer>> table = new Hashtable<String, ArrayList<Integer>>();
	private int size;
	
	public TLB () {
		this.table.put("Valid", new ArrayList<Integer>());
		this.table.put("Tag", new ArrayList<Integer>());
		this.table.put("Physical Page #", new ArrayList<Integer>());
		this.table.put("LRU", new ArrayList<Integer>());
		this.size = 0;
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
	
	public void addEntry(ArrayList<Integer> entry) {
		this.table.get("Valid").add(entry.get(0));
		this.table.get("Tag").add(entry.get(1));
		this.table.get("Physical Page #").add(entry.get(2));
		this.table.get("LRU").add(entry.get(3));
		this.size++;
	}
	
	@Override
	public String toString() {
		StringBuffer output = new StringBuffer();
		output.append("#Valid, Tag, Physical Page #, LRU");
		for (int i = 0; i < this.size; i++) {
			for (int j = 0; i < 3; j++) {
				output.append(this.getEntry(i).get(j));
				output.append(",");
			}
			output.append(this.getEntry(i).get(3));
			output.append("\n");
		}
		return output.toString();
	}
}
