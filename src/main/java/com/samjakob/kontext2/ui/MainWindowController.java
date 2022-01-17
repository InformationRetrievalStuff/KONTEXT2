package com.samjakob.kontext2.ui;

import com.samjakob.kontext2.nlp.Indexer;
import com.samjakob.kontext2.nlp.Sanitizer;
import com.samjakob.kontext2.results.IndexResult;
import com.samjakob.kontext2.utils.FileSystem;
import com.samjakob.kontext2.utils.Interface;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Callback;

import java.io.File;
import java.text.DecimalFormat;
import java.util.*;

public class MainWindowController {

    @FXML
    public BorderPane mainWindowRoot;

    @FXML
    public Tab tabInput;

    @FXML
    public Tab tabTasks;

    @FXML
    public ListView<String> inputFileList;

    @FXML
    public ButtonBar inputButtonBar;

    @FXML
    public Button inputRemoveAllButton;

    @FXML
    public Button inputRemoveSelectedButton;

    @FXML
    public Button inputViewSelectedButton;

    @FXML
    public Button inputAddFileButton;

    @FXML
    public Button inputAddDirectoryButton;

    @FXML
    public ChoiceBox<String> taskName;

    @FXML
    public Button taskStart;

    @FXML
    public TabPane tabPane;

    @FXML
    public TableView<IndexResult> resultsTable;

    /**
     * This gets populated with the initial label of the 'Input' tab as the app
     * loads. This allows it to be dynamically updated.
     */
    private String inputTabLabel;

    @FXML
    public void initialize() {
        inputTabLabel = tabInput.getText();

        // Set the button bar data for each of the input buttons, so they appear
        // in the correct order.
        ButtonBar.setButtonData(inputRemoveAllButton, ButtonBar.ButtonData.LEFT);
        ButtonBar.setButtonData(inputRemoveSelectedButton, ButtonBar.ButtonData.OTHER);
        ButtonBar.setButtonData(inputViewSelectedButton, ButtonBar.ButtonData.OTHER);
        ButtonBar.setButtonData(inputAddFileButton, ButtonBar.ButtonData.RIGHT);
        ButtonBar.setButtonData(inputAddDirectoryButton, ButtonBar.ButtonData.RIGHT);

        // Add the columns to the resultsTable.
        TableColumn<IndexResult, String> wordColumn = new TableColumn<>("Word");
        wordColumn.setCellValueFactory(new PropertyValueFactory<>("word"));
        resultsTable.getColumns().add(wordColumn);

        TableColumn<IndexResult, Integer> absFreqColumn = new TableColumn<>("Absolute Freq.");
        absFreqColumn.setCellValueFactory(new PropertyValueFactory<>("absoluteFrequency"));
        absFreqColumn.setSortType(TableColumn.SortType.DESCENDING);
        resultsTable.getColumns().add(absFreqColumn);

        TableColumn<IndexResult, String> relFreqColumn = new TableColumn<>("Relative Freq.");
        relFreqColumn.setCellValueFactory(new PropertyValueFactory<>("relativeFrequency"));
        resultsTable.getColumns().add(relFreqColumn);

        TableColumn<IndexResult, Double> fileCountColumn = new TableColumn<>("File Count");
        fileCountColumn.setCellValueFactory(new PropertyValueFactory<>("fileCount"));
        resultsTable.getColumns().add(fileCountColumn);

        resultsTable.getSortOrder().clear();
        resultsTable.getSortOrder().add(absFreqColumn);
    }

    @FXML
    public void onInputRemoveAll() {
        inputFileList.getItems().clear();
        refreshTabLabels();
    }

    @FXML
    public void onInputRemoveSelected() {
        inputFileList.getItems().removeAll(inputFileList.getSelectionModel().getSelectedItems());
        refreshTabLabels();
    }

    @FXML
    public void onInputViewSelected() {
        if (inputFileList.getSelectionModel().isEmpty()) {
            Interface.showAlertDialog(
                    Alert.AlertType.ERROR,
                    "No File Selected",
                    "You have not selected a file...",
                    "You have clicked 'View Selected' which will show a file if you selected one from the list however you have not selected a file from the list."
            );
            return;
        }

        String fileToView = inputFileList.getSelectionModel().getSelectedItem();
        Interface.showFileViewerDialog(mainWindowRoot.getScene().getWindow(), fileToView);
    }

    @FXML
    public void onInputAddFile() {
        var chooser = new FileChooser();
        chooser.setTitle("Add File(s)...");

        // Set the initial directory to the user's HOME directory.
        chooser.setInitialDirectory(new File(
                System.getProperty("user.home")
        ));

        // Allow the user to select the files that should be added.
        var files = chooser.showOpenMultipleDialog(mainWindowRoot.getScene().getWindow());
        if (files == null) return;

        // Attempt to add the files...
        try {
            int addedFiles = addFiles(files.stream().map(File::getAbsolutePath).toList());

            // There were files, but we didn't add any because there were
            // no unique ones.
            if (files.size() == 0) {
                // There were no valid files to add at all.
                Interface.showAlertDialog(
                        Alert.AlertType.ERROR,
                        "Filesystem Error",
                        "Failed to load any of the selected files...",
                        "Your selection was empty or did not contain any valid files, so no files were loaded."
                );
            } else if (addedFiles < 1) {
                // There were valid files, but they had all already been added.
                Interface.showAlertDialog(
                        Alert.AlertType.WARNING,
                        "No Additional Files Loaded...",
                        "No additional files were loaded...",
                        "Your selection did not contain any valid files that weren't already selected, so no additional files were loaded."
                );
            } else if (files.size() > addedFiles) {
                // Some additional valid files were loaded, but there were some
                // already loaded files that were skipped.
                int skippedFiles = files.size() - addedFiles;
                Interface.showAlertDialog(
                        Alert.AlertType.INFORMATION,
                        Interface.withEnglishNumericalSuffix("File", skippedFiles) + " Skipped...",
                        "Some of the selected files were skipped...",
                        Interface.withEnglishNumericalSuffixAndTemporalVerb("file", skippedFiles) + " already previously selected, so those files have been skipped."
                );
            }

            // (Otherwise, the entire selection was loaded.)
        } catch (Exception ex) {
            ex.printStackTrace();
            Interface.showAlertDialog(
                    Alert.AlertType.ERROR,
                    "Filesystem Error",
                    "Failed to load the requested files...",
                    ex.getMessage()
            );
        }

        refreshTabLabels();
    }

