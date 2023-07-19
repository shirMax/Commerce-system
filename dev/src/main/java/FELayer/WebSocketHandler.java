package FELayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import util.Records.NotificationRecord;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketHandler extends TextWebSocketHandler {

    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Called when a new WebSocket connection is established
        String sessionId = getSessionId(session); // Function to retrieve sessionId from cookie
        sessions.put(sessionId, session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session,  CloseStatus status) throws Exception {
        // Called when a WebSocket connection is closed
        String sessionId = getSessionId(session); // Function to retrieve sessionId from cookie
        sessions.remove(sessionId);
    }

    public static boolean sendMessageToClient(String sessionId, NotificationRecord notificationRecord) {
        WebSocketSession session = sessions.get(sessionId);
        if (session != null && session.isOpen()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();

                // Format the sendingTime as a string
                String formattedSendingTime = notificationRecord.sendingTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                // Construct a JSON object with the message and additional data
                Map<String, Object> payload = new HashMap<>();
                payload.put("message", notificationRecord.message());
                payload.put("sendingTime", formattedSendingTime);
                payload.put("sender", notificationRecord.sender());
                String payloadString = objectMapper.writeValueAsString(payload);

                session.sendMessage(new TextMessage(payloadString));
                return true;
            } catch (IOException ignored) {
            }
        }
        return false;
    }

    private String getSessionId(WebSocketSession session) {
        Map<String, Object> attributes = session.getAttributes();
        HttpServletRequest request = (HttpServletRequest) attributes.get("HTTP_REQUEST");

        if (request != null) {
            Cookie[] cookies = (Cookie[]) session.getAttributes().get("HTTP_COOKIES");
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("sessionId".equals(cookie.getName())) {
                        return cookie.getValue();
                    }
                }
            }
        }

        return null;
    }
}
