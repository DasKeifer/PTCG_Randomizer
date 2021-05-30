package data;

import java.util.EnumMap;
import java.util.Map.Entry;

import compiler.CompilerUtils;
import compiler.DataBlock;
import compiler.dynamic.BlockAddress;
import compiler.dynamic.RawBytes;
import constants.RomConstants;
import datamanager.BankPreference;
import datamanager.FloatingBlock;
import constants.DuelConstants.EffectCommandTypes;
import rom.Blocks;
import util.ByteUtils;

public class MoveEffect 
{
	private class DataOrAddr
	{
		DataOrAddr(DataBlock block)
		{
			this.block = block;
			this.address = CompilerUtils.UNASSIGNED_ADDRESS;
		}

		DataOrAddr(short address)
		{
			this.block = null;
			this.address = address;
		}
		
		public DataBlock block;
		public short address;
	}
	
	String id;
	byte priority;
	private EnumMap<EffectCommandTypes, DataOrAddr> effects;
	private DataBlock effectCommand;
	
	public MoveEffect(String id, byte priority)
	{
		this.id = id;
		this.priority = priority;
		effects = new EnumMap<>(EffectCommandTypes.class);
	}
	
	public void addEffectCommand(EffectCommandTypes type, DataBlock function)
	{
		effects.put(type, new DataOrAddr(function));
	}

	public void addEffectCommand(EffectCommandTypes type, short addr)
	{
		effects.put(type, new DataOrAddr(addr));
	}

	public void convertAndAddBlocks(Blocks blocks)
	{
		if (!effects.isEmpty())
		{
			effectCommand = new DataBlock(id);
			for (Entry<EffectCommandTypes, DataOrAddr> effect : effects.entrySet())
			{
				effectCommand.appendInstruction(new RawBytes(effect.getKey().getValue()));
				if (effect.getValue().address >= 0)
				{
					byte[] bytes = new byte[2];
					ByteUtils.writeAsShort(effect.getValue().address, bytes, 0);
					effectCommand.appendInstruction(new RawBytes(bytes));
				}
				else
				{
					effectCommand.appendInstruction(new BlockAddress(effect.getValue().block.getId(), 2, RomConstants.EFFECT_FUNCTION_POINTER_OFFSET)); // Is there any? Think its always relative to loaded bank
					blocks.addMoveableBlock(new FloatingBlock(priority, effect.getValue().block, 
							new BankPreference((byte) 1, (byte)0xb, (byte)0xc)));//,
						//	new BankPreference((byte) 2, (byte)0xc, (byte)0xd))); // TODO: Probably more spaces we can place this
				}
			}
			// Append a null at the end so we know its finished
			effectCommand.appendInstruction(new RawBytes((byte) 0));
			blocks.addMoveableBlock(new FloatingBlock(priority, effectCommand, new BankPreference((byte) 1, (byte)6, (byte)7))); // TODO: figure out where these can effectively live	
		}
	}
	
	// TODO temp
	public int getAssignedAddress()
	{
		return effectCommand.getAssignedAddress();
	}
}
