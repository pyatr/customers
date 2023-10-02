package com.customertestdatabase.RequestParsers;

import java.util.Iterator;

import org.json.simple.JSONObject;

import com.customertestdatabase.SQL.Database;

public class StatRequestParser extends AbstractRequestParser {

    public StatRequestParser(Database database, String outputFilename) {
        super(database, outputFilename);
    }

    public void ParseJSON(Iterator<JSONObject> criteriaUnit) {

    }

    @Override
    protected String GetOperationName() {
        return "stat";
    }

}
