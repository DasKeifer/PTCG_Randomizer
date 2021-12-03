package bps_writer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map.Entry;

import bps_writer.BpsHunk.BpsHunkType;

import java.util.SortedMap;
import java.util.TreeMap;

import gbc_framework.SegmentedWriter;
import gbc_framework.utils.ByteUtils;

public class BpsWriter implements SegmentedWriter
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
	SortedMap<Integer, BpsHunk> hunks;
	
	int patchReadBeingCreatedAddress;
	ByteArrayOutputStream patchReadBeingCreated;
	
	public BpsWriter() 
	{
		hunks = new TreeMap<>();
		
		patchReadBeingCreatedAddress = -1;
		patchReadBeingCreated = new ByteArrayOutputStream();
	}

	@Override
	public void append(byte... bytes) throws IOException
	{
		patchReadBeingCreated.write(bytes);
	}

	@Override
	public void newSegment(int segmentStartAddress)
	{
		finalizePatchBeingCreated();
		patchReadBeingCreatedAddress = segmentStartAddress;
	}

	public void newCopyHunk(int targetAddress, BpsHunkCopyType type, int size, int copyFromStartIndex)  
	{
		finalizePatchBeingCreated();
		hunks.put(targetAddress, new BpsHunkCopy(type, size, copyFromStartIndex));
	}
	
	private void finalizePatchBeingCreated()
	{
		if (patchReadBeingCreatedAddress > 0 && patchReadBeingCreated.size() > 0)
		{
			// TODO: Check against the source to see if its actually needed or if we can just freeride
			hunks.put(patchReadBeingCreatedAddress, new BpsHunkSelfRead(patchReadBeingCreated.toByteArray()));
			patchReadBeingCreated.reset();
			patchReadBeingCreatedAddress = -1;
		}
	}
	
	// TODO: Take filename? also metadata?
	public void writeBps(File file, byte[] originalBytes)
	{
		// Set the offsets for writing
		BpsHunkCopy.setOffsetsForWriting();
		
		FileOutputStream fos = new FileOutputStream(file);
		
		// First go through and apply the patches so we can find out target crc
		// Start with a copy of the original so we don't have to do the
		// SOURCE_READ hunks here
		byte[] targetBytes = originalBytes.clone();
		for (Entry<Integer, BpsHunk> entry : hunks.entrySet())
		{
			entry.getValue().apply(targetBytes, entry.getKey(), originalBytes);
		}
		
		// Now Calculate the destination CRC
		long destinationCrc = ByteUtils.computeCrc32(targetBytes);
		
		// Now fill any empty spaces for the sourceRead

		
		// Start writing the bytes for the BPS and the header
		ByteArrayOutputStream bpsOs = new ByteArrayOutputStream();
		bpsOs.write('B');
		bpsOs.write('P');
		bpsOs.write('S');
		bpsOs.write('1');
		
		// Write the sizes
		bpsOs.write(ByteUtils.sevenBitEncode(originalBytes.length));
		bpsOs.write(ByteUtils.sevenBitEncode(targetBytes.length));
		bpsOs.write(ByteUtils.sevenBitEncode(0)); // TODO: For now no metadata
		
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
	}
	
	public void fillEmptyHunksWithSourceRead(int sourceLength)
	{
		// TODO: implement
		
		// TODO: Check for overlaps and spaces too
	}
}
