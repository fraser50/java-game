package com.castrovala.fraser.orbwar.save;

import net.minidev.json.JSONObject;

import com.castrovala.fraser.orbwar.gameobject.GameObject;

public interface GameObjParser {
	public JSONObject toJSON(GameObject obj);
	public GameObject fromJSON(JSONObject obj);

}
