package compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import compiler.referenceInstructs.PlaceholderInstruction;
import rom.Texts;

public class DataBlock 
{	
	public static String END_OF_DATA_BLOCK_SUBSEG_LABEL = "__end_of_data_block__";
	LinkedHashMap<String, Segment> segments; // linked to keep order
	private String id;

	String rootSegmentName;
	Segment currSegment;
	Segment endSegment; // and empty segment so we can refer to with "." + END_OF_BLOCK_LABEL for any datablock
							// TODO: maybe move to linking logic instead?
	
	// Constructor to keep instruction/line less constructors from being ambiguous
	public DataBlock(String startingSegmentName)
	{
		// The instruction version takes is probably less overhead and its more likely to
		// be aligned with the manner in which it will be used (i.e instructions added later)
		this (startingSegmentName, new Instruction[0]);
	}
	
	public DataBlock(String startingSegmentName, Instruction... instructions)
	{
		segments = new LinkedHashMap<>();
		id = startingSegmentName.trim();
		endSegment = new Segment();
		
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

	public DataBlock(String startingSegmentName, String... source)
	{
		this (startingSegmentName, Arrays.asList(source));
	}
	
	// Generic, highest level construct for holding just raw data or a series of functions
	// Represents one block that we want to place contiguously in the ROM
	// I.e. replace Code Snippet
	public DataBlock(String startingSegmentName, List<String> sourceLines)
	{
		segments = new LinkedHashMap<>();
		id = startingSegmentName.trim();
		parseSource(startingSegmentName, sourceLines);
	}
	
	public DataBlock(List<String> sourceLines)
	{
		segments = new LinkedHashMap<>();

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
		
		endSegment = new Segment();
		newSegment(startingSegmentName);
		
		for (String line : sourceLines)
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
		Map<String, Segment> segRefsById = new HashMap<>();
		for (Entry<String, Segment> idSeg : segments.entrySet())
		{
			segRefsById.put(idSeg.getKey(), idSeg.getValue());
		}
		
		// Add the end segment marker
		segRefsById.put(CompilerUtils.formSubsegmentName(END_OF_DATA_BLOCK_SUBSEG_LABEL, id), endSegment);
		return segRefsById;
	}
	
	public int getWorstCaseSizeOnBank(byte bankToGetSizeOn, int allocAddress, Map<String, Integer> allocatedIndexes)
	{
		// Because some instructions like JR can change in size based on the other
		// code lengths, we need to do this somewhat iteratively in case the size
		// does change
		short worstCaseSize = 0;
		
		Iterator<Segment> segItr = segments.values().iterator();
		Segment currSeg;
		while (segItr.hasNext())
		{
			currSeg = segItr.next();
			worstCaseSize += currSeg.getWorstCaseSizeOnBank(bankToGetSizeOn, allocAddress, allocatedIndexes);
		}
		
		return worstCaseSize;
	}
	
	public void replacePlaceholderIds(Map<String, String> placeholderToArgsForIds)
	{
		// Replace placeholders in Id
		id = CompilerUtils.replacePlaceholders(id, placeholderToArgsForIds);
		
		LinkedHashMap<String, Segment> refreshedSegments = new LinkedHashMap<>();
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
		for (Segment seg : segments.values())
		{
			seg.extractTexts(texts);
		}
		
		// End reference has no code so no text exisist in it
	}

	public static boolean debug;
	public int writeBytes(byte[] bytes, int assignedAddress, Map<String, Integer> allocatedIndexes)
	{
		debug = id.contains("CallFor");
		if (debug) 
		{
			System.out.println("Segment - " + id);
		}
		
		int lastEndOffset = 0;
		for (Segment seg : segments.values())
		{
			lastEndOffset += seg.writeBytes(bytes, assignedAddress + lastEndOffset, allocatedIndexes);
		}
		
		// End reference has no code so no writing needs to be done
		
		// TODO: Value used?
		return lastEndOffset;
	}
}
