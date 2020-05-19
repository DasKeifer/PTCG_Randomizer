package gameData;

import util.ByteUtils;

public class NonPokemonCard extends Card
{
	public static final int TOTAL_SIZE_IN_BYTES = 14;
	public static final int SIZE_OF_PAYLOAD_IN_BYTES = TOTAL_SIZE_IN_BYTES - CARD_COMMON_SIZE;

	// Internal pointers used when reading and storing data to the rom
	private short descriptionPtr;
	private short descriptionExtendedPtr;
	
	short effectPtr;
	String desciption;

	@Override
	public int getCardSizeInBytes() 
	{
		return TOTAL_SIZE_IN_BYTES;
	}
	
	@Override
	public int readData(byte[] cardBytes, int startIndex) 
	{
		int index = readCommonData(cardBytes, startIndex);
		
		// reading non pokemon specific data
		effectPtr = ByteUtils.readAsShort(cardBytes, index);
		index += 2;
		descriptionPtr = ByteUtils.readAsShort(cardBytes, index);
		index += 2;
		descriptionExtendedPtr = ByteUtils.readAsShort(cardBytes, index);
		return index + 2;
	}
	
	@Override
	public int writeData(byte[] cardBytes, int startIndex) 
	{
		int index = writeCommonData(cardBytes, startIndex);
		
		// write non pokemon specific data
		ByteUtils.writeAsShort(effectPtr, cardBytes, index);
		index += 2;
		ByteUtils.writeAsShort(descriptionPtr, cardBytes, index);
		index += 2;
		ByteUtils.writeAsShort(descriptionExtendedPtr, cardBytes, index);		
		return index + 2;
	}
}
