package data;

import datamanager.AllocatedIndexes;

public abstract class CardEffect 
{
	public abstract CardEffect copy();
	public abstract void writeEffectPointer(byte[] moveBytes, int startIndex, AllocatedIndexes allocIndexes);
	@Override
	public abstract String toString();
}
