package ru.boomearo.worldlister.database.sections;

import ru.boomearo.worldlister.objects.WorldAccessType;

public class SectionWorld {

	private final int id;
	private final String name;
	private final boolean joinIf;
	private final WorldAccessType access;

	public SectionWorld(int id, String Name, boolean joinIf, WorldAccessType access) {
		this.id = id;
		this.name = Name;
		this.joinIf = joinIf;
		this.access = access;
	}

	public int getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public boolean isJoinIf() {
		return this.joinIf;
	}

	public WorldAccessType getAccess() {
		return this.access;
	}
}
