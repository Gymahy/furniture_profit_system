package server;

import enums.RequestType;
import enums.ResponseStatus;
import enums.RoleType;
import models.dto.LoginDTO;
import models.entities.User;
import models.tcp.Request;
import models.tcp.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import server.thread.ClientThread;
import support.TestDatabaseSupport;
import utility.PasswordHasher;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@Tag("integration")
class ClientThreadIntegrationTest {
    private Integer userId;

    @AfterEach
    void cleanup() throws Exception {
        if (userId != null) {
            try (Connection connection = TestDatabaseSupport.connection()) {
                TestDatabaseSupport.deleteUser(connection, userId);
            }
        }
    }

    @Test
    void authenticatesOverSocket() throws Exception {
        String login = TestDatabaseSupport.unique("socket_user");
        try (Connection connection = TestDatabaseSupport.connection()) {
            userId = insertSocketUser(connection, login);
        }

        try (ServerSocket serverSocket = new ServerSocket(0);
             ExecutorService executor = Executors.newSingleThreadExecutor()) {
            Future<?> server = executor.submit(() -> {
                try (Socket socket = serverSocket.accept()) {
                    new ClientThread(socket).run();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            try (Socket client = new Socket("localhost", serverSocket.getLocalPort());
                 ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(client.getInputStream())) {
                out.writeObject(new Request(RequestType.LOGIN, new LoginDTO(login, "socketPassword123")));
                out.flush();

                Response response = (Response) in.readObject();

                assertEquals(ResponseStatus.OK, response.getStatus());
                assertInstanceOf(User.class, response.getData());
                assertEquals(login, ((User) response.getData()).getLogin());
            }

            server.get();
            executor.shutdownNow();
        }
    }

    private int insertSocketUser(Connection connection, String login) throws Exception {
        int roleId = TestDatabaseSupport.ensureRole(connection, RoleType.ACCOUNTANT);
        try (var statement = connection.prepareStatement("""
                INSERT INTO users(login, password_hash, full_name, role_id, is_active)
                VALUES (?, ?, ?, ?, TRUE)
                """, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, login);
            statement.setString(2, PasswordHasher.hash("socketPassword123"));
            statement.setString(3, "Socket User");
            statement.setInt(4, roleId);
            statement.executeUpdate();
            try (var keys = statement.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }
}
