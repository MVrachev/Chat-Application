package chat.server;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class UserThread implements Runnable {

	private ChatServer chatServer;
	private BufferedReader bufferedReader;
	private PrintWriter printWriter;
	private Socket socket;

	// I will use this helper class to initialise my UserThread instation

	private InitUserThread init;

	// I will use it as help class for the additional features

	private UserThreadFeatures features;

	// I keep the name of the user who has connection to the server
	// through that socket
	private String userName;

	// The current chat room where the user belongs to.

	private String chatRoom;

	// There is one special user for the room. The user who created the room.
	// If he quits then the room shuts down. Only he can close the room.
	// The variable is for is the current client owner of a chat room

	private boolean isOwner = false;

	public UserThread(Socket socket, ChatServer server) {

		this.chatServer = server;
		this.socket = socket;

		if (socket == null || server == null) {
			return;
		}

		try {

			InputStream input = socket.getInputStream();

			this.bufferedReader = new BufferedReader(new InputStreamReader(input));

			OutputStream outputStream = socket.getOutputStream();

			this.printWriter = new PrintWriter(outputStream, true);

			features = new UserThreadFeatures(chatServer, bufferedReader, printWriter, init);

			init = new InitUserThread(bufferedReader, printWriter, server);

		} catch (IOException e) {

			System.out.println("Problem in the constructor of UserThread!");

			e.getStackTrace();
		}
	}

	public UserThread() {
		// TODO Auto-generated constructor stub
	}

	public String getName() {
		return userName;
	}

	public String getChatRoom() {
		return chatRoom;
	}

	public boolean isOwner() {
		return isOwner;
	}

	public String getMessage() {

		String result = null;
		try {

			result = bufferedReader.readLine();
		} catch (IOException e) {
			System.out.println("Problem getting the message in UserThread class!");
			e.getStackTrace();
		}
		return result;
	}

	public void setIsOwner(boolean isOwner) {
		this.isOwner = isOwner;
	}

	public void sendWithNewLine(String message) {
		printWriter.println(message);
	}

	public void writeWholeBlockOfInformation(String information) {
		
		if(information == null) {
			printWriter.println('0');
			return;
		}
		
		String[] strings = information.split("\n");

		printWriter.println(strings.length);

		for (String string : strings) {
			printWriter.println(string);
		}
		

	}

	public void setChatRoom(String chatRoom) {
		this.chatRoom = chatRoom;
	}

	public void setUserName(String name) {
		this.userName = name;
	}
	
	
	private void setUser() {
		
		init.setupUser(this);

		printAllChatHistory();

		features.printAllUsersFromRoom(this, chatRoom);

		chatServer.broadCastMessage(chatRoom, "New user connected: " + userName);
	}
	
	
	private void closeUser() {

		chatServer.removeUser(this);

		System.out.println("The user: " + userName + " has logged out!");

		try {
			socket.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	

	private String getOnlyMessage(String input) {

		String tmp[] = input.split(":");
		return tmp[1].substring(1);
	}

	private void printAllChatHistory() {

		chatServer.readAllChatHistory(this, chatRoom);
	}

	private void addChatHistory(String message) {
		chatServer.addChatHistory(message, chatRoom);
	}

	@Override
	public void run() {

		try {
			
			setUser();

			// chatServer.broadCastMessage(chatRoom, "There are special commands. If you
			// want to see the list of them write: >> help ");

			String clientMessage = null;

			do {

				if (chatRoom == null) {

					init.chooseRoom(this);
					continue;
				}

				clientMessage = bufferedReader.readLine();

				if (clientMessage.charAt(0) == '>' && clientMessage.charAt(1) == '>') {

					features.specialCommands(clientMessage, this);
					continue;
				}

				chatServer.broadCastMessage(chatRoom, clientMessage);

				addChatHistory(clientMessage);

				if (getOnlyMessage(clientMessage).equals("bye")) {

					break;
				}

			} while (clientMessage != null);
			
			closeUser();

		} catch (IOException e) {

			System.out.println("Error in UserThread class by buffered reader!");
			e.printStackTrace();
		}

	}

}
