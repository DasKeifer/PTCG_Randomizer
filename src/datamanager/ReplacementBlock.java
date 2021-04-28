package datamanager;

import compiler.SetLengthCodeSnippet;

public abstract class ReplacementBlock
{
	int addressStartToReplace;
	boolean onlyInPlace;
	
	// bank specific
	SetLengthCodeSnippet replaceWith;
	byte[] dataToReplace;
	
	// Code to write if the additional commands are being written to a 
	// remote bank
	// Note that a call is 4 and a jump is 5 and conditional add +2
	SetLengthCodeSnippet replaceWithRemote;
	byte[] dataToReplaceRemote;

	protected ReplacementBlock(int startAddress)
	{
		addressStartToReplace = startAddress;
		onlyInPlace = false;
	}
	
	// Only applicable for overwriting a specific location?	
	protected void setInPlaceData(SetLengthCodeSnippet replaceWith, byte[] bytesToReplace)
	{
		if (replaceWithRemote != null || (replaceWith != null && !onlyInPlace))
		{
			throw new UnsupportedOperationException("Tried to set a replacement block that was set to have an additional call to only be an in place call");
		}

		this.replaceWith = new SetLengthCodeSnippet(replaceWith);
		this.dataToReplace = dataToReplace.clone();
		onlyInPlace = true;
	}
	
	protected void setMinimalInPlacePlusLocalData(SetLengthCodeSnippet replaceWith, byte[] dataToReplace)
	{
		if (!onlyInPlace)
		{
			throw new UnsupportedOperationException("Tried to set a replacement block that was set to be only in place to have an additional call");
		}
		
		this.replaceWith = new SetLengthCodeSnippet(replaceWith);
		this.dataToReplace = dataToReplace.clone();
		
		// Modding code (multibank effect functions, more booster packs, etc.)
		
		// Overwrite a section of code using JP/Call
	}
	
	// Applicable for in place writing or block specific writing (like effect function)
	protected void setMinimalInPlacePlusRemoteData(SetLengthCodeSnippet replaceWith, byte[] dataToReplace)
	{
		if (!onlyInPlace)
		{
			throw new UnsupportedOperationException("Tried to set a replacement block that was set to be only in place to have an additional call");
		}

		replaceWithRemote = new SetLengthCodeSnippet(replaceWith);
		dataToReplaceRemote = dataToReplace.clone();
		
		// Same as local. Perhaps we automate this and keep them as the same?
		
		// Overwrite a section of code using Farcall
	}
}
