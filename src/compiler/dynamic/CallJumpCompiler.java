package compiler.dynamic;

import compiler.CompilerConstants.InstructionConditions;
import constants.RomConstants;
import compiler.BasicInstructionCompiler;

public class CallJumpCompiler implements DynamicCompiler
{
	private static final byte JR_BYTE_LENGTH = 2;
	private static final byte JP_BYTE_LENGTH = 3;
	private static final byte CALL_BYTE_LENGTH = 3;
	private static final byte FARCALL_BYTE_LENGTH = 4;
	
	// Required bank (things like being in home/replacing specific portions with farcalls)
	// Allowed banks (things like effect commands if we support a secondary bank for them)
	// Preferred banks (For grouping like data)
	
	// Not really necessarily bank 0 but we use this internally instead of a bool and it makes life easier
	// internally to treat it this way
	public static final byte LOCAL_CALL_BANK = 0;
	
	public enum CallJumpType
	{
		CALL, JP, JR;
	}
	
	// Implement JP to HL?
	// Implement JR as well since this may change with our reprogramming/changing of lengths?
	// TODO: implement JR - assume it will always be local, may need to replace with JP though
		// First do all the call jumps. Then go through top to bottom and do the JRs?
	
	byte bank;
	Short address;
	CallJumpType type;
	InstructionConditions condition;
	
	// We use a private constructor and factory-like constructors so we can name them to make it clear
	// what is being created
	private CallJumpCompiler(byte bankToCall, short addressToCall, boolean isCallNotJp, InstructionConditions instructCond)
	{
		this(bankToCall, 
				addressToCall, 
				isCallNotJp ? CallJumpType.CALL : CallJumpType.JP, 
				InstructionConditions.NONE
		);
	}
	
	private CallJumpCompiler(byte bankToCall, short addressToCall, CallJumpType jumpCallType, InstructionConditions instructCond)
	{
		bank = bankToCall;
		address = addressToCall;
		type = jumpCallType;
		condition = instructCond;
	}
	
	// TODO: probably should error check these factories
	public static CallJumpCompiler createLocalCallJump(short localAddressToCall, boolean isCallNotJp)
	{
		return new CallJumpCompiler(LOCAL_CALL_BANK, localAddressToCall, isCallNotJp, InstructionConditions.NONE);
	}
	
	public static CallJumpCompiler createLocalConditionalCallJump(short localAddressToCall, boolean isCallNotJp, InstructionConditions instructCond)
	{
		return new CallJumpCompiler(LOCAL_CALL_BANK, localAddressToCall, isCallNotJp, instructCond);
	}
	
	public static CallJumpCompiler createCallJump(byte bankToCall, short addressToCall, boolean isCallNotJp)
	{
		// If its in the home bank, its a local call/jump
		if (addressToCall < RomConstants.BANK_SIZE)
		{
			createLocalCallJump(addressToCall, isCallNotJp);
		}
		return new CallJumpCompiler(bankToCall, addressToCall, isCallNotJp, InstructionConditions.NONE);
	}
	
	public static CallJumpCompiler createConditionalCallJump(byte bankToCall, short addressToCall, boolean isCallNotJp, InstructionConditions instructCond)
	{
		// If its in the home bank, its a local call/jump
		if (addressToCall < RomConstants.BANK_SIZE)
		{
			createLocalConditionalCallJump(addressToCall, isCallNotJp, instructCond);
		}
		return new CallJumpCompiler(bankToCall, addressToCall, isCallNotJp, instructCond);
	}

	public static CallJumpCompiler crateJumpRelative(byte jpOffset, InstructionConditions instructCond)
	{
		throw new UnsupportedOperationException("JR");
		//return new CallJumpCompiler(LOCAL_CALL_BANK, jpOffset, JumpCallType.JR, instructCond);
	}
	
	@Override
	public int getSizeOnBank(byte hostBank)
	{
		// Local call - call to the loaded bank
		if (bank == hostBank || bank == LOCAL_CALL_BANK)
		{
			// add as a call or jp as preferred
			// Both can handle conditionals
			if (CallJumpType.CALL == type)
			{
				return CALL_BYTE_LENGTH;
			}
			else if (CallJumpType.JP == type)
			{
				return JP_BYTE_LENGTH;
			}
			else
			{
				// More complicated - if the new offset is larger than the max, then
				// we have to replace with a JP
				throw new UnsupportedOperationException("JR - Size loaded");
			}
		}
		// TODO: add a farjump in RST
		// Far call/jump - call to an unloaded bank
		else
		{
			// Always need a farcall/farjump
			int size = FARCALL_BYTE_LENGTH;
			
			// If we have a condition, we need to add a JR
			if (InstructionConditions.NONE != condition)
			{
				size += JR_BYTE_LENGTH;
			}
			
			// If its a jump, we need to add a return
			if (CallJumpType.CALL != type)
			{
				size += compiler.instructions.Ret.SIZE;
			}
			return size;
		}
	}

	@Override
	public int writeBytesForBank(byte[] bytes, int indexToWriteAt, byte hostBank)
	{		
		// If its a local bank or JR, replace it as appropriate
		if (bank == hostBank || bank == LOCAL_CALL_BANK)
		{
			if (CallJumpType.CALL == type)
			{
				indexToWriteAt = BasicInstructionCompiler.writeCall(bytes, indexToWriteAt, address, condition);
			}
			else if (CallJumpType.JP == type)
			{
				indexToWriteAt = BasicInstructionCompiler.writeJp(bytes, indexToWriteAt, address, condition);
			}
			else
			{
				// More complicated - if the new offset is larger than the max, then
				// we have to replace with a JP
				throw new UnsupportedOperationException("JR - write local");
			}
		}
		// If its a remote bank, we need to make a remote call with some wrappers
		// around it based on if its a jump and if it has conditionals
		else
		{			
			// If it has conditionals, we need to add a JR to be able to skip over it
			if (InstructionConditions.NONE != condition)
			{
				byte jumpSize = FARCALL_BYTE_LENGTH;
				if (CallJumpType.CALL != type)
				{
					jumpSize += compiler.instructions.Ret.SIZE;
				}
				indexToWriteAt = BasicInstructionCompiler.writeJr(bytes, indexToWriteAt, jumpSize, condition);
			}
			
			// Add the farcall
			indexToWriteAt = BasicInstructionCompiler.writeFarcall(bytes, indexToWriteAt, bank, address);
			
			// If its a jump, we need to add a return after the call so it behaves similarly
			if (CallJumpType.CALL != type)
			{
				indexToWriteAt = BasicInstructionCompiler.writeRet(bytes, indexToWriteAt);
			}
		}
		
		return indexToWriteAt;
	}
}
