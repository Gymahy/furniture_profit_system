package server;

import config.Config;
import server.thread.ClientThread;
import services.UserService;
import utility.DBConnection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerApp {
    public static void main(String[] args) {
        int port = Config.getIntProperty("server.port", 8081);
        int poolSize = Config.getIntProperty("server.thread.pool.size", 10);
        ExecutorService executorService = Executors.newFixedThreadPool(poolSize);

        try {
            new UserService().bootstrapAdmin();
        } catch (Exception e) {
            System.err.println("Bootstrap failed: " + e.getMessage());
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            executorService.shutdownNow();
            DBConnection.closeDataSource();
        }));

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Furniture profit server started on port " + port);
            while (!executorService.isShutdown()) {
                Socket socket = serverSocket.accept();
                executorService.submit(new ClientThread(socket));
            }
        } catch (IOException e) {
            System.err.println("Server stopped: " + e.getMessage());
        }
    }
}
