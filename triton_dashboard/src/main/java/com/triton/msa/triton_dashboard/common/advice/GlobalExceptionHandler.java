package com.triton.msa.triton_dashboard.common.advice;

import com.triton.msa.triton_dashboard.private_data.exception.ElasticsearchDeleteException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ElasticsearchDeleteException.class)
    public String handleElasticsearchDeleteException(ElasticsearchDeleteException e,
                                                     HttpServletRequest request,
                                                     RedirectAttributes redirectAttributes) {
        String uri = request.getRequestURI();
        String projectId = "1";

        String[] parts = uri.split("/");
        for (int i = 0; i < parts.length; i++) {
            if ("projects".equals(parts[i]) && i + 1 < parts.length) {
                projectId = parts[i + 1];
                break;
            }
        }

        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage()); //
        return "redirect:/projects/" + projectId + "/private-data";
    }
}
