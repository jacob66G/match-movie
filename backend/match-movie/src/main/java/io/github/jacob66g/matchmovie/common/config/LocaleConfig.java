package io.github.jacob66g.matchmovie.common.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;

import java.util.List;
import java.util.Locale;

@Configuration
public class LocaleConfig {

    private static final String LOCALE_HEADER = "X-Locale";
    private static final List<Locale> SUPPORTED = List.of(Locale.ENGLISH, Locale.of("pl", "PL"));

    @Bean
    public LocaleResolver localeResolver() {
        return new LocaleResolver() {
            @Override
            public Locale resolveLocale(HttpServletRequest request) {
                String header = request.getHeader(LOCALE_HEADER);
                if (header != null) {
                    Locale requested = Locale.forLanguageTag(header);
                    if (SUPPORTED.contains(requested)) {
                        return requested;
                    }
                }
                Locale accepted = request.getLocale();
                return SUPPORTED.contains(accepted) ? accepted : Locale.ENGLISH;
            }

            @Override
            public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
                throw new UnsupportedOperationException("Use X-Locale header instead");
            }
        };
    }
}
