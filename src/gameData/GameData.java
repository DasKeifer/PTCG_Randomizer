package gameData;

import java.util.Map;
import java.util.Set;

public interface GameData 
{
	void convertPointers(Map<Short, String> ptrToText, Set<Short> ptrsUsed);
	int readData(byte[] data, int startIndex);
	int writeData(byte[] data, int startIndex);
}
