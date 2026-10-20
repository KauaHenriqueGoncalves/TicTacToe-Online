package presentation.socket.event.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tic.tac.toe.domain.entity.User;
import com.tic.tac.toe.presentation.socket.connection.Connection;
import com.tic.tac.toe.presentation.socket.connection.ConnectionManager;
import com.tic.tac.toe.presentation.socket.room.RoomManager;
import org.java_websocket.WebSocket;
import org.mockito.ArgumentCaptor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Utilitarios compartilhados pelos testes dos handlers de WebSocket.
 */
final class HandlerTestSupport {
    private HandlerTestSupport() {
    }

    static ObjectMapper mapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    /** ObjectMapper que falha ao serializar, para cobrir os blocos catch(JsonProcessingException). */
    static ObjectMapper brokenMapper() throws JsonProcessingException {
        ObjectMapper broken = mock(ObjectMapper.class);
        when(broken.writeValueAsString(any())).thenThrow(new JsonProcessingException("falha json") {
        });
        return broken;
    }

    static User user(String name) {
        return new User(UUID.randomUUID(), name, name + "@mail.com", "hash", LocalDateTime.now());
    }

    /** Cria uma conexao com WebSocket mockado e ja registrada no ConnectionManager. */
    static Connection connect(ConnectionManager manager, UUID userId) {
        WebSocket ws = mock(WebSocket.class);
        when(ws.isOpen()).thenReturn(true);
        Connection connection = Connection.create(ws, userId);
        manager.add(connection);
        return connection;
    }

    /** Cria a sala em memoria e coloca a conexao dentro dela. */
    static void joinRoom(RoomManager roomManager, UUID roomId, Connection connection) {
        try {
            roomManager.get(roomId);
        } catch (RuntimeException notCreated) {
            roomManager.create(roomId);
        }
        roomManager.join(roomId, connection);
    }

    /** Todas as mensagens enviadas para o WebSocket da conexao. */
    static List<String> sent(Connection connection) {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(connection.getConnection(), atLeast(0)).send(captor.capture());
        return captor.getAllValues();
    }

    static boolean anySentContains(Connection connection, String text) {
        for (String message : sent(connection)) {
            if (message.contains(text)) return true;
        }
        return false;
    }
}
