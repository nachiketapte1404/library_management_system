package com.nachiket.library_management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class LibraryManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(LibraryManagementApplication.class, args);
	}

	@Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Match all endpoints
                        .allowedOrigins("*") // Allow your frontend port
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Explicitly allow PUT/DELETE
                        .allowedHeaders("*"); // Allow all headers
            }
        };
    }

}
