package compiler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import compiler.dynamic.Instruction;
import compiler.dynamic.Jump;
import compiler.dynamic.JumpCallCommon;
import compiler.fixed.*;

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
	
	private void parseSource(String startingSegmentName, String[] sourceLines)
	{
		segments.clear();
		
		String rootSegmentName = startingSegmentName;
		short currOffset = 0;
		Segment currSegment = new Segment(rootSegmentName, currOffset);
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
				currSegment = new Segment(rootSegmentName, currOffset);
				
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
				currSegment = new Segment(secondarySegName, currOffset);

				// Ensure there was no conflict within the block
				if (segments.put(secondarySegName, currSegment) != null)
				{
					throw new IllegalArgumentException("Duplicate segment label was found: " + rootSegmentName);
				}
			}
			// A line that will turn into bytes
			else
			{
				Instruction instruct = create(line, rootSegmentName);
				currSegment.appendInstruction(instruct);
				
				// We assume worst case. Later when we link and place, we will use the more
				// accurate sizes
				currOffset += instruct.getMaxSize();
			}
		}
		
		// Go through and try to make any intrablock links
		for (Segment seg : segments.values())
		{
			seg.linkIntrablockLinks(segments);
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
	
	public int getWorstCaseSizeOnBank(byte bank)
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
			
			// If its a new segment offset, that means something shrunk earlier on so
			// we need to start over in case it impacted other segments
			if (currSeg.setOffset(worstCaseSize))
			{
				segItr = segments.values().iterator();
				worstCaseSize = 0;
			}
			
			worstCaseSize += currSeg.getWorstCaseSizeOnBank(bank);
		}
		
		return worstCaseSize;
	}
	
	public static Instruction create(String line, String rootSegment)
	{
		// Split the keyword off
		String[] keyArgs = line.split(" ", 1);
		
		// Split the args apart
		String[] args = new String[0];
		if (keyArgs.length >= 1)
		{
			args = keyArgs[1].split(",");
		}
		
		switch (keyArgs[0])
		{
			// TODO: complete this list
			// Loading
			case "lb":
				return Lb.create(args);
			case "ld":
				return Ld.create(args);
		
			// Logic
			case "cp":
				return Cp.create(args);
			case "inc":
				return Inc.create(args);
				
			// Flow control
			case "jr":
				// JR is a bit special because we only allow it inside a block and we only
				// allow referencing labels
				return Jump.createJr(args, rootSegment);
			case "jp":
			case "farjp":
				return JumpCallCommon.create(args, rootSegment, true); // true == jump
			case "call":
			case "farcall":
				return JumpCallCommon.create(args, rootSegment, false); // false == call
				
			// Writing raw data
				
			default:
				throw new UnsupportedOperationException("Unrecognized instruction key: " + keyArgs[0]);
		}
	}
}
