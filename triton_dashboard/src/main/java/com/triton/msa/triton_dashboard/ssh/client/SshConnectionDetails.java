package com.triton.msa.triton_dashboard.ssh.client;

import org.apache.sshd.client.channel.ChannelShell;
import org.apache.sshd.client.session.ClientSession;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.Path;

public record SshConnectionDetails(
        ClientSession session,
        ChannelShell channelShell,
        PipedOutputStream ptyOut,
        PipedInputStream shellOut,
        Path tempKeyPath
) {
}
