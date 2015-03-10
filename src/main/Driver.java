package main;

import server.Server;

/**
 * The Driver class holds the main method, starts up everything all nice and
 * proper.
 * 
 * @author Aidan O'Grady
 * @version 0.3
 * @since 0.3
 *
 */
public class Driver {

	/**
	 * Main method, sets up everything for us.
	 * @param args - command line arguments
	 */
	public static void main(String[] args){
		Server server = new Server();
		server.run();
	}

}
