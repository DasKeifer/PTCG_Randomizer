package ptcgr.compiler;

import java.io.IOException;
import java.util.Arrays;

import compiler.CompilerUtils;
import compiler.Instruction;
import compiler.LabelReferenceInstruction;
import gbc_framework.QueuedWriter;
import gbc_framework.rom_addressing.AssignedAddresses;
import gbc_framework.rom_addressing.BankAddress;
import gbc_framework.utils.ByteUtils;
import gbc_framework.utils.RomUtils;
import ptcgr.constants.DuelConstants.EffectFunctionTypes;
import ptcgr.data.custom_card_effects.CustomCardEffect;

public class EffectFunctionPointer extends LabelReferenceInstruction
{
	EffectFunctionTypes functionType;
	
	// No version that takes an address because thats only use in the case where we
	// use an address from the rom which will always use the default logic
	public EffectFunctionPointer(EffectFunctionTypes functionType, String functionLabel) 
	{
		super(functionLabel);
		this.functionType = functionType;
	}
	
	public static EffectFunctionPointer create(String[] args)
	{	
		final String supportedArgs = "efp only supports (byte, string): ";	

		if (args.length != 2)
		{
			throw new IllegalArgumentException(supportedArgs + "given: " + Arrays.toString(args));
		}
		
		try
		{
			return new EffectFunctionPointer(
					EffectFunctionTypes.readFromByte(CompilerUtils.parseByteArg(args[0])), args[1]);
		}
		catch (IllegalArgumentException iae)
		{
			throw new IllegalArgumentException(supportedArgs + iae.getMessage());
		}
	}

	@Override
	public int getWorstCaseSize(
			BankAddress unused, 
			AssignedAddresses assignedAddresses,
			AssignedAddresses tempAssigns) 
	{
		// First try to get it from the temp ones
		BankAddress address = Instruction.tryGetAddress(getLabel(), assignedAddresses, tempAssigns);
		
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
	public int writeBytes(QueuedWriter writer, BankAddress instructionAddress, AssignedAddresses assignedAddresses)
			throws IOException 
	{
		BankAddress addressToWrite = assignedAddresses.getThrow(getLabel());
		if (!addressToWrite.isFullAddress())
		{
			throw new IllegalAccessError("EffectFunctionPointerInstruct tried to write address for " + 
					getLabel() + " but it is not fully assigned: " + addressToWrite.toString());
		}
		
		// If its not in the bank we can't use the shortcut
		// So we need to write the type with the multibank offset value then the bank it actually
		// is in
		int writeSize = 1;
		if (addressToWrite.getBank() != CustomCardEffect.EFFECT_FUNCTION_SHORTCUT_BANK)
		{
			writeSize++;
			writer.append(
					(byte)(functionType.getValue() + CustomCardEffect.MULTIBANK_EFFECT_OFFSET),
					addressToWrite.getBank()
			);
		}
		else
		{
			// If it is in the shortcut bank, we just have to write the type
			writer.append(functionType.getValue());
		}
		
		// Then we always write the loaded bank offset
		writer.append(ByteUtils.shortToLittleEndianBytes(RomUtils.convertFromBankOffsetToLoadedOffset(addressToWrite)));
		writeSize += 2;
		
		return writeSize;
	}
}
