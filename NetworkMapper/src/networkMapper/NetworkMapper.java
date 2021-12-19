package networkMapper;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Simple program to map a network
 * @author Jake @ PidginForge
 *
 */
public class NetworkMapper {
	// Create a scanner instance for user input
	static Scanner sc = new Scanner(System.in);
	static int[] targetPorts;
	static double duration;
	
	
	public static void main(String[] args) {		
		// Welcome User
		System.out.println("Welcome to the Network Mapper!");
		System.out.println("*********************");
		
		// Ask User for Network address in CIDR Notation
		System.out.println("Please enter the network address in CIDR notation (e.g. 192.168.1.0/24):");
		String addressInput = sc.nextLine();
		
		// Split the address into its parts
		String[] addressParts = addressInput.split("/");
		String address = addressParts[0];
		// Convert the CIDR block to int and store
		int CIDR = Integer.parseInt(addressParts[1]);
		
		// Status message
		System.out.println("Initializing network " + address + "/" + CIDR + "...");
		
		// Initialize new Network object
		Network target = new Network(address, CIDR);
		
		
		// Get target ports (or default):
		System.out.println("Please enter the ports you wish to scan as a comma-separated list (e.g. 1, 2, 3...) or just hit 'enter' for defaults:");
		String portInput = sc.nextLine();
		
		if(portInput.strip().length()<1) {
			// default value:
			targetPorts = new int[] {21, 22, 23, 25, 53, 80, 110, 135, 137, 138, 139, 443, 1433, 1434};
		} else {
			// split input into a string array
			String[] inputPorts = portInput.split(",");
			// initialize the targetPorts array to the length of inputPorts
			targetPorts = new int[inputPorts.length];
			// iterate over inputPorts array
			for(int i = 0; i < inputPorts.length; i++) {
				// strip each string and cast to integer, assign to targetPorts
				targetPorts[i] = Integer.parseInt(inputPorts[i].strip());
			}
		}
		
		// Close the scanner
		sc.close();

		// BEGIN OPERATIONS

		// Create a daemon thread to show a simple progress bar
		// Initialize a DisplayProgress runnable object using "#" as the progress character
		Runnable displayProg = new DisplayProgress('#', 25);
		// Initialize a new thread
		Thread progThread = new Thread(displayProg);
		// Set the thread to Daemon
		progThread.setDaemon(true);
		// Set the thread to lowest priority
		progThread.setPriority(Thread.MIN_PRIORITY);
		// Start thread
		progThread.start();
		
		
		// Get starting system time to measure total duration
		long startTime = System.currentTimeMillis();
		// Build and ping the list of hosts
		target.buildHosts();		
		// Scan the ports
		target.scanPorts(targetPorts);
		
		// Interrupt the progress bar thread 
		progThread.interrupt();
	
		// Get ending system time
		long endTime = System.currentTimeMillis();
		// Reduce millis to seconds & calculated duration
		duration = ((endTime - startTime) / 1000);  //divide by 1000000 to get milliseconds.
		
		
		// Puts the main thread to sleep long enough to ensure other threads have been terminated
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Print final message
		System.out.println("\n*****************************************");
		System.out.println("Scan completed in " + duration + " seconds.");
		
		// Output results to file
		System.out.println("*****************************************");
		writeToFile(target);
		
		// Print final message & exit
		System.out.println("Have a nice day!!");
		System.exit(0);
		
	}

	/**
	 * A progress bar class that runs as a separate thread
	 * @author Jake @ PidginForge
	 *
	 */
	static class DisplayProgress implements Runnable {
		/**
		 * The character to represent the display bar
		 */
		private char displayChar;
		/**
		 * Sleep time between characters in ms
		 */
		private int sleep;
		/**
		 * Constructor
		 * @param displayChar
		 */
		public DisplayProgress(char displayChar, int sleep) {
			this.displayChar = displayChar;
			this.sleep = sleep;
		}
		/**
		 * Run method for the thread
		 */
		public void run() {
			// run until interrupted
			while(true)
			{
				try {
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
					// Upon interruption, return (i.e. commit thread suicide)
					return;
				}
				System.out.print(displayChar);
			}
		}
	}
	
	/**
	 * Method to write the results of the mapping to a file
	 * @param target network object
	 */
	static void writeToFile(Network target) {
		// Get the current date
		Date dateTime = new Date();
		// Format the date
		String dateTimeString = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(dateTime);
		// Initialize a file name based on date and time
		String fileName = "Scan_" + dateTimeString + ".txt";
		// Initialized a new file object
		File file = new File (fileName);
		// Create a new filewriter
		FileWriter fw;
		try {
			// Try to open the file for writing
			fw = new FileWriter(file);
			// Initialize a buffered writer
			BufferedWriter bw = new BufferedWriter(fw);
			// Initialize a print writer
			PrintWriter out = new PrintWriter(bw, true); // true is for flush
			// Print to file, calling the printResults method to get the text
			out.println(printResults(target, dateTime));
			// Close the PrintWriter, BufferedWriter, & FileWriter
			out.close();
			bw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Status message
		System.out.println("Results have been written to " + fileName);

	}
	/**
	 * Generates a detailed message for scan results
	 * @param target
	 * @param dateTime
	 * @return
	 */
	static String printResults(Network target, Date dateTime) {
		String newLine = "\n";
		String results = "\nNetwork Scan - "
			+ new SimpleDateFormat("EEE, d MMM yyyy @ HH:mm:ss").format(dateTime)
			+ newLine
			+ "*****************************************"
			+ newLine
			+ "Target Network: " + target.networkAddress + "/" + target.CIDR
			+ newLine
		 	+ "Subnet Mask: " + target.subnetMask
		 	+ newLine
		 	+ "Maximum Network Hosts: " + target.maxHosts
		 	+ newLine
			+ "*****************************************"
			+ newLine
			+ "Found " + target.activeHosts.size() + " Active Hosts: "
			+ newLine;
		// Iterates over each host
		for(Host host : target.activeHosts) {
			results += " - " + host.address + ", " + host.name + ":";
			results += newLine;
			String ports;
			// Generates a comma-separated string of the active ports
			if(host.activePorts.size() > 0) {
				ports = host.activePorts.stream().map(Object::toString).collect(Collectors.joining(", "));
			} else {
				ports = "no open ports found";
			}
			results += "   - Open Ports: " + ports + "\n";
		}
		results += "*****************************************";
		System.out.println(results);
		return results;
	}
}
