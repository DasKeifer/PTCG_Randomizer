package bps_writer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import bps_writer.BpsHunk.BpsHunkType;

import java.util.TreeMap;

import gbc_framework.QueuedWriter;
import gbc_framework.rom_addressing.AddressRange;
import gbc_framework.utils.ByteUtils;

public class BpsWriter implements QueuedWriter
{	
	public enum BpsHunkCopyType
	{
		SOURCE_COPY(BpsHunkType.SOURCE_COPY),
		TARGET_COPY(BpsHunkType.TARGET_COPY);

        private BpsHunkType type;

        private BpsHunkCopyType(BpsHunkType type) 
        {
			this.type = type;
		}
        
        public BpsHunkType asBpsHunkType()
        {
        	return type;
        }
	}
	
	// The target address and the hunk that starts at the target address
	TreeMap<Integer, BpsHunk> hunks;
	TreeMap<Integer, Integer> spacesToBlank;
	
	int selfReadBeingCreatedAddress;
	String selfReadBeingCreatedName;
	ByteArrayOutputStream selfReadBeingCreated;
	
	public BpsWriter() 
	{
		hunks = new TreeMap<>();
		spacesToBlank = new TreeMap<>();
		
		selfReadBeingCreatedAddress = -1;
		selfReadBeingCreated = new ByteArrayOutputStream();
	}

	@Override
	public void append(byte... bytes) throws IOException
	{
		selfReadBeingCreated.write(bytes);
	}
	
	@Override
	public String getCurrentBlockName()
	{
		return selfReadBeingCreatedName;
	}
	
	@Override
	public void startNewBlock(int segmentStartAddress)
	{
		startNewBlock(segmentStartAddress, BpsHunkSelfRead.DEFAULT_NAME);
	}
	
	@Override
	public void startNewBlock(int segmentStartAddress, String segmentName)
	{
		// Check if we are overwriting the previous hunk
		checkForPrevHunkOverwrite(segmentStartAddress, segmentName);		
		finalizeSelfReadBeingCreated();
		selfReadBeingCreatedAddress = segmentStartAddress;
		selfReadBeingCreatedName = segmentName;
	}

	@Override
	public void startNewBlock(int segmentStartAddress, List<AddressRange> reuseHints)
	{		
		startNewBlock(segmentStartAddress, reuseHints, BpsHunkSelfRead.DEFAULT_NAME);
	}
	
	@Override
	public void startNewBlock(int segmentStartAddress, List<AddressRange> reuseHints, String segmentName)
	{	
		// TODO: Optimization Use hints to reduce patch size. For now its fine to just ignore
		startNewBlock(segmentStartAddress, segmentName);
		
		// Create a self read patch but store the existing start address and length? Then we
		// can blank the previous spot if it was moved or do a source read or source copy
		// if its unchanged?
	}

	@Override
	public void queueBlankedBlock(AddressRange range) 
	{
		spacesToBlank.put(range.getStart(), range.getStopExclusive());
	}

	public void newCopyHunk(int targetAddress, BpsHunkCopyType type, int size, int copyFromStartIndex)  
	{
		BpsHunkCopy copyHunk = new BpsHunkCopy(type, size, copyFromStartIndex);
		newCopyHunkCommon(copyHunk, targetAddress);
	}
	
	public void newCopyHunk(String name, int targetAddress, BpsHunkCopyType type, int size, int copyFromStartIndex)  
	{
		BpsHunkCopy copyHunk = new BpsHunkCopy(name, type, size, copyFromStartIndex);
		newCopyHunkCommon(copyHunk, targetAddress);
	}
	
	private void newCopyHunkCommon(BpsHunkCopy copyHunk, int targetAddress)  
	{
		// Check that this hunk doesn't overwrite any others
		checkForPrevHunkOverwrite(targetAddress, copyHunk.getName());
		checkForNextHunkOverwrite(targetAddress, copyHunk.getLength(), copyHunk.getName());
		
		finalizeSelfReadBeingCreated();
		hunks.put(targetAddress, copyHunk);
	}
	
