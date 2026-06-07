package com.github.kjetilv.uplift.json.gen;

import module java.base;
import com.github.kjetilv.uplift.json.JsonReader;
import org.junit.jupiter.api.Test;

import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("ClassNameDiffersFromFileName")
class CompilerTest {

    @Test
    void simpleCase() {
        var session = Sessions.create(
            "junker.barabas.Foo",
            //language=java
            """
                package junker.barabas;
                
                import com.github.kjetilv.uplift.json.anno.JsonRecord;
                
                @JsonRecord
                public record Foo(
                    Boolean foo,
                    String zot,
                    Integer five
                ) {
                }
                """
        );
        session.readAndVerify(
            //language=json
            """
                {
                "foo": true,
                "zot": "zip",
                "five": 6
                }
                """
        );
    }

    /**
     * Tests basic Java compiler functionality by obtaining the system Java compiler
     * and its standard file manager. This test verifies that the compiler tools are
     * accessible and can be used to create compilation tasks programmatically.
     */
    @SuppressWarnings({"ClassNameDiffersFromFileName"})
    @Test
    void simple() throws IOException {
        var compiler = ToolProvider.getSystemJavaCompiler();
        var fm = compiler.getStandardFileManager(null, Locale.ROOT, StandardCharsets.UTF_8);

        var srcDir = Files.createTempDirectory("src");
        var classOut = Files.createTempDirectory("classes");
        var srcOut = Files.createTempDirectory("gen-src");

        var foo = srcDir.resolve("Foo.java");
        Files.writeString(
            foo,
            //language=java
            """
                import com.github.kjetilv.uplift.json.anno.JsonRecord;
                
                @JsonRecord
                public record Foo(
                    Boolean foo,
                    String zot
                ) {
                }
                """
        );

        fm.setLocationFromPaths(StandardLocation.CLASS_OUTPUT, List.of(classOut));
        fm.setLocationFromPaths(StandardLocation.SOURCE_OUTPUT, List.of(srcOut));

        var units = fm.getJavaFileObjectsFromPaths(List.of(foo));
        var task = compiler.getTask(new PrintWriter(System.out), fm, null, null, null, units);
        task.setProcessors(List.of(new JsonRecordProcessor()));
        var ok = task.call();
        System.out.println(ok);

        try (
            var urlClassLoader = URLClassLoader.newInstance(
                new URL[] {classOut.toUri().toURL()},
                Thread.currentThread().getContextClassLoader()
            )
        ) {

            try (var list = Files.list(srcOut)) {
                list.filter(file -> file.getFileName().toString().endsWith(".java"))
                    .forEach(file -> {
                        IO.println();
                        IO.println(file.getFileName());
                        IO.println();
                        try (var lines = Files.lines(file)) {
                            lines
                                .map(line -> "    " + line)
                                .forEach(IO::println);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
            }
            var fooClass = urlClassLoader.loadClass("Foo");
            IO.println(fooClass);
            var stringReader = stringReader(urlClassLoader);
            //language=json
            var fooInstance = stringReader.read("""
                {
                "foo": true,
                "zot": "zip"
                }
                """);
            assertThat(fooInstance).isInstanceOf(fooClass);
            IO.println(fooInstance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static JsonReader<String, ?> stringReader(URLClassLoader urlClassLoader) throws
        ClassNotFoundException,
        NoSuchFieldException,
        IllegalAccessException,
        NoSuchMethodException,
        InvocationTargetException {
        var fooRWClass = urlClassLoader.loadClass("FooRW");
        var fooRWField = fooRWClass.getField("INSTANCE");
        var fooRW = fooRWField.get(null);
        var stringReaderMethod = JsonRW.class.getMethod("stringReader");
        var stringReader = (JsonReader<String, ?>) stringReaderMethod.invoke(fooRW);
        return stringReader;
    }
}
