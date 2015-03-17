package server;

/**
 * The server class is the class that brings everything together, initialising
 * it all so that it works. It ensures that when the IO gets a quit command, the
 * server will gracefully close.
 * 
 * @author Aidan O'Grady
 * @version 1.0
 * @since 0.3
 *
 */
public class Server {
	
	/**
	 * The server to be set up
	 */
	private WebServer webServer;
	
	/**
	 * Used to listen for when to quit the system
	 */
	private ServerIO io;
	
	/**
	 * Constructor, yay
	 */
	public Server(){
		webServer = new WebServer();
		io = new ServerIO();
	}
	
	/**
	 * The system begins running, the web server and the sever IO are run
	 * concurrently listening for their respective 
	 */
	public void run(){
		new Thread(webServer).start();
		while(true){
			if(io.getQuit()){
				webServer.quit();
				break;
			}
		}
		System.out.println("Closing system");
		System.exit(0);
	}
}
