package chat.client;

import java.io.IOException;

import java.net.Socket;
import java.net.UnknownHostException;

public class ChatClient {

	private String hostName;
	private int port;
	private String userName;

	private String chatRoom;

	public ChatClient(String hostname, int port) {
		this.hostName = hostname;
		this.port = port;
	}

	public void setRoom(String room) {
		this.chatRoom = room;
	}

	public void setUserName(String name) {
		this.userName = name;
	}

	public String getUserName() {
		return this.userName;
	}

	public String getChatRoom() {
		return chatRoom;
	}

	public void execute() {
		try {
			Socket socket = new Socket(hostName, port);

			System.out.println("\nConnected to the chat server.");

			Thread worker = new Thread(new Worker(socket, this));
			worker.start();

		} catch (UnknownHostException ex) {
			System.out.println("Server not found: ");
		} catch (IOException e) {
			System.out.println("Problem opening the socket!");
			e.getStackTrace();
		}

	}

	public static void main(String[] args) {

		if (args.length < 2)
			return;

		String hostname = args[0];
		int port = Integer.parseInt(args[1]);

		ChatClient client = new ChatClient(hostname, port);
		client.execute();
	}

}
