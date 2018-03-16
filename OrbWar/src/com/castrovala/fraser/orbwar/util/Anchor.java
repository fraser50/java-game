package com.castrovala.fraser.orbwar.util;

import com.castrovala.fraser.orbwar.world.Position;

public class Anchor {
	private Position objcentre;
	private double initialangle;
	private double magnitude;
	
	public Anchor(Position pos, Position objcentre) {
		this.objcentre = objcentre;
		
		initialangle = Util.targetRadius(pos, objcentre);
		magnitude = pos.distance(objcentre);
		
	}
	
	public Position getAnchorPosition(double rotation) {
		Position apos = objcentre.copy();
		apos.add(Util.angleToVel(rotation + initialangle, (float)magnitude));
		return apos;
	}

}
