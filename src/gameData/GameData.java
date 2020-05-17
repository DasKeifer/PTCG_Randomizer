package gameData;

public interface GameData {
	int readData(byte[] data, int startIndex);
	int writeData(byte[] data, int startIndex);
}
