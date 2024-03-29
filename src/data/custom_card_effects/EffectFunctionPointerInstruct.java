package data.custom_card_effects;

import compiler.Instruction;
import constants.DuelConstants.EffectFunctionTypes;
import rom.Texts;
import rom_addressing.AssignedAddresses;
import rom_addressing.BankAddress;
import util.ByteUtils;
import util.RomUtils;

public class EffectFunctionPointerInstruct implements Instruction
{
	EffectFunctionTypes functionType;
	String functionLabel;
	
	// No version that takes an address because thats only use in the case where we
	// use an address from the rom which will always use the default logic
	public EffectFunctionPointerInstruct(EffectFunctionTypes functionType, String functionLabel) 
	{
		this.functionType = functionType;
		this.functionLabel = functionLabel;
	}

	@Override
	public void extractText(Texts texts) 
	{
		// Nothing to do here
	}

	@Override
	public int getWorstCaseSize(
			BankAddress unused, 
			AssignedAddresses assignedAddresses,
			AssignedAddresses tempAssigns) 
	{
		// First try to get it from the temp ones
		BankAddress address = tempAssigns.getTry(functionLabel);
		
		// If it was unassigned, then try from the assigned ones
		if (address == BankAddress.UNASSIGNED)
		{
			address = assignedAddresses.getTry(functionLabel);
		}
		
		// If its in b we can use the default logic that only needs the function type and the loaded bank b offset
		if (address.getBank() == CustomCardEffect.EFFECT_FUNCTION_SHORTCUT_BANK)
		{
			return 3;
		}
		
		// Otherwise we either haven't assigned the bank or its not in b so we assume
		// the worst
		return 4;
	}

	@Override
	public int writeBytes(byte[] bytes, int addressToWriteAt, AssignedAddresses assignedAddresses) 
	{
		BankAddress address = assignedAddresses.getThrow(functionLabel);
		if (!address.isFullAddress())
		{
			throw new IllegalAccessError("EffectFunctionPointerInstruct tried to write address for " + 
					functionLabel + " but it is not fully assigned: " + address.toString());
		}
		
		// If its not in the bank we can't use the shortcut
		// So we need to write the type with the multibank offset value then the bank it actually
		// is in
		int writeIdx = addressToWriteAt;
		if (address.getBank() != CustomCardEffect.EFFECT_FUNCTION_SHORTCUT_BANK)
		{
			bytes[writeIdx++] = (byte)(functionType.getValue() + CustomCardEffect.MULTIBANK_EFFECT_OFFSET);
			bytes[writeIdx++] = address.getBank();
		}
		else
		{
			// If it is in the shortcut bank, we just have to write the type
			bytes[writeIdx++] = functionType.getValue();
		}
		
		// Then we always write the loaded bank offset
		ByteUtils.writeAsShort(RomUtils.convertFromBankOffsetToLoadedOffset(address), bytes, writeIdx);
		writeIdx += 2;
		
		return writeIdx - addressToWriteAt;
	}
}
