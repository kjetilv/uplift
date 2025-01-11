package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Json;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;

public class JsonLinesTest {

    @Test
    void parse() {
        MyMultiCallbacks parse = (MyMultiCallbacks) Json.INSTANCE.parseMulti(
            //language=jsonl
            """
                { "foo": "bar", "a": "b" }
                { "zot":  "zip" }
                { "zot":  "zot", "a": [ 2, 3, true, "yikes"] }
                """,
            new MyMultiCallbacks()
        );
        assertThat(parse.getStuff()).hasSize(3)
            .allSatisfy(tokens ->
                assertThat(tokens).first().asInstanceOf(STRING).startsWith("objectStarted"));
    }
}
