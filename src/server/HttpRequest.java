package server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
 * @version 0.4
 * @since 0.2
 *
 */
public class HttpRequest implements Runnable{
	
	/**
	 * Enforces new line after each string
	 */
	final static String CRLF = "\r\n";
	
	/**
	 * The socket that was created by the server
	 */
	Socket socket;
	
	private FileInputStream fis;
	
	private DataOutputStream os;
	
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
		
		// TODO refactor
		
		InputStream is = socket.getInputStream();
		os = new DataOutputStream(socket.getOutputStream());
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		
		String requestLine = requestLine(br);
		
		headerLine(br);
		
		String fileName = fileName(requestLine);
		
		boolean fileExists = fileExists(fileName);
		
		String statusLine, contentTypeLine, entityBody;
		
		if(fileExists){
			statusLine = "HTTP/1.1 200 OK";
			contentTypeLine = "Content-type: " + contentType(fileName) + CRLF;
			entityBody = null; // Not required anymore
		} else{
			statusLine = "HTTP/1.1 404 Not Found";
			contentTypeLine = "Content-type: text/html" + CRLF;
			entityBody = "<HTML>" + "<HEAD><TITLE>Not Found</TITLE></HEAD>" +
					"<BODY>Not Found</BODY></HTML>";
		}

		respond(statusLine, contentTypeLine);
		
		sendContent(fileExists, entityBody);
				
		os.close();
		br.close();
		System.out.println("Closing socket");
		socket.close();
	}
	
	/**
	 * Returns the request line. It has been separated to refactor it and keep
	 * it separate from the rest of the system. 
	 * 
	 * @param br - the buffered reader to extract the request line
	 * @return the request line
	 */
	private String requestLine(BufferedReader br){
		String requestLine = "";
		try {
			requestLine = br.readLine();
			System.out.println("\nRequest Line:\n========");
			System.out.println(requestLine);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return requestLine;
	}
	
	/**
	 * Displays the header line. Separated to refactor class etc
	 * @param br - the buffered reader to extract the request line
	 */
	private void headerLine(BufferedReader br){
		
		String headerLine;
		try {
			headerLine = br.readLine();
			System.out.println("\nHeader Lines:\n========");
			while(!headerLine.equals("")){
				System.out.println(headerLine);
				headerLine = br.readLine();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Given the request line, returns the name of the requested file
	 * @param requestLine - the socket request line
	 * @return - the name of the desired file
	 */
	private String fileName(String requestLine){
		// Extract filename from the request line
		StringTokenizer tokens = new StringTokenizer(requestLine);
		tokens.nextToken(); // skip method
		String fileName = tokens.nextToken();				
		fileName = "." + fileName;		
		return fileName;
	}

	/**
	 * Returns whether or not the desired file exists. If it does exist, then
	 * a file input stream is created for the file. The file not existing will
	 * cause this to fail.
	 * 
	 * @param fileName - the to be checked
	 * @return whether or not the file exists
	 */
	private boolean fileExists(String fileName){
		try{
			fis = new FileInputStream(fileName);
			return true;
		} catch(FileNotFoundException e){
			return false;
		}	
	}
	
	/**
	 * If a valid file has been found, the file will be streamed to the client
	 * socket.
	 * 
	 * @param fis - InputStream reads the file to sent.
	 * @param os - OutputStream to send the data to client 
	 * @throws Exception - If it craps itself, you deal with it.
	 */
	private void sendBytes() throws
	Exception {
		byte[] buffer = new byte[1024];
		int bytes = 0;
		
		// Continuously send the data
		while((bytes = fis.read(buffer)) != -1){
			os.write(buffer, 0, bytes);
		}
		
	}

	/**
	 * Returns the MIME type corresponding to the given file name.
	 * @param fileName - the file to be sent
	 * @return MIME type
	 */
	private String contentType(String fileName){
		// TODO Handle more types
		if(fileName.endsWith(".htm") || fileName.endsWith(".html")){
			return "text/html";
		}
		return "application/octet-stream";
	}

	/**
	 * Generates the start of the response by writing the status line and
	 * content type line to the output stream.
	 * 
	 * @param statusLine - response status line
	 * @param contentTypeLine - data content type
	 * @throws Exception
	 */
	private void respond(String statusLine, String contentTypeLine) throws Exception{
		System.out.println("\nResponse:\n========");
		System.out.println(statusLine);
		System.out.println(contentTypeLine);
		os.writeBytes(statusLine);
		System.out.println("Writing status line");
		os.writeBytes(contentTypeLine);
		System.out.println("Writing content type");
		os.writeBytes(CRLF);
	}
	
	/**
	 * Sends the content to the client. If the file exists, then sendBytes is
	 * called to send the file, otherwise, it will end the entityBody, which
	 * is the 404 error page.
	 * 
	 * @param fileExists - whether or not the file exists
	 * @param entityBody - the 404 page if needed
	 * @throws Exception
	 */
	private void sendContent(boolean fileExists, String entityBody) throws
	Exception{
		System.out.println("Writing content");
		
		if(fileExists){
			sendBytes();
			fis.close();
		}else{
			os.writeBytes(entityBody);
		}
	}
}
