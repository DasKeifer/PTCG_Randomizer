package compiler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import compiler.referenceInstructs.PlaceholderInstruction;
import datamanager.AllocatedIndexes;
import datamanager.BankAddress;
import rom.Texts;
import util.RomUtils;

public class DataBlock 
{	
	public static String END_OF_DATA_BLOCK_SUBSEG_LABEL = "__end_of_data_block__";
	LinkedHashMap<String, Segment> segments; // linked to keep order
	private String id;

	String rootSegmentName;
	Segment currSegment;
	Segment endSegment; // and empty segment so we can refer to with "." + END_OF_BLOCK_LABEL for any datablock
	
	// Constructor to keep instruction/line less constructors from being ambiguous
	public DataBlock(String startingSegmentName)
	{
		setCommonData(startingSegmentName.trim());
	}
	
	public DataBlock(List<String> sourceLines)
	{
		List<String> sourceLinesTrimmed = new ArrayList<>(sourceLines);
		sourceLinesTrimmed.toArray();
		String segName = CompilerUtils.tryParseSegmentName(sourceLinesTrimmed.remove(0));
		if (id == null)
		{
			throw new IllegalArgumentException("The first line must be a Segment label (i.e. the segment name followed by a ':'");
		}
		
		setCommonData(segName);

		for (String line : sourceLines)
		{
			parseLine(line);
		}
	}
	
	private void setCommonData(String id)
	{
		segments = new LinkedHashMap<>();
		this.id = id;
		endSegment = new Segment();
		newSegment(id);
	}
	
	private void parseLine(String line)
	{
		// split of the instruction (if there is one)
		line = line.trim();
		
		String segName = CompilerUtils.tryParseSegmentName(line);
		// If its not null, its a new segment
		if (segName != null)
		{
			newSegment(segName);
		}
		else // Otherwise see if its a subsegment
		{
			segName = CompilerUtils.tryParseFullSubsegmentName(line, rootSegmentName);
			if (segName != null)
			{
				newSubSegment(segName);
			}
		}

		// If its not a segment, then its a line that will turn into bytes
		if (segName == null)
		{
			// If its a placeholder, we defer filling it out
			if (CompilerUtils.containsPlaceholder(line) || CompilerUtils.containsImplicitPlaceholder(line, rootSegmentName))
			{
				appendPlaceholderInstruction(PlaceholderInstruction.create(line, rootSegmentName));
			}
			else
			{
				appendInstruction(CompilerUtils.parseInstruction(line, rootSegmentName));
			}
		}
	}
	
	public void newSubSegment(String fullSubSegName)
	{
		currSegment = new Segment();
		
		// Ensure there was no conflict within the block
		if (segments.put(fullSubSegName, currSegment) != null)
		{
			throw new IllegalArgumentException("Duplicate segment label was found: " + fullSubSegName);
		}
	}
	
	public void newSegment(String name)
	{
		rootSegmentName = name;
		currSegment = new Segment();
		
		// Ensure there was no conflict within the block
		if (segments.put(name, currSegment) != null)
		{
			throw new IllegalArgumentException("Duplicate segment label was found: " + rootSegmentName);
		}
	}
	
	public void parseAppendInstruction(String instruct)
	{
		parseLine(instruct);
	}
	
	public void appendPlaceholderInstruction(PlaceholderInstruction instruct)
	{
		currSegment.appendPlaceholderInstruction(instruct);
	}
	
	public void appendInstruction(Instruction instruct)
	{
		currSegment.appendInstruction(instruct);
	}
	
	public String getId()
	{
		return id;
	}
	
	public Map<String, Segment> getSegmentsById()
	{
		Map<String, Segment> segRefsById = new LinkedHashMap<>();
		for (Entry<String, Segment> idSeg : segments.entrySet())
		{
			segRefsById.put(idSeg.getKey(), idSeg.getValue());
		}
		
		// Add the end segment marker
		segRefsById.put(CompilerUtils.formSubsegmentName(END_OF_DATA_BLOCK_SUBSEG_LABEL, id), endSegment);
		return segRefsById;
	}
	
	public int getWorstCaseSize(AllocatedIndexes allocatedIndexes)
	{
		BankAddress blockAddress = allocatedIndexes.getTry(getId());
		return getSizeAndSegmentsRelativeAddresses(blockAddress, allocatedIndexes, null); // null = don't care about the relative address of segments
	}
	
	public AllocatedIndexes getSegmentsRelativeAddresses(BankAddress blockAddress, AllocatedIndexes allocatedIndexes)
	{
		AllocatedIndexes relAddresses = new AllocatedIndexes();
		getSizeAndSegmentsRelativeAddresses(blockAddress, allocatedIndexes, relAddresses);
		return relAddresses;
	}
	
