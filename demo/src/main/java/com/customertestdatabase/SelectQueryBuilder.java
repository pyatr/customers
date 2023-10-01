package com.customertestdatabase;

import java.util.ArrayList;
import java.util.List;

class SelectQueryBuilder extends AbstractQueryBuilder {
    private final static String[] EVERYTHING = new String[] { "*" };

    private String select = "";
    private String from = "";
    private String orderBy = "";
    private String limit = "";

    private String offset = "";

    public SelectQueryBuilder Select() {
        return this.Select(EVERYTHING);
    }

    public SelectQueryBuilder Select(String[] fields) {
        String fieldsAsString = "*";

        if (fields.length > 0) {
            fieldsAsString = String.join(", ", fields);
        }
        this.select = "SELECT " + fieldsAsString;
        return this;
    }

    public SelectQueryBuilder From(String table) {
        this.from = "FROM " + table;
        return this;
    }

    public SelectQueryBuilder OrderBy() {
        return this.OrderBy("1");
    }

    public SelectQueryBuilder OrderBy(String field) {
        return this.OrderBy(field, "ASC");
    }

    public SelectQueryBuilder OrderBy(String field, String order) {
        // Order by 1 means ordering by first column
        this.orderBy = "ORDER BY " + field + " " + order;
        return this;
    }

    public SelectQueryBuilder Limit() {
        return this.Limit(1);
    }

    public SelectQueryBuilder Limit(int limitCount) {
        this.limit = "LIMIT " + limitCount;
        return this;
    }

    public SelectQueryBuilder Offset(int offset) {
        this.offset = "OFFSET " + offset;
        return this;
    }

    @Override
    protected String[] GetRequestParts() {
        String[] allParts = new String[] { this.select, this.from, this.where, this.orderBy, this.limit, this.offset };
        ArrayList<String> realParts = new ArrayList<String>();
        for (String part : allParts) {
            if (!part.equals("")) {
                realParts.add(part);
            }
        }
        return realParts.toArray(new String[realParts.size()]);
    }
}