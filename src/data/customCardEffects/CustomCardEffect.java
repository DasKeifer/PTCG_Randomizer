package data.customCardEffects;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import compiler.CompilerUtils;
import compiler.referenceInstructs.Jump;
import compiler.staticInstructs.RawBytes;
import datamanager.MoveableBlock;
import datamanager.ReplacementBlock;
import datamanager.UnconstrainedMoveBlock;
import constants.DuelConstants.EffectFunctionTypes;
import data.CardEffect;
import rom.Blocks;
import romAddressing.AssignedAddresses;
import romAddressing.BankAddress;
import romAddressing.PrioritizedBankRange;
import util.ByteUtils;
import util.RomUtils;

public class CustomCardEffect extends CardEffect
{
	public static final byte EFFECT_FUNCTION_SHORTCUT_BANK = 0xb;
	public static final byte MULTIBANK_EFFECT_OFFSET = (byte) 0x80;

	private static final String checkCommandLoopFunction = "$3012";
	private static final String foundCommandFunction = "$301d";
	private static final String wEffectFunctionsBank = "$ce22";
	private static final List<String>	MoreEffectBanksTweakLogicSegCode = Arrays.asList(new String[] {
			"MoreEffectBanksTweak_logicSeg:", 
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
	});
	
	private static final List<PrioritizedBankRange> effectFunctionPrefs = Arrays.asList(new PrioritizedBankRange[] {
		new PrioritizedBankRange(1, (byte)0xb, (byte)0xc),
		new PrioritizedBankRange(2, (byte)0xa, (byte)0xb)
	});
	
	private static final PrioritizedBankRange effectCommandPref = new PrioritizedBankRange(1, (byte)6, (byte)7);
				
	
	private class DataOrAddr
	{
		DataOrAddr(MoveableBlock block)
		{
			this.block = block;
			this.address = CompilerUtils.UNASSIGNED_ADDRESS;
		}

		DataOrAddr(short address)
		{
			this.block = null;
			this.address = address;
		}
		
		public MoveableBlock block;
		public short address;
	}
	
	String id;
	private EnumMap<EffectFunctionTypes, DataOrAddr> effects;
	// TODO later: For now they are constrained. Maybe make a tweak to allow more banks
	private MoveableBlock effectCommand; 
	
	public CustomCardEffect(String id)
	{
		this.id = id;
		effects = new EnumMap<>(EffectFunctionTypes.class);
		effectCommand = null;
	}

	@Override
	public CardEffect copy()
	{
		// TODO later: Try an remove card copying. If not then consider
		// making this a deep copy
		CustomCardEffect copy = new CustomCardEffect(id);
		copy.effects = new EnumMap<>(effects);
		copy.effectCommand = effectCommand;
		return copy;
	}
	
	public static void addTweakToAllowEffectsInMoreBanks(Blocks blocks)
	{
		/* Skip over 
		ld a, BANK("Effect Functions")		(2 bytes)
		ld [wEffectFunctionsBank], a 		(3 bytes)
		* We add an empty block because the replacement block will handle placing in nops for us to 
		* get it to the correct size
		*/
		blocks.addFixedBlock(new ReplacementBlock("MoreEffectBanksTweak_removeSeg", 0x300d, 5));
		
		/* replace
		 cp c
		 jr z, .matching_command_found
		 inc hl
		 inc hl
		 with our custom logic that handles other banks
		 */	
		UnconstrainedMoveBlock logicBlock = new UnconstrainedMoveBlock(MoreEffectBanksTweakLogicSegCode,
				0, (byte)0x0, (byte)0x1); //Try to fit it in home to avoid bankswaps
		logicBlock.addAllowableBankRange(1, (byte)0xb, (byte)0xd);
		blocks.addMoveableBlock(logicBlock);

		// Create the block to jump to our new logic and make sure it fits nicely into
		// the existing instructions
		ReplacementBlock callLogicSeg = new ReplacementBlock("MoreEffectBanksTweak_jumpToLogicSeg", 0x3016, 5);
		callLogicSeg.appendInstruction(new Jump(logicBlock.getId()));
		blocks.addFixedBlock(callLogicSeg);
	}
	
	public void addEffectFunction(EffectFunctionTypes type, List<String> sourceLines)
	{
		effects.put(type, new DataOrAddr(new UnconstrainedMoveBlock(sourceLines, effectFunctionPrefs)));
	}

	// For referencing existing functions in the game
	public void addEffectFunction(EffectFunctionTypes type, short bankBAddress)
	{
		effects.put(type, new DataOrAddr(bankBAddress));
	}

	public List<MoveableBlock> convertToBlocks()
	{
		List<MoveableBlock> blocks = new LinkedList<>();
		if (!effects.isEmpty())
		{
			effectCommand = new MoveableBlock(id, effectCommandPref);
			blocks.add(effectCommand);
			for (Entry<EffectFunctionTypes, DataOrAddr> effect : effects.entrySet())
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
					// Add the block containing the custom effect
					blocks.add(effect.getValue().block);
					// Now add the instruction to this effect command for pointing to it
					effectCommand.appendInstruction(new EffectFunctionPointerInstruct(effect.getKey(), effect.getValue().block.getId()));
				}
			}
			// Append a null at the end so we know its finished
			effectCommand.appendInstruction(new RawBytes((byte) 0));
		}
		return blocks;
	}

	@Override
	public void writeEffectPointer(byte[] moveBytes, int startIndex, AssignedAddresses assignedAddresses)
	{
		BankAddress pointerAddress = assignedAddresses.getThrow(effectCommand.getId());
		ByteUtils.writeAsShort(RomUtils.convertFromBankOffsetToLoadedOffset(pointerAddress), moveBytes, startIndex);
	}

	@Override
	public String toString() 
	{
		return effectCommand.getId();
	}
}
