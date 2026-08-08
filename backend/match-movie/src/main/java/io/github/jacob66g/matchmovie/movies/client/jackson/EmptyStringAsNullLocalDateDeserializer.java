package io.github.jacob66g.matchmovie.movies.client.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;

import java.io.IOException;
import java.time.LocalDate;


public class EmptyStringAsNullLocalDateDeserializer extends StdScalarDeserializer<LocalDate> {

    public EmptyStringAsNullLocalDateDeserializer() {
        super(LocalDate.class);
    }

    @Override
    public LocalDate deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String text = parser.getValueAsString();
        if (text == null || text.isBlank()) {
            return null;
        }
        return LocalDate.parse(text);
    }
}
