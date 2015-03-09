package server;

import java.util.Scanner;

/**
 * ServerIO allows for the server to be given commands from whoever is using it.
 * This is primarily to allow for closing to be done nicer.
 * 
 * @author Aidan O'Grady
 * @version 0.3
 * @since 0.3
 *
 */
public class ServerIO{

	private Scanner scanner;
	
	public ServerIO(){
		scanner = new Scanner(System.in);
	}
	
	public boolean getQuit(){
		try{
			String input = scanner.nextLine().toLowerCase();
			
			if(input.equals("quit")){
				return true;
			}
		} catch (Exception e){
			System.out.println("Shit");
		}
		return false;
	}
}
