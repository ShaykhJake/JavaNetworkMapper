package networkMapper;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.ArrayList;


public class Host {
	/**
	 * The Host's IP address
	 */
	String address;
	
	/**
	 * 
	 */
	private double ping; // in ms
	String name;
	ArrayList<Integer> activePorts;
	boolean isActive;
	

	/**
	 * Constructor
	 * @param address
	 */
	public Host(String address) {
		this.address = address;
		this.activePorts = new ArrayList<>();
	}
	
	/**
	 * Scans the given port on the host to determine if it is open
	 * @param port
	 * @return true if open
	 */
	public boolean scanPort(int port){
		try {
			// Initialize a new Socket
			Socket socket = new Socket();
			// Attempt to connect to the socket with given address & port
			socket.connect(new InetSocketAddress(address, port), 100);
			// Close the socket
			socket.close();
			// Return true
			return true;
		} catch (Exception e) {
			// if the host port cannot be connected to, return false
//			e.printStackTrace(System.out);
			return false;
		}

	}
	/**
	 * Sets the ping time in ms
	 * @param ping
	 */
	public void setPing(double ping) {
		this.ping = ping;
	}
	/**
	 * Gets the formatted string value of the ping time
	 * @return formatted string
	 */
	public String getPingString() {
		DecimalFormat df = new DecimalFormat("###.##");
		return df.format(ping) + "ms";
	}
}
