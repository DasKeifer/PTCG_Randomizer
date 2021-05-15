package compiler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import compiler.dynamic.PlaceholderInstruction;

import java.util.Set;

import rom.Texts;

public class DataBlock 
{	
	private int assignedAddress;
	Map<String, Segment> segments;
	private String id;
	
	// TODO move most of the CompilerUtils references inside functions in the compiler utils class
	
	// Generic, highest level construct for holding just raw data or a series of functions
	// Represents one block that we want to place contiguously in the ROM
	// I.e. replace Code Snippet
	public DataBlock(String startingSegmentName, String source)
	{
		assignedAddress = CompilerUtils.UNASSIGNED_ADDRESS;
		segments = new HashMap<>();
		id = startingSegmentName.trim();
		String sourcePlusSegmentName = startingSegmentName + CompilerUtils.SEGMENT_ENDLINE + CompilerUtils.LINE_BREAK + source;
		parseSource(startingSegmentName, sourcePlusSegmentName.split(CompilerUtils.LINE_BREAK));
	}
	
	public DataBlock(String source)
	{
		assignedAddress = CompilerUtils.UNASSIGNED_ADDRESS;
		segments = new HashMap<>();
		
		String[] lines = source.split(CompilerUtils.LINE_BREAK);
		if (!lines[0].trim().endsWith(CompilerUtils.SEGMENT_ENDLINE))
		{
			throw new IllegalArgumentException("The first line must be a Segment label (i.e. the segment name followed by a ':'");
		}

		id = getSegmentName(lines[0]);
		parseSource(id, lines);
	}
	
	// TODO change to manually input lines as separate strings so we can format strings inline
	private void parseSource(String startingSegmentName, String[] sourceLines)
	{
		segments.clear();
		
		String rootSegmentName = startingSegmentName;
		Segment currSegment = new Segment();
		segments.put(rootSegmentName, currSegment);
		
		// Plus one because we already handled the first line
		String line;
		for (int lineIdx = 1; lineIdx < sourceLines.length; lineIdx++)
		{
			// TODO: alot of this maybe should be moved to compiler utils
			
			// split of the instruction (if there is one)
			line = sourceLines[lineIdx].trim();
			if (line.endsWith(CompilerUtils.SEGMENT_ENDLINE))
			{
				rootSegmentName = getSegmentName(line);
				currSegment = new Segment();
				
				// Ensure there was no conflict within the block
				if (segments.put(rootSegmentName, currSegment) != null)
				{
					throw new IllegalArgumentException("Duplicate segment label was found: " + rootSegmentName);
				}
			}
			else if (line.startsWith(CompilerUtils.SUBSEGMENT_STARTLINE))
			{
				// Form it with the addition of the "."
				String secondarySegName = getSubsegmentName(rootSegmentName, line);
				currSegment = new Segment();

				// Ensure there was no conflict within the block
				if (segments.put(secondarySegName, currSegment) != null)
				{
					throw new IllegalArgumentException("Duplicate segment label was found: " + secondarySegName);
				}
			}
			// A line that will turn into bytes
			else
			{
				// If its a placeholder, we defer filling it out
				if (CompilerUtils.isPlaceholderLine(line))
				{
					currSegment.appendPlaceholderInstruction(PlaceholderInstruction.create(line, rootSegmentName));
				}
				else
				{
					currSegment.appendInstruction(CompilerUtils.parseInstruction(line, rootSegmentName));
				}
			}
		}
	}
	
	private String getSegmentName(String line)
	{
		return line.substring(0, line.indexOf(CompilerUtils.SEGMENT_ENDLINE)).trim();
	}
	
	private String getSubsegmentName(String segmentName, String line)
	{
		return segmentName + line.trim();
	}
	
	public void setAssignedAddress(int address)
	{
		this.assignedAddress = address;
	}
	
	public int getAssignedAddress()
	{
		return assignedAddress;
	}
	
	public String getId()
	{
		return id;
	}
	
	public Set<String> getAllSegmentIds()
	{
		return segments.keySet();
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
			}
			
			worstCaseSize += currSeg.getWorstCaseSizeOnBank(bank);
		}
		
		return worstCaseSize;
	}

	// TODO: copy as many times as needed, evaluate placeholders in labels/ids, create 
	// list of all strings to segments, replace all instruction placeholders and link, 
	// then allocate/pack then write
	
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
	
	public void evaluateInstructionPlaceholdersAndLinkData(Texts romTexts, Map<String, Segment> labelToSegment, Map<String, String> placeholderToArgs)
	{
		for (Segment seg : segments.values())
		{
			seg.evaluatePlaceholdersAndLinkData(romTexts, segments, labelToSegment, placeholderToArgs);
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
