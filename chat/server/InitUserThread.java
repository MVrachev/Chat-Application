package chat.server;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.PrintWriter;

//I make this class package-private because its a implementation detail
//that has no sense to be public

class InitUserThread {

	private ChatServer chatServer;
	private BufferedReader bufferedReader;
	private PrintWriter printWriter;

	public InitUserThread(BufferedReader bufferedReader, PrintWriter printWriter, ChatServer chatServer) {

		this.bufferedReader = bufferedReader;
		this.printWriter = printWriter;
		this.chatServer = chatServer;
	}

	public void setName(UserThread user) {

		String name = null;
		try { 

			printWriter.println("Enter your name: ");
			name = bufferedReader.readLine();

			while (chatServer.getOnlineUsers().containsKey(name)) {

				printWriter.println("The user name is already online!");

				printWriter.println("Enter your name: ");
				name = bufferedReader.readLine();
			}

			printWriter.println("The name is approved!");

			System.out.println(name);

			user.setUserName(name);

		} catch (IOException e) {

			System.out.println("Problem in the setName function!");
			e.printStackTrace();
		}

	}

	public void chooseRoom(UserThread user) {

		printWriter.println("Choose which room do you want to join or create a new one: ");

		chatServer.printAllRooms(user);

		printWriter.println("Your room of choice: ");

		try {

			String room = bufferedReader.readLine();

			chatServer.addUserInChatRooms(room, user);

			user.setIsOwner(chatServer.isThisEmptyRoom(room));
			user.setChatRoom(room);

		} catch (IOException e) {

			System.out.println("Problem in choose a room function in UserThread class!");
			e.getStackTrace();
		}
	}

	public void setupUser(UserThread user) {

		setName(user);

		// The user register or sign in in the server
		chatServer.getUsersInfoFileParser().userEntry(user);

		chooseRoom(user);

		chatServer.userSetOnline(user.getName(), user.getChatRoom());
 
	}
}
