package com.customertestdatabase;

import java.util.Arrays;

import org.codehaus.plexus.util.StringUtils;

abstract class AbstractQueryBuilder {
    protected String where = "";

    public AbstractQueryBuilder Where(String[] conditions) {
        String conditionsAsString = String.join(" ", conditions);
        if (conditions.length > 0) {
            this.where = "WHERE " + conditionsAsString;
        } else {
            this.where = "";
        }
        return this;
    }

    abstract protected String[] GetRequestParts();

    public String GetQuery() {
        String[] allRequestParts = this.GetRequestParts();
        String[] cleanRequest = Arrays.stream(allRequestParts).filter(StringUtils::isNotEmpty).toArray(String[]::new);
        return String.join(" ", cleanRequest) + ";";
    }
}