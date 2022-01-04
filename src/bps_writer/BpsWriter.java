package bps_writer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import bps_writer.BpsHunk.BpsHunkType;

import java.util.TreeMap;

import gbc_framework.QueuedWriter;
import gbc_framework.rom_addressing.AddressRange;

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
	public void blankUnusedSpace(AddressRange range) 
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
	
	public void createBlanksAndFillEmptyHunksWithSourceRead(int sourceLength)
	{
		// Ensure any pending ones are finalized prior to filling gaps
		finalizeSelfReadBeingCreated();
		
		// TODO: BPS implement
		
		// TODO: BPS Check for overlaps and spaces too
	}
	
	// TODO: Minor Take metadata?
	public void writeBps(File file, byte[] originalBytes)
	{
		// Ensure any pending ones are finalized prior to writing
		finalizeSelfReadBeingCreated();
		
		// Set the offsets for writing
		BpsHunkCopy.setOffsetsForWriting();

		byte[] targetBytes = originalBytes.clone();
		
		try (FileOutputStream fos = new FileOutputStream(file))
		{
			// First go through and apply the patches so we can find out target crc
			// Start with a copy of the original so we don't have to do the
			// SOURCE_READ hunks here
			for (Entry<Integer, BpsHunk> entry : hunks.entrySet())
			{
				entry.getValue().apply(targetBytes, entry.getKey(), originalBytes);
			}
			
			// TODO: BPS temp
			fos.write(targetBytes);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Now fill any empty spaces for the sourceRead
		
		// TODO: BPSFor now just save the whole rom until we are sure the
		// changes to saving works
		/*
		 * 
		// Now Calculate the destination CRC
		long destinationCrc = ByteUtils.computeCrc32(targetBytes);
		
		// Start writing the bytes for the BPS and the header
		ByteArrayOutputStream bpsOs = new ByteArrayOutputStream();
		bpsOs.write('B');
		bpsOs.write('P');
		bpsOs.write('S');
		bpsOs.write('1');
		
		// Write the sizes
		bpsOs.write(ByteUtils.sevenBitEncode(originalBytes.length));
		bpsOs.write(ByteUtils.sevenBitEncode(targetBytes.length));
		bpsOs.write(ByteUtils.sevenBitEncode(0)); // TODO: Minor For now no metadata
		
		// Write the hunks
		for (BpsHunk hunk : hunks.values())
		{
			hunk.write(bpsOs);
		}
		
		// Write the source and destination CRCs
		bpsOs.write(ByteUtils.toLittleEndianBytes(ByteUtils.computeCrc32(originalBytes), 4));
		bpsOs.write(ByteUtils.toLittleEndianBytes(destinationCrc, 4));
		
		// Now get the BPS bytes written
		byte[] bpsBytes = bpsOs.toByteArray();
		
		// Write them to the file then append the CRC32 for the BPS to the file
		fos.write(bpsBytes);
		fos.write(ByteUtils.toLittleEndianBytes(ByteUtils.computeCrc32(bpsBytes), 4));
		fos.close();
		*/
	}

}
