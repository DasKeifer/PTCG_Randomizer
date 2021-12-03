package data;

import java.util.LinkedList;
import java.util.List;

import bps_writer.BpsInstructionParser;
import compiler.Instruction;
import data.custom_card_effects.EffectFunctionPointerInstruct;
import data.romtexts.LdtxInstruct;
import rom.Texts;

public class PtcgInstructionParser extends BpsInstructionParser
{
	private List<LdtxInstruct> ldtxInstructs;
	
	public PtcgInstructionParser() 
	{
		ldtxInstructs = new LinkedList<>();
	}
	
	@Override
	protected Instruction parseInstructionImpl(String[] keyArgs, String[] args, String rootSegment)
	{
		switch (keyArgs[0])
		{
			case "ldtx":
				// we don't want to split on commas since the text
				// may have it - let it handle it itself
				 LdtxInstruct ldtx = LdtxInstruct.create(keyArgs[1]);
				 ldtxInstructs.add(ldtx);
				 return ldtx;
				 
			case "efp":
				return EffectFunctionPointerInstruct.create(args);
		
			default:
				return super.parseInstructionImpl(keyArgs, args, rootSegment);
		}
	}
	
	public void finalizeAndAddTexts(Texts texts) 
	{
		// Use segments because we know we don't need to extract anything in the
		// end segment placeholder
		for (LdtxInstruct ldtx : ldtxInstructs)
		{
			ldtx.finalizeAndAddTexts(texts);
		}
	}
}