	private void finalizeSelfReadBeingCreated()
	{
		if (selfReadBeingCreatedAddress > 0 && selfReadBeingCreated.size() > 0)
		{
			// Check for overflow of the current hunk into next one
			checkForNextHunkOverwrite(selfReadBeingCreatedAddress, selfReadBeingCreated.size(), selfReadBeingCreatedName);
			
			// TODO: Optimization possibly elsewhere... Check against the source to see if its actually needed or if we can just freeride
			hunks.put(selfReadBeingCreatedAddress, new BpsHunkSelfRead(selfReadBeingCreatedName, selfReadBeingCreated.toByteArray()));
			selfReadBeingCreated.reset();
			selfReadBeingCreatedAddress = -1;
			selfReadBeingCreatedName = "INTERNAL_NAME_ERROR";
		}
	}
	
	private void checkForPrevHunkOverwrite(int hunkStartAddress, String hunkName)
	{
		Entry<Integer, BpsHunk> prevHunk = hunks.lowerEntry(hunkStartAddress);
		if (prevHunk != null &&
				prevHunk.getKey() + prevHunk.getValue().getLength() - 1 >= hunkStartAddress)
		{
			throw new IllegalArgumentException("Overwrite of the previous hunk \"" + 
					prevHunk.getValue().getName() + "\"(starting at " + prevHunk.getKey() + 
					" and ending at " + (prevHunk.getKey() + 
					prevHunk.getValue().getLength() - 1) + ") was detected starting at " +
					hunkStartAddress + " while adding hunk \"" + hunkName + "\"");
		}
	}
	
	private void checkForNextHunkOverwrite(int hunkStartAddress, int length, String hunkName)
	{
		Entry<Integer, BpsHunk> nextHunk = hunks.higherEntry(hunkStartAddress);
		if (nextHunk != null &&
				hunkStartAddress + length - 1 >= nextHunk.getKey())
		{
			throw new IllegalArgumentException("Overwrite of the next hunk \"" + 
					nextHunk.getValue().getName() + "\"(starting at " + nextHunk.getKey() + 
					") was detected while checking hunk \"" + hunkName + "\" starting at "
					+ hunkStartAddress + " and ending at " + (hunkStartAddress + length - 1));
		}
	}
	
	public void createBlanksAndFillEmptyHunksWithSourceRead(int targetLength, int sourceLength, List<AddressRange> toBlank)
	{
		queueBlankedBlocks(toBlank);
		createBlanksAndFillEmptyHunksWithSourceRead(targetLength, sourceLength);
	}

	public void createBlanksAndFillEmptyHunksWithSourceRead(int targetLength, int sourceLength)
	{
		// Ensure any pending ones are finalized prior to filling gaps
		finalizeSelfReadBeingCreated();
		
		// Handle if the target is longer than the source
		if (targetLength > sourceLength)
		{
			spacesToBlank.put(sourceLength, targetLength);
		}
		
		// Go through the existing hunks in order filling in any gaps until we reach
		// the end of the file
		TreeMap<Integer, BpsHunk> fillerHunks = new TreeMap<>();
		int lastEndAddressExclusive = 0;
		Iterator<Entry<Integer, Integer>> nextBlankItr = spacesToBlank.entrySet().iterator();
		Entry<Integer, Integer> nextBlank = getNextOrNull(nextBlankItr);
		for (Entry<Integer, BpsHunk> hunk : hunks.entrySet())
		{
			int hunkStart = hunk.getKey();
			
			// There is a gap we need to fill
			if (hunkStart > lastEndAddressExclusive)
			{
				fillSpaceWithSourceReadOrBlanks(lastEndAddressExclusive, hunkStart, nextBlank, nextBlankItr, fillerHunks);
			}
			// We filled too much of a gap or we have overlap between hunks
			else if (hunkStart < lastEndAddressExclusive)
			{
				// TODO: error
				throw new IllegalArgumentException("Ovelapping hunks detected! TODO");
			}
			// else the space matches up to the end of the previous hunk - we don't need to do anything
			
			// Now that we are done processing this hunk, set the last address to the end of this hunk
			// and move to the next one
			lastEndAddressExclusive = hunkStart + hunk.getValue().getLength();
		}
		
		// Ensure the target wasn't too short
		if (targetLength < lastEndAddressExclusive)
		{
			throw new IllegalArgumentException("TODO");
		}
		
		// Add the final reads to the end of the file
		fillSpaceWithSourceReadOrBlanks(lastEndAddressExclusive, targetLength, nextBlank, nextBlankItr, fillerHunks);
		
		// Now add any added hunks to the map
		hunks.putAll(fillerHunks);
	}
	
