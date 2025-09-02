package com.triton.msa.triton_dashboard.common.advice;

import com.triton.msa.triton_dashboard.monitoring.exception.YamlFileException;
import com.triton.msa.triton_dashboard.private_data.exception.PrivateDataDeleteException;
import com.triton.msa.triton_dashboard.private_data.exception.ZipSlipException;
import com.triton.msa.triton_dashboard.user.dto.ApiKeyValidationResponseDto;
import com.triton.msa.triton_dashboard.user.exception.ApiKeysValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PrivateDataDeleteException.class)
    public String handleElasticsearchDeleteException(PrivateDataDeleteException e,
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

    @ExceptionHandler(ApiKeysValidationException.class)
    public ModelAndView handleApiKeysValidationException(ApiKeysValidationException ex) {
        Map<String, Object> viewResults = ex.getResults().results();

        viewResults.forEach((provider, v) -> {
            if (v instanceof Exception e) {
                viewResults.put(provider, "error: " + formatApiKeyErrorMessage(e));
            } else {
                viewResults.put(provider, v);
            }
        });

        ModelAndView mv = new ModelAndView("register");
        mv.addObject("user", ex.getUserInput());
        mv.addObject("validation", new ApiKeyValidationResponseDto(viewResults));
        mv.addObject("errorMessage", "검증에 실패한 API 키가 있습니다.");

        return mv;
    }

    @ExceptionHandler(YamlFileException.class)
    public String handleYamlFileException(YamlFileException e,
                                          HttpServletRequest request,
                                          RedirectAttributes redirectAttributes) {
        String projectId = extractProjectId(request.getRequestURI());
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:/projects/" + projectId + "/monitoring";
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

    private String formatApiKeyErrorMessage(Exception e) {
        if (e instanceof WebClientResponseException w) {
            int s = w.getRawStatusCode();
            String body = w.getResponseBodyAsString();

            if (s == 401) return "API 키가 유효하지 않습니다.";
            if (s == 429) return "요청이 너무 많습니다(429). 잠시 후 다시 시도해주세요.";
            if (body != null && body.contains("insufficient_quota")) return "API 사용 할당량이 초과되었습니다.";

            return "알 수 없는 오류: HTTP " + s;
        }

        String msg = String.valueOf(e.getMessage()).toLowerCase();

        if (msg.contains("timeout")) return "검증 요청이 시간 초과되었습니다.";
        if (msg.contains("connection") || msg.contains("i/o")) return "네트워크 오류가 발생했습니다.";

        return "알 수 없는 오류가 발생했습니다.";
    }
}
