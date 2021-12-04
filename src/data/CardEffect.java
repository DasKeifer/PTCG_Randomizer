package data;

import compiler.CodeBlock;

public abstract class CardEffect 
{
	public abstract CardEffect copy();
	public abstract void appendToCodeBlock(CodeBlock block);
	@Override
	public abstract String toString();
}
