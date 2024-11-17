package isd;
import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParallelFileServer {
    private static final int PORT = 8080;
    private static final String FILE_DIRECTORY = "server_files"; // Directory where files are stored
    private static final int THREAD_POOL_SIZE = 10; // Number of threads in the thread pool

    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT + "...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());
                threadPool.execute(new FileHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
    }

    static class FileHandler implements Runnable {
        private Socket clientSocket;

        public FileHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true)) {

                output.println("Welcome to the Parallel File Server! Enter the filename to download:");
                String fileName = input.readLine();
                File file = new File(FILE_DIRECTORY, fileName);

                if (file.exists() && !file.isDirectory()) {
                    output.println("File found. Preparing to send...");
                    sendFile(file, clientSocket);
                } else {
                    output.println("File not found.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void sendFile(File file, Socket clientSocket) throws IOException {
            try (BufferedInputStream fileInput = new BufferedInputStream(new FileInputStream(file));
                 OutputStream socketOutput = clientSocket.getOutputStream()) {
                 
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fileInput.read(buffer)) != -1) {
                    socketOutput.write(buffer, 0, bytesRead);
                }
                socketOutput.flush();
                System.out.println("File " + file.getName() + " sent to " + clientSocket.getInetAddress());
            }
        }
    }
}
