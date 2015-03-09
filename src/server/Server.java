package server;

public class Server {
	
	private WebServer webServer;
	
	private ServerIO io;
	
	
	public Server(){
		webServer = new WebServer();
		new Thread(webServer).start();
		
		io = new ServerIO();
		while(true){
			if(io.getQuit()){
				webServer.close();
				break;
			}
		}
		System.out.println("Closing system");
	}
	
	public static void main(String[] args){
		Server server = new Server();
	}
	

}
