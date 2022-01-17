package com.samjakob.kontext2.nlp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Sanitizer {

    public static List<String> tokenize(String contents) {
        return new ArrayList<>(Arrays.stream(contents
            .toLowerCase()
            // Split on any non-word characters.
            .split("\\W+")).filter(word -> !word.matches("\\d+"))
            .toList()
        );
    }

}
