module willmcg {
    requires javafx.controls;
    requires javafx.fxml;
    requires opencsv;
    requires java.naming;
    requires java.sql;
    requires java.desktop;
    requires javax.mail;

    opens willmcg to javafx.fxml;
    exports willmcg;
}