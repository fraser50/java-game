package com.castrovala.fraser.orbwar.util;

import java.util.List;

import com.castrovala.fraser.orbwar.world.Position;

public class RenderDebug {
	private int rendereditems;
	private long start;
	private long end;
	private Position renderloc;
	private boolean editor;
	private boolean isTouching;
	private Position mousepos;
	private List<LightCircle> lightsources;
	
	public RenderDebug(int rendereditems, long start, long end, Position renderloc, List<LightCircle> lightsources) {
		this.rendereditems = rendereditems;
		this.start = start;
		this.end = end;
		this.setRenderloc(renderloc);
		this.lightsources = lightsources;
		
	}
	
	public RenderDebug(Position renderloc, List<LightCircle> lightsources) {
		this(0, System.currentTimeMillis(), 0l, renderloc, lightsources);
	}
	
	public void onRender(int amount) {
		rendereditems += amount;
	}
	
	public void onRender() {
		onRender(1);
	}

	public int getRendereditems() {
		return rendereditems;
	}

	public void setRendereditems(int rendereditems) {
		this.rendereditems = rendereditems;
	}

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public long getEnd() {
		return end;
	}

	public void setEnd(long end) {
		this.end = end;
	}
	
	public long getTime() {
		return end - start;
	}

	public Position getRenderloc() {
		return renderloc;
	}

	public void setRenderloc(Position renderloc) {
		this.renderloc = renderloc;
	}

	public boolean isEditor() {
		return editor;
	}

	public void setEditor(boolean editor) {
		this.editor = editor;
	}

	public boolean isTouching() {
		return isTouching;
	}

	public void setTouching(boolean isTouching) {
		this.isTouching = isTouching;
	}

	public Position getMousepos() {
		return mousepos;
	}

	public void setMousepos(Position mousepos) {
		this.mousepos = mousepos;
	}
	
	public void addLight(LightCircle c) {
		lightsources.add(c);
	}

}
