package files.parser;

import chat.server.UserThread;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UsersInfoFileParser {

	
	private File usersFile;

	// The key in the map is the user name and the value is the password
	private Map<String, String> usersInfo = new ConcurrentHashMap<>();

	private PrintWriter writeInUserFile;

	private BufferedReader readFromUserFile;

	private boolean noNetwork = false;

	public UsersInfoFileParser() {

		usersFile = new File("__usersFile.txt");
		openFile("__usersFile.txt");
		setReadAndWrite();
		readUserInfoFile();
	}

	public UsersInfoFileParser(String name) {
		
		openFile(name);
		setReadAndWrite();
		readUserInfoFile();
		
		noNetwork = true;
	}
	
	private void setReadAndWrite() {
		try {

			FileWriter fileWriter = new FileWriter(usersFile, true);

			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

			writeInUserFile = new PrintWriter(bufferedWriter, true);

			FileReader fileReader = new FileReader(usersFile);

			readFromUserFile = new BufferedReader(fileReader);
			
		} catch (IOException e) {
			e.getStackTrace();
		}

	}

	private void openFile(String name) {

		usersFile = new File(name);

		if (!usersFile.exists()) {

			try {
				usersFile.createNewFile();

			} catch (IOException e) {

				System.out.println("Problem creating the new userFile in UsersInfoFileParser class!");
				e.printStackTrace();
			}
		}
		
	}

	private String readUsersFile() {

		int length = (int) usersFile.length();

		if (length == 0) {
			return null;
		}

		char[] buff = new char[length];

		String result = null;

		try {

			readFromUserFile.read(buff, 0, length);

			result = String.copyValueOf(buff);

			return result;

		} catch (IOException e) {
			System.out.println("Problem opening the FileReader in UsersInfoFileParser class!");
			e.getStackTrace();
		}

		return result;
	}
	

	private void readUserInfoFile() {

		String allInfo = readUsersFile();

		if (allInfo == null || allInfo.length() == 0) {
			return;
		}

		String[] users = allInfo.split("\n");

		for (String user : users) {

			// The format of the information in my file is:
			// <user name> : <password> so I get it that way

			String[] separateInfo = user.split(":");

			System.out.println("Name: " + separateInfo[0]);
			System.out.println("Passwd: " + separateInfo[1]);

			usersInfo.putIfAbsent(separateInfo[0], separateInfo[1]);
		}
	}

	
	
	private boolean checkPassword(UserThread user) {

		if (noNetwork == true) {

			return usersInfo.get(user.getName()).trim().equals("1234");
		}

		user.sendWithNewLine("Enter your password: ");

		String passwd = user.getMessage();
		if (passwd == null) {
			System.out.println("Problem with getting the password!");
			return false;
		}

		while (!usersInfo.get(user.getName()).trim().equals(passwd)) {

			user.sendWithNewLine("Your passwrod is not correct!");

			// The same prompt "Enter your password" will be displayed.

			passwd = user.getMessage();
		}

		user.sendWithNewLine("You have successfully logged into the chat program!");

		return true;
	}

	
	private void registerUser(UserThread user) {

		user.sendWithNewLine("You are new to the system. You will be register. Enter your password: ");

		String passwd = user.getMessage();

		if (passwd != null) {

			// This operation is thread safe.
			usersInfo.putIfAbsent(user.getName(), passwd);

			user.sendWithNewLine("You have successfully register into the chat program!");

			//String data = user.getName() + ":" + passwd;
			//System.out.println("Data to be put: " + data);

			synchronized (writeInUserFile) {
				writeInUserFile.println(user.getName() + ":" + passwd);
				writeInUserFile.flush();
			}

		} else {
			user.sendWithNewLine("Your password is null!");
		}
	}

	
	// Here if the user is already in the system he will enter his password
	// and verification check will be done with his password.
	// Otherwise he will be register with the new password he gives.

	public boolean userEntry(UserThread user) {

		if (user == null) {
			return false;
		}

		if (usersInfo.isEmpty() || !usersInfo.containsKey(user.getName())) {
			registerUser(user);
		} 
		else {
			return checkPassword(user);
		}

		return true;
	}

}

