package com.triton.msa.triton_dashboard.ssh.handler;

import com.triton.msa.triton_dashboard.ssh.client.SshConnectionDetails;
import com.triton.msa.triton_dashboard.ssh.client.SshServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class SshWebSocketHandler extends TextWebSocketHandler {

    private final SshServiceClient sshService;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public SshWebSocketHandler(SshServiceClient sshService) {
        this.sshService = sshService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String sessionId = extractSessionId(session);
        SshConnectionDetails details = sshService.findSessionDetails(sessionId);

        if(details == null) {
            log.warn("WebSocket 연결 시도, 유효하지 않은 세션 ID: {}", sessionId);
            closeSessionWithError(session, "유효하지 않은 세션 ID입니다.");
            return;
        }

        executorService.submit(() -> {
            try{
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = details.shellOut().read(buffer)) != -1 && session.isOpen()) {
                    session.sendMessage(new TextMessage(new String(buffer, 0, bytesRead)));
                }
            }
            catch (IOException e) {
                if(!"Pipe closed".equalsIgnoreCase(e.getMessage())) {
                    log.error("SSH 출력 스트림 읽기 오류 (세션 ID: {}): {}", sessionId, e.getMessage());
                }
            }
            finally {
                sshService.closeSshSession(sessionId);
            }
        });
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String sessionId = extractSessionId(session);
        sshService.sendCommand(sessionId, message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sessionId = extractSessionId(session);
        sshService.closeSshSession(sessionId);
        log.info("WebSocket 연결 종료 (세션 ID: {}), 상태: {}", sessionId, status);
    }

    private String extractSessionId(WebSocketSession session) {
        String path = Objects.requireNonNull(session.getUri()).getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    private void closeSessionWithError(WebSocketSession session, String reason) {
        try {
            session.close(CloseStatus.SERVER_ERROR.withReason(reason));
        }
        catch (IOException ex) {
            log.error("에러 발생 후 WebSocket 세션 닫기 실패", ex);
        }
    }
}
