package rom;


public class RomData
{
	// Make package so we don't change it unintentionally
	byte[] rawBytes;
	
	// Make public - we will be modifying these
	public Cards cardsByName = new Cards();
	public Texts idsToText = new Texts();
}
