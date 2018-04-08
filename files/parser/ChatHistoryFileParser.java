package files.parser;

import java.io.BufferedWriter;

import java.io.File;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ChatHistoryFileParser {

	private String fileName;
	
	private PrintWriter fileWriter;
	
	private String chatHistory;
		
	public ChatHistoryFileParser(String roomName) {
		
			fileName = roomName + ".txt";	
			chatHistory = new String();
			
			if(Files.exists(Paths.get(fileName))) {
				
				readChatHistoryFile(); 
				
				System.out.println("Yess the file doesn't exists!");
			}
			else {
				
				System.out.println("Yess the file doesn't exists!");	
				
				chatHistory = null;
			}
			
			
			try {
				FileWriter writer = new FileWriter(fileName, true);
				BufferedWriter bufferedWriter = new BufferedWriter(writer);
				
				fileWriter = new PrintWriter(bufferedWriter, true);
				
			} catch (IOException e) {
				
				e.printStackTrace();
			} 
			
	}
	
	
	private void readChatHistoryFile() {
		
		Path path = Paths.get(fileName);

		String history = null;
		try {
			
			if (Files.size(Paths.get(fileName)) != 0) {
				history = new String(Files.readAllBytes(path));
			}
			
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		
		chatHistory = history;
	}

	
	public String getChatHistory() {
		return chatHistory;
	}
	
	
	public void addMoreHistory(String newLine) {
		
		if(chatHistory == null) {
			chatHistory = new String();
		}
		chatHistory += newLine;
		
		fileWriter.println(newLine);

	}
}
