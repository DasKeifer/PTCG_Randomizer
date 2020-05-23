package rom;

import java.util.HashMap;
import java.util.Map;

import gameData.CardVersions;

public class RomData
{
	// Make package so we don't change it unintentionally
	byte[] rawBytes;
	
	// Make public - we will be modifying these
	public Map<String, CardVersions> cardsByName = new HashMap<>();
	public IdsToText idsToText = new IdsToText();
}
