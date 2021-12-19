package networkMapper;

import java.net.InetAddress;
import java.util.ArrayList;

public class Network {
	/**
	 * Address of the network
	 */
	String networkAddress;
	/**
	 * Subnet Mask for the Network
	 */
	String subnetMask;
	/**
	 * Broadcast address for the network
	 */
	String broadcastAddress;
	/**
	 * CIDR block for the subnet
	 */
	int CIDR;
	/**
	 * Maximum number of hosts that the given subnet can have
	 */
	int maxHosts;
	/**
	 * An ArrayList of all possible host addresses
	 */
    ArrayList<String> possibleHosts;
    /**
     * An ArrayList of those hosts which were active when scanned
     */
    ArrayList<Host> activeHosts;
	
	/**
	 * Constructor
	 * @param address
	 * @param CIDR
	 */
	public Network(String address, int CIDR){
		this.networkAddress = address;
		this.CIDR = CIDR;
		// Calculate the maximum hosts
		maxHosts = calculateMaxHosts(CIDR);
		// Calculate the subnet mask
		subnetMask = calculateSubnetMask();
		
		// Display initial details
		printDetails();
	}
	
	/**
	 * Prints the network's details;
	 */
	public void printDetails() {
		System.out.println("**************************");
		System.out.println("* Target Network Details *");
		System.out.println("**************************");
		System.out.println("Network: " + this.networkAddress + "/" + CIDR);
		System.out.println("Max network hosts: " + maxHosts);
		System.out.println("Subnet mask: " + calculateSubnetMask());
		System.out.println("**************************");
	}
	

	/*
	 * Generates a list of all possible host address for the subnet
	 */
	public void generatePossibleHosts() {
		// initialize arraylist
		possibleHosts = new ArrayList<>();
		// convert network address to a long decimal to more easily manipulate
		// 255.255.255.255 maxes out the bit size of integer, which is a signed number type
		long longAddress = stringToLongIP(networkAddress);
		// iterate over max hosts and generate IP addresses
		for(int i = 0; i < maxHosts; i++) {
			// be sure to add a one to the iterator for the actual host cardinal
			int host = i + 1;
			// Add the host cardinal to the network IP
			String hostAddress = longToStringIP(longAddress + host);
			// add IP address to the list of possible hosts
			possibleHosts.add(hostAddress);
		}
		// print results
	}

