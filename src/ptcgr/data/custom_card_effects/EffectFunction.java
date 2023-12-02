package ptcgr.data.custom_card_effects;

import gbc_framework.rom_addressing.BankAddress;
import rom_packer.MovableBlock;

public class EffectFunction
{
	private MovableBlock block;
	private short address;
	
	EffectFunction(MovableBlock block)
	{
		this.block = block;
		this.address = BankAddress.UNASSIGNED_ADDRESS;
	}

	EffectFunction(short address)
	{
		this.block = null;
		this.address = address;
	}
	
	public boolean isExistingFunction()
	{
		return block == null;
	}
	
	public MovableBlock getBlock()
	{
		return block;
	}
	
	public short getAddress()
	{
		return address;
	}
}
