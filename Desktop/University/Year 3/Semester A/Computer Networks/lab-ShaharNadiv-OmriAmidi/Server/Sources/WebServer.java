import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebServer {

    private static ServerSocket serverSocket;
    private static ExecutorService threadPool; // thread pool

    public static void main(String[] args) {
        try {
            int port = ConfigReader.getPort("config.ini");
            int maxThreads = ConfigReader.getMaxThreads("config.ini"); // Read MaxThreads
            serverSocket = new ServerSocket(port);
            threadPool = Executors.newFixedThreadPool(maxThreads); // Initialize the thread pool
            
            System.out.println("Server is listening on port " + port);

            String rootDirectory = ConfigReader.getRootDirectory();

            // Add shutdown hook to close server socket on shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(WebServer::shutdown));

            // Main loop to accept incoming connections
            while (!serverSocket.isClosed()) {
                try {
                    
                    Socket clientSocket = serverSocket.accept();
                    threadPool.execute(new ClientHandler(clientSocket, rootDirectory)); // Use thread pool
                } catch (IOException e) {
                    if (serverSocket.isClosed()) {
                        System.out.println("Server socket closed.");
                    } else {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void shutdown() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            if (threadPool != null && !threadPool.isShutdown()) {
                threadPool.shutdown(); // Shutdown thread pool
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Server shutdown.");
    }
}