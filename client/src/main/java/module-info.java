module com.filekeeper.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires io.netty.codec;
    requires cloud;


    opens com.filekeeper.client to javafx.fxml;
    exports com.filekeeper.client;
}