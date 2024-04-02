package com.loopy.loopy.plugins.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
public class StaticResourceConfigurer extends WebMvcConfigurationSupport {

    @Value("${file.audio_path}")
    private String audio_path;

    @Value("${file.image_path}")
    private String image_path;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/audio/**").addResourceLocations("classpath:/static/audio/").setCachePeriod(0);
//        registry.addResourceHandler("/image/**").addResourceLocations("classpath:/static/image/").setCachePeriod(0);

        registry.addResourceHandler("/audio/**").addResourceLocations("file:"+audio_path+"/");
        registry.addResourceHandler("/image/**").addResourceLocations("file:"+image_path+"/");
        super.addResourceHandlers(registry);
    }
}