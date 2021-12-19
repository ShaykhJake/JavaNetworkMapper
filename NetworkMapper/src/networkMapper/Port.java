package networkMapper;

public class Port {
	int port;
	boolean isOpen;
	String service;
	
	public Port(int port, boolean isOpen) {
		this.port = port;
		this.isOpen = isOpen;
	}
}
