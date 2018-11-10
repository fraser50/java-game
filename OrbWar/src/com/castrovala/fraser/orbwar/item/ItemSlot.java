package com.castrovala.fraser.orbwar.item;

public class ItemSlot {
	private Item item;
	private Class<? extends Item> constraint;
	
	public ItemSlot(Class<? extends Item> constraint) {
		this.constraint = constraint;
	}
	
	public ItemSlot() {
		this(Item.class);
	}

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}
	
	public boolean canFit(Item i) {
		return constraint.isInstance(i);
	}
	
	public Class<? extends Item> getConstraint() {
		return constraint;
	}

}
