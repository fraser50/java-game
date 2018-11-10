package com.castrovala.fraser.orbwar.item;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.castrovala.fraser.orbwar.world.Position;

public class Inventory {
	private InventoryType type;
	private List<ItemSlot> slots = new ArrayList<>();
	private ItemSlot selected;
	private Map<Rectangle, ItemSlot> slotrect = new HashMap<>();
	private static final int padding = 1;
	
	public Inventory(InventoryType type, int slotnum) {
		this.type = type;
		
		for (int i = 0; i<slotnum; i++) {
			slots.add(new ItemSlot());
		}
		
		slotnum = 0;
		int rownum = 0;
		for (ItemSlot slot : slots) {
			slotrect.put(new Rectangle(120 + (64 * slotnum) + (padding * slotnum), 120 + (64 * rownum) + (padding * rownum), 64, 64), slot);
			slotnum++;
			if (slotnum == 5) {
				slotnum = 0;
				rownum++;
			}
		}
		
	}
	
	public InventoryType getType() {
		return type;
	}
	
	public void setType(InventoryType type) {
		this.type = type;
	}
	
	public void addSlot(ItemSlot slot) {
		slots.add(slot);
	}
	
	public List<ItemSlot> getSlots() {
		return slots;
	}
	
	public boolean swapSlots(int slot1n, int slot2n) {
		ItemSlot slot1, slot2;
		try {
			slot1 = slots.get(slot1n);
			slot2 = slots.get(slot1n);
		} catch (IndexOutOfBoundsException e) {
			return false;
		}
		
		Item a = slot1.getItem();
		Item b = slot2.getItem();
		
		if (!slot1.canFit(b) || slot2.canFit(a)) return false;
		
		slot1.setItem(b);
		slot2.setItem(a);
		
		return true;
		
	}
	
	public boolean swapSlots(ItemSlot slot1, ItemSlot slot2) {
		Item a = slot1.getItem();
		Item b = slot2.getItem();
		
		slot1.setItem(b);
		slot2.setItem(a);
		return true;
	}
	
	public void render(Graphics2D g2d) {
		int slotnum = 0;
		int rownum = 0;
		for (ItemSlot slot : slots) {
			if (selected == slot) {
				g2d.setColor(Color.GREEN);
			} else {
				g2d.setColor(Color.GRAY);
			}
			
			g2d.drawRect(120 + (65 * slotnum) + (padding * slotnum), 120 + (65 * rownum) + (padding * rownum), 65, 65);
			
			if (slot.getItem() != null) {
				BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g = (Graphics2D) img.getGraphics();
				slot.getItem().render(g);
				g.dispose();
				g2d.drawImage(img, 120 + (64 * slotnum), 120 + (64 * rownum), 64, 64, null);
				
			}
			
			slotnum++;
			if (slotnum == 5) {
				slotnum = 0;
				rownum++;
			}
		}
	}
	
	public void onClick(Position mousepos) {
		for (Rectangle rect : slotrect.keySet()) {
			if (rect.intersects(new Rectangle((int)mousepos.getX(), (int)mousepos.getY(), 1, 1))) {
				ItemSlot slot = slotrect.get(rect);
				
				if (selected == null) {
					selected = slot;
				} else {
					swapSlots(selected, slot);
					selected = null;
				}
				
				break;
			}
		}
		
	}

}
