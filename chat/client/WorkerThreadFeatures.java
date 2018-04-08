package chat.client;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;


//I make this class package-private because its a implementation detail
//that has no sense to be public

class WorkerThreadFeatures {

	private BufferedReader bufferedReader;
	private PrintWriter printWriter;
	private ChatClient chatClient;
	private Scanner stdIn;

	public WorkerThreadFeatures(BufferedReader bufferedReader, PrintWriter printWriter, ChatClient chatClient,
			Socket socket, Scanner stdIn) {

		this.bufferedReader = bufferedReader;
		this.printWriter = printWriter;
		this.chatClient = chatClient;
		this.stdIn = stdIn;
	}

//	private void closeSocket() {
//		try {
//			socket.close();
//		} catch (IOException e) {
//
//			System.out.println("Problem closing the socket!");
//			e.printStackTrace();
//		}
//	}

	private void printAllUsersFromAllRooms() {

		try {
			String allUsersMessage = bufferedReader.readLine();

			System.out.println(allUsersMessage);

		} catch (IOException e) {

			e.printStackTrace();
		}
	}
	
	
	private void printAllRooms() {
		
		try {
			String allRooms = bufferedReader.readLine();

			System.out.println(allRooms);

		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	public void printAllUsersFromRoom() {

		try {
			String allUsersMessage = bufferedReader.readLine();

			System.out.println(allUsersMessage);

		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	private void sendPrivateMessage(String input) {

		try {

			// A message "To: "
			String serverMessage = bufferedReader.readLine();
			System.out.print(serverMessage);

			// Writes to who will be send
			String endReceiver = stdIn.nextLine();
			printWriter.println(endReceiver);

			// Prompt for the message itself
			String prompt = bufferedReader.readLine();
			System.out.print(prompt);

			// The message itself
			String message = stdIn.nextLine();
			printWriter.println(message);

			// Feedback from the server about the message
			String feedback = bufferedReader.readLine();
			System.out.println(feedback);

		} catch (IOException e) {
			System.out.println("Problem with bufferedReader in sendPrivateMessage function!");
		}
	}

	private void closeRoom(InitWorker initWorker) {

		try {

			// A message if the operation is going to succeed

			String amIOwner = bufferedReader.readLine();
			System.out.println(amIOwner);

			String endOfTheRoom = bufferedReader.readLine();
			System.out.println(endOfTheRoom);

			System.out.println();
			chatClient.setRoom(null);
			initWorker.chooseRoom();

		} catch (IOException e) {
			System.out.println("Problem with bufferedReadr in closeRoom in Worker class!");
			e.getStackTrace();
		}

	}

	private void changeRoom(InitWorker initWorker) {

		try {

			String serverMessage = bufferedReader.readLine();
			System.out.println(serverMessage);

			System.out.println();
			initWorker.chooseRoom();

			// The end message if the operation is successful
			String endMessage = bufferedReader.readLine();
			System.out.println(endMessage);

		} catch (IOException e) {
			System.out.println("Problem with bufferedReadr in changeRoom in Worker class!");
			e.getStackTrace();
		}

	}

	
	private void allUsersInSpeficRoom() {
		
		try {
			String promptMess = bufferedReader.readLine();
			System.out.print(promptMess);

			String room = stdIn.nextLine();
			printWriter.println(room);
			
			printAllUsersFromRoom();
		
		} catch (IOException e) {

			e.printStackTrace();
		}
	}
	
	
	public void specialCommands(String input, InitWorker initWorker) {

		printWriter.println(input);

		if (input.equals(">> print all users")) {

			printAllUsersFromAllRooms();
		} else if (input.equals(">> print all rooms")) {
			
			printAllRooms();
		} else if (input.equals(">> all users in room")) {

			allUsersInSpeficRoom();
		} else if (input.equals(">> send private message")) {
			
			sendPrivateMessage(input);
		} else if (input.equals(">> close the room")) {
			
			closeRoom(initWorker);
		} else if (input.equals(">> change room")) {
			
			changeRoom(initWorker);
		}
		else {
			
			try {
				String wrongCommandMessage = bufferedReader.readLine();
				
				System.out.println(wrongCommandMessage);
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		}

	}

}
