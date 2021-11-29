package data.custom_card_effects;

import compiler.CompilerUtils;
import rom_packer.MovableBlock;

public class EffectFunction
{
	private MovableBlock block;
	private short address;
	
	EffectFunction(MovableBlock block)
	{
		this.block = block;
		this.address = CompilerUtils.UNASSIGNED_ADDRESS;
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
