package server;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * The WebServer class is the main class of the system. It contains the main
 * method and is responsible for listening and delegating any requests it is
 * sent. The port field defines which port the server is listening on.
 * 
 * @author Aidan O'Grady
 * @version 0.0
 * @since 0.0
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

/**
 * The HttpRequest class is what deals with any requests that are sent to the
 * WebServer. Implementing runnable is what allows for the system to be
 * multithreaded so that multiple requests can be handled simultaneously.
 * 
 * @author Aidan O'Grady
 * @version 0.0
 * @since 0.0
 *
 */
final class HttpRequest implements Runnable{
	
	final static String CRLF = "\r\n";
	Socket socket;
	
	/**
	 * Constructor
	 * @param socket - the connecting socket
	 * @throws Exception
	 */
	public HttpRequest(Socket socket) throws Exception{
		this.socket = socket;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try{
			processRequest();
		} catch (Exception e){
			System.out.println(e);
		}
		
	}
	
	/**
	 * The request is handled here, executing the server side stuff.
	 * @throws Exception
	 */
	private void processRequest() throws Exception{
		InputStream is = socket.getInputStream();
		DataOutputStream os = new DataOutputStream(socket.getOutputStream());
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		
		String requestLine = br.readLine();
		System.out.println("\nRequest Line:\n========");
		System.out.println(requestLine);
		
		System.out.println("\nHeader Lines:\n========");
		String headerLine = br.readLine();
		while(headerLine != null){
			System.out.println(headerLine);
			headerLine = br.readLine();
		}		
		// Extract filename from the request line
		StringTokenizer tokens = new StringTokenizer(requestLine);
		tokens.nextToken(); // skip method
		String fileName = tokens.nextToken();
		
		fileName = "." + fileName;
		
		FileInputStream fis = null;
		boolean fileExists = true;
		try{
			fis = new FileInputStream(fileName);
		} catch(FileNotFoundException e){
			fileExists = false;
		}
		
		String statusLine = null;
		String contentTypeLine = null;
		String entityBody = null;
		if(fileExists){
			statusLine = "HTTP/1.1 200 OK";
			contentTypeLine = "Content-type: " + contentType(fileName) + CRLF;
		} else{
			statusLine = "HTTP/1.1 404 Not Found";
			contentTypeLine = "text/html";
			entityBody = "<HTML>" + "<HEAD><TITLE>Not Found</TITLE></HEAD>" +
					"<BODY>Not Found</BODY></HTML>";
		}

		
		System.out.println("\nResponse:\n========");
		System.out.println(statusLine);
		System.out.println(contentTypeLine);
		os.writeBytes(statusLine);
		os.writeBytes(contentTypeLine);
		os.writeBytes(CRLF);
		
		if(fileExists){
			sendBytes(fis, os);
			fis.close();
		}else{
			os.writeBytes(entityBody);
		}
		os.close();
		br.close();
		socket.close();
	}
		
	private void sendBytes(FileInputStream fis, DataOutputStream os) throws
	Exception {
		byte[] buffer = new byte[1024];
		int bytes = 0;
		
		while((bytes = fis.read(buffer)) != -1){
			os.write(buffer, 0, bytes);
		}
		
	}

	private String contentType(String fileName){
		if(fileName.endsWith(".htm") || fileName.endsWith(".html")){
			return "text/html";
		}
		return "application/octet-stream";
	}
}
