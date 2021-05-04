package compiler.dynamic;

import compiler.fixed.Instruction;

// TODO: lots more other load functions...

public class Ldtx extends Instruction
{
	// TODO Does this belong here or in dynamic?
	
	String textName;
	
	public Ldtx(String textName)
	{
		this.textName = textName;
	}
}
