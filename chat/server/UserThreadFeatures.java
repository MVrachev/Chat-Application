package chat.server;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.PrintWriter;

//I make this class package-private because its a implementation detail
//that has no sense to be public

class UserThreadFeatures {

	private ChatServer chatServer;
	private BufferedReader bufferedReader;
	private PrintWriter printWriter;
	private InitUserThread init;

	public UserThreadFeatures(ChatServer chatServer, BufferedReader bufferedReader, PrintWriter printWriter,
			InitUserThread init) {

		this.chatServer = chatServer;
		this.bufferedReader = bufferedReader;
		this.printWriter = printWriter;
		this.init = init;
	}
	
	
	public void printAllUsersFromRoom(UserThread user, String room) {
		chatServer.printUsersFromRoom(user, room);
	}
	

	private void sendPrivateMessage(UserThread user) {

		try {

			printWriter.println("To: ");
			String endReceiver = bufferedReader.readLine();

			printWriter.println("The message: ");
			String message = bufferedReader.readLine();

			if (chatServer.sendPrivateMessage(user.getName(), endReceiver, message) == false) {

				printWriter.println("The end user of the message is not found!");
			} else {
				printWriter.println("The message was send successfully!");
			}

		} catch (IOException e) {
			System.out.println("Problem with bufferedReader in sendPrivateMessage function!");
		}
	}

	private void changeRoom(UserThread user) {

		chatServer.leaveRoom(user);
		init.chooseRoom(user);

		if (user.getChatRoom() != null) {
			printWriter.println("Succsesfully changed the chat room!");
		} else {
			printWriter.println("Problem changing the chat room!");

		}
		
	}

	private void closeRoom(UserThread user) {

		if (user.isOwner() == false) {

			printWriter.println("You are not an owner of that room so you can't close it!");

			return;
		} 

		printWriter.println("You are the owner of the room and the room will be closed!");

		chatServer.closeRoom(user.getChatRoom());

		init.chooseRoom(user);

		System.out.println("Your new room: " + user.getChatRoom());

	}

	public void specialCommands(String input, UserThread user) {

		if (input.equals(">> print all users")) {

			chatServer.printAllUsersFromAllRooms(user);
		} else if (input.equals(">> print all rooms")) {

			chatServer.printAllRooms(user);
		} else if (input.equals(">> all users in room")) {

			printWriter.println("Users from which room do you want to see: ");
			
			try {
				String room = bufferedReader.readLine();
				
				chatServer.printUsersFromRoom(user, room);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} else if (input.equals(">> send private message")) {

			sendPrivateMessage(user);
		} else if (input.equals(">> close this room")) {

			closeRoom(user);
		} else if (input.equals(">> change room")) {

			changeRoom(user);
		} else {

			printWriter.println("The command is not recognised!");
		}  
	}
}
