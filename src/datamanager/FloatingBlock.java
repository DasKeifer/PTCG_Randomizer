package datamanager;

import java.util.SortedMap;

import compiler.DataBlock;
import constants.RomConstants;

public class FloatingBlock extends MoveableBlock
{
	public FloatingBlock(byte priority, DataBlock toPlaceInBank, BankPreference... prefs)
	{
		super(priority, toPlaceInBank, prefs);
	}

	@Override
	public SortedMap<Byte, BankRange> getPreferencedAllowableBanks()
	{
		SortedMap<Byte, BankRange> toReturn = super.getPreferencedAllowableBanks();
		toReturn.put(Byte.MAX_VALUE, new BankRange((byte) 0, (byte) (RomConstants.NUMBER_OF_BANKS - 1)));
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
