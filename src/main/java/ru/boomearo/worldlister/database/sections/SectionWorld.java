package ru.boomearo.worldlister.database.sections;

public class SectionWorld {
	public final int id;
	public final String name;
	public final boolean joinIf;
	public final String access;
	public SectionWorld(int id, String Name, boolean joinIf, String access) {
		this.id = id;
		this.name = Name;
		this.joinIf = joinIf;
		this.access = access;
	}
	
}
