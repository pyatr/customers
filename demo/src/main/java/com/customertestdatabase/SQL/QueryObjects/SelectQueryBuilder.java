package com.customertestdatabase.SQL.QueryObjects;

import java.util.ArrayList;

public class SelectQueryBuilder extends AbstractQueryBuilder {

    private String select = "";
    private String from = "";
    private String orderBy = "";
    private String limit = "";
    private String offset = "";
    private String groupBy = "";
    private String join = "";
    private String on = "";
    private String in = "";
    private String having = "";

    public SelectQueryBuilder Select() {
        return this.Select("*");
    }

    public SelectQueryBuilder Select(String... fields) {
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

    public SelectQueryBuilder Join(String joinType, String joinTableName, String[] on) {
        this.join = "JOIN " + joinTableName;
        if (joinType != "")
            this.join = joinType + " " + this.join;
        this.on = "ON " + String.join("", on);
        return this;
    }

    public SelectQueryBuilder GroupBy(String field) {
        this.groupBy = "GROUP BY " + field;
        return this;
    }

    public SelectQueryBuilder In(String... values) {
        this.in = "IN (";
        for (int i = 0; i < values.length; i++) {
            this.in += "'" + values[i] + "'";
            if (i < values.length - 1) {
                this.in += ",";
            }
        }
        this.in += ")";
        return this;
    }

    public SelectQueryBuilder Having(String... values) {
        this.having = String.join(" ", values);
        return this;
    }

    @Override
    protected String[] GetRequestParts() {
        String[] allParts = new String[] { select, from, join, on, where, in, or, orderBy, groupBy, having, limit,
                offset };
        ArrayList<String> realParts = new ArrayList<String>();
        for (String part : allParts) {
            if (!part.equals("")) {
                realParts.add(part);
            }
        }
        return realParts.toArray(new String[realParts.size()]);
    }
}