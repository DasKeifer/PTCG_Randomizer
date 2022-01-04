package bps_writer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.EnumMap;

import bps_writer.BpsWriter.BpsHunkCopyType;
import gbc_framework.utils.ByteUtils;

public class BpsHunkCopy extends BpsHunk
{
	public static final String DEFAULT_NAME = "UNNAMED_COPY_HUNK";
	
	// Do this as an enum map for future proofing
	private static EnumMap<BpsHunkType, Integer> prevVals = new EnumMap<>(BpsHunkType.class);
	
	private int fromIndex;
	
	public BpsHunkCopy(BpsHunkCopyType type, int length, int fromIndex) 
	{
		this(DEFAULT_NAME, type, length, fromIndex);
	}
	
	public BpsHunkCopy(String name, BpsHunkCopyType type, int length, int fromIndex) 
	{
		super(name, type.asBpsHunkType(), length);
		this.fromIndex = fromIndex;
	}
	
	@Override
	public void apply(byte[] targetBytes, int targetIndex, byte[] originalBytes) 
	{
		switch (getType())
		{
			case SOURCE_COPY:
				ByteUtils.copyBytes(targetBytes, targetIndex, originalBytes, fromIndex, getLength());
				break;
			case TARGET_COPY:
				ByteUtils.copyBytes(targetBytes, targetIndex, targetBytes, fromIndex, getLength());
				break;
			default:
				throw new IllegalArgumentException("Internal error: Invalid type for copy BPS Hunk was found:" + getType());
		}
	}
	
	@Override
	public void write(ByteArrayOutputStream bpsOs) throws IOException 
	{
		writeHunkHeader(bpsOs);
		
		// These are stored as offsets from the last used value
		// instead of absolute values. Convert the absolute value
		// to the offset then update the last used value
		int offset = fromIndex - prevVals.get(getType());
		prevVals.put(getType(), fromIndex);
		
		bpsOs.write(ByteUtils.sevenBitEncodeSigned(offset));
	}
	
	public static void setOffsetsForWriting()
	{
		// Set all values to 0
		for (BpsHunkCopyType type : BpsHunkCopyType.values())
		{
			prevVals.put(type.asBpsHunkType(), 0);
		}
	}
}
