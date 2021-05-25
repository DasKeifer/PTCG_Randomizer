package compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import compiler.dynamic.PlaceholderInstruction;

import rom.Texts;
import util.RomUtils;

public class DataBlock 
{	
	private int assignedAddress;
	Map<String, Segment> segments;
	private String id;

	String rootSegmentName;
	Segment currSegment;
	
	public DataBlock(String startingSegmentName, Instruction... instructions)
	{
		assignedAddress = CompilerUtils.UNASSIGNED_ADDRESS;
		segments = new HashMap<>();
		id = startingSegmentName.trim();
		
		newSegment(startingSegmentName);
		
		for (Instruction instruct : instructions)
		{
			if (instruct instanceof PlaceholderInstruction)
			{
				appendPlaceholderInstruction((PlaceholderInstruction) instruct);
			}
			else
			{
				appendInstruction(instruct);
			}
		}
	}
	
	// Generic, highest level construct for holding just raw data or a series of functions
	// Represents one block that we want to place contiguously in the ROM
	// I.e. replace Code Snippet
	public DataBlock(String startingSegmentName, List<String> sourceLines)
	{
		assignedAddress = CompilerUtils.UNASSIGNED_ADDRESS;
		segments = new HashMap<>();
		id = startingSegmentName.trim();
		parseSource(startingSegmentName, sourceLines);
	}
	
	public DataBlock(List<String> sourceLines)
	{
		assignedAddress = CompilerUtils.UNASSIGNED_ADDRESS;
		segments = new HashMap<>();

		List<String> sourceLinesTrimmed = new ArrayList<>(sourceLines);
		id = CompilerUtils.tryParseSegmentName(sourceLinesTrimmed.remove(0));
		if (id == null)
		{
			throw new IllegalArgumentException("The first line must be a Segment label (i.e. the segment name followed by a ':'");
		}
		parseSource(id, sourceLinesTrimmed);
	}
	
	private void parseSource(String startingSegmentName, List<String> sourceLines)
	{
		segments.clear();
		newSegment(startingSegmentName);
		
		for (String line : sourceLines)
		{
			// split of the instruction (if there is one)
			line = line.trim();
			
			String segName = CompilerUtils.tryParseSegmentName(line);
			// If its not null, its a new segment
			if (line != null)
			{
				rootSegmentName = segName;
			}
			else // Otherwise see if its a subsegment
			{
				segName = CompilerUtils.tryParseSubsegmentName(line, rootSegmentName);
			}
			
			// If we found a new segment, create it and check that its name is unique
			if (segName != null)
			{
				currSegment = new Segment();
				
				// Ensure there was no conflict within the block
				if (segments.put(segName, currSegment) != null)
				{
					throw new IllegalArgumentException("Duplicate segment label was found: " + rootSegmentName);
				}
			}
			// If its not a segment, then its a line that will turn into bytes
			else
			{
				// If its a placeholder, we defer filling it out
				if (CompilerUtils.isPlaceholderLine(line))
				{
					appendPlaceholderInstruction(PlaceholderInstruction.create(line, rootSegmentName));
				}
				else
				{
					appendInstruction(CompilerUtils.parseInstruction(line, rootSegmentName));
				}
			}
		}
	}
	
	public void newSubSegment(String name)
	{
		currSegment = new Segment();
		
		// Ensure there was no conflict within the block
		if (segments.put(CompilerUtils.formSubsegmentName(name, rootSegmentName), currSegment) != null)
		{
			throw new IllegalArgumentException("Duplicate segment label was found: " + CompilerUtils.formSubsegmentName(name, rootSegmentName));
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
	
	public void appendPlaceholderInstruction(PlaceholderInstruction instruct)
	{
		currSegment.appendPlaceholderInstruction(instruct);
	}
	
	public void appendInstruction(Instruction instruct)
	{
		currSegment.appendInstruction(instruct);
	}
	
	public void setAssignedAddress(int address)
	{
		this.assignedAddress = address;
		assignSegmentAddresses();
	}
	
	private void assignSegmentAddresses()
	{
		getWorstCaseSizeOnBank(RomUtils.determineBank(assignedAddress));
	}
	
	public int getAssignedAddress()
	{
		return assignedAddress;
	}
	
	public String getId()
	{
		return id;
	}
	
	public Map<String, SegmentReference> getSegmentReferencesById()
	{
		Map<String, SegmentReference> segRefsById = new HashMap<>();
		for (Entry<String, Segment> idSeg : segments.entrySet())
		{
			segRefsById.put(idSeg.getKey(), idSeg.getValue());
		}
		return segRefsById;
	}
	
	public int getWorstCaseSizeOnBank(byte bank)
	{
		// Because some instructions like JR can change in size based on the other
		// code lengths, we need to do this somewhat iteratively in case the size
		// does change
		short worstCaseSize = 0;
		int baseAddress = assignedAddress;
		if (baseAddress == CompilerUtils.UNASSIGNED_ADDRESS || baseAddress == CompilerUtils.UNASSIGNED_LOCAL_ADDRESS)
		{
			baseAddress = 0;
		}
		
		Iterator<Segment> segItr = segments.values().iterator();
		Segment currSeg;
		while (segItr.hasNext())
		{
			currSeg = segItr.next();
			
			// If its a new segment offset, that means something shrunk earlier on so
			// we need to start over in case it impacted other segments
			if (currSeg.setAssignedAddress(baseAddress + worstCaseSize))
			{
				segItr = segments.values().iterator();
				worstCaseSize = 0;
				continue;
			}
			
			worstCaseSize += currSeg.getWorstCaseSizeOnBank(bank);
		}
		
		return worstCaseSize;
	}
	
	public void replacePlaceholderIds(Map<String, String> placeholderToArgsForIds)
	{
		Map<String, Segment> refreshedSegments = new HashMap<>();
		for (Entry<String, Segment> seg : segments.entrySet())
		{
			String segId = CompilerUtils.replacePlaceholders(seg.getKey(), placeholderToArgsForIds);
			if (refreshedSegments.put(segId, seg.getValue()) != null)
			{
				throw new IllegalArgumentException("Duplicate segment label was found while replacing placeholders: " + segId);
			}
		}
		segments = refreshedSegments;
	}
	
	public void linkData(Texts romTexts, Map<String, SegmentReference> segRefsById)
	{
		for (Segment seg : segments.values())
		{
			seg.linkData(romTexts, getSegmentReferencesById(), segRefsById);
		}
	}
		
	public int writeBytes(byte[] bytes)
	{
		int lastEnd = 0;
		for (Segment seg : segments.values())
		{
			lastEnd = seg.writeBytes(bytes);
		}
		return lastEnd;
	}
}
