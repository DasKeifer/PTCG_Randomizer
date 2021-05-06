package compiler.dynamic;

import compiler.fixed.FixedInstruction;

// TODO: Have this separate? Depends on how we do things

public class Ldtx extends FixedInstruction
{
	// TODO Does this belong here or in dynamic?
	
	String textName;
	
	public Ldtx(String textName)
	{
		this.textName = textName;
	}
}
