package server;

import java.net.*;

/**
 * The WebServer class is the main class of the system. It contains the main
 * method and is responsible for listening and delegating any requests it is
 * sent. The port field defines which port the server is listening on.
 * 
 * @author Aidan O'Grady
 * @version 0.2
 * @since 0.1
 *
 */
public final class WebServer {
	
	static final int PORT = 6789;
	
	/**
	 * Main method for stuff
	 * @param args - command line arguments
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception{
		System.out.println("CS313-ACE-4 Web Server");
		System.out.println("Author: Aidan O'Grady (201218150)");
		
		try{
			ServerSocket socket = new ServerSocket(PORT);
			System.out.println("Opening server");
			
			while(true){
				Socket client = socket.accept(); // Connection found
				System.out.println("\nConnection found");
				HttpRequest request = new HttpRequest(client);
				Thread thread = new Thread(request);
				thread.start();
			}
		} catch (Exception e){
			// TODO refine exception catching once I see what gets excepted.
			System.out.println("Bugger");
			e.printStackTrace();
		}
	}
}