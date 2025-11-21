package io.github.jaredmcc4.gtm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir}")
    private String uploadDir;

    /**
     * Expone el directorio de cargas locales bajo el prefijo HTTP {@code /uploads/**},
     * permitiendo servir archivos estaticos subidos por los usuarios.
     *
     * @param registry registro de handlers de recursos estaticos
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}
