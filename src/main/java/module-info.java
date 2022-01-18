module com.samjakob.kontext2 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires validatorfx;

    opens com.samjakob.kontext2 to javafx.fxml;
    exports com.samjakob.kontext2;
    exports com.samjakob.kontext2.ui;
    exports com.samjakob.kontext2.results;
    opens com.samjakob.kontext2.ui to javafx.fxml;
    exports com.samjakob.kontext2.ui.controller;
    opens com.samjakob.kontext2.ui.controller to javafx.fxml;
}