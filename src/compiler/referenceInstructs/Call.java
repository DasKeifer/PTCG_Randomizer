package compiler.referenceInstructs;


import java.util.Map;

import compiler.Segment;
import compiler.CompilerConstants.InstructionConditions;
import rom.Texts;

public class Call extends JumpCallCommon
{
	private static final byte CONDITIONLESS_INSTRUCT = (byte) 0xCD;
	private static final byte CONDITIONED_INSTRUCT = (byte) 0xC4;
	private static final byte FAR_INSTRUCT_RST_VAL = 0x28;
	
	public Call(String labelToGoTo, InstructionConditions conditions) 
	{
		super(labelToGoTo, conditions, CONDITIONLESS_INSTRUCT, CONDITIONED_INSTRUCT, FAR_INSTRUCT_RST_VAL);
	}
	
	public Call(int addressToGoTo, InstructionConditions conditions) 
	{
		super(addressToGoTo, conditions, CONDITIONLESS_INSTRUCT, CONDITIONED_INSTRUCT, FAR_INSTRUCT_RST_VAL);
	}

	@Override
	public void linkData(
			Texts romTexts,
			Map<String, Segment> labelToLocalSegment, 
			Map<String, Segment> labelToSegment
	) 
	{		
		if (!labelToGoTo.isEmpty() && toGoTo == null)
		{
			// No need to check for local ones - they are treated the same
			toGoTo = labelToSegment.get(labelToGoTo);
			if (toGoTo == null)
			{
				throw new IllegalArgumentException("Specified label '" + labelToGoTo + "' was not found!");
			}
		}
	}
}
