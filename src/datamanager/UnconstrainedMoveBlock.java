package datamanager;

import java.util.SortedMap;

import compiler.DataBlock;
import constants.RomConstants;

public class UnconstrainedMoveBlock extends MoveableBlock
{
	public UnconstrainedMoveBlock(byte priority, DataBlock toPlaceInBank, BankPreference... prefs)
	{
		super(priority, toPlaceInBank, prefs);
	}

	@Override
	public SortedMap<Byte, BankRange> getPreferencedAllowableBanks()
	{
		// TODO: be consistent with priority - low is high?
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
	public UnconstrainedMoveBlock getRemoteBlock() 
	{
		return this;
	}
}
