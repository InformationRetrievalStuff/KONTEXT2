package com.samjakob.kontext2.results;

import com.samjakob.kontext2.ui.PartialRenderer;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public record IndexResult(String word, int absoluteFrequency, double relativeFrequency, int fileCount) {

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
        return new DecimalFormat("0.####").format(relativeFrequency);
    }

    public int getFileCount() { return fileCount; }

}