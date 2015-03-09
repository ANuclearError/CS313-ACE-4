package server;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
public final class WebServer implements Runnable {
	
	static final int PORT = 6789;
	
	private boolean running = true;
	
	private ServerSocket socket;
	
	private ExecutorService threadPool;

	
	@Override
	public void run() {
		System.out.println("CS313-ACE-4 Web Server");
		System.out.println("Author: Aidan O'Grady (201218150)");
		
		open();
		listen();
	}
	
	private void open(){
		try {
			socket = new ServerSocket(PORT);
			System.out.println("Opening server");
			threadPool = Executors.newCachedThreadPool();
		} catch (IOException e) {
			System.out.println("Open bugger");
			e.printStackTrace();
		}
	}
	
	private void listen(){
		while(running){
			try {
				Socket client = socket.accept(); // Connection found
				System.out.println("\nConnection found");				
				threadPool.execute(new HttpRequest(client));
			} catch (Exception e) {
				System.out.println("Listen bugger");
				e.printStackTrace();
			}
		}
	}
	
	public void close(){
		running = false;
		try {
			threadPool.shutdown();
			socket.close();
			System.out.println("Closing Server");
		} catch (IOException e) {
			System.out.println("There was a problem closing the server.");
			e.printStackTrace();
		}
	}
}