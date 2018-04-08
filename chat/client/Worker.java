package chat.client;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;


public class Worker implements Runnable {

	private BufferedReader bufferedReader;
	private PrintWriter printWriter;
	private ChatClient chatClient;
	private Socket socket;

	private Scanner stdIn;

	// I use this object for my needed features in workerThread

	private WorkerThreadFeatures features;

	// For initialisation of my worker

	private InitWorker initWorker;

	// I will use this variable for synchronisation for use of the bufferedReader
	// because if it used incorrectly it will block

	private boolean toStop = false;

	// I use that boolean value to know where to stop the threads

	private boolean logOut = false;

	public Worker(Socket socket, ChatClient chatClient) {

		this.chatClient = chatClient;
		this.socket = socket;

		try {

			InputStream inputStream = socket.getInputStream();
			bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

			OutputStream outputStream = socket.getOutputStream();
			printWriter = new PrintWriter(outputStream, true);

			stdIn = new Scanner(System.in);

			features = new WorkerThreadFeatures(bufferedReader, printWriter, chatClient, socket, stdIn);
			initWorker = new InitWorker(bufferedReader, printWriter, chatClient, socket, stdIn);

		} catch (SocketException e1) {

			System.out.println("Problem with setSoTimeout function in run method in Worker class!");

			e1.printStackTrace();
		} catch (IOException e) {

			System.out.println("Problem in the constructor of Worker class!");
			e.printStackTrace();

		} catch (Exception e) {
			closeSocket();
		}
	}

	private void closeSocket() {
		try {
			socket.close();
		} catch (IOException e) {

			System.out.println("Problem closing the socket!");
			e.printStackTrace();
		}
	}

	public void setUpUser() {

		initWorker.setName();

		initWorker.chooseRoom();
		
		System.out.println();
		
		printAllChatHistory();

		features.printAllUsersFromRoom();
		
		System.out.println();

	}
	
	private void printAllChatHistory() {
		
		try {
			String strSize = bufferedReader.readLine();
			
			int size = Integer.parseInt(strSize);
			
			if(size <= 0) {
				System.out.println("The room is new. There is no chat history. \n"); 
			}
			else {
				
				System.out.println("Chat room history: \n"); 
				
				String message = new String();
				
				for(int i = 0; i < size; ++i) {
					message = bufferedReader.readLine();
					System.out.println(message);
				}
				
				System.out.println();
			}
			
		}
		catch (IOException e) {

			e.getStackTrace();
		}
		
		
	}

	private void setChatRoom() {

		initWorker.chooseRoom();
	}

	private void specialCommands(String outMessage) {

		features.specialCommands(outMessage, initWorker);
	}	

	public void execRunStart() {

		Thread sendMessage = new Thread(new Runnable() {

			@Override
			public void run() {

				String outMessage = null;

				while (true) {

					if (chatClient.getChatRoom() == null) {

						toStop = true;
						setChatRoom();
						toStop = false;
						
						continue;

					}

//					if(toStop == true) {
//						continue;
//					}
					outMessage = stdIn.nextLine();

					while (outMessage.length() < 2) {

						System.out.println("The input must be at least 2 characters!");
						outMessage = stdIn.nextLine();
					}

					if (outMessage.charAt(0) == '>' && outMessage.charAt(1) == '>') {

						toStop = true;
						specialCommands(outMessage);
						
						toStop = false;

						continue;
					}

					// Print the message to the server so it can be send
					printWriter.println("[ " + chatClient.getUserName() + " ]: " + outMessage);

					if (outMessage.equals("bye")) {
						logOut = true;
						break;
					}

				}
			}
		});
		
		sendMessage.start();
	}

	
	public void execRecieveThread() {
	
		Thread receiveMessage = new Thread(new Runnable() {

			@Override
			public void run() {

				while (true) {

					if (logOut == true) {
						break;
					}

					if (chatClient.getChatRoom() == null) {
						continue;
					}

					try {

						if (toStop == false && bufferedReader.ready()) {

							// Reads a message from the server
							String inputMessage = bufferedReader.readLine();

							// and then prints it to all users
							System.out.println(inputMessage);

						}

					} catch (IOException e) {

						System.out.println("Problem with bufferedReader in receiveMessage thread!");
						e.printStackTrace();
						break;

					}
				}

			}
		});
		
		receiveMessage.start();
	}
	
	
	
	
	@Override
	public void run() {

		setUpUser();
		
		execRunStart();
		
		execRecieveThread();

	}


}
