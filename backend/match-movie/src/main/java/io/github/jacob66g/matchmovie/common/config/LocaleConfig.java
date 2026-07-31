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
    private static final Locale DEFAULT = Locale.ENGLISH;
    private static final List<Locale> SUPPORTED = List.of(DEFAULT, Locale.of("pl"));

    @Bean
    public LocaleResolver localeResolver() {
        return new LocaleResolver() {
            @Override
            public Locale resolveLocale(HttpServletRequest request) {
                String header = request.getHeader(LOCALE_HEADER);
                if (header != null) {
                    Locale fromHeader = match(Locale.forLanguageTag(header));
                    if (fromHeader != null) {
                        return fromHeader;
                    }
                }
                Locale fromAcceptLanguage = match(request.getLocale());
                return fromAcceptLanguage != null ? fromAcceptLanguage : DEFAULT;
            }

            @Override
            public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
                throw new UnsupportedOperationException("Use X-Locale header instead");
            }
        };
    }

    /**
     * Matches on language only, so both {@code pl} and {@code pl-PL} resolve to the supported locale.
     */
    private static Locale match(Locale requested) {
        if (requested == null || requested.getLanguage().isEmpty()) {
            return null;
        }
        return SUPPORTED.stream()
                .filter(supported -> supported.getLanguage().equals(requested.getLanguage()))
                .findFirst()
                .orElse(null);
    }
}
