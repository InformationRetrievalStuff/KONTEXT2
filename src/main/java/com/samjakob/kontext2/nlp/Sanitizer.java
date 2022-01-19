package com.samjakob.kontext2.nlp;

import java.util.Arrays;
import java.util.List;

public class Sanitizer {

    public static List<List<String>> tokenize(String contents) {
        return Arrays.stream(contents.split("\n")).map(
            line -> Arrays.stream(line.toLowerCase().split("\\W+"))
                    .filter(word -> !word.matches("\\d+"))
                    .filter(word -> word.length() > 0)
                    .toList()
        ).toList();
    }

}
