package com.castrovala.fraser.orbwar.editor;

import com.castrovala.fraser.orbwar.gameobject.GameObject;
import com.castrovala.fraser.orbwar.util.WorldProvider;

public abstract class Editor {
	private String name;
	
	public Editor(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public abstract GameObject spawn(WorldProvider controller);

}
