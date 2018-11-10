package com.castrovala.fraser.orbwar.item;

import java.awt.Graphics2D;

public abstract class Item {
	private int amount;
	
	public Item(int amount) {
		this.setAmount(amount);
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}
	
	public abstract int getMaxItemCount();
	public abstract String getName();
	public abstract void render(Graphics2D g2d); // Assume 64x64 pixels
	

}