	/**
	 * Build a hashmap of active hosts
	 */
	public void buildHosts() {
		// List out all possible hosts
		generatePossibleHosts();
		// Initial Status message
		System.out.println("\nPinging " + possibleHosts.size() + " addresses to find active hosts...");
		
		// Initialize activeHosts arraylist
		activeHosts = new ArrayList<>();
		
		// Ensure possibleHosts has elements
		if(possibleHosts != null) {
			// Initialize a new array of threads with the size of the total possible hosts
			Thread[] threads = new Thread[possibleHosts.size()];
			// Iterate over the threads, assigning methods to them, and starting them
			for(int i=0; i < threads.length; i++) {
				// Assign threadedPing method to the current thread for the given address
				threads[i] = new Thread(new ThreadedPing(possibleHosts.get(i), this));
				// Start the thread
				threads[i].start();
			}
			// Iterate over the threads to start shutting them down
			for(int i=0; i < threads.length; i++) {
				// Try to join the thread
				try {
					threads[i].join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		} else {
			// If possibleHosts is null, inform the user to generate possible hosts
			System.out.println("You must generate a list of possible hosts first.");
		}
	}
	
	/**
	 * A class extending Thread to handle pings
	 * 
	 */
	public class ThreadedPing extends Thread {
		/**
		 * Host address
		 */
		String address;
		/**
		 * Network address
		 */
		Network network;
		/**
		 * Constructor
		 * @param address
		 * @param network
		 */
		public ThreadedPing(String address, Network network) {
			this.address = address;
			this.network = network;
		}
		/**
		 * Synchronized run method
		 */
		synchronized public void run() {
			try {
				// Create a new Host object
				Host host = new Host(address);
				// Try to ping the host
				if(pingHost(host)) {
					// Add to the active hosts lists if active
					network.activeHosts.add(host);
				} 
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
		}
	}
	
	/**
	 * Pings the address of a given host
	 * @param host
	 * @return
	 */
	public boolean pingHost(Host host){
		// Create a new inet object;
		InetAddress inet;
		try {
			// Initialize inet object to given host address
			inet = InetAddress.getByName(host.address);
			// Ping using given timeout (ms)
			
			long startTime = System.nanoTime();
			if (inet.isReachable(50)) {
				long stopTime = System.nanoTime();
				double durationNS = stopTime - startTime;
				double durationMS = durationNS/1000000.0;
				host.setPing(durationMS);
				// if reachable, add the returned host name
				host.name = inet.getHostName();
				// return true
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace(System.out);
			return false;
		}
	}
	
	/**
	 * Scans the ports of given target ports array
	 * @param targetPorts
	 */
	public void scanPorts(int[] targetPorts) {
		// Iterate through ports; scanning each port, one at a time, concurrently on all networks
		System.out.println("\nScanning " + targetPorts.length + " ports on each active host...");
		// Ensure ports array has elements
		if(targetPorts != null) {
			// interate through array
			for(int port : targetPorts) {
				// generate array of Threads using size of activeHosts
				Thread[] threads = new Thread[activeHosts.size()];
				// Iterate over thread array
				for(int i=0; i < threads.length; i++) {
					// Create new thread for a threaded scan of the host's port
					threads[i] = new Thread(new ThreadedScan(activeHosts.get(i), port));
					// Start the thread
					threads[i].start();
				}
				for(int i=0; i < threads.length; i++) {
					// Join the thread
					try {
						threads[i].join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}	
			}
		}
	}
	
	/**
	 * scans the given port for the given host, extending Thread
	 *
	 */
	public class ThreadedScan extends Thread {
		/**
		 * host object
		 */
		Host host;
		/**
		 * target port
		 */
		int port;
		/**
		 * constructor
		 * @param host
		 * @param port
		 */
		public ThreadedScan(Host host, int port) {
			this.host = host;
			this.port = port;
		}
		
		/**
		 * Run method
		 */
		synchronized public void run() {
			try {
				// scans given port
				if(host.scanPort(port)) {
					// adds to active port list if active
					host.activePorts.add(port);
				} else {
					
				}
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
		}
	}
	

	/**
	 * Calculates the maximum number of hosts
	 * @param CIDR
	 * @return integer
	 */
	public int calculateMaxHosts(int CIDR) {
		// Using the CIDR block length, calculates total hosts possible
		// subtracting 2 for the network address and broadcast
		return ((int) Math.pow(2, (32 - CIDR)) - 2);
	}
	
	/**
	 * Calculates the subnet mask string for the given network
	 * @return String
	 */
    public String calculateSubnetMask() {
    	long mask = (((long) Math.pow(2, CIDR) - 1) << (32 - CIDR)) ;
    	return (longToStringIP(mask));
    }
    /**
     * Converts a string 000.000.000.000 IP to a long number
     * @param IP as a string
     * @return IP as a long
     */
    
	public long stringToLongIP(String IP) {
		// Split the string by . into octects
		String[] octets = IP.split("\\.");
		// initialize a long for address
		long address = 0;
		// iterate over octets
		for(int i = 0; i < octets.length; i++) {
			// bitwise shift based on octet's place
			// EXPLAIN this
			address += (Long.parseLong(octets[i]) << (8 * (3 - i)));
		}
		return address;
	}
	
	/**
	 * Converts long integer into a string IP address
	 * @param longIP
	 * @return
	 */
    public String longToStringIP(long longIP) {
    	// initialize array for octet strings
		String[] octets = new String[4];
		// Loop through each octect
		for(int i = 3; i > -1; i--) {
			// use bitwise AND to get final 8 bits
		    long octet =  255L & longIP;
		    // Add octet string to the array
		    octets[i] = String.valueOf(octet);
		    // shift 8 bits to the right
		    longIP = longIP >> 8;
		}
		return String.join(".", octets);
	}

	
}
