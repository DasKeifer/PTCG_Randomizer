package datamanager;

import java.util.Arrays;

import compiler.CodeSnippit;

public class SpecificEmptyLocBlock extends ReplacementBlock
{
	SpecificEmptyLocBlock(int startAddress)
	{
		super(startAddress);
	}
	
	// Only applicable for overwriting a specific location?	
	public void setInPlaceData(CodeSnippit replaceWith)
	{
		super.SpecificLocBlock(replaceWith, createAndFill(replaceWith.size(), 0xFF));
	}
	
	public void setMinimalInPlacePlusLocalData(CodeSnippit replaceWith)
	{
		super.setMinimalInPlacePlusLocalData(replaceWith, createAndFill(replaceWith.size(), 0xFF));
	}
	
	// Applicable for in place writing or block specific writing (like effect function)
	public void setMinimalInPlacePlusRemoteData(CodeSnippit replaceWith)
	{
		super.setMinimalInPlacePlusRemoteData(replaceWith, createAndFill(replaceWith.size(), 0xFF));
	}
	
	private byte[] createAndFill(int length, byte value)
	{
		byte[] created = new byte[length];
		Arrays.fill(created, value);
		return created;
	}
}