package de.unistuttgart.towercrushbackend.controller.components;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

import java.util.ArrayList;
import java.util.Map;

import static org.springframework.messaging.support.NativeMessageHeaderAccessor.NATIVE_HEADERS;

public class UserInterceptor implements ChannelInterceptor {

    /**
     * This method saves the transmitted user UUID in the subscription.
     *
     * @param message websocket message
     * @param channel
     * @return
     */
    @Override
    public Message<?> preSend(final Message<?> message, final MessageChannel channel) {
        final StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            final Object raw = message.getHeaders().get(NATIVE_HEADERS);

            if (raw instanceof Map) {
                final Object name = ((Map<?, ?>) raw).get("userUUID");

                if (name instanceof ArrayList) {
                    accessor.setUser(new User(((ArrayList<String>) name).get(0)));
                }
            }
        }
        return message;
    }
}
