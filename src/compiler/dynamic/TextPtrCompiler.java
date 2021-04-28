package compiler.dynamic;

import constants.RomConstants;
import util.ByteUtils;

public class TextPtrCompiler implements DynamicCompiler
{
	short textId;

	public TextPtrCompiler(short textId)
	{
		this.textId = textId;
	}

	@Override
	public int getSizeOnBank(byte bank)
	{
		return RomConstants.TEXT_POINTER_SIZE_IN_BYTES;
	}

	@Override
	public int writeBytesForBank(byte[] bytes, int indexToWriteAt, byte bank)
	{
		ByteUtils.writeAsShort(textId, bytes, indexToWriteAt);
		return indexToWriteAt + RomConstants.TEXT_POINTER_SIZE_IN_BYTES;
	}
}
