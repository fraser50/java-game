package com.castrovala.fraser.orbwar.item;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import com.castrovala.fraser.orbwar.gameobject.PlayerShip;

public class ItemBattery extends Item {
	public ItemBattery(int amount) {
		super(amount);
	}

	private static BufferedImage renderimg;

	@Override
	public int getMaxItemCount() {
		return 100;
	}

	@Override
	public String getName() {
		return "Battery";
	}

	@Override
	public void render(Graphics2D g2d) {
		g2d.drawImage(renderimg, 0, 0, 64, 64, null);
		
	}
	
	public static void loadResources() {
		ClassLoader cl = PlayerShip.class.getClassLoader();
		try {
			renderimg = ImageIO.read(cl.getResourceAsStream("resources/items/batteryItem.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void registerItem() {
		ItemParser parser = new ItemParser() {
			
			@Override
			public byte[] toBytes(Item item) {
				ByteBuffer buf = ByteBuffer.allocate(4);
				buf.putInt(item.getAmount());
				return buf.array();
			}
			
			@Override
			public Item fromBytes(ByteBuffer buf) {
				return new ItemBattery(buf.getInt());
			}
		};
		
		ItemProcessor.addParser(parser, ItemBattery.class);
	}

}
