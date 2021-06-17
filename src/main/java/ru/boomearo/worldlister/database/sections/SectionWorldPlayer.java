package ru.boomearo.worldlister.database.sections;

public class SectionWorldPlayer {
	public int id;
	public String name;
	public String type;
	public Long timeAdd;
	public String whoAdd;
	public SectionWorldPlayer(int id, String Name, String type, Long timeAdd, String whoAdd) {
		this.id = id;
		this.name = Name;
		this.type = type;
		this.timeAdd = timeAdd;
		this.whoAdd = whoAdd;
	}
	
}
