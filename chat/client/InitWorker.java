package chat.client;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

// I make this class package-private because its a implementation detail
// that has no sense to be public

public class InitWorker {

	private BufferedReader bufferedReader;
	private PrintWriter printWriter;
	private ChatClient chatClient;
	private Socket socket;

	private Scanner stdIn;

	public InitWorker(BufferedReader bufferedReader, PrintWriter printWriter, ChatClient chatClient, Socket socket,
			Scanner stdIn) {

		this.bufferedReader = bufferedReader;
		this.printWriter = printWriter;
		this.chatClient = chatClient;
		this.socket = socket;
		this.stdIn = stdIn;
	}

	private void closeSocket() {
		try {
			socket.close();
		} catch (IOException e) {

			System.out.println("Problem closing the socket!");
			e.printStackTrace();
		}
	}

	private String helperSetName() {

		String name = null;

		try {

			String nameMessage = bufferedReader.readLine();

			System.out.print(nameMessage);

			name = stdIn.nextLine();

			printWriter.println(name);

		} catch (IOException e) {

			System.out.println("Problem in the helperSetName function!");
			e.getStackTrace();

		} catch (Exception e) {
			closeSocket();
		}

		return name;
	}

	private void passwdInput() {

		try {

			String passwdMessage = bufferedReader.readLine();
			System.out.print(passwdMessage);

			String password = stdIn.nextLine();
			printWriter.println(password);

			String isItSuccess = null;

			while ((isItSuccess = bufferedReader.readLine()) != null) {

				if (isItSuccess.equals("You have successfully logged into the chat program!")
						|| isItSuccess.equals("You have successfully register into the chat program!")) {
					break;
			}

				System.out.println(isItSuccess);

				System.out.print(passwdMessage);

				password = stdIn.nextLine();
				printWriter.println(password);

			}

			// Print the end message that the user is accepted into the system
			System.out.println(isItSuccess + "\n");
		} catch (IOException e) {

			e.getStackTrace();
		}

	}

	public void setName() {

		try {

			String name = helperSetName();

			String serverMessage = null;

			while ((serverMessage = bufferedReader.readLine()).equals("The user name is already online!")) {

				System.out.println(serverMessage);

				name = helperSetName();
			}

			System.out.println(serverMessage);

			chatClient.setUserName(name);

			// Gets the right password
			passwdInput();

		} catch (IOException e) {

			System.out.println("Problem in reading in the set name function in Worker class!");
			e.printStackTrace();
		} catch (Exception e) {
			closeSocket();
		}
	}

	public void chooseRoom() {

		try {

			String roomMessage = bufferedReader.readLine();
			String availableRooms = bufferedReader.readLine();
			String invitingMessage = bufferedReader.readLine();

			System.out.println(roomMessage);
			System.out.println(availableRooms);
			System.out.print(invitingMessage);

			String room = stdIn.nextLine();

			printWriter.println(room);

			chatClient.setRoom(room);

		} catch (IOException e) {

			System.out.println("Problem in setChatRoom fuction in Worker class!");

			e.printStackTrace();
		} catch (Exception e) {
			closeSocket();
		}

	}

}
