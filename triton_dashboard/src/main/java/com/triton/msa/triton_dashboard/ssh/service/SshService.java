package com.triton.msa.triton_dashboard.ssh.service;

import com.triton.msa.triton_dashboard.ssh.entity.SshInfo;
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
public class SshService {

    private static final long CONNECT_TIMEOUT = TimeUnit.SECONDS.toMillis(5);
    private static final long AUTH_TIMEOUT = TimeUnit.SECONDS.toMillis(5);

    public boolean testConnection(SshInfo sshInfo) {
        if (sshInfo == null || sshInfo.getSshIpAddress() == null || sshInfo.getSshAuthKey() == null) {
            return false;
        }

        try(SshClient client = SshClient.setUpDefaultClient()) {
            client.start();

            Path tempKeyFile = createTempKeyFile(sshInfo.getSshAuthKey());

            FileKeyPairProvider keyPairProvider = new FileKeyPairProvider(tempKeyFile);
            KeyPair keyPair = keyPairProvider.loadKeys(null).iterator().next();

            try (ClientSession session = client.connect(sshInfo.getHostname(), sshInfo.getSshIpAddress(), sshInfo.getPort())
                    .verify(CONNECT_TIMEOUT).getSession()) {
                session.addPublicKeyIdentity(keyPair);

                session.auth().verify(AUTH_TIMEOUT);

                boolean isAuthenticated = session.isAuthenticated();
                Files.delete(tempKeyFile);
                return isAuthenticated;
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private Path createTempKeyFile(String privateKey) throws IOException {
        Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rw-------");
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);

        Path tempFile = Files.createTempFile("ssh-key-", ".pem", attr);

        try(FileOutputStream fos = new FileOutputStream(tempFile.toFile())) {
            fos.write(privateKey.getBytes());
        }

        tempFile.toFile().deleteOnExit();

        return tempFile;
    }
}
