package server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.StringTokenizer;

/**
 * The HttpRequest class is what deals with any requests that are sent to the
 * WebServer. Implementing runnable is what allows for the system to be
 * multithreaded so that multiple requests can be handled simultaneously.
 * 
 * @author Aidan O'Grady
 * @version 0.2
 * @since 0.2
 *
 */
public class HttpRequest implements Runnable{
	
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
