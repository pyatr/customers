package com.customertestdatabase.RequestParsers;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import org.json.simple.JSONObject;

import com.customertestdatabase.SQL.Database;

public abstract class AbstractRequestParser {
    protected Database database;
    protected String outputFilename;

    public AbstractRequestParser(Database database, String outputFilename) {
        this.database = database;
        this.outputFilename = outputFilename;
    }

    public abstract void ParseJSON(Iterator<JSONObject> criteriaUnit);

    protected abstract String GetOperationName();

    protected void WriteJSON(JSONObject json, String outputFilename) {

        Writer fstream = null;
        try {
            fstream = new OutputStreamWriter(new FileOutputStream(outputFilename), StandardCharsets.UTF_8);
            fstream.write(json.toJSONString());
            fstream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
