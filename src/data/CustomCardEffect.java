package data;

import java.util.EnumMap;
import java.util.Map.Entry;

import compiler.CompilerUtils;
import compiler.DataBlock;
import compiler.referenceInstructs.BlockBankLoadedAddress;
import compiler.staticInstructs.RawBytes;
import datamanager.AllocatedIndexes;
import datamanager.BankAddress;
import datamanager.BankPreference;
import datamanager.MoveableBlock;
import datamanager.ReplacementBlock;
import datamanager.UnconstrainedMoveBlock;
import constants.DuelConstants.EffectCommandTypes;
import rom.Blocks;
import util.ByteUtils;
import util.RomUtils;

public class CustomCardEffect extends CardEffect
{
	private final byte MULTIBANK_EFFECT_OFFSET = (byte) 0x80;
	
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
	
	public CustomCardEffect(String id, byte priority)
	{
		this.id = id;
		this.priority = priority;
		effects = new EnumMap<>(EffectCommandTypes.class);
	}

	@Override
	public CardEffect copy()
	{
		// TOOD: Implement
		return null;
	}
	
	// TODO: Add tweak to allow for effect commands in banks other than 6?
	
	public static void addTweakToAllowEffectsInMoreBanks(Blocks blocks)
	{
		/* Skip over 
		ld a, BANK("Effect Functions")		(2 bytes)
		ld [wEffectFunctionsBank], a 		(3 bytes)
		* We add an empty block because the replacement block will handle placing in nops for us to 
		* get it to the correct size
		*/
		DataBlock removeSeg = new DataBlock("MoreEffectBanksTweak_removeSeg", "nop $5"); // TODO: Add as auto optimization for fixed blocks
		blocks.addFixedBlock(new ReplacementBlock(0x300d, removeSeg, 5));
		
		/* replace
		 cp c
		 jr z, .matching_command_found
		 inc hl
		 inc hl
		 with our custom logic that handles other banks
		 */		
		final String checkCommandLoopFunction = "$3012";
		final String foundCommandFunction = "$301d";
		final String wEffectFunctionsBank = "$ce22";
		DataBlock logicSeg = new DataBlock("MoreEffectBanksTweak_logicSeg", 
				"cp c",
				"jr z, .default_bank",
				"sub a, $80",
				"push af", // Store the flags
				"cp c",
				"jr z, .other_bank",
				// If the subtract carried, then it was not one off our custom command
				// pointers, so we skip passed one of the inc hl. Otherwise it was
				// one of the custom pointers so we need to increment 3 times
				"pop af", // Get the flags back and see if it was a carry
				"jr c, .inc_hl_twice",	
				"inc hl",
			".inc_hl_twice",
				"inc hl",
				"inc hl",
				// Normally we would jump back to where we cut from but that just immediately
				// jumps to the check command loop so we save some ops and just jump there
				// directly
				"jp " + checkCommandLoopFunction,
				
			".default_bank",
				"ld a, $b",
				"jr .loadBank",

			".other_bank",
				"pop af", // pop extra push of stack to support our longer pointers
				"ld a, [hl]",
				"inc hl",
				// flow through to loadbank

			".loadBank",
				"ld ["+ wEffectFunctionsBank + "], a",
				"jp " + foundCommandFunction
		);
		blocks.addMoveableBlock(new UnconstrainedMoveBlock((byte) 0, logicSeg, 
				new BankPreference((byte) 0, (byte)0x0, (byte)0x1), //Try to fit it in home to avoid bankswaps
				new BankPreference((byte) 1, (byte)0xb, (byte)0xd)));

		// Create the block to jump to our new logic and make sure it fits nicely into
		// the existing instructions
		DataBlock callLogicSeg = new DataBlock("MoreEffectBanksTweak_jumpToLogicSeg", "jp " + logicSeg.getId());
		blocks.addFixedBlock(new ReplacementBlock(0x3016, callLogicSeg, 5));
	}
	
	public void addEffectCommand(EffectCommandTypes type, DataBlock function)
	{
		effects.put(type, new DataOrAddr(function));
	}

	// For referencing existing functions in the game
	public void addEffectCommand(EffectCommandTypes type, short bankBAddress)
	{
		effects.put(type, new DataOrAddr(bankBAddress));
	}

	public void convertAndAddBlocks(Blocks blocks)
	{
		if (!effects.isEmpty())
		{
			effectCommand = new DataBlock(id);
			for (Entry<EffectCommandTypes, DataOrAddr> effect : effects.entrySet())
			{
				if (effect.getValue().address >= 0) // If it has an address, its an existing function we will tie into
				{
					byte[] bytes = new byte[3];
					bytes[0] = effect.getKey().getValue();
					ByteUtils.writeAsShort(effect.getValue().address, bytes, 1);
					effectCommand.appendInstruction(new RawBytes(bytes));
				}
				else // Otherwise we need to figure out where to put it
				{
					// TODO: Make a new class for this that will compress as possible?					
					
					// Signal its a 3 byte pointer while preserving the value
					effectCommand.appendInstruction(new RawBytes((byte)(effect.getKey().getValue() + MULTIBANK_EFFECT_OFFSET)));
					// Add the 3 byte pointer
					effectCommand.appendInstruction(new BlockBankLoadedAddress(effect.getValue().block.getId(), true)); // True = include bank
					// Add the moveable block
					blocks.addMoveableBlock(new UnconstrainedMoveBlock(priority, effect.getValue().block, 
							new BankPreference((byte) 1, (byte)0xb, (byte)0xc),
							new BankPreference((byte) 2, (byte)0xa, (byte)0xb))); // TODO: Probably additional preferred spaces we can place this
				}
			}
			// Append a null at the end so we know its finished
			effectCommand.appendInstruction(new RawBytes((byte) 0));
			blocks.addMoveableBlock(new MoveableBlock(priority, effectCommand, new BankPreference((byte) 1, (byte)6, (byte)7)));
		}
	}

	@Override
	public void writeEffectPointer(byte[] moveBytes, int startIndex, AllocatedIndexes allocIndexes)
	{
		BankAddress pointerAddress = allocIndexes.getThrow(effectCommand.getId());
		ByteUtils.writeAsShort(RomUtils.convertFromBankOffsetToLoadedOffset(pointerAddress.bank, pointerAddress.addressInBank), moveBytes, startIndex);
	}

	@Override
	public String toString() 
	{
		return effectCommand.getId();
	}
}
