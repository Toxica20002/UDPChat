module com.yinlin.udpchat {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.yinlin.udpchat to javafx.fxml;
    exports com.yinlin.udpchat;
}