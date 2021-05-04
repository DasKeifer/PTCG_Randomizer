package compiler.dynamic;

public class LocalJump
{
	// Could be a JR or a JP based on distance from where this is written
	// Maybe only worry about JRs inside a snippet. That may make life easier
	// Or else we may need to do an addition rejiggering once we know the
	// "semi final" addresses of blocks
	
	String jumpTo;
	
	public LocalJump(String label)
	{
		
	}
	
	public LocalJump(int globalAddress)
	{
		
	}
}
