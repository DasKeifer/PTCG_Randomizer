package bps_writer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import gbc_framework.utils.ByteUtils;

public class BpsHunkSelfRead extends BpsHunk
{
	public static final String DEFAULT_NAME = "UNNAMED_SELF_READ_HUNK";
	
	byte[] data;
	
	public BpsHunkSelfRead(int destinationIndex, byte toRepeat, int numRepeats) 
	{
		this(DEFAULT_NAME, destinationIndex, createRepeatedByteArray(toRepeat, numRepeats));
	}
	
	private static byte[] createRepeatedByteArray(byte toRepeat, int numRepeats) 
	{
		byte[] repeatData = new byte[numRepeats];
		for (int i = 0; i < numRepeats; i++)
		{
			repeatData[i] = toRepeat;
		}
		return repeatData;
	}
	
	public BpsHunkSelfRead(int destinationIndex, byte[] data) 
	{
		this(DEFAULT_NAME, destinationIndex, data);
	}
	
	public BpsHunkSelfRead(String name, int destinationIndex, byte[] data) 
	{
		super(name, destinationIndex, BpsHunkType.SELF_READ, data.length);
		this.data = data;
	}
	
	@Override
	public void apply(byte[] targetBytes,byte[] originalBytes) 
	{
		ByteUtils.copyBytes(targetBytes, getDestinationIndex(), data);
	}
	
	@Override
	public void write(ByteArrayOutputStream bpsOs) throws IOException 
	{
		checkDestinationIndex(bpsOs);
		writeHunkHeader(bpsOs);
		bpsOs.write(data);
	}
}
