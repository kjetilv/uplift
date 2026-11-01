package com.github.kjetilv.uplift.s3.test;

import module java.base;
import com.github.kjetilv.uplift.s3.util.UriEncoding;
import com.github.kjetilv.uplift.s3.util.Xml;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class XmlTest {

    @Test
    void elements() {
        assertThat(Xml.elements("<a><C>1</C><C></C><C>x</C></a>", "C"))
            .containsExactly("1", "", "x");
    }

    @Test
    void noElements() {
        assertThat(Xml.elements("<a></a>", "C")).isEmpty();
        assertThat(Xml.text("<a></a>", "C")).isEmpty();
    }

    @Test
    void missingCloseTag() {
        assertThatThrownBy(() -> Xml.elements("<C>1", "C").toList())
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void textIsIndependentOfOrder() {
        var xml = "<Size>3</Size><Key> k </Key>";
        assertThat(Xml.text(xml, "Key")).hasValue("k");
        assertThat(Xml.text(xml, "Size")).hasValue("3");
    }

    @Test
    void unescape() {
        assertThat(Xml.unescape("a&amp;b&lt;c&gt;&quot;&apos;&#65;&#x42;&#xD;"))
            .isEqualTo("a&b<c>\"'AB\r");
        assertThat(Xml.unescape("&amp;lt;")).isEqualTo("&lt;");
    }

    @Test
    void escapeRoundTrip() {
        var text = "a&b<c>\"d'e\r\nf😀";
        assertThat(Xml.escape(text)).doesNotContain("<", ">", "\"", "'", "\r", "\n");
        assertThat(Xml.unescape(Xml.escape(text))).isEqualTo(text);
    }

    @Test
    void uriEncoding() {
        assertThat(UriEncoding.encode("a b*c~d-e_f.g/h+i$æ", false))
            .isEqualTo("a%20b%2Ac~d-e_f.g%2Fh%2Bi%24%C3%A6");
        assertThat(UriEncoding.encode("dir/my file.txt", true))
            .isEqualTo("dir/my%20file.txt");
    }
}