    @FXML
    public void onInputAddDirectory() {
        var chooser = new DirectoryChooser();
        chooser.setTitle("Add Directory...");

        // Set the initial directory to the user's HOME directory.
        chooser.setInitialDirectory(new File(
            System.getProperty("user.home")
        ));

        // Open the specified directory and attempt to recursively add text
        // files from it.
        var directory = chooser.showDialog(mainWindowRoot.getScene().getWindow());
        if (directory == null) return;

        try {
            var files = FileSystem.getFilesInDirectory(directory.getAbsolutePath(), ".txt");
            int addedFiles = addFiles(files.stream().map(File::getAbsolutePath).toList());

            // There were files, but we didn't add any because there were
            // no unique ones.
            if (files.size() == 0) {
                // There were no valid files to add at all.
                Interface.showAlertDialog(
                        Alert.AlertType.ERROR,
                        "Filesystem Error",
                        "Failed to load any files from the specified directory...",
                        "The specified directory was empty or did not contain any valid files, so no files were loaded."
                );
            } else if (addedFiles < 1) {
                // There were valid files, but they had all already been added.
                Interface.showAlertDialog(
                        Alert.AlertType.WARNING,
                        "No Additional Files Loaded...",
                        "No additional files were loaded from the specified directory...",
                        "The specified directory did not contain any valid files that weren't already selected, so no additional files were loaded."
                );
            } else if (files.size() > addedFiles) {
                // Some additional valid files were loaded, but there were some
                // already loaded files that were skipped.
                int skippedFiles = files.size() - addedFiles;
                Interface.showAlertDialog(
                    Alert.AlertType.INFORMATION,
                    Interface.withEnglishNumericalSuffix("File", skippedFiles) + " Skipped...",
                    "Some files were skipped from the specified directory...",
                    Interface.withEnglishNumericalSuffixAndTemporalVerb("file", skippedFiles) + " already previously selected, so those files have been skipped."
                );
            }

            // (Otherwise, the entire directory was loaded.)
        } catch (Exception ex) {
            Interface.showAlertDialog(
                Alert.AlertType.ERROR,
                "Filesystem Error",
                "Failed to load the requested files...",
                ex.getMessage()
            );
        }

        // Ensure tab labels are refreshed regardless of what happened.
        refreshTabLabels();
    }

    @FXML
    public void onStartTask() {
        taskStart.setDisable(true);

        switch (taskName.getValue()) {
            case "Index" -> {
//                Map<String, Integer> wordList = new HashMap<>();

//                inputFileList.getItems().forEach(filePath -> {
//                    String contents = FileSystem.readFile(filePath);
//                    assert contents != null;
//
//                    List<String> words = Sanitizer.tokenize(contents);
//
//                    for (String word : words) {
//                        if (!wordList.containsKey(word)) {
//                            wordList.put(word, 1);
//                        } else wordList.put(word, wordList.get(word) + 1);
//                    }
//                });
//
//                List<IndexResult> results = wordList.entrySet().stream().map(
//                    (entry) -> new IndexResult(
//                        entry.getKey(),
//                        entry.getValue(),
//                        (double) entry.getValue() / wordList.size()
//                    )
//                ).toList();

                List<IndexResult> results = Indexer.index(inputFileList.getItems().stream().map(
                    filePath -> Sanitizer.tokenize(Objects.requireNonNull(FileSystem.readFile(filePath)))
                ).toList());

                // Empty the results table.
                resultsTable.getItems().clear();

                // Add the new results to the results table.
                resultsTable.getItems().addAll(results);
                resultsTable.sort();
                tabPane.getSelectionModel().selectLast();
            }
        }

        taskStart.setDisable(false);
    }

    /**
     * Adds only files that are not already selected from the list.
     * @param files A string list of files to add.
     * @return The number of files that were added.
     */
    private int addFiles(List<String> files) {
        List<String> filesToAdd = new ArrayList<>();

        for (String file : files) {
            if (!inputFileList.getItems().contains(file))
                filesToAdd.add(file);
        }

        inputFileList.getItems().addAll(filesToAdd);
        return filesToAdd.size();
    }

    /**
     * Refresh dynamic elements of tab labels.
     */
    private void refreshTabLabels() {
        int inputFileCount = inputFileList.getItems().size();
        if (inputFileCount == 0) {
            tabInput.setText(inputTabLabel);
            return;
        }

        tabInput.setText(
            inputTabLabel + " (" +  Interface.withEnglishNumericalSuffix("file", inputFileCount) + ")"
        );
    }

}