package com.samjakob.kontext2.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableHeaderRow;

public class ResultsTableController {

    @FXML
    public TableView resultsTable;

    private TableColumn idColumn;

    @FXML
    public void initialize() {

    }

    public void initIdColumn(TableColumn idColumn) {
        if (this.idColumn != null) {
            throw new IllegalStateException("ID Column initialized after already being initialized.");
        }
        this.idColumn = idColumn;

        resultsTable.skinProperty().addListener((__, ___, ____) -> {
            final TableHeaderRow header = (TableHeaderRow) resultsTable.lookup("TableHeaderRow");

            header.reorderingProperty().addListener((observer, oldValue, newValue) -> {
                if (resultsTable.getColumns().indexOf(idColumn) != 0) {
                    resultsTable.getColumns().remove(idColumn);
                    resultsTable.getColumns().add(0, idColumn);
                }
            });
        });
    }

}
