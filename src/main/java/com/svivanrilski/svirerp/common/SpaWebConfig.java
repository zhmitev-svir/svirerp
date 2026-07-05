package com.svivanrilski.svirerp.common;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * The Angular app does client-side routing (e.g. /portal-access, /persons),
 * so a direct browser navigation or refresh on any of those paths hits the
 * backend, not the SPA router. There's no static file at that path, so
 * without this fallback it 404s at the resource-resolution layer — falling
 * back to index.html instead lets Angular's own router decide what to show,
 * matching its existing wildcard route. This only applies to requests that
 * don't match a real @Controller mapping (e.g. /api/**), since those are
 * resolved before Spring MVC ever consults resource handlers.
 */
@Configuration
public class SpaWebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requested = location.createRelative(resourcePath);
                        return requested.exists() && requested.isReadable()
                                ? requested
                                : new ClassPathResource("/static/index.html");
                    }
                });
    }
}
