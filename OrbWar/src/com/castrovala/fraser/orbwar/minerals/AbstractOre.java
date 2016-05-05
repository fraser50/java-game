package com.castrovala.fraser.orbwar.minerals;

public abstract class AbstractOre {
	private int amount;
	
	public AbstractOre(int amount) {
		this.setAmount(amount);
	}
	
	public abstract String getName();

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

}
