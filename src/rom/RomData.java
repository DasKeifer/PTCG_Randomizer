package rom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gameData.Card;

public class RomData
{
	// Make package so we don't change it unintentionally
	byte[] rawBytes;
	
	// Make public - we will be modifying these
	public Map<String, List<Card>> cardsByName = new HashMap<>();
	public Map<Short, String> ptrToText = new HashMap<>();
}
