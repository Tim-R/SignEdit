package net.timroden.signedit;

public enum SignFunction {
	EDIT(0),
	COPY(1),
	PASTE(2);
	
	private int type;
	private SignFunction(int i) {
		this.type = i;
	}
	
	public int getType() {
		return type;
	}
}
