package compiler.staticInstructs;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import compiler.StaticInstruction;
import util.ByteUtils;

public class RawBytes extends StaticInstruction
{
	List<byte[]> allBytes;
	
	public RawBytes(byte... bytes) 
	{
		super(bytes.length);
		
		allBytes = new LinkedList<>();
		allBytes.add(bytes);
	}
	
	public RawBytes(byte[]... bytes) 
	{
		super(determineSize(bytes));
		allBytes = new LinkedList<>();
		Collections.addAll(allBytes, bytes);
	}
	
	private static int determineSize(byte[]... bytes)
	{
		int size = 0;
		for (byte[] set : bytes)
		{
			size += set.length;
		}
		return size;
	}

	@Override
	public void writeStaticBytes(byte[] bytes, int indexToAddAt) 
	{
		for (byte[] set : allBytes)
		{
			ByteUtils.copyBytes(bytes, indexToAddAt, set);
			indexToAddAt += set.length;
		}
		
	}
}
