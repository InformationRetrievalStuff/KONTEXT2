package com.samjakob.kontext2.nlp;

import com.samjakob.kontext2.results.IndexResult;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Indexer {

    public static List<IndexResult> index(List<List<String>> files) {
        // files = tokenized contents of each file.

        AtomicInteger totalWordCount = new AtomicInteger(0);
        Map<String, IndexerState> wordList = new HashMap<>();

        AtomicInteger currentFile = new AtomicInteger(1);

        files.forEach(file -> {
            file.forEach(word -> {
                totalWordCount.incrementAndGet();

                if (!wordList.containsKey(word)) {
                    wordList.put(word, new IndexerState(currentFile.get()));
                } else {
                    wordList.get(word).absoluteFrequency++;
                    wordList.get(word).files.add(currentFile.get());
                }
            });

            currentFile.incrementAndGet();
        });

        return wordList.entrySet().stream().map(entry -> new IndexResult(
            entry.getKey(),
            entry.getValue().absoluteFrequency,
            (double) entry.getValue().absoluteFrequency / totalWordCount.get(),
            entry.getValue().files.size()
        )).toList();
    }

    private static class IndexerState {
        int absoluteFrequency;
        Set<Integer> files;

        public IndexerState(int firstFile) {
            absoluteFrequency = 1;
            files = new HashSet<>();
            files.add(firstFile);
        }
    }

}
