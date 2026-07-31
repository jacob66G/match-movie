package io.github.jacob66g.matchmovie.common.log;


public final class LogSanitizer {

    private static final int MAX_LENGTH = 200;

    private LogSanitizer() {
    }

    public static String sanitize(Object value) {
        if (value == null) {
            return "-";
        }
        String text = value.toString();
        StringBuilder sanitized = new StringBuilder(Math.min(text.length(), MAX_LENGTH));
        text.codePoints()
                .limit(MAX_LENGTH)
                .map(codePoint -> Character.isISOControl(codePoint) ? '_' : codePoint)
                .forEach(sanitized::appendCodePoint);

        if (text.length() > MAX_LENGTH) {
            sanitized.append("...");
        }
        return sanitized.toString();
    }
}
