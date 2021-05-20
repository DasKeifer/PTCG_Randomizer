package datamanager;

import java.util.SortedMap;

import compiler.DataBlock;

public class FloatingBlock extends MoveableBlock
{
	protected FloatingBlock(byte priority, DataBlock toPlaceInBank)
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
	public boolean movesNotShrinks() 
	{
		// This moves
		return true;
	}

	@Override
	public int getShrunkWorstCaseSizeOnBank(byte bank) 
	{
		return 0;
	}

	@Override
	public FloatingBlock getRemoteBlock() 
	{
		return this;
	}
}
