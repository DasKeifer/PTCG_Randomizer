package data;

import datamanager.AllocatedIndexes;
import util.ByteUtils;

public class UnchangedCardEffect extends CardEffect
{
	public static final CardEffect NONE = new UnchangedCardEffect((short) 0);
	
	short pointer;
	
	public UnchangedCardEffect(short pointer) 
	{
		this.pointer = pointer;
	}

	@Override
	public CardEffect copy()
	{
		return new UnchangedCardEffect(pointer);
	}

	@Override
	public void writeEffectPointer(byte[] moveBytes, int startIndex, AllocatedIndexes allocIndexes) 
	{
		ByteUtils.writeAsShort(pointer, moveBytes, startIndex);
	}

	@Override
	public String toString() 
	{
		return String.format("0x%x", pointer);
	}
}
