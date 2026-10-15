package client.utility;

import config.Config;
import enums.ResponseStatus;
import models.entities.User;
import models.tcp.Request;
import models.tcp.Response;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public final class ClientSocket {
    private static ClientSocket instance;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private User currentUser;

    private ClientSocket() {
    }

    public static synchronized ClientSocket getInstance() {
        if (instance == null) {
            instance = new ClientSocket();
        }
        return instance;
    }

    public synchronized boolean connect() {
        String host = Config.getProperty("server.host", "localhost");
        int port = Config.getIntProperty("server.port", 8081);
        return connect(host, port);
    }

    public synchronized boolean connect(String host, int port) {
        disconnect();
        try {
            socket = new Socket(host, port);
            socket.setKeepAlive(true);
            socket.setTcpNoDelay(true);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            return true;
        } catch (Exception e) {
            disconnect();
            return false;
        }
    }

    public synchronized Response send(Request request) {
        if (!isConnected()) {
            return Response.error("Нет соединения с сервером");
        }
        try {
            out.writeObject(request);
            out.flush();
            out.reset();
            Object response = in.readObject();
            if (response instanceof Response typedResponse) {
                return typedResponse;
            }
            return Response.error("Сервер вернул некорректный ответ");
        } catch (Exception e) {
            disconnect();
            return new Response(ResponseStatus.ERROR, "Ошибка сети: " + e.getMessage(), null);
        }
    }

    public synchronized void disconnect() {
        try {
            if (in != null) {
                in.close();
            }
        } catch (Exception ignored) {
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (Exception ignored) {
        }
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (Exception ignored) {
        }
        in = null;
        out = null;
        socket = null;
        currentUser = null;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }
}
