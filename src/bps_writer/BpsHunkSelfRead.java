package bps_writer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import gbc_framework.utils.ByteUtils;

public class BpsHunkSelfRead extends BpsHunk
{
	byte[] data;
	
	public BpsHunkSelfRead(byte[] data) 
	{
		super(BpsHunkType.SELF_READ, data.length);
		this.data = data;
	}
	
	@Override
	public void apply(byte[] targetBytes, int targetIndex, byte[] originalBytes) 
	{
		ByteUtils.copyBytes(targetBytes, targetIndex, data);
	}
	
	@Override
	public void write(ByteArrayOutputStream bpsOs) throws IOException 
	{
		writeHunkHeader(bpsOs);
		bpsOs.write(data);
	}
}
