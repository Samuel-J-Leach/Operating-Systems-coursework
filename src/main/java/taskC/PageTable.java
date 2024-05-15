package taskC;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

public class PageTable {
	private Dictionary<String, ArrayList<Integer>> table = new Hashtable<String, ArrayList<Integer>>();
	int size;
	
	public PageTable() {
		this.table.put("Index", new ArrayList<Integer>());
		this.table.put("Valid", new ArrayList<Integer>());
		this.table.put("Physical Page or On Disk", new ArrayList<Integer>());
		this.size = 0;
	}
	
	public ArrayList<Integer> getEntry(int i) {
		ArrayList<Integer> entry = new ArrayList<Integer>();
		entry.add(this.table.get("Index").get(i));
		entry.add(this.table.get("Valid").get(i));
		entry.add(this.table.get("Physical Page or On Disk").get(i));
		return entry;
	}
	
	public void addEntry(ArrayList<Integer> entry) {
		this.table.get("Index").add(entry.get(0));
		this.table.get("Valid").add(entry.get(1));
		this.table.get("Physical Page or On Disk").add(entry.get(2));
		this.size++;
	}
	
	@Override
	public String toString() {
		StringBuffer output = new StringBuffer();
		output.append("#Index,Valid,Physical Page or On Disk\n");
		for (int i = 0; i < this.size; i++) {
			for (int j = 0; j < 2; j++) {
				if (this.getEntry(i).get(j) == -1) {
					output.append(this.getEntry(i).get(j));
				} else {
					output.append(this.getEntry(i).get(j));
				}
				output.append(",");
			}
			if (this.getEntry(i).get(2) == -1) {
				output.append(this.getEntry(i).get(2));
			} else {
				output.append(this.getEntry(i).get(2));
			}
			output.append("\n");
		}
		return output.toString();
	}
}
