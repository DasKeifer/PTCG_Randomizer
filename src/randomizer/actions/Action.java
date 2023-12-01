package randomizer.actions;

import java.util.function.Supplier;
import java.util.stream.Stream;

import randomizer.MonsterCardRandomizerWrapper;

public abstract class Action {
	private static int nextId = 0;
	public static final Action BLANK = new LambdaAction("", "", o -> {});
	
	private int id;
	private String name;
	private String description;
	
	protected Action(String name, String description)
	{
		this.id = nextId++;
		this.name = name;
		this.description = description;
	}
	
	abstract public void Perform(Supplier<Stream<MonsterCardRandomizerWrapper>> cards);

	public int getId()
	{
		return id;
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
