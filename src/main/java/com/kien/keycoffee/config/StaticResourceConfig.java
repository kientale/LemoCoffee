package com.kien.keycoffee.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    private static final String DRINK_IMAGE_UPLOAD_DIR = "src/main/resources/static/img/drink";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/img/drink/**")
                .addResourceLocations(getDrinkImageLocation())
                .setCacheControl(CacheControl.noStore());
    }

    private String getDrinkImageLocation() {
        Path uploadPath = Paths.get(DRINK_IMAGE_UPLOAD_DIR).toAbsolutePath().normalize();
        String location = uploadPath.toUri().toString();
        return location.endsWith("/") ? location : location + "/";
    }
}
