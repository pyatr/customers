package com.customertestdatabase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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

        File f = new File(inputFilename);
        if (!f.exists() || !f.isFile()) {
            ErrorPrinter.Print("File at path " + inputFilename + " does not exist", outputFilename);
            return;
        }

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
            else
                ErrorPrinter.Print("Unknown operation type " + operationType, outputFilename);
        } catch (FileNotFoundException e) {
            ErrorPrinter.Print(e.toString(), outputFilename);
        } catch (IOException e) {
            ErrorPrinter.Print(e.toString(), outputFilename);
        } catch (ParseException e) {
            ErrorPrinter.Print(e.toString(), outputFilename);
        } catch (Exception e) {
            ErrorPrinter.Print(e.toString(), outputFilename);
        }
    }
}