package com.triton.msa.triton_dashboard.ssh.service;

import com.triton.msa.triton_dashboard.ssh.entity.SshInfo;
import com.triton.msa.triton_dashboard.ssh.exception.SshAuthenticationException;
import com.triton.msa.triton_dashboard.ssh.exception.SshConnectionException;
import com.triton.msa.triton_dashboard.ssh.exception.SshKeyFileException;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelShell;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.keyprovider.FileKeyPairProvider;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.KeyPair;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SshService {

    private static final long CONNECT_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(10);
    private static final long AUTH_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(10);
    private final SshClient client;

    private final Map<String, SshConnectionDetails> activeSessions = new ConcurrentHashMap<>();

    public SshService() {
        this.client = SshClient.setUpDefaultClient();
        this.client.start();
    }

    public boolean testConnection(SshInfo sshInfo) {
        if (sshInfo == null || sshInfo.getSshIpAddress() == null || sshInfo.getSshAuthKey() == null) {
            return false;
        }

        Path tempKeyFile = null;
        try {
            tempKeyFile = createTempKeyFile(sshInfo.getSshAuthKey());

            FileKeyPairProvider keyPairProvider = new FileKeyPairProvider(tempKeyFile);
            KeyPair keyPair = loadKeyPair(keyPairProvider);

            return verifyClientSession(sshInfo, keyPair);
        } finally {
            deleteTempKeyFile(tempKeyFile);
        }
    }

    public String startSshSession(SshInfo sshInfo) {
        Path tempKeyFile = createTempKeyFile(sshInfo.getSshAuthKey());
        ClientSession session = null;

        try {
            FileKeyPairProvider keyPairProvider = new FileKeyPairProvider(tempKeyFile);
            KeyPair keyPair = loadKeyPair(keyPairProvider);

            session = connectClientSession(sshInfo);
            session.addPublicKeyIdentity(keyPair);
            session.auth().verify(AUTH_TIMEOUT_MS);

            if(!session.isAuthenticated()) {
                throw new SshAuthenticationException("SSH 인증에 실패했습니다. 자격 증명과 서버 설정을 확인해주세요.");
            }

            ChannelShell channelShell = session.createShellChannel();
            channelShell.setupSensibleDefaultPty();

            PipedOutputStream ptyOut = new PipedOutputStream();
            PipedInputStream ptyIn = new PipedInputStream(ptyOut);

            PipedInputStream shellOut = new PipedInputStream();
            PipedOutputStream shellIn = new PipedOutputStream(shellOut);

            channelShell.setIn(ptyIn);
            channelShell.setOut(shellIn);
            channelShell.setErr(shellIn);

            channelShell.open().verify(CONNECT_TIMEOUT_MS);

            String sessionId = UUID.randomUUID().toString();
            SshConnectionDetails details = new SshConnectionDetails(session, channelShell, ptyOut, shellOut, tempKeyFile);
            activeSessions.put(sessionId, details);

            log.info("SSH session started with ID: {}", sessionId);
            return sessionId;
        }
        catch (SshAuthenticationException | SshConnectionException ex) {
            cleanupOnFailure(session, tempKeyFile);
            throw ex;
        }
        catch (Exception ex) {
            cleanupOnFailure(session, tempKeyFile);
            throw new SshConnectionException("SSH 세션 시작 중 오류 발생: " + ex.getMessage());
        }
    }

    public void sendCommand(String sessionId, String command) {
        SshConnectionDetails details = activeSessions.get(sessionId);

        if(details != null && details.channelShell().isOpen()) {
            try {
                details.outputStream().write(command.getBytes());
                details.outputStream().flush();
            }
            catch (IOException ex) {
                log.error("SSH 세션 {}에 명령어 전송 실패", sessionId, ex);
                closeSshSession(sessionId);
            }
        }
    }

    public void closeSshSession(String sessionId) {
        SshConnectionDetails details = activeSessions.remove(sessionId);

        if (details != null) {
            log.info("Closing SSH session: {}", sessionId);

            details.channelShell().close(true);
            details.session().close(true);

            deleteTempKeyFile(details.tempKeyPath());
        }
    }

    public SshConnectionDetails findSessionDetails(String sessionId) {
        return activeSessions.get(sessionId);
    }

    private Path createTempKeyFile(String privateKey) {
        Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rw-------");
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);

        return createTempKeyFileTemplate(privateKey, attr);
    }

    private Path createTempKeyFileTemplate(String privateKey, FileAttribute<Set<PosixFilePermission>> attribute) {
        try {
            Path tempFile = Files.createTempFile("ssh-key-", ".pem", attribute);

            try (FileOutputStream fos = new FileOutputStream(tempFile.toFile())) {
                fos.write(privateKey.getBytes());
            }
            return tempFile;
        }
        catch (IOException ex) {
            throw new SshKeyFileException("SSH 임시 키 파일 생성 실패");
        }
    }

    private KeyPair loadKeyPair(FileKeyPairProvider keyPairProvider) {
        try {
            return keyPairProvider.loadKeys(null).iterator().next();
        }
        catch (Exception ex) {
            throw new SshAuthenticationException("제공된 SSH 키가 유효하지 않거나 손상되었습니다.", ex);
        }
    }

    private boolean verifyClientSession(SshInfo sshInfo, KeyPair keyPair) {
        try (ClientSession session = connectClientSession(sshInfo)) {
            session.addPublicKeyIdentity(keyPair);
            session.auth().verify(AUTH_TIMEOUT_MS);
            return session.isAuthenticated();
        } catch (Exception ex) {
            throw new SshAuthenticationException("SSH 인증에 실패했습니다. 자격 증명과 서버 설정을 확인해주세요", ex);
        }
    }

    private ClientSession connectClientSession(SshInfo sshInfo) {
        try{
            return client.connect(sshInfo.getHostname(), sshInfo.getSshIpAddress(), sshInfo.getPort())
                    .verify(CONNECT_TIMEOUT_MS).getSession();
        }
        catch (IOException ex) {
            String msg = ex.getMessage().toLowerCase();
            if(msg.contains("auth") || msg.contains("permission")) {
                throw new SshAuthenticationException("SSH 인증 실패: " + ex.getMessage());
            }
            else{
                throw new SshConnectionException("SSH 연결 실패: " + ex.getMessage());
            }
        }
    }

    private void cleanupOnFailure(ClientSession session, Path tempKeyFile) {
        if (session != null) {
            try {
                session.close();
            }
            catch (IOException e) {
                log.error("Error closing SSH session during cleanup", e);
            }
        }
        deleteTempKeyFile(tempKeyFile);
    }

    private void deleteTempKeyFile(Path path) {
        try{
            if(path != null) {
                Files.deleteIfExists(path);
            }
        }
        catch (IOException e) {
            log.error("Failed to delete temporary SSH key file: {}", path, e);
        }
    }

    @PreDestroy
    public void stop() {
        if (this.client != null) {
            this.client.stop();
        }
    }
}
