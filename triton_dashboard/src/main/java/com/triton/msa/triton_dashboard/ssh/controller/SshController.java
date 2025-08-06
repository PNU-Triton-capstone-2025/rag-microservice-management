package com.triton.msa.triton_dashboard.ssh.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ssh")
public class SshController {

    @GetMapping("/connect/{projectId}")
    public String sshConnectUI(@PathVariable String projectId, Model model) {
        model.addAttribute("projectId", projectId);

        return "ssh/ssh-client";
    }
}
