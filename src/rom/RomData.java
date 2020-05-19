package rom;

import java.util.ArrayList;
import java.util.List;

import gameData.Card;

public class RomData
{
	// Make package so we don't change it unintentionally
	byte[] rawBytes;
	
	// Make public - we will be modifying these
	public Card[] cards;
	public List<String> text = new ArrayList<>();
}
