package datamanager;

import java.util.SortedMap;

import compiler.CodeSnippit;

public class NoConstraintBlock extends FlexibleBlock
{
	protected NoConstraintBlock(byte priority, CodeSnippit toPlaceInBank)
	{
		super(priority, toPlaceInBank);
	}
	
	protected void addAllowableBank(byte priority, byte bank)
	{
		addAllowableBank(priority, bank);
	}
	
	protected void addAllowableBankRange(byte priority, byte bankStart, byte bankStopExclusive)
	{
		addAllowableBankRange(priority, bankStart, bankStopExclusive);
	}

	@Override
	public SortedMap<Byte, BankRange> getPreferencedAllowableBanks()
	{
		SortedMap<Byte, BankRange> toReturn = super.getPreferencedAllowableBanks();
		toReturn.put(Byte.MAX_VALUE, new BankRange((byte) 0, Byte.MAX_VALUE));
		return toReturn;
	}

	@Override
	public boolean shrinksNotMoves() 
	{
		// This moves
		return false;
	}

	@Override
	public int getMinimalSizeOnBank(byte bank) 
	{
		return 0;
	}

	@Override
	public NoConstraintBlock applyShrink() 
	{
		return null;
	}

	@Override
	public NoConstraintBlock revertShrink() 
	{
		return null;
	}
}
