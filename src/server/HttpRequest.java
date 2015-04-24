package server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.StringTokenizer;

/**
 * The HttpRequest class is what deals with any requests that are sent to the
 * WebServer. Implementing runnable is what allows for the system to be
 * multithreaded so that multiple requests can be handled simultaneously.
 * 
 * @author Aidan O'Grady
 * @version 1.1
 * @since 0.2
 *
 */
public class HttpRequest implements Runnable {
	
	/**
	 * Ensures proper returns when sending data over.
	 */
	private final static String CRLF = "\r\n";
	
	/**
	 * File not found, then display this. If this is missing then things will
	 * probably screw up badly.
	 */
	private final static String NOTFOUND = "files/404.html";
	
	/**
	 * Home page, if there's no actual file in the request.
	 */
	private final static String INDEX = "index.html";
		
	/**
	 * Input stream when receiving request
	 */
	private InputStream fis;
	
	/**
	 * Output stream for sending responses
	 */
	private DataOutputStream dos;
	
	/**
	 * The client being processed.
	 */
	private Socket socket;
	
	/**
	 * Constructor
	 * @param socket - the connecting socket
	 * @throws Exception
	 */
	public HttpRequest(Socket socket) throws Exception {
		this.socket = socket;
	}

	@Override
	public void run() {
		try {
			processRequest();
		} catch (NullPointerException e) {
			System.out.println("We got a null in here, most likely a phantom socket.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * The request is handled here, executing the server side stuff.
	 * @throws Exception
	 */
	private void processRequest() throws Exception {
		OutputStream os = socket.getOutputStream();
		dos = new DataOutputStream(os);
		InputStream is = socket.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		
		String requestLine = readRequest(new BufferedReader(isr));
		// Extract filename from the request line
		String fileName = getFileName(requestLine);
		respond(fileName);
		close();
	}
	
	
	/**
	 * Reads the incoming request and displays the request line along with the
	 * header lines, and then returns the request line so it can be used later
	 * on.
	 * 
	 * @param isr - The reader of the request
	 * @return requestLine
	 * @throws Exception
	 */
	private String readRequest(BufferedReader br) throws Exception {
		String requestLine = br.readLine();
		
		System.out.println("\nRequest Line:\n========");
		System.out.println(requestLine);
		System.out.println("\nHeader Lines:\n========");
		
		String headerLine = br.readLine();
		while(!headerLine.equals("") && !(headerLine == null)) {
			System.out.println(headerLine);
			headerLine = br.readLine();
		}
		return requestLine;
	}
	
	
	/**
	 * Returns the requested file name, extracted from the request line.
	 * @param requestLine - the request line containing the file name.
	 * @return fileName
	 */
	private String getFileName(String requestLine) {
		StringTokenizer tokens = new StringTokenizer(requestLine);
		tokens.nextToken(); // skip method
		String fileName = tokens.nextToken();
		if(fileName.equals("/")) {
			fileName = INDEX;
		}
		System.out.println("\nRequested URL:\n========");
		System.out.println(fileName);
		return fileName;
	}
	
	
	/**
	 * Handles the response to the socket, determining whether to send the
	 * requested file or the 404 page in the case that the file does not exist.
	 * 
	 * @param fileName
	 * @throws Exception
	 */
	private void respond(String fileName) throws Exception{
		String statusLine = null;
		String contentTypeLine = null;
		
		boolean fileExists = fileExists(fileName);
		
		if(fileExists) {
			statusLine = "HTTP/1.1 200 OK";
			contentTypeLine = "Content-type: " + contentType(fileName) + CRLF;
			sendResponse(statusLine, contentTypeLine);
		} else {
			forward(fileName);
		}
	}
	
	private void forward(String fileName) throws Exception{
		URL url = new URL(fileName);
		HttpURLConnection connection = 
				(HttpURLConnection) url.openConnection();
		connection.setInstanceFollowRedirects(true);
				
		int responseCode = connection.getResponseCode();
		
		String statusLine = "HTTP/1.1 " + connection.getResponseCode() + " " + 
				connection.getResponseMessage();
		String contentTypeLine = "Content-type: " +
				connection.getContentType() + CRLF;
		
		if(responseCode > 400 && responseCode < 600){
			fis = connection.getErrorStream();
		} else {
			fis = connection.getInputStream();
		}
		
		sendResponse(statusLine, contentTypeLine);

	}
	
	
	/**
	 * Returns whether or not the file exists, and creates the appropriate
	 * file input stream.
	 * 
	 * Right now, the 404 page being lost will probably mess things up, throwing
	 * an exception.
	 * 
	 * @param fileName - the file to be looked up
	 * @return if the file exists
	 * @throws Exception
	 */
	private boolean fileExists(String fileName) throws Exception {
		try { // Horray
			fis = new FileInputStream("files/" + fileName);
			return true;
		} catch(FileNotFoundException e) { // Forward
			return false;
		}
	}
	
	
	/**
	 * Returns the related MIME type given a file name.
	 * 
	 * @param fileName - the file whose MIME type is to be determined
	 * @return MIME type
	 */
	private String contentType(String fileName) {
		if(fileName.endsWith(".htm") || fileName.endsWith(".html")) {
			return "text/html";
		}
		return "application/octet-stream";
	}
	
	
	/**
	 * Sends the response to the user, starting with status and content type
	 * lines.
	 * 
	 * @param status - response status line
	 * @param content - response content type
	 * @throws Exception
	 */
	private void sendResponse(String status, String content) throws Exception {
		System.out.println("\nResponse:\n========");
		
		System.out.println("Writing status line");
		System.out.println(status);
		dos.writeBytes(status);
		
		System.out.println("Writing content type");
		System.out.println(content);
		dos.writeBytes(content);
		
		dos.writeBytes(CRLF);
		sendBytes();
	}
	
	
	/**
	 * Given a FileInputStream and DataOutputStream, the FIS reads in the data
	 * of its file and sends it out to the DOS.
	 * 
	 * @param fis - what is to be send to the client
	 * @param os - what sends the data to the client
	 * @throws Exception
	 */
	private void sendBytes() throws
	Exception {
		byte[] buffer = new byte[1024];
		int bytes = 0;
		
		while((bytes = fis.read(buffer)) != -1) {
			dos.write(buffer, 0, bytes);
		}
	}
	
	
	/**
	 * Shut down the socket, it's no longer needed.
	 * @throws Exception
	 */
	private void close() throws Exception {
		// Close everything
		fis.close();
		dos.close();
		System.out.println("Closing socket");
		socket.close();
	}
}
