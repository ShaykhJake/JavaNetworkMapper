package networkMapper;

/**
 * Object to represent each port on a host
 * @author Jake @ PidginForge
 *
 */
public class Port {
	/**
	 * Port number
	 */
	int port;
	/**
	 * Whether or not the port is open/active
	 */
	boolean isOpen;
	
	/**
	 * Constructor
	 * @param port
	 * @param isOpen
	 */
	public Port(int port, boolean isOpen) {
		this.port = port;
		this.isOpen = isOpen;
	}
}
