package chat.server;

import chat.server.UserThread;

import files.parser.UsersInfoFileParser;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.net.ServerSocket;
import java.net.Socket;
 
public class ChatServer {

	// Key value of the map is the chat room represented by a string and
	// The value is a ClassRomm object who has information for all
	// online users, chat history and owner of the chat room.
	
	// I use ConcurrentHashMap because I have this resource is shared
	// by all threads connected to the server
	
	private Map<String, ChatRoom> allChatRooms = new ConcurrentHashMap<>();

	private static final int SERVER_PORT = 4444;

	// Map with key the name of the user and value the room where he is

	private Map<String, String> onlineUsers = new ConcurrentHashMap<>();

	// This class job is to maintain all the file work needed in the chat
	// application
	
	private UsersInfoFileParser usersInfoFileParser = new UsersInfoFileParser();
	
	public Map<String, ChatRoom> getAllChatRooms() {
				return allChatRooms;
	}

	public Map<String, String> getOnlineUsers() {
		return onlineUsers;
	}

	public UsersInfoFileParser getUsersInfoFileParser() {
		return usersInfoFileParser;
	}

	public boolean isThisEmptyRoom(String room) {

		if (allChatRooms.get(room).getNumOfOnlineUsers() == 1) {
			return true;
		}
		return false;
	}

	public void userSetOnline(String name, String room) {
		onlineUsers.putIfAbsent(name, room);
	}

	public void addUserInChatRooms(String room, UserThread user) {

		synchronized (allChatRooms) {
			
			if (allChatRooms.containsKey(room) == false) {

				ChatRoom newRoom = new ChatRoom(room);
				newRoom.addUser(user);

				allChatRooms.put(room, newRoom);

			} else {
				allChatRooms.get(room).addUser(user);
			}
			
		}
	
		// I know this operation is atomic and I am sure
		// that the user doesn't exists in the map
		onlineUsers.putIfAbsent(user.getName(), room);

	}
	
	
	public void addChatHistory(String message, String room) {
		allChatRooms.get(room).addChatHistory(message);
	}
	
	
	public void readAllChatHistory(UserThread user, String room) {
		
		String chatHistory = allChatRooms.get(room).getChatHistory(user);
		
		user.writeWholeBlockOfInformation(chatHistory);
		
		System.out.println(chatHistory);
	}
	
	

	public String printAllUsersFromAllRooms(UserThread user) {

		Collection<ChatRoom> chatRoomsCollection = allChatRooms.values();
		String result = "Online users from all rooms: ";

		for( ChatRoom chatRoom : chatRoomsCollection) {
			
			result += chatRoom.getOnlineUsersNames();
		}
		
		user.sendWithNewLine(result);
		
		return result;
	}
 
	public String printUsersFromRoom(UserThread user, String room) {

		System.out.println("Room for printing users: " + room);
		if (!allChatRooms.containsKey(room)) {
			user.sendWithNewLine("There is no such room!");
			return null;
		}

		String result = "Connected users in " + room + " chat room are: "
				+ allChatRooms.get(room).getOnlineUsersNames();
		
		user.sendWithNewLine(result);
		
		return result;
	}
	

	public String printAllRooms(UserThread user) {

		if (allChatRooms.isEmpty()) {

			user.sendWithNewLine("no rooms available");
			return null;
		} 
		
		Set<String> allChatRoomsNames = allChatRooms.keySet();
		
		String result = "All active rooms: ";

		for (String chatRoomName : allChatRoomsNames) {

//			if (chatRoomName == null) {
//				continue;
//			}
			result += chatRoomName + " ; ";
		}

//		if (result.equals("All active rooms: ")) {
//			result = "no rooms available";
//		}
		
		user.sendWithNewLine(result);
		
		return result;
	}

	public void broadCastMessage(String room, String message) {

		if (!allChatRooms.containsKey(room)) {
			return; 
		}
		
		 
		allChatRooms.get(room).broadcastMessage(message);
		
	}

	public boolean sendPrivateMessage(String from, String to, String message) {

		String room = onlineUsers.get(to);
		
		if(room == null) {
			return false;
		}
		
		UserThread endReceiver = allChatRooms.get(room).getCertainUser(to);
		
		if(endReceiver == null) {
			return false;
		}

		endReceiver.sendWithNewLine("Private message from " + from + " : " + message);
	
		return true;
	}

	public void closeRoom(String room) {

		broadCastMessage(room, "This chat room will be closed!");
		
		List<UserThread> users = allChatRooms.get(room).getOnlineUsers();
		
		synchronized (onlineUsers) {
			
			for(UserThread user : users) {
				
				user.setChatRoom(null);
				onlineUsers.put(user.getName(), null);
			}
		}
		
		// The operation is guaranteed atomic
		allChatRooms.remove(room);

	}

	public void leaveRoom(UserThread user) {

		broadCastMessage(user.getChatRoom(), "The user: " + user.getName() + " is leaving the room!");

		synchronized (allChatRooms) {
			
			if (allChatRooms.get(user.getChatRoom()).getNumOfOnlineUsers() == 1) {
				allChatRooms.remove(user.getChatRoom());
			}

			else {
				allChatRooms.get(user.getChatRoom()).removeUser(user);
			}
		}
		
		// Because this user is already inside the map then putIfAbsent won't be atomic
		// and I need this synchronised block
		synchronized (onlineUsers) {
			onlineUsers.put(user.getName(), null);
		}
		
		user.setChatRoom(null);
	}

	 
	public void removeUser(UserThread user) {

		synchronized (user) {
			allChatRooms.get(user.getChatRoom()).removeUser(user);
		}
		allChatRooms.get(user.getChatRoom()).removeUser(user);
		onlineUsers.remove(user.getName());

		broadCastMessage(user.getChatRoom(), "The user: " + user.getName() + " has quitted.");

		
		if(allChatRooms.get(user.getChatRoom()).getNumOfOnlineUsers() == 0) {
			allChatRooms.remove(user.getChatRoom());
		}
	}

	
	public void execute() {

		try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {

			System.out.println("Chat Server is listening on port " + SERVER_PORT);

			while (true) {

				Socket socket = serverSocket.accept();
				System.out.println("New user connected.");

				Thread userThread = new Thread(new UserThread(socket, this));
				userThread.start();
			}
		} catch (IOException e) {
			System.out.println("Error in the server!");	
			e.getStackTrace();
		}
	}

	public static void main(String[] args) {

		ChatServer chatRoomServer = new ChatServer();

		chatRoomServer.execute();

	}
}
