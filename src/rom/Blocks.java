package rom;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import gbc_rom_packer.AllocBlock;
import gbc_rom_packer.FixedBlock;
import gbc_rom_packer.MoveableBlock;

public class Blocks 
{
	// BlockId, Block
	// This can be used for determining references of one block in another and ensures
	// each ID is unique
	private Set<String> usedIds;
	private List<FixedBlock> replacementBlocks;
	private List<MoveableBlock> blocksToPlace;
	
	public Blocks()
	{
		usedIds = new HashSet<>();
		replacementBlocks = new LinkedList<>();
		blocksToPlace = new LinkedList<>();
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
	
	private void addBlockById(AllocBlock block)
	{
		block.addAllIds(usedIds);
	}
	
	public List<FixedBlock> getAllFixedBlocks()
	{
		return replacementBlocks;
	}
	
	public List<MoveableBlock> getAllBlocksToAllocate()
	{
		return blocksToPlace;
	}
}
