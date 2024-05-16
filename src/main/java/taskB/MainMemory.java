package taskB;

import java.util.ArrayList;

/**
 * This class main purpose is to be a linked list for the current blocks of
 * memory that are placed or free for the simulation of First Fit, Best Fit, and
 * Worst Fit memory allocation methods.
 */
public class MainMemory {

    private BlockNode start;
    private BlockNode end;
    private int size;

    /**
     * Constructor, initialize linked list
     */
    public MainMemory() {
		start = null;
		end = null;
		size = 0;
    }

    /**
     * Checks if linked list is empty
     * 
     * @return True if empty, false if not
     */
    public boolean isEmpty() {
    	return start == null;
    }

    /**
     * Gets the size of linked list
     * 
     * @return size of linked list
     */
    public int getSize() {
    	return size;
    }

    /**
     * Inserts Block at start of linked list, best to be used to initialize first
     * node.
     * 
     * @param block Block of memory to insert.
     */
    public void insertAtStart(Block block) {
		BlockNode nptr = new BlockNode(block, null);
		size++;
		if (start == null) {
		    start = nptr;
		    end = start;
		} else {
		    nptr.setNext(start);
		    start = nptr;
		}
    }

    /**
     * First fit insert, this method goes through the linked list finding the first
     * place it can insert the block into memory.
     * 
     * @param the Process proc to insert into memory
     * @return True if successfully inserted block of memory, False if failed.
     */
    public boolean firstFitInsert(Process proc) {
		Block block = new Block(proc);
		BlockNode nptr = new BlockNode(block, null);
	
		if (start == null) {
		    start = nptr;
		    end = start;
		    return true;
		} else {
	
		    BlockNode curr = start;
	
		    // look at all available slots/holes in memory
		    // select the first available position of suitable size for block
		    while (curr != null) {
	
			// enough available space in memory identified
			if (curr.getBlock().canPlace(block.getProcess())) {
	
			    // get the end memory location for available block curr
			    int end = curr.getBlock().getHole().getEnd();
	
			    // add the process in memory
			    curr.getBlock().setProcess(block.getProcess());
	
			    // take only what we need from memory
			    int block_start = curr.getBlock().getHole().getStart();
			    int memory_needs = block.getProcess().getArgument();
			    curr.getBlock().getHole().setRange(block_start, block_start + memory_needs - 1);
	
			    // create a new block with the rest of memory we don't need
			    // notice curr.getBlock().getHole().getEnd() was changed by line 155
			    if (curr.getBlock().getHole().getEnd() < end) {
				BlockNode newBlock = new BlockNode(
					new Block(null, new Hole(curr.getBlock().getHole().getEnd() + 1, end)), curr.getNext());
	
				curr.setNext(newBlock);
			    }
			    size++;
			    return true;
			}
			curr = curr.getNext();
		    }
		    return false;
		}
    }

    /**
     * Best fit insert, this method goes through the linked list finding the
     * best place it can insert the block into memory.
     * 
     * @param Process proc to insert into memory
     * @return True if successfully placed, false if it failed.
     */
    public boolean bestFitInsert(Process proc) {
		Block block = new Block(proc);
		BlockNode ptr = new BlockNode(block, null);
		if (start == null) {
            start = ptr;
            end = start;
            return true;
        } else {
        	BlockNode curr = start;
        	BlockNode best = null;
        	while(curr != null){
        		if(curr.getBlock().canPlace(block.getProcess())){
        			if (best == null || best.getBlock().getSize() > curr.getBlock().getSize()) {
        				best = curr;
        			}
        		}
        		curr = curr.getNext();
        	}
        	if (best == null) {
        		return false;
        	} else {
        		int end = best.getBlock().getHole().getEnd();
        		best.getBlock().setProcess(block.getProcess());
        		int block_start  = best.getBlock().getHole().getStart();
                int memory_needs = block.getProcess().getArgument();
                best.getBlock().getHole().setRange(block_start, block_start + memory_needs - 1);
                if(best.getBlock().getHole().getEnd() < end) {
                	BlockNode newBlock = new BlockNode(new Block(null, new Hole(best.getBlock().getHole().getEnd() + 1, end)), best.getNext());
                	best.setNext(newBlock);
                }
                size++;
        		return true;
        	}
        }
    }

    /**
     * This method goes through current memory blocks. If blocks are next to each
     * other and free it will join the blocks together making a larger block.
     */
    public void joinBlocks() {
		BlockNode ptr = start;
	
		while (ptr.getNext() != null) {
	
		    BlockNode next = ptr.getNext();
	
		    if (ptr.getBlock().getProcess() == null && next.getBlock().getProcess() == null) {
				int start = ptr.getBlock().getHole().getStart();
				int end = next.getBlock().getHole().getEnd();
				ptr.getBlock().getHole().setRange(start, end);
				ptr.setNext(next.getNext());
				size--;
				continue;
		    }
		    ptr = ptr.getNext();
		}
    }

    /**
     * This method gets the external fragmentation of the current memory blocks
     * if a block of memory failed to placed.
     * 
     * @return external fragmentation of memory.
     */
    public int externalFragmentation() {
		BlockNode ptr = start;
		int externalFragmentation = 0;
		while (ptr != null) {
			if (ptr.getBlock().available()) {
				externalFragmentation += ptr.getBlock().getSize();
			}
			ptr = ptr.getNext();
		}
		return externalFragmentation;
    }

    /**
     * This method goes through the blocks of memory and de-allocates the block for
     * the provided process_number
     * 
     * @param process_number Process to be de-allocated.
     */
    public void deallocateBlock(int process_number) {
		BlockNode ptr = start;
		while (ptr != null) {
	
		    if (ptr.getBlock().getProcess() != null) {
				if (ptr.getBlock().getProcess().getReference_number() == process_number) {
				    ptr.getBlock().setProcess(null);
				    joinBlocks();
				    return;
				}
		    }
		    ptr = ptr.getNext();
		}
    }

    /**
     * This method prints the whole list of current memory.
     */
    public void printBlocks() {
		System.out.println("Current memory display");
		BlockNode ptr = start;
		while (ptr != null) {
		    ptr.getBlock().displayBlock();
		    ptr = ptr.getNext();
		}
    }

    /**
     * This method combines all unused holes in memory into one block in memory
     */
    public void compactBlocks() {
    	BlockNode ptr = this.start;
    	BlockNode next;
    	BlockNode prev = null;
    	while (ptr.getNext() != null) {
    		next = ptr.getNext();
    		if (ptr.getBlock().available()) {// if current block is unallocated
    			if (next.getBlock().available()) {// if next block is unallocated
    				//join unused holes
    				int ptrStart = ptr.getBlock().getHole().getStart();
    				int ptrEnd = next.getBlock().getHole().getEnd();
    				ptr.getBlock().getHole().setRange(ptrStart, ptrEnd);
    				ptr.setNext(next.getNext());
    				size--;
    				continue;
    			} else {// if next block is allocated
    				//swap positions in memory
    				int nextStart = ptr.getBlock().getHole().getStart();
    				int nextEnd = nextStart + next.getBlock().getSize() - 1;
    				int ptrStart = nextEnd + 1;
    				int ptrEnd = next.getBlock().getHole().getEnd();
    				next.getBlock().getHole().setRange(nextStart, nextEnd);
    				ptr.getBlock().getHole().setRange(ptrStart, ptrEnd);
    				if (prev != null) {
    					prev.setNext(next);
    				} else {
    					this.start = next;
    				}
    				ptr.setNext(next.getNext());
					next.setNext(ptr);
    			}
    		}
    		prev = ptr;
    		ptr = next;
    	}
    }

}