	private int getSizeAndSegmentsRelativeAddresses(BankAddress blockAddress, AllocatedIndexes allocatedIndexes, AllocatedIndexes relAddresses)
	{
		if (relAddresses == null)
		{
			relAddresses = new AllocatedIndexes();
		}
		else
		{
			relAddresses.clear();
		}
		
		// If we have none or one segment, its super easy...
		// Note we shouldn't ever have none but it doesn't hurt to catch this case in the chance
		// that this changes later
		// Empty because segments doesn't include the end segment
		if (segments.isEmpty())
		{
			return 0;
		}
		
		// Otherwise we need more complex logic
		// Get the starting point of the addresses
		Map<String, Segment> segmentsWithEndPlaceholder = getSegmentsById();
		getSegRelAddressesStartingPoint(segmentsWithEndPlaceholder, blockAddress, allocatedIndexes, relAddresses);

		// Now do more passes until we have a stable length for all the segments
		boolean stable = false;
		BankAddress foundAddress;
		BankAddress relAddress = new BankAddress(blockAddress.bank, (short) 0);
		while (!stable)
		{
			// Assume we are good until proven otherwise
			stable = true;
			
			// Reset the start relative address for this pass
			relAddress = new BankAddress(blockAddress.bank, (short) 0);
			for (Entry<String, Segment> segEntry : segmentsWithEndPlaceholder.entrySet())
			{
				// Check if the rel address is the address already in record
				foundAddress = relAddresses.getThrow(segEntry.getKey());
				
				// If it doesn't we aren't stable yet and we need to update the stored
				// address
				if (!foundAddress.equals(relAddress))
				{
					stable = false;
					foundAddress.setToCopyOf(relAddress);
				}
						
				// Now determine the overall block size so far/where the next segments rel address would be
				relAddress.addressInBank += segEntry.getValue().getWorstCaseSize(relAddress, allocatedIndexes, relAddresses);
			}
		}
		
		// The size will be stored in the relAddress since it was added at the end to get the final value
		return relAddress.addressInBank;
	}
	
	private AllocatedIndexes getSegRelAddressesStartingPoint(
			Map<String, Segment> segmentsWithEndPlaceholder, 
			BankAddress blockAddress, 
			AllocatedIndexes allocatedIndexes, 
			AllocatedIndexes startingPoint
	)
	{
		BankAddress allocAddress = allocatedIndexes.getTry(segmentsWithEndPlaceholder.entrySet().iterator().next().getKey());
		if (allocAddress.isFullAddress())
		{
			// This means some allocation has already been done. Leverage that for a starting
			// point
			for (Entry<String, Segment> segEntry : segmentsWithEndPlaceholder.entrySet())
			{
				startingPoint.put(segEntry.getKey(), 
						new BankAddress(blockAddress.bank, 
								(short) (allocatedIndexes.getThrow(segEntry.getKey()).addressInBank - blockAddress.addressInBank)
				));
			}
		}
		else
		{			
			BankAddress nextSegRelAddress = new BankAddress(blockAddress.bank, (short) 0);
			for (Entry<String, Segment> segEntry : segmentsWithEndPlaceholder.entrySet())
			{
				startingPoint.put(segEntry.getKey(), new BankAddress(nextSegRelAddress));
				nextSegRelAddress = new BankAddress(nextSegRelAddress.bank, 
						(short) (nextSegRelAddress.addressInBank + segEntry.getValue().getWorstCaseSize(nextSegRelAddress, allocatedIndexes, startingPoint))
				);
			}
		}
		
		return startingPoint;
	}
	
	public void replacePlaceholderIds(Map<String, String> placeholderToArgsForIds)
	{
		// Replace placeholders in Id
		id = CompilerUtils.replacePlaceholders(id, placeholderToArgsForIds);
		
		LinkedHashMap<String, Segment> refreshedSegments = new LinkedHashMap<>();
		// Use segments because we know we don't need to replace anything in the
		// end segment placeholder
		for (Entry<String, Segment> seg : segments.entrySet())
		{
			String segId = CompilerUtils.replacePlaceholders(seg.getKey(), placeholderToArgsForIds);
			seg.getValue().fillPlaceholders(placeholderToArgsForIds);
			
			if (refreshedSegments.put(segId, seg.getValue()) != null)
			{
				throw new IllegalArgumentException("Duplicate segment label was found while replacing placeholders: " + segId);
			}
		}
		segments = refreshedSegments;
	}

	public void extractTexts(Texts texts) 
	{
		// Use segments because we know we don't need to extract anything in the
		// end segment placeholder
		for (Segment seg : segments.values())
		{
			seg.extractTexts(texts);
		}
		
		// End reference has no code so no text exisist in it
	}

	public static boolean debug = false;
	public void writeBytes(byte[] bytes, AllocatedIndexes allocatedIndexes)
	{
		debug = id.contains("CallFor");
		if (debug) 
		{
			System.out.println("Segment - " + id);
		}

		// Don't need to write anything for the end of the segment
		for (Entry<String, Segment> segEntry : segments.entrySet())
		{
			BankAddress segAddress = allocatedIndexes.getThrow(segEntry.getKey());
			segEntry.getValue().writeBytes(bytes, RomUtils.convertToGlobalAddress(segAddress.bank, segAddress.addressInBank), allocatedIndexes);
		}
		
		// End reference has no code so no writing needs to be done
	}
}
