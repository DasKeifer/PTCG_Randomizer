package rom;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import compiler.DataBlock;
import compiler.Segment;
import datamanager.FixedBlock;
import datamanager.MoveableBlock;
import rom_addressing.AssignedAddresses;

public class Blocks 
{
	// BlockId, Block
	// This can be used for determining references of one block in another and ensures
	// each ID is unique
	private Map<String, DataBlock> blocksById;
	List<FixedBlock> replacementBlocks;
	List<MoveableBlock> blocksToPlace;
	
	// SegmentId, Segment Reference - mainly for linking
	private Map<String, Segment> segmentRefsById;
	
	public Blocks()
	{
		blocksById = new HashMap<>();
		replacementBlocks = new LinkedList<>();
		blocksToPlace = new LinkedList<>();
		segmentRefsById = new HashMap<>();
	}
	
	public void addFixedBlock(FixedBlock block)
	{
		addBlockById(block);
		replacementBlocks.add(block);
	}
	
	public void addMoveableBlock(MoveableBlock block)
	{
		addBlockById(block);
		blocksToPlace.add(block);
	}
	
	private void addBlockById(DataBlock block)
	{
		// See if it already had an entry that is not this instance of the block
		DataBlock existing = blocksById.put(block.getId(), block);
		if (existing != null && existing != block)
		{
			throw new IllegalArgumentException("Duplicate block ID detected! There must be only " +
					"one allocation block per data block: " + block.getId());
		}

		// Add the references for its segments
		for (Entry<String, Segment> idSegRef : block.getSegmentsById().entrySet())
		{
			if (segmentRefsById.put(idSegRef.getKey(), idSegRef.getValue()) != null)
			{
				throw new IllegalArgumentException("Duplicate segment ID detected: " + idSegRef.getKey());
			}
		}
	}
	
	public List<FixedBlock> getAllFixedBlocks()
	{
		return replacementBlocks;
	}
	
	public List<MoveableBlock> getAllBlocksToAllocate()
	{
		List<MoveableBlock> temp = new LinkedList<>();
		for (DataBlock block : blocksById.values())
		{
			if (block instanceof MoveableBlock)
			{
				temp.add((MoveableBlock) block);
			}
		}
		return temp;
	}
	
	public void writeData(byte[] bytes, AssignedAddresses assignedAddresses)
	{		
		for (DataBlock block : blocksById.values())
		{
			block.writeBytes(bytes, assignedAddresses);
		}
	}
}
