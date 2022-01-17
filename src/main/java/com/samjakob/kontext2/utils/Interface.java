package com.samjakob.kontext2.utils;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.Locale;

public class Interface {

    private static final String[] fileSizeUnits = {"bytes", "KiB", "MiB", "GiB", "PiB", "EiB"};

    public static void showAlertDialog(Alert.AlertType type, String title, String header, String content) {
        var alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void showFileViewerDialog(javafx.stage.Window parent, String filePath) {
        Path target = Paths.get(filePath);

        var dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.initOwner(parent);
        dialog.setTitle(filePath);

        String data = FileSystem.readFile(filePath);
        long rawFileSize;
        String fileSize;
        try {
            rawFileSize = Files.size(target);
            fileSize = Interface.toHumanReadableSize(rawFileSize);
        } catch (Exception ex) { return; }
        if (data == null) return;

        // VBox with Labels, (header content).
        var vbox = new VBox();
        GridPane.setHgrow(vbox, Priority.ALWAYS);
        vbox.setFillWidth(true);
        vbox.setPadding(new Insets(0, 0, 10, 0));

        var headerLabel = new Label(target.getFileName().toString());
        headerLabel.setFont(Font.font(
            headerLabel.getFont().getFamily(),
            FontWeight.BOLD, 24
        ));
        vbox.getChildren().add(headerLabel);
        vbox.getChildren().add(new Label(filePath));

        var fileSizeLabel = new TextFlow();
        var fileSizeLabelPrefix = new Text("File Size:");
        fileSizeLabelPrefix.setStyle("-fx-font-weight: bold");
        fileSizeLabel.getChildren().add(fileSizeLabelPrefix);
        fileSizeLabel.getChildren().add(new Text(" "));
        fileSizeLabel.getChildren().add(new Text(fileSize));
        fileSizeLabel.getChildren().add(new Text(" (" + Interface.withEnglishNumericalSuffix("byte", rawFileSize) + ")"));
        vbox.getChildren().add(fileSizeLabel);

        // Text Area (contains document).
        var textArea = new TextArea(data);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        // GridPane (wraps all content).
        var expandableContent = new GridPane();
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        expandableContent.setMaxWidth(Double.MAX_VALUE);
        expandableContent.add(vbox, 0, 0);
        expandableContent.add(textArea, 0, 1);

        // Add the buttons
        ButtonType save = new ButtonType("Save to File");
        ButtonType copy = new ButtonType("Copy to Clipboard");
        ButtonType print = new ButtonType("Print");
        ButtonType ok = new ButtonType("OK", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getButtonTypes().setAll(save, copy, print, ok);

        // Set event handlers for the buttons.
        dialog.getDialogPane().lookupButton(save).addEventFilter(
                ActionEvent.ACTION,
                event -> {
                    saveStringToFile(dialog.getDialogPane().getScene().getWindow(), data);
                    event.consume();
                }
        );
        dialog.getDialogPane().lookupButton(copy).addEventFilter(
                ActionEvent.ACTION,
                event -> {
                    copyStringToClipboard(data);
                    event.consume();
                }
        );
        dialog.getDialogPane().lookupButton(print).addEventFilter(
            ActionEvent.ACTION,
            event -> {
                printNode(textArea, filePath);
                event.consume();
            }
        );

        // Set content and show dialog.
        dialog.getDialogPane().setHeader(new VBox());
        dialog.getDialogPane().setContent(expandableContent);
        dialog.initModality(Modality.NONE);
        dialog.show();
        dialog.getDialogPane().lookupButton(ok).requestFocus();
    }

    /**
     * Creates a system print job for the specified node.
     * @param node The node to print.
     */
    public static void printNode(Node node, String jobTitle) {
        // TODO: when I have time - https://stackoverflow.com/a/1097593/2872279

        var job = PrinterJob.createPrinterJob();
        if (job != null) {
            job.getJobSettings().setJobName(jobTitle);

            if (job.printPage(node)) {
                job.endJob();
            }
        }
    }

    /**
     * Saves the specified string to a user-specified file. A dialog is
     * displayed to the user to allow them to choose where they want to save
     * the string.
     * @param owner The owning window of the file save dialog.
     * @param data The string to save.
     */
    public static void saveStringToFile(javafx.stage.Window owner, String data) {
        var chooser = new FileChooser();
        chooser.setTitle("Save File...");
        var saveFile = chooser.showSaveDialog(owner);
        if (saveFile == null) return;

//        if (saveFile.exists()) {
//            Interface.showAlertDialog(
//                    Alert.AlertType.ERROR,
//                    "Filesystem Error",
//                    "Failed to save the file in the requested location...",
//                    "A file already exists in the specified location (" + saveFile + ") so this file could not be saved there."
//            );
//            return;
//        }

//        if (!saveFile.canWrite()) {
//            Interface.showAlertDialog(
//                    Alert.AlertType.ERROR,
//                    "Filesystem Permission Error",
//                    "Failed to save the file in the requested location...",
//                    "KONTEXT does not have permission to save the file in the requested location."
//            );
//            return;
//        }

        FileSystem.writeFile(saveFile.getAbsolutePath(), data);
    }

    /**
     * Copies the specified string to the user's clipboard.
     * @param string The string to copy.
     */
    public static void copyStringToClipboard(String string) {
        var content = new ClipboardContent();
        content.putString(string);
        Clipboard.getSystemClipboard().setContent(content);
    }

    public static String withEnglishNumericalSuffix(String root, long n) {
        if (n == 1) return NumberFormat.getInstance().format(n) + " " + root;
        else return NumberFormat.getInstance().format(n) + " " + root + "s";
    }

    public static String withEnglishNumericalSuffixAndTemporalVerb(String root, int n) {
        if (n == 1) return NumberFormat.getInstance().format(n) + " " + root + " was";
        else return NumberFormat.getInstance().format(n) + " " + root + "s were";
    }

    /**
     * Converts a size in bytes (given as a long) to a human-readable file
     * size string.
     *
     * Thanks to Michel Jung on StackOverflow for the original implementation:
     * https://stackoverflow.com/a/68924895/2872279
     *
     * @param value Size of the file in bytes.
     * @return The human-readable formatted string.
     */
    public static String toHumanReadableSize(long value) {
        if (value == 1) return value + " byte";
        if (value < 1024) {
            return value + " bytes";
        }

        // Number of zeroes (w.r.t value of bytes) to omit, because of the unit.
        int zeroes = (63 - Long.numberOfLeadingZeros(value)) / 10;
        // Number of zeroes to omit, clamping at the final specified unit.
        int normalizedZeroes = Math.min(zeroes, fileSizeUnits.length - 1);
        return String.format(
                Locale.getDefault(),
                "%.1f %s",
                (double) value / (1L << (normalizedZeroes * 10)),
                fileSizeUnits[normalizedZeroes]
        );
    }

}
