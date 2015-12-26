package com.castrovala.fraser.orbwar.gui;

import java.util.ArrayList;
import java.util.List;

public class GuiScreen {
	private List<GuiElement> elements = new ArrayList<>();
	
	public GuiScreen() {}
	
	public List<GuiElement> getElements() {
		return elements;
	}
	
	public void addElement(GuiElement e) {
		elements.add(e);
	}

}
