package com.samjakob.kontext2.results;

import java.text.DecimalFormat;

public record IndexResult(String word, int absoluteFrequency, double relativeFrequency, int fileCount) {

    public String getWord() {
        return word;
    }

    public int getAbsoluteFrequency() {
        return absoluteFrequency;
    }

    public String getRelativeFrequency() {
        return new DecimalFormat("0.####").format(relativeFrequency);
    }

    public int getFileCount() { return fileCount; }

}