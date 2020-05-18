package gameData;

import util.ByteUtils;

public class NonPokemonCard extends Card
{
	public static final int TOTAL_SIZE_IN_BYTES = 14;
	public static final int SIZE_OF_PAYLOAD_IN_BYTES = TOTAL_SIZE_IN_BYTES - CARD_COMMON_SIZE;
	
	short effect;
	short description;
	short descriptionCont;

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
		effect = ByteUtils.readAsShort(cardBytes, index);
		index += 2;
		description = ByteUtils.readAsShort(cardBytes, index);
		index += 2;
		descriptionCont = ByteUtils.readAsShort(cardBytes, index);
		return index + 2;
	}
	
	@Override
	public int writeData(byte[] cardBytes, int startIndex) 
	{
		int index = writeCommonData(cardBytes, startIndex);
		
		// write non pokemon specific data
		ByteUtils.writeAsShort(effect, cardBytes, index);
		index += 2;
		ByteUtils.writeAsShort(description, cardBytes, index);
		index += 2;
		ByteUtils.writeAsShort(descriptionCont, cardBytes, index);		
		return index + 2;
	}
}
