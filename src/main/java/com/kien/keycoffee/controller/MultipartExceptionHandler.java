package com.kien.keycoffee.controller;

import com.kien.keycoffee.constant.DrinkManagementResult;
import com.kien.keycoffee.constant.DrinkValidationResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class MultipartExceptionHandler {

    @ExceptionHandler({MaxUploadSizeExceededException.class, MultipartException.class})
    public String handleMultipartException(
            Exception exception,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) throws Exception {
        String requestUri = request.getRequestURI();
        if (requestUri == null || !requestUri.contains("/drink-management")) {
            throw exception;
        }

        redirectAttributes.addFlashAttribute("status", DrinkManagementResult.IMAGE_SAVE_FAILED);
        redirectAttributes.addFlashAttribute("message", DrinkValidationResult.INVALID_IMAGE.getMessage());

        return "redirect:/drink-management";
    }
}
