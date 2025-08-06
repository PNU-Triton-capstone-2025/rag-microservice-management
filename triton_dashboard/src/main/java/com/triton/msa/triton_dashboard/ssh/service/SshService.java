package com.triton.msa.triton_dashboard.ssh.service;

import com.triton.msa.triton_dashboard.ssh.entity.SshInfo;
import com.triton.msa.triton_dashboard.ssh.exception.SshAuthenticationException;
import com.triton.msa.triton_dashboard.ssh.exception.SshKeyFileException;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.keyprovider.FileKeyPairProvider;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.KeyPair;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SshService {

    private static final long CONNECT_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(10);
    private static final long AUTH_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(10);
    private final SshClient client;

    public SshService() {
        this.client = SshClient.setUpDefaultClient();
        this.client.start();
    }

    public boolean testConnection(SshInfo sshInfo) {
        if (sshInfo == null || sshInfo.getSshIpAddress() == null || sshInfo.getSshAuthKey() == null) {
            return false;
        }

        Path tempKeyFile = createTempKeyFile(sshInfo.getSshAuthKey());

        FileKeyPairProvider keyPairProvider = new FileKeyPairProvider(tempKeyFile);
        KeyPair keyPair = keyPairProvider.loadKeys(null).iterator().next();

        try(ClientSession session = getClientSession(sshInfo)) {

            session.addPublicKeyIdentity(keyPair);
            session.auth().verify(AUTH_TIMEOUT_MS);
            deleteTempKeyFile(tempKeyFile);
            return session.isAuthenticated();
        }
        catch (IOException ex) {
            throw new RuntimeException("세션 닫기 실패", ex);
        }
    }

    private ClientSession getClientSession(SshInfo sshInfo) {
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
                throw new SshAuthenticationException("SSH 연결 실패: " + ex.getMessage());
            }
        }
    }

    private Path createTempKeyFile(String privateKey) {
        Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rw-------");
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);

        try {
            Path tempFile = Files.createTempFile("ssh-key-", ".pem", attr);

            try (FileOutputStream fos = new FileOutputStream(tempFile.toFile())) {
                fos.write(privateKey.getBytes());
            }

            tempFile.toFile().deleteOnExit();

            return tempFile;
        }
        catch (IOException ex) {
            throw new SshKeyFileException("SSH 임시 키 파일 생성 실패");
        }
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
