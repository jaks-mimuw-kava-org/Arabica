package org.kava.arabica.async;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest {
    @Test
    @DisplayName("Test buffers getters")
    public void testBufferGetters() {
        var buffer = new CyclicBuffer(12);
        var client = new Client(12) {
            @Override
            protected CyclicBuffer getBuffer(int size) {
                return buffer;
            }
        };

        assertEquals(buffer, client.getInput());
        assertEquals(buffer, client.getOutput());
    }

    @Test
    @DisplayName("Test flags setters")
    public void testFlags() {
        var client = new Client(12);

        assertFalse(client.isHandled());
        assertFalse(client.isRegistered());
        assertFalse(client.sentToBeHandled());

        client.setHandled(true);
        client.setRegistered(true);
        client.setSentToBeHandled(true);

        assertTrue(client.isHandled());
        assertTrue(client.isRegistered());
        assertTrue(client.sentToBeHandled());
    }
}
