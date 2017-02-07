package com.castrovala.fraser.orbwar.save;

import org.json.simple.JSONObject;

import com.castrovala.fraser.orbwar.gameobject.GameObject;

public interface GameObjParser {
	public JSONObject toJSON(GameObject obj);
	public GameObject fromJSON(JSONObject obj);

}
