package isd;

import java.io.*;
import java.net.*;

public class FileClient {
	private static final String SERVER_ADDRESS = "localhost";
	private static final int SERVER_PORT = 8080;
	private static final String DOWNLOAD_DIRECTORY = "client_files"; // Directory to save downloaded files

	public static void main(String[] args) {
		try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
				BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter output = new PrintWriter(socket.getOutputStream(), true)) {

			while (true) {

				System.out.println(input.readLine()); // Welcome message
				System.out.print("Enter filename to download: ");
				BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));
				String fileName = consoleInput.readLine();
				output.println(fileName);

				String serverResponse = input.readLine();
				System.out.println(serverResponse);

				if (serverResponse.startsWith("File found")) {
					saveFile(socket, fileName);
					System.out.println("File " + fileName + " downloaded successfully.");
				} else {
					System.out.println("File not found on the server.");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void saveFile(Socket socket, String fileName) throws IOException {
		File downloadDir = new File(DOWNLOAD_DIRECTORY);
		if (!downloadDir.exists()) {
			downloadDir.mkdir();
		}

		try (BufferedInputStream socketInput = new BufferedInputStream(socket.getInputStream());
				FileOutputStream fileOutput = new FileOutputStream(new File(downloadDir, fileName))) {

			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = socketInput.read(buffer)) != -1) {
				fileOutput.write(buffer, 0, bytesRead);
			}
		}
	}
}
