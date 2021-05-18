package compiler;

public abstract class SegmentReference 
{
	protected int assignedAddress;
	
	public SegmentReference()
	{
		assignedAddress = CompilerUtils.UNASSIGNED_ADDRESS;
	}
	
	public int getAssignedAddress() 
	{
		return assignedAddress;
	}
}
