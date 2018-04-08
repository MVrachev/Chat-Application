package chat.server;

import java.util.ArrayList;
import java.util.List;

import files.parser.ChatHistoryFileParser;

public class ChatRoom {

	
	private List<UserThread> onlineUsers = new ArrayList<>();
	
	private String owner = new String();
	
	private ChatHistoryFileParser chatHistoryFileParser;
	
	public ChatRoom(String roomName) {
		
		chatHistoryFileParser = new ChatHistoryFileParser(roomName);
	}
	
	public List<UserThread> getOnlineUsers() {
		return onlineUsers;
	}

	public void setOnlineUsers(List<UserThread> onlineUsers) {
		this.onlineUsers = onlineUsers;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	
	public int getNumOfOnlineUsers() {
		return onlineUsers.size();
	}
	
	
	public void addUser(UserThread newUser) {
		
		synchronized (onlineUsers) {
			onlineUsers.add(newUser);
		}
	}
	
	public void addChatHistory(String message) {
		chatHistoryFileParser.addMoreHistory(message);
	}
	
	public String getChatHistory(UserThread user) {
		return chatHistoryFileParser.getChatHistory();
	}
	
	
	public void removeUser(UserThread user) {
		onlineUsers.remove(user);
	}
	
	
	public String getOnlineUsersNames() {
		
		String result = new String();
		
		for(UserThread user : onlineUsers) {
			
//			if (user.getName() != null)
//				result += curr.getName() + " ; ";
			
			result+= user.getName() + " ; ";
		}
		
		return result;
	}
	
	
	public void broadcastMessage(String message) {
		
		for (UserThread user : onlineUsers) {

			user.sendWithNewLine(message);
		}
	}
	
	
	public UserThread getCertainUser(String userName) {
		
		for(UserThread userThread : onlineUsers) {
			
			if (userThread.getName().equals(userName)) {
				return userThread;
			}
		}
		
		return null;
	}
}
