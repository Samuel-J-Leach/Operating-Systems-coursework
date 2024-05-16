package taskA;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class TaskA {

    public static void main(String[] args) {
		System.out.println("Operating Systems Coursework");
		System.out.println("Name: Samuel Leach");
		System.out.println("Please enter your commands - cat, cut, sort, uniq, wc or |");
	
		// reads the command from the terminal
		Scanner in = new Scanner(System.in);
		while (true) {
			System.out.print(">> ");

			// separates piped commands
			String[] input = in.nextLine().strip().split("\\|");
			for (int i = 0; i < input.length; i++) {
				input[i] = input[i].strip();
			}

			String[] command;
			List<String> output = null;
			for (int i = 0; i < input.length; i++) {
				command = input[i].split(" ");
				//executes each command using input piped from the previous command's output
				output = execute(command, output);
			}

			//displays final output
			for (String i : output) {
				System.out.println(i);
			}
		}
    }
    
    /**
     * this method reads a file and places each line into a list of strings
     * 
     * @param path - file path to be read from
     * @return string list containing each line of the file
     */
    private static List<String> readFileContent(String path) {
    	List<String> lines = new ArrayList<>();
    	try {
    		File file = new File(path);
    		Scanner reader = new Scanner(file);
    		while (reader.hasNextLine()) {
    			lines.add(reader.nextLine());
    		}
    		reader.close();
    	} catch (FileNotFoundException e) {
    		System.out.println("file: " + path + " not found");
    		e.printStackTrace();
    	}
    	return lines;
    }

    /**
     * this method takes a range of field indexes and
     * returns each field index within that range
     * 
     * @param range
     * @return list of fields to be accessed
     */
	private static List<Integer> generateFieldListFromRange(List<Integer> range) {
    	List<Integer> fields = new ArrayList<Integer>();
    	if (range.size() == 1) {
    		fields = range;
    	} else if (range.size() == 2 && range.get(0) < range.get(1)) {
    		for (int i = range.get(0); i <= range.get(1); i++) {
    			fields.add(i);
    		}
    	} else {
    		fields.add(-1);
    	}
    	return fields;
    }

	/**
	 * 
	 * @param command - command to be executed
	 * @param input - input piped from previous commands output
	 * @return output of the command executed
	 */
    private static List<String> execute(String[] command, List<String> input) {
    	if (input == null) {
    		input = readFileContent(command[command.length-1]);
    	}
    	if (command[0].equals("cat")) {
    		//do nothing since the input does not need to be altered in any way
    	} else if (command[0].equals("cut")) {
    		List<Integer> fields = new ArrayList<Integer>();
    		String delimiter = null;
    		String fregex1 = "[0-9]+(-[0-9]+){0,1}";
    		String fregex2 = "[0-9]+(,[0-9]+){0,1}";
    		String dregex1 = "'.+'";
    		String dregex2 = "\".+\"";
    		boolean err = false;
    		for (int i = 0; i < command.length; i++) {
    			if (command[i].equals("-f")) {
    				if (command[i+1].matches(fregex1)) {
    					for (String j : command[i+1].split("-")) {
    						fields.add(Integer.parseInt(j) - 1);
    					}
    					fields = generateFieldListFromRange(fields);
    				}else if (command[i+1].matches(fregex2)) {
    					for (String j : command[i+1].split(",")) {
    						fields.add(Integer.parseInt(j) - 1);
    					}
    				} else {
    					if (!err) {
    						input = new ArrayList<String>();
    						err = true;
    					}
    		    		input.add("invalid fields: '" + command[i+1] + "'");
    				}
    				if (fields.contains(-1)) {
    					if (!err) {
    						input = new ArrayList<String>();
    						err = true;
    					}
    		    		input.add("invalid fields: '" + command[i+1] + "'");
    				}
    			} else if (command[i].equals("-d")) {
    				if (command[i+1].matches(dregex1) || command[i+1].matches(dregex2)) {
    					delimiter = command[i+1].substring(1, command[i+1].length()-1);
    				} else {
    					if (!err) {
    						input = new ArrayList<String>();
    						err = true;
    					}
    		    		input.add("invalid delimiter: '" + command[i+1] + "'");
    				}
    			}
    		}
    		if (!err) {
    			input = cut(input, fields, delimiter);
    		}
    	} else if (command[0].equals("sort")) {
    		input = sort(input);
    	} else if (command[0].equals("uniq")) {
    		input = uniq(input);
    	} else if (command[0].equals("wc")) {
    		boolean l = false;
    		for (String i : command) {
    			if (i.equals("-l")) {
    				l = true;
    			}
    		}
    		input = wc(input, l);
    	} else {
    		input = new ArrayList<String>();
    		input.add("command: '" + command[0] + "' not recognised");
    	}
    	return input;
    }
    
    /**
     * this method outputs specific sections from each line of text in the input
     * 
     * @param lines - lines of text to be manipulated
     * @param fields - list of field indexes
     * @param delimiter - character that separates fields
     * @return specified fields of each line from the input
     */
    private static List<String> cut(List<String> lines, List<Integer> fields, String delimiter) {
    	if (fields.size() == 0 || fields == null) {
    		lines = new ArrayList<String>();
    		lines.add("you must specify a valid list of fields to be displayed");
    	} else {
	    	if (delimiter == null) {
	    		delimiter = ",";
	    	}
	    	String[] line;
	    	String newLine;
	    	for (int i = 0; i < lines.size(); i++) {
	    		try {
	    			line = lines.get(i).split(delimiter);
	    			newLine = "";
	    			for (int j : fields) {
	    				if (j < line.length) {
		    				if (newLine.equals("")) {
		    					newLine = line[j];
		    				} else {
		    					newLine = String.join(",", newLine, line[j]);
		    				}
	    				} else {
	    					newLine = String.join(",", newLine, "[[ERROR: field index out of bounds]]");
	    				}
	    			}
	    			lines.set(i, newLine);
	    		} catch (IndexOutOfBoundsException e) {
	    			lines = new ArrayList<String>();
	        		lines.add("index out of bounds");
	    		}
	    	}
    	}
    	return lines;
    }

    /**
     * this method sorts each line from the input alphabetically
     * 
     * @param lines - lines of text to be manipulated
     * @return lines of text sorted alphabetically
     */
    private static List<String> sort(List<String> lines) {
    	Collections.sort(lines);
    	return lines;
    }

    /**
     * this method removes any repeated lines of text from the input
     * 
     * @param lines - lines of text to be manipulated
     * @return unique lines of text
     */
    private static List<String> uniq(List<String> lines) {
    	List<String> unique = new ArrayList<String>();
    	boolean found;
    	for (String i : lines) {
    		found = false;
    		for (String j : unique) {
    			if (i.equals(j)) {
    				found = true;
    				break;
    			}
    		}
    		if (!found) {
    			unique.add(i);
    		}
    	}
    	return unique;
    }

    /**
     * this method counts the number of lines 
     * 
     * @param lines - lines of text to be analysed
     * @param l - specifies whether to only show the line count
     * @return line count, word count, byte count
     */
    private static List<String> wc(List<String> lines, boolean l) {
    	String output;
    	int newlines = lines.size();
		output = Integer.toString(newlines);
    	if (!l) {
    		int words = 0;
    		int bytes = 0;
    		boolean err = false;
    		for (String i : lines) {
    			words += i.split(" ").length;
    			try {
    				bytes += i.getBytes("UTF-8").length;
    			} catch (UnsupportedEncodingException e) {
    				output = e.getMessage();
    				err = true;
    				break;
    			}
    		}
    		if (!err) {
    			output += " " + Integer.toString(words) + " " + Integer.toString(bytes);
    		}
    	}
    	lines = new ArrayList<String>();
    	lines.add(output);
    	return lines;
    }
}
