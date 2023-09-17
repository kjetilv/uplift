package com.github.kjetilv.uplift.json.events;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import com.github.kjetilv.uplift.json.Address;
import com.github.kjetilv.uplift.json.Events;
import com.github.kjetilv.uplift.json.User;
import com.github.kjetilv.uplift.json.gens.AddressCallbacks;
import com.github.kjetilv.uplift.json.gens.AbstractCallbacks;
import com.github.kjetilv.uplift.json.gens.UserCallbacks;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({ "DynamicRegexReplaceableByCompiledPattern" })
public class JsonEventCallbacksTest {

    @Test
    void arr() {
        MyCallbacks myCallbacks = Events.parse(
            callbacks(),
            """
            {
              "els": [1, 2, "a", "b"],
              "foo": ["tip", true, [ 3, 4 ], []],
              "bar": [{"zit": "quz"}, 4]
            }
            """
        );
        assertThat(myCallbacks.getStuff()).containsExactlyElementsOf(Arrays.stream(
            """
            objectStarted
              field:els arrayStarted
                number:1
                number:2
                string:a
                string:b
              arrayEnded
              field:foo arrayStarted
                string:tip
                truth:true
                arrayStarted
                  number:3
                  number:4
                arrayEnded
                arrayStarted
                arrayEnded
              arrayEnded
              field:bar arrayStarted
                objectStarted
                  field:zit string:quz
                objectEnded
              number:4
              arrayEnded
            objectEnded
            """.split("\\s+")).toList()
        );
    }

    @Test
    void obj() {
        MyCallbacks myCallbacks = Events.parse(
            callbacks(),
            """
            {
              "foo": "bar",
              "zot": 5,
              "obj2": {
                "oops": true
              }
            }
            """
        );
        assertThat(myCallbacks.getStuff()).containsExactlyElementsOf(Arrays.stream(
            """
            objectStarted
              field:foo string:bar
              field:zot number:5
              field:obj2 objectStarted
                field:oops truth:true
              objectEnded
            objectEnded
            """.split("\\s+")).toList()
        );
    }

    @Test
    void parse() {
        MyCallbacks myCallbacks = Events.parse(
            callbacks(),
            """
            {
              "foo": "bar",
              "zot": 5,
              "obj2": {
                "oops": true
              }
            }
            """
        );
        assertThat(myCallbacks.getStuff()).containsExactlyElementsOf(Arrays.stream(
            """
            objectStarted
              field:foo string:bar
              field:zot number:5
              field:obj2 objectStarted
                field:oops truth:true
              objectEnded
            objectEnded
            """.split("\\s+")).toList()
        );
    }

    @Test
    void parseMap() {
        MyCallbacks callbacks = callbacks();
        Events.parse(
            callbacks,
            """
            {
              "foo": "bar",
              "zot": 5,
              "obj2": {
                "oops": true
              }
            }
            """
        );
    }

    @Test
    void parseUser() {
        AtomicReference<User> reference = new AtomicReference<>();
        AbstractCallbacks parse = Events.parse(
            new UserCallbacks(null, reference::set),
            """
            {
              "name": "Kjetil",
              "address": {
                "streetName": "None Street",
                "houseNumber": 1729,
                "modifier": "B",
                "code": 1450,
                "residents": [
                  {
                    "name": "foo",
                    "permanent": true
                  },
                  {
                    "name": "bar",
                    "permanent": false
                  }
                ]
              },
              "roadWarrior": true,
              "birthYear": 1973,
              "misc": [
                50,
                "hacker",
                true
              ]
            }
            """
        );
        System.out.println(reference.get());
        AtomicReference<Address> reference1 = new AtomicReference<>();
        Events.parse(
            new AddressCallbacks(null, reference1::set),
            """
            {
                "streetName": "None Street",
                "houseNumber": 1729,
                "modifier": "B",
                "code": 1450
            }
            """
        );
        System.out.println(reference1.get());
    }

    private static MyCallbacks callbacks() {
        return new MyCallbacks();
    }
}
