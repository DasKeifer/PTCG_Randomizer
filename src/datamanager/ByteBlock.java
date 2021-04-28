package datamanager;

public class ByteBlock 
{
	// TODO:
	// Make blocks independent at high level - dependencies determined by the code snippets - if there
	// if a function placeholder/reference, then its a dependency. Perhaps have typed of dependencies in
	// these placeholders/references
	// Then have only one type of block with preferred and excluded addresses. Required can be done by
	// excluding under the hood. These blocks then have a full and a minimal + additional function version.
	// Where the additional is a separate block potentially with a dependency type
	// The allocator/manager then goes through and tries to do the full version but if it doesn't fit, then
	// tries to do the minimal + additional function. second is optional
	// Still have replaces separate since we need to somewhat bypass the allocator for those and want to check
	// what we are replacing
	// How do we do required starting positions too?
	
	
	ByteBlock(byte reqBank)
	{
		requiredBank = reqBank;
		requiredStartAddress = NO_REQ_ADDRESS;
	}
	
	ByteBlock(byte reqBank, short reqStartAddress)
	{
		requiredBank = reqBank;
		requiredStartAddress = reqStartAddress;
	}
	
	// A required byte block could support all 3 of these
	// Applicable for subset of specific location overwrites and for
	// things like the text pointers
	public void addFullInPlaceData()
	{
		// Text pointers, card pointers, effect commands
	}
	
	// TODO: easiest if we keep this distinction. That way we can write over more or less of the
	// data as needed
	// either do a JR/JP/Call (3) or a JR + farcall + ret (4 or 7)
	
	// Only applicable for overwriting a specific location?
	public void addMinimalInPlacePlusLocalData()
	{
		// Modding code (multibank effect functions, more booster packs, etc.)
		
		// Overwrite a section of code using JP/Call
	}
	
	// Applicable for in place writing or block specific writing (like effect function)
	public void addMinimalInPlacePlusRemoteData()
	{
		// Same as local. Perhaps we automate this and keep them as the same?
		
		// Overwrite a section of code using Farcall
	}
	
	// Full in place
	// in place + local calls
	// in place + remote calls
}
