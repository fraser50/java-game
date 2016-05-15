package com.castrovala.fraser.orbwar.util;

import java.util.HashMap;

import net.minidev.json.JSONObject;

public class MaterialStore {
	private HashMap<String, Integer> materials = new HashMap<>();

	public HashMap<String, Integer> getMaterials() {
		return materials;
	}
	
	public void setMaterials(HashMap<String , Integer> materials) {
		this.materials = materials;
	}
	
	public void addMaterial(String material, int amount) {
		if (amount <= 0) {
			return;
		}
		
		if (materials.containsKey(material)) {
			materials.put(material, materials.get(material) + amount);
		} else {
			materials.put(material, amount);
		}
		
	}
	
	public void removeMaterial(String material, int amount) {
		if (!materials.containsKey(material)) {
			return;
		}
		
		if (materials.get(material) - amount <= 0) {
			materials.remove(material);
		} else {
			materials.put(material, materials.get(material) + amount);
		}
		
	}
	
	public JSONObject toJSON() {
		return new JSONObject(materials);
	}

}
