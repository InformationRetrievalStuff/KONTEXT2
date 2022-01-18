package com.samjakob.kontext2.ui;

import com.samjakob.kontext2.MainApplication;
import com.samjakob.kontext2.results.IndexResult;
import com.samjakob.kontext2.ui.controller.ResultsTableController;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.IOException;
import java.util.List;

public class PartialRenderer {

    /**
     * The tab pane that new tabs should be rendered in.
     */
    private TabPane rootTabPane;

    public PartialRenderer(TabPane rootTabPane) {
        this.rootTabPane = rootTabPane;
    }

    public <T> void displayResultsTable(PartialRenderer.TableRendererData tableRendererData, List<T> records) throws IOException {
        this.displayResultsTable(tableRendererData, records, false);
    }

    public <T> void displayResultsTable(PartialRenderer.TableRendererData tableRendererData, List<T> records, boolean disableIdColumn) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("ui/ResultsTable.fxml"));
        Tab resultsTab = new Tab("Results", loader.load());

        ResultsTableController resultsTableController = loader.getController();
        TableView resultsTable = resultsTableController.resultsTable;

        // Add the ID column if it is enabled for this table.
        if (!disableIdColumn) {
            TableColumn<IndexResult, Integer> idColumn = new TableColumn<>("#");
            idColumn.setCellValueFactory(
                p -> new ReadOnlyObjectWrapper<>(resultsTable.getItems().indexOf(p.getValue()) + 1)
            );
            idColumn.setSortable(false);
            idColumn.setReorderable(false);
            resultsTable.getColumns().add(idColumn);

            resultsTableController.initIdColumn(idColumn);
        }

        // Add the remaining columns.
        for (TableColumn column : tableRendererData.columns) {
            resultsTable.getColumns().add(column);
        }

        // Empty the results table.
        resultsTable.getItems().clear();

        // Add the new results to the results table.
        resultsTable.getItems().addAll(records);
        resultsTable.sort();

        // Perform the onTableLoad callback.
        tableRendererData.tableRenderCallback.execute(resultsTable);

        rootTabPane.getTabs().add(resultsTab);
        rootTabPane.getSelectionModel().selectLast();
    }

    public static record TableRendererData(
        List<TableColumn> columns,
        TableRenderCallback tableRenderCallback
    ) {}

    public interface TableRenderCallback {
        void execute(TableView table);
    }

}
