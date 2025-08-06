package com.triton.msa.triton_dashboard.ssh.service;

import com.triton.msa.triton_dashboard.ssh.entity.SshInfo;
import com.triton.msa.triton_dashboard.ssh.exception.SshAuthenticationException;
import com.triton.msa.triton_dashboard.ssh.exception.SshConnectionException;
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
        try (ClientSession session = getClientSession(sshInfo)) {
            session.addPublicKeyIdentity(keyPair);
            session.auth().verify(AUTH_TIMEOUT_MS);
            return session.isAuthenticated();
        } catch (Exception ex) {
            throw new SshAuthenticationException("SSH 인증에 실패했습니다. 자격 증명과 서버 설정을 확인해주세요", ex);
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
                throw new SshConnectionException("SSH 연결 실패: " + ex.getMessage());
            }
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
