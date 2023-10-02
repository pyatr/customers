package com.customertestdatabase.SQL.QueryObjects;

public class InsertQueryBuilder extends AbstractQueryBuilder {
    private String insert = "";

    public InsertQueryBuilder insert(String table, String[] columnNames, String[] values) {
        String columnsAsString = String.join(", ", columnNames);
        for (int i = 0; i < values.length; i++) {
            values[i] = "'" + values[i] + "'";
        }
        String valuesAsString = String.join(", ", values);
        this.insert = "INSERT INTO table (" + columnsAsString + ") VALUES(" + valuesAsString + ")";
        return this;
    }

    @Override
    protected String[] GetRequestParts() {
        return new String[] { this.insert };
    }
}