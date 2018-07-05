package org.zalando.logbook;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonHeuristicTest {

    private final JsonHeuristic unit = new JsonHeuristic();

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            " ",
            "\t",
            "\n",
            "void",
            "True",
            "<skipped>",
            "\"",
            "\"missing end quote",
            "123.4.5",
            "{},",
            "[],",
            "null\n",
            "true\n",
            "false\n",
            "\"string\"\n",
            "123\n",
            "123.45\n",
    })
    void notJson(final String value) {
        assertFalse(unit.isProbablyJson(value));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "null",
            "true",
            "false",
            "\"string\"",
            "\"technically\"not a valid string\"", // acceptable false positive
            "123",
            "123.45",
            "{}",
            "{}\n",
            "\n{}",
            "{\"key\",\"value\"}",
            "{key:value}", // acceptable false positive
            "{\"key\",{}", // acceptable false positive
            "[]",
            "[]\n",
            "\n[]",
            "[\"value\"]",
            "[]]", // acceptable false positive
            "[value]", // acceptable false positive
    })
    void probablyJson(final String value) {
        assertTrue(unit.isProbablyJson(value));
    }

}
