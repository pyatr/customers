package com.customertestdatabase.SQL.QueryObjects;

import java.util.Arrays;

import org.codehaus.plexus.util.StringUtils;

public abstract class AbstractQueryBuilder {
    protected String where = "";
    protected String or = "";

    public AbstractQueryBuilder Where(String... conditions) {
        String conditionsAsString = String.join(" ", conditions);
        if (conditions.length > 0) {
            this.where = "WHERE " + conditionsAsString;
        } else {
            this.where = "";
        }
        return this;
    }

    public AbstractQueryBuilder Or(String... or) {
        this.or = String.join(" ", or);
        return this;
    }

    abstract protected String[] GetRequestParts();

    public String GetQuery() {
        String[] allRequestParts = this.GetRequestParts();
        String[] cleanRequest = Arrays.stream(allRequestParts).filter(StringUtils::isNotEmpty).toArray(String[]::new);
        return String.join(" ", cleanRequest) + ";";
    }
}