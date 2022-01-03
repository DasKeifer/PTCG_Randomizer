package bps_writer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import gbc_framework.utils.ByteUtils;

public class BpsHunkSourceRead extends BpsHunk
{
	public static final String DEFAULT_NAME = "UNNAMED_SOURCE_READ_HUNK";
	
	public BpsHunkSourceRead(int length)
	{
		this(DEFAULT_NAME, length);
	}
	
	public BpsHunkSourceRead(String name, int length)
	{
		super(name, BpsHunkType.SOURCE_READ, length);
	}

	@Override
	public void apply(byte[] targetBytes, int targetIndex, byte[] originalBytes) 
	{
		ByteUtils.copyBytes(targetBytes, targetIndex, originalBytes, targetIndex, getLength());
	}
	
	@Override
	public void write(ByteArrayOutputStream bpsOs) throws IOException 
	{
		writeHunkHeader(bpsOs);
		// Nothing else to write
	}
}
