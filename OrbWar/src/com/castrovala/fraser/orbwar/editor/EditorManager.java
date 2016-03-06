package com.castrovala.fraser.orbwar.editor;

import java.util.ArrayList;
import java.util.List;

public class EditorManager {
	private static List<Editor> editors = new ArrayList<>();
	
	public static void addEditor(Editor e) {
		EditorManager.editors.add(e);
	}

	public static List<Editor> getEditors() {
		return editors;
	}

}
