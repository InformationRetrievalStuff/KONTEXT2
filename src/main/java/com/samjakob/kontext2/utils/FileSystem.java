package com.samjakob.kontext2.utils;

import javafx.scene.control.Alert;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FileSystem {

    /**
     * Fetches the list of files in the specified directory.
     * @param sourceDirectory The directory to look for files in.
     * @param suffix If not null, the suffix that files must match to be
     *               selected.
     * @return The list of files from the directory.
     */
    public static List<File> getFilesInDirectory(String sourceDirectory, String suffix)
            throws FileNotFoundException {
        File directory = new File(sourceDirectory);

        if (!directory.exists()) {
            throw new FileNotFoundException(
                    "The specified path (" + sourceDirectory + ") does not exist."
            );
        }

        if (!directory.isDirectory()) {
            throw new FileNotFoundException(
                    "The specified path (" + sourceDirectory + ") is not a directory."
            );
        }

        List<File> results = new ArrayList<>();

        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (suffix != null && !file.getName().endsWith(suffix)) continue;

            if (file.isDirectory()) {
                results.addAll(getFilesInDirectory(file.getAbsolutePath(), suffix));
                continue;
            }

            if (file.isFile()) {
                results.add(file.getAbsoluteFile());
            }
        }

        return results;
    }

    /**
     * Reads the specified fileName into a string.
     * @param fileName The filename to load.
     * @return The entire file in a String.
     */
    public static String readFile(String fileName) {
        try {
            try (var lines = Files.lines(Path.of(fileName), StandardCharsets.UTF_8)) {
                String data = lines.collect(Collectors.joining("\n"));
                lines.close();
                return data;
            } catch (Exception ex) {
                // Bad character set. Let's try again with ISO
                var lines = Files.lines(Path.of(fileName), StandardCharsets.ISO_8859_1);
                String data = lines.collect(Collectors.joining("\n"));
                lines.close();
                return data;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Interface.showAlertDialog(
                    Alert.AlertType.ERROR,
                    "Filesystem Error",
                    "Failed to load the requested file: " + fileName,
                    ex.getMessage()
            );

            return null;
        }
    }

    /**
     * Writes data into the file as specified by fileName.
     * @param fileName The fileName to write.
     * @param data The data to write into the file.
     * @return Whether writing the file was successful or not.
     */
    public static boolean writeFile(String fileName, String data) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(data);
            writer.close();

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            Interface.showAlertDialog(
                    Alert.AlertType.ERROR,
                    "Filesystem Error",
                    "Failed to save the requested file: " + fileName,
                    ex.getMessage()
            );

            return false;
        }
    }

}
