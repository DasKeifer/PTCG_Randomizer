package data.custom_card_effects;

import compiler.CompilerUtils;
import gbc_rom_packer.MoveableBlock;

public class EffectFunction
{
	private MoveableBlock block;
	private short address;
	
	EffectFunction(MoveableBlock block)
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
	
	public MoveableBlock getBlock()
	{
		return block;
	}
	
	public short getAddress()
	{
		return address;
	}
}
