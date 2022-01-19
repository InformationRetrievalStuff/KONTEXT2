package com.samjakob.kontext2.nlp;

import com.samjakob.kontext2.results.IndexResult;
import com.samjakob.kontext2.struct.FileIndex;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Indexer {

    public static List<IndexResult> index(List<List<List<String>>> files) {
        // files = tokenized lines of each file.

        AtomicInteger totalWordCount = new AtomicInteger(0);
        Map<String, IndexerState> wordList = new HashMap<>();

        AtomicInteger currentFile = new AtomicInteger(1);

        files.forEach(file -> {
            AtomicInteger currentLine = new AtomicInteger(1);

            file.forEach(line -> {
                AtomicInteger currentWord = new AtomicInteger(1);

                line.forEach(word -> {
                    FileIndex index = new FileIndex(currentFile.get(), currentLine.get(), currentWord.get());

                    totalWordCount.incrementAndGet();

                    if (!wordList.containsKey(word)) {
                        wordList.put(word, new IndexerState(currentFile.get()));
                    } else {
                        wordList.get(word).absoluteFrequency++;
                        wordList.get(word).files.add(currentFile.get());
                    }

                    if (!wordList.get(word).fileIndices.contains(index)) {
                        wordList.get(word).fileIndices.add(index);
                    }

                    currentWord.incrementAndGet();
                });

                currentLine.incrementAndGet();
            });

            currentFile.incrementAndGet();
        });

        return wordList.entrySet().stream().map(entry -> new IndexResult(
            entry.getKey(),
            entry.getValue().absoluteFrequency,
            (double) entry.getValue().absoluteFrequency / totalWordCount.get(),
            entry.getValue().files.size(),
            entry.getValue().fileIndices
        )).sorted((a, b) -> {
            if (a.fileIndices().size() > b.fileIndices().size()) return 1;
            else if (a.fileIndices().size() < b.fileIndices().size()) return -1;
            else {
                int sortIndex = b.fileIndices().size() - 1;

                while (sortIndex >= 0) {
                    int sort = b.fileIndices().get(sortIndex).compareTo(a.fileIndices().get(sortIndex));
                    if (sort != 0) return sort;
                    sortIndex--;
                }

                return 0;
            }
        }).toList();
    }

    private static class IndexerState {
        int absoluteFrequency;
        Set<Integer> files;
        List<FileIndex> fileIndices;

        public IndexerState(int firstFile) {
            absoluteFrequency = 1;
            files = new HashSet<>();
            files.add(firstFile);
            fileIndices = new ArrayList<>();
        }
    }

}
