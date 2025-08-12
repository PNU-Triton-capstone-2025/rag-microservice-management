package com.triton.msa.triton_dashboard.common.advice;

import com.triton.msa.triton_dashboard.private_data.exception.ElasticsearchDeleteException;
import com.triton.msa.triton_dashboard.private_data.exception.ZipSlipException;
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

        String projectId = extractProjectId(request.getRequestURI());
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

        return "redirect:/projects/" + projectId + "/private-data";
    }

    @ExceptionHandler(ZipSlipException.class)
    public String handleZipSlipException(ZipSlipException e,
                                         HttpServletRequest request,
                                         RedirectAttributes redirectAttributes) {

        String projectId = extractProjectId(request.getRequestURI());
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

        return "redirect:/projects/" + projectId + "/private-data";
    }


    private String extractProjectId(String uri) {
        String[] parts = uri.split("/");
        for (int i = 0; i < parts.length; i++) {
            if ("projects".equals(parts[i]) && i + 1 < parts.length) {
                return parts[i + 1];
            }
        }

        return "1";
    }

}
