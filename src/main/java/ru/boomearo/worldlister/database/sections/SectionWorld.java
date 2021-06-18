package ru.boomearo.worldlister.database.sections;

import ru.boomearo.worldlister.objects.WorldAccessType;

public class SectionWorld {

	public final int id;
	public final String name;
	public final boolean joinIf;
	public final WorldAccessType access;

	public SectionWorld(int id, String Name, boolean joinIf, WorldAccessType access) {
		this.id = id;
		this.name = Name;
		this.joinIf = joinIf;
		this.access = access;
	}
	
}
