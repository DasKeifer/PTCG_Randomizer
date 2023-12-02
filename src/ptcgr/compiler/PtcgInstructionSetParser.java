package ptcgr.compiler;

import java.util.LinkedList;
import java.util.List;

import compiler.CompilerUtils;
import compiler.Instruction;
import compiler.InstructionSetParser;
import ptcgr.rom.Texts;

public class PtcgInstructionSetParser implements InstructionSetParser
{
	private List<Ldtx> ldtxInstructs;
	
	public PtcgInstructionSetParser() 
	{
		ldtxInstructs = new LinkedList<>();
	}

	@Override
	public List<String> getSupportedInstructions() 
	{
		return List.of("jp", "jr", "call", "farcall", "bank1call", "ldtx", "efp");
	}
	
	@Override
	public Instruction parseInstruction(String instruction, String args, String rootSegment)
	{
		String[] splitArgs = CompilerUtils.splitArgs(args);
		switch (instruction)
		{	
			// "overwrite" calls to allow farcalls
			case "call":
			case "farcall":
				return FarCall.create(splitArgs, rootSegment);
			case "bank1call":
				return Bank1Call.create(splitArgs);
				
			case "ldtx":
				// we don't want to split on commas since the text
				// may have it - let it handle it itself
				 Ldtx ldtx = Ldtx.create(args);
				 ldtxInstructs.add(ldtx);
				 return ldtx;
				 
			case "efp":
				return EffectFunctionPointer.create(splitArgs);
		
			default:
				throw new UnsupportedOperationException("Unrecognized instruction: " + instruction);
		}
	}
	
	public void finalizeAndAddTexts(Texts texts) 
	{
		// Use segments because we know we don't need to extract anything in the
		// end segment placeholder
		for (Ldtx ldtx : ldtxInstructs)
		{
			ldtx.finalizeAndAddTexts(texts);
		}
	}
}
