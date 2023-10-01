package com.customertestdatabase;

class UpdateQueryBuilder extends AbstractQueryBuilder {
    private String update = "";

    public UpdateQueryBuilder Update(String table, String[] columnNames, String[] values) {
        for (int i = 0; i < values.length; i++) {
            columnNames[i] = columnNames[i] + "='" + values[i] + "'";
        }
        String columnsAsString = String.join(", ", columnNames);
        this.update = "UPDATE table SET " + columnsAsString;
        return this;
    }

    @Override
    protected String[] GetRequestParts() {
        return new String[] { this.update, this.where };
    }
}