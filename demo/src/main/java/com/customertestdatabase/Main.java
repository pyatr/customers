package com.customertestdatabase;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.customertestdatabase.RequestParsers.AbstractRequestParser;
import com.customertestdatabase.RequestParsers.SearchRequestParser;
import com.customertestdatabase.RequestParsers.StatRequestParser;
import com.customertestdatabase.SQL.Database;

public class Main {
    public static void main(String[] args) {
        Main main = new Main();

        String requestType = args[0];
        String inputFileName = args[1];
        String outputFileName = args[2];
        main.OpenInput(requestType, inputFileName, outputFileName);
    }

    private Database database;

    Main() {
        database = new Database();
    }

    public void OpenInput(String operationType, String inputFilename, String outputFilename) {
        JSONParser parser = new JSONParser();

        try {
            Object obj = parser.parse(new FileReader(inputFilename));
            JSONObject jsonObject = (JSONObject) obj;

            AbstractRequestParser requestParser = null;
            switch (operationType) {
                case "search":
                    requestParser = new SearchRequestParser(database, outputFilename);
                    break;
                case "stat":
                    requestParser = new StatRequestParser(database, outputFilename);
                    break;
            }
            if (requestParser != null)
                requestParser.ParseJSON(jsonObject);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}