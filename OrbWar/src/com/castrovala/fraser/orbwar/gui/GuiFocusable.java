package com.castrovala.fraser.orbwar.gui;

import java.awt.event.KeyEvent;

public interface GuiFocusable {
	public void onKeyPress(KeyEvent key);
	public void onCharEnter(char c);
	public void setFocus(boolean focus);

}
