package com.samjakob.kontext2.results;

import com.samjakob.kontext2.struct.FileIndex;
import com.samjakob.kontext2.ui.PartialRenderer;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record IndexResult(String word, int absoluteFrequency, double relativeFrequency, int fileCount, List<FileIndex> fileIndices) {

    public static PartialRenderer.TableRendererData getTableColumns() {
        List<TableColumn> tableColumns = new ArrayList<>();

        TableColumn<IndexResult, String> wordColumn = new TableColumn<>("Word");
        wordColumn.setCellValueFactory(new PropertyValueFactory<>("word"));
        tableColumns.add(wordColumn);

        TableColumn<IndexResult, Integer> absFreqColumn = new TableColumn<>("Absolute Freq.");
        absFreqColumn.setCellValueFactory(new PropertyValueFactory<>("absoluteFrequency"));
        absFreqColumn.setSortType(TableColumn.SortType.DESCENDING);
        absFreqColumn.setPrefWidth(125);
        tableColumns.add(absFreqColumn);

        TableColumn<IndexResult, String> relFreqColumn = new TableColumn<>("Relative Freq.");
        relFreqColumn.setCellValueFactory(new PropertyValueFactory<>("relativeFrequency"));
        relFreqColumn.setPrefWidth(125);
        tableColumns.add(relFreqColumn);

        TableColumn<IndexResult, Double> fileCountColumn = new TableColumn<>("File Count");
        fileCountColumn.setCellValueFactory(new PropertyValueFactory<>("fileCount"));
        tableColumns.add(fileCountColumn);

        TableColumn<IndexResult, String> fileIndexColumn = new TableColumn<>("File Indexes");
        fileIndexColumn.setCellValueFactory(new PropertyValueFactory<>("fileIndexes"));
        tableColumns.add(fileIndexColumn);

        return new PartialRenderer.TableRendererData(
            tableColumns,
            table -> {
                table.getSortOrder().clear();
                table.getSortOrder().add(absFreqColumn);
            }
        );
    }

    public String getWord() {
        return word;
    }

    public int getAbsoluteFrequency() {
        return absoluteFrequency;
    }

    public String getRelativeFrequency() {
        return String.format("%.4G", relativeFrequency);
    }

    public int getFileCount() { return fileCount; }

    public String getFileIndexes() {
        return fileIndices.stream().map(FileIndex::toString).collect(Collectors.joining(", "));
    }

}