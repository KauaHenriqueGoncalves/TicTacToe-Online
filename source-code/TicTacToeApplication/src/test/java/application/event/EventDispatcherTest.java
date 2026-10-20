package application.event;

import com.tic.tac.toe.application.event.*;
import com.tic.tac.toe.domain.event.GlobalMessageEvent;
import com.tic.tac.toe.domain.event.InfoRoomEvent;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EventDispatcherTest {

    @Test
    void dispatchCallsRegisteredHandler() {
        EventDispatcher dispatcher = new EventDispatcher();
        List<GlobalMessageEvent> received = new ArrayList<>();
        dispatcher.register(GlobalMessageEvent.class, received::add);

        GlobalMessageEvent event = new GlobalMessageEvent(UUID.randomUUID(), "oi");
        dispatcher.dispatch(event);
        dispatcher.publish(event);

        assertEquals(2, received.size());
        assertSame(event, received.get(0));
    }

    @Test
    void dispatchWithoutHandlerThrows() {
        EventDispatcher dispatcher = new EventDispatcher();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> dispatcher.publish(new InfoRoomEvent(UUID.randomUUID(), UUID.randomUUID())));
        assertTrue(ex.getMessage().contains("InfoRoomEvent"));
    }

    @Test
    void registerReplacesPreviousHandler() {
        EventDispatcher dispatcher = new EventDispatcher();
        List<String> calls = new ArrayList<>();
        dispatcher.register(GlobalMessageEvent.class, e -> calls.add("primeiro"));
        dispatcher.register(GlobalMessageEvent.class, e -> calls.add("segundo"));
        dispatcher.dispatch(new GlobalMessageEvent(UUID.randomUUID(), "x"));
        assertEquals(1, calls.size());
        assertEquals("segundo", calls.get(0));
    }
}
