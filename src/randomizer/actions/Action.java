package randomizer.actions;

import java.util.function.Supplier;
import java.util.stream.Stream;

import randomizer.MonsterCardRandomizerWrapper;

public abstract class Action {
	private static int nextId = 0;
	
	private int id;
	private String category;
	private String subcategory;
	private String name;
	private String description;
	
	protected Action(String category, String name, String description)
	{
		this.id = nextId++;
		this.category = category;
		this.subcategory = "";
		this.name = name;
		this.description = description;
	}
	
	abstract public void Perform(Supplier<Stream<MonsterCardRandomizerWrapper>> cards);

	public int getId()
	{
		return id;
	}

	public String getCategory()
	{
		return category;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getDescription()
	{
		return description;
	}
}
