package compiler.tmp;

import util.ByteUtils;

public class RawBytesCompiler implements DynamicCompiler 
{
	byte[] storedBytes;
	
	public RawBytesCompiler(byte[] bytes)
	{
		storedBytes = bytes.clone();
	}

	@Override
	public int getSizeOnBank(byte bank)
	{
		return storedBytes.length;
	}

	@Override
	public int writeBytesForBank(byte[] bytes, int indexToWriteAt, byte bank)
	{
		ByteUtils.copyBytes(bytes, indexToWriteAt, storedBytes);
		return indexToWriteAt + storedBytes.length;
	}
}
