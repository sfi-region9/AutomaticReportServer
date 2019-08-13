package fr.colin.arsext.utils;

import javafx.scene.chart.PieChart;

public class DatabaseWrapper {

    private Database db;

    public DatabaseWrapper(Database db) {
        this.db = db;
    }

    public Database getDb() {
        return db;
    }
}
