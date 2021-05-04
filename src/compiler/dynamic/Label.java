package compiler.dynamic;

public class Label 
{
	private String label;
	
	// Functions are label:
	// internal to functions are .label
	// TODO: Remove functions/labels and change to segments/blocks? Go from label to label automagically?
	// THis is probably more generic and works for things like the cards
	// We could get super fancy and allow the blocks to be split up by sub-blocks or force them to be written contiguously
	public Label(String label)
	{
		this.label = label;
	}
	
	public String getLabel()
	{
		return label;
	}
}
