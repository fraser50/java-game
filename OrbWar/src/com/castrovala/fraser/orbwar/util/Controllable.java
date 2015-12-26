package com.castrovala.fraser.orbwar.util;

import com.castrovala.fraser.orbwar.server.ControlUser;

public interface Controllable {
	
	public void left();
	public void right();
	public void fly();
	public void fire();
	public void toggleBrake();
	public void suicide();
	public void shield();
	public ControlUser getControl();
	public void setControl(ControlUser user);

}
