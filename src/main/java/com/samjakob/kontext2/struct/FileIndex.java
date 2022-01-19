package com.samjakob.kontext2.struct;

import java.util.Objects;

public record FileIndex(int fileNumber, int lineNumber, int wordNumber) implements Comparable<FileIndex> {

    @Override
    public String toString() {
        return fileNumber + "_" + lineNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileNumber, lineNumber);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FileIndex otherIndex)) return false;
        else {
            return fileNumber == otherIndex.fileNumber &&
                    lineNumber == otherIndex.lineNumber;
        }
    }

    @Override
    public int compareTo(FileIndex o) {
        // Negative -> this object is less than the other.
        // Positive -> this object is more than the other.

        if (o.fileNumber > fileNumber) return -1;
        else if (o.fileNumber < fileNumber) return 1;
        else {
            if (o.lineNumber > lineNumber) return -1;
            else if (o.lineNumber < lineNumber) return 1;
            else {
                if (o.wordNumber > wordNumber) return -1;
                else if (o.wordNumber < wordNumber) return 1;
            }
        }

        return 0;
    }

}
