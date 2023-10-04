package com.customertestdatabase;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.json.simple.JSONObject;

public class ErrorPrinter {
    public static void Print(String message, String outputFilename) {
        JSONObject json = new JSONObject();
        json.put("type", "error");
        json.put("message", message);

        Writer fstream = null;
        try {
            fstream = new OutputStreamWriter(new FileOutputStream(outputFilename), StandardCharsets.UTF_8);
            fstream.write(json.toJSONString());
            fstream.close();
            System.out.println("Error encountered, see " + outputFilename + " for details");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
