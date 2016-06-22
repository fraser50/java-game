package com.castrovala.fraser.orbwar.util;

public class RenderDebug {
	private int rendereditems;
	private long start;
	private long end;
	private Position renderloc;
	private boolean editor;
	
	public RenderDebug(int rendereditems, long start, long end, Position renderloc) {
		this.rendereditems = rendereditems;
		this.start = start;
		this.end = end;
		this.setRenderloc(renderloc);
	}
	
	public RenderDebug(Position renderloc) {
		this(0, System.currentTimeMillis(), 0l, renderloc);
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

}
