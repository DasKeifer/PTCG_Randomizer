package bps_writer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import gbc_framework.utils.ByteUtils;

public abstract class BpsHunk
{
	public enum BpsHunkType
	{
		SOURCE_READ(0), SELF_READ(1), SOURCE_COPY(2), TARGET_COPY(3);
		
		private byte value;
		BpsHunkType(int inValue)
		{
			if (inValue > ByteUtils.MAX_BYTE_VALUE || inValue < 0)
			{
				throw new IllegalArgumentException("Invalid constant input for "
						+ "BpsHunkType enum: " + inValue);
			}
			value = (byte) inValue;
		}
		
		byte getValue()
		{
			return value;
		}
	}
	
	private BpsHunkType type;
	private int length;
	
	protected BpsHunk(BpsHunkType type, int length)
	{
		this.type = type;
		this.length = length;
	}

	public abstract void apply(byte[] targetBytes, int targetIndex, byte[] originalBytes);	
	public abstract void write(ByteArrayOutputStream bpsOs) throws IOException;
	
	protected void writeHunkHeader(ByteArrayOutputStream bpsOs) throws IOException
	{
	    // We know the length is at least 1
		long lengthAndType = (getLength() - 1) << 2 + getType().getValue();
		bpsOs.write(ByteUtils.sevenBitEncode(lengthAndType));
	}

	
	public int getLength() 
	{
		return length;
	}

	public BpsHunkType getType() 
	{
		return type;
	}
}
