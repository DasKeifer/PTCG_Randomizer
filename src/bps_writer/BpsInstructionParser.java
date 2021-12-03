package bps_writer;


import compiler.Instruction;
import compiler.InstructionParser;

public class BpsInstructionParser extends InstructionParser
{		
	@Override
	protected Instruction parseInstructionImpl(String[] keyArgs, String[] args, String rootSegment)
	{
		switch (keyArgs[0])
		{
			case "bps_sc":
			case "bps_tc":
				return SourceTargetCopyInstruct.create(keyArgs[0], args);
		
			default:
				return super.parseInstructionImpl(keyArgs, args, rootSegment);
		}
	}
}
