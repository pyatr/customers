package com.customertestdatabase.SQL;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Database {
    private Connection connection = null;
    private String url = "";
    private String user = "";
    private String password = "";

    public Database() {
        JSONParser parser = new JSONParser();

        try {
            Object obj = parser.parse(new FileReader("database_connect.json"));
            JSONObject jsonObject = (JSONObject) obj;

            url = (String) jsonObject.get("url");
            user = (String) jsonObject.get("user");
            password = (String) jsonObject.get("password");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        connection = connect();
    }

    public ResultSet ExecuteQuery(String query) {
        if (connection == null)
            connection = connect();
        if (connection == null) {
            System.out.println("No connection for query!");
            return null;
        }
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            return preparedStatement.executeQuery();
        } catch (Exception ex) {
            System.out.println(ex);
            return null;
        }
    }

    public void PrintResultSet(ResultSet resultSet) {
        try {
            System.out.println(resultSet.getMetaData().getColumnCount() + "/" + resultSet.getMetaData().toString());
            while (resultSet.next()) {
                int columnCount = resultSet.getMetaData().getColumnCount();
                for (int i = 0; i < columnCount; i++) {
                    System.out.println(resultSet.getObject(i + 1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return conn;
    }
}
