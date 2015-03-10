package server;

import java.util.Scanner;

/**
 * ServerIO allows for the server to be given commands from whoever is using it.
 * This is primarily to allow for closing to be done nicer.
 * 
 * @author Aidan O'Grady
 * @version 0.4
 * @since 0.3
 *
 */
public class ServerIO{

	/**
	 * The scanner used for interpreting the user input
	 */
	private Scanner scanner;
	
	/**
	 * Constructor
	 */
	public ServerIO(){
		scanner = new Scanner(System.in);
	}
	
	/**
	 * Returns whether the user's latest input is "quit".
	 * @return input == "quit"
	 */
	public boolean getQuit(){
		try{
			String input = scanner.nextLine().toLowerCase(); //Get user input
			
			if(input.equals("quit")){
				return true;
			}
		} catch (Exception e){ // TODO Add better exception handling.
			System.out.println("Shit");
		}
		return false;
	}
}
