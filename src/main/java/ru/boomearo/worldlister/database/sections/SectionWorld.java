package ru.boomearo.worldlister.database.sections;

public class SectionWorld {
	public int id;
	public String name;
	public boolean joinIf;
	public String access;
	public SectionWorld(int id, String Name, boolean joinIf, String access) {
		this.id = id;
		this.name = Name;
		this.joinIf = joinIf;
		this.access = access;
	}
	
}