	private void fillSpaceWithSourceReadOrBlanks(
			int fillFrom, 
			int fillTo, Entry<Integer, Integer> nextBlank,
			Iterator<Entry<Integer, Integer>> nextBlankItr,
			TreeMap<Integer, BpsHunk> fillerHunks
	)
	{
		// TODO: Arg?
		final byte fillByte = 0;

		while (fillTo > fillFrom)
		{
			// While the next blank is already passed, get the next one
			while (nextBlank != null && nextBlank.getValue() <= fillFrom)
			{
				nextBlank = getNextOrNull(nextBlankItr);
			}
			
			// See if the blank starts after this hunk or there are no more blanks. If so, we have no
			// blanks in this gap and can finish filling in with source reads
			if (nextBlank == null || nextBlank.getKey() >= fillTo)
			{
				// Fill to the next hunk with source reads
				fillerHunks.put(fillFrom, new BpsHunkSourceRead(fillTo - fillFrom));
				fillFrom = fillTo;
			}
			// Otherwise the next blank overlaps with the space we are filling and we need to see how
			// to split it up
			else
			{
				// If the blank starts after the last end address, we need to do some source reads to the start of
				// the next blank
				if (nextBlank.getKey() > fillFrom)
				{
					// Fill to the blank with source reads
					fillerHunks.put(fillFrom, new BpsHunkSourceRead(nextBlank.getKey() - fillFrom));
					fillFrom = nextBlank.getKey(); // Causes the else to be hit in the next loop if not start of next hunk
				}
				// If it starts at or before this fill segment, go ahead and do a blank hunk to the
				// end of the blank/next hunk whichever is first
				else
				{
					int blankEnd = nextBlank.getValue();
					if (blankEnd >= fillTo)
					{
						blankEnd = fillTo;
					}
					fillerHunks.put(fillFrom, new BpsHunkSelfRead(fillByte, blankEnd - fillFrom));
					fillFrom = blankEnd;
				}
			}
		}
	}
	
	private <T> T getNextOrNull(Iterator<T> itr)
	{
		if (itr.hasNext())
		{
			return itr.next();
		}
		else
		{
			return null;
		}
	}
	
	// TODO: Minor Take metadata?	
	public void writeBps(File file, byte[] originalBytes)
	{
		// Ensure any pending ones are finalized prior to writing
		finalizeSelfReadBeingCreated();
		
		// TODO: Try to combine hunks?
		// TODO: Overlap & gap (target final length) checking?
		
		// Set the offsets for writing
		BpsHunkCopy.setOffsetsForWriting();

		// TODO: Support differing sizes
		byte[] targetBytes = originalBytes.clone();
		
		// Start writing the bytes for the BPS and the header
		try (ByteArrayOutputStream bpsOs = new ByteArrayOutputStream(); 
				FileOutputStream fos = new FileOutputStream(file))
		{
			bpsOs.write('B');
			bpsOs.write('P');
			bpsOs.write('S');
			bpsOs.write('1');
			
			// Write the sizes in four byte sizes
			bpsOs.write(ByteUtils.sevenBitEncode(originalBytes.length));
			bpsOs.write(ByteUtils.sevenBitEncode(targetBytes.length));
			bpsOs.write(ByteUtils.sevenBitEncode(0)); // TODO: Minor For now no metadata
			
			// Write the hunks to the patch output stream
			for (BpsHunk hunk : hunks.values())
			{
				hunk.write(bpsOs);
			}
			
			// Write the source CRC
			bpsOs.write(ByteUtils.toLittleEndianBytes(ByteUtils.computeCrc32(originalBytes), 4));
			
			// Next we need to determine the target CRC by applying the patch and computing
			// the CRC on the patch bytes and then write that
			for (Entry<Integer, BpsHunk> entry : hunks.entrySet())
			{
				entry.getValue().apply(targetBytes, entry.getKey(), originalBytes);
			}
			bpsOs.write(ByteUtils.toLittleEndianBytes(ByteUtils.computeCrc32(targetBytes), 4));
			

			// Finally we need to put the CRC of the patch itself
			// So we get the BPS bytes written, write them, calculate the CRC
			// then write that
			byte[] bpsBytes = bpsOs.toByteArray();
			// TODO: BPS temp
//			fos.write(targetBytes);
			fos.write(bpsBytes);
			fos.write(ByteUtils.toLittleEndianBytes(ByteUtils.computeCrc32(bpsBytes), 4));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
