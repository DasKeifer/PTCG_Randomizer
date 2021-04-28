package compiler.dynamic;

public interface DynamicCompiler
{
	public int getSizeOnBank(byte bank);
	public int writeBytesForBank(byte[] bytes, int indexToWriteAt, byte bank);
}