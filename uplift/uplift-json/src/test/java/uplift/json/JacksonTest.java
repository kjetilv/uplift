package uplift.json;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

public class JacksonTest {

    @Test
    void testEscaped() {
        Tests.testEscaped(Json.INSTANCE);
    }

    @Test
    void test() {
        Tests.test(json);
    }

    @Test
    void testAws() {
        Tests.testAws(json);
    }

    private static final Json json = new JacksonJson();

    private static final class JacksonJson implements Json {

        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public Map<String, Object> jsonMap(String source) {
            try {
                return objectMapper.readerFor(Map.class).readValue(source);
            } catch (Exception e) {
                throw new IllegalStateException(source, e);
            }
        }

        @Override
        public List<Object> jsonArray(String source) {
            try (
                MappingIterator<Object> iterator = objectMapper.readerFor(Map.class).readValues(source);
            ) {
                return iterator.readAll();
            } catch (Exception e) {
                throw new IllegalStateException(source, e);
            }
        }

        @Override
        public Object read(InputStream source) {
            return read(new InputStreamReader(source, StandardCharsets.UTF_8));
        }

        @Override
        public Object read(String source) {
            return source.startsWith("{") ? jsonMap(source) : jsonArray(source);
        }

        @Override
        public String write(Object object) {
            try {
                return objectMapper.writeValueAsString(object);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException(object.toString(), e);
            }
        }

        @Override
        public void write(Object object, OutputStream outputStream) {
            try {
                objectMapper.writeValue(outputStream, object);
            } catch (Exception e) {
                throw new IllegalStateException(object.toString(), e);
            }
        }
    }
}
