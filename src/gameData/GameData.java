package gameData;

public interface GameData {
	void readData(byte[] rom, int startIndex);
	void writeData(byte[] rom, int startIndex);
}
