package compiler;


import java.util.Map;

import rom.Texts;

public abstract class StaticInstruction extends FixedLengthInstruct
{
	protected StaticInstruction(int size) 
	{
		super(size);
	}
	
	@Override
	public void extractTexts(Texts texts)
	{
		// Nothing to do here - these are fully static
	}
	
	@Override
	public void linkData(
			Texts romTexts,
			Map<String, Segment> labelToLocalSegment, 
			Map<String, Segment> labelToSegment
	) 
	{
		// Nothing to do here - these are fully static
	}
}
