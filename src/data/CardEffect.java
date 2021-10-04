package data;

import romAddressing.AssignedAddresses;

public abstract class CardEffect 
{
	public abstract CardEffect copy();
	public abstract void writeEffectPointer(byte[] moveBytes, int startIndex, AssignedAddresses assignedAddresses);
	@Override
	public abstract String toString();
}
