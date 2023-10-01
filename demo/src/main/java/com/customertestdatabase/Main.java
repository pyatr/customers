package com.customertestdatabase;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Main {
    private final String DATABASE_URL = "jdbc:postgresql://localhost/customersdb";
    private final String DATABASE_USER = "postgres";
    private final String DATABASE_PASSWORD = "root";

    private final String SCHEMA_NAME = "customersschema";

    private final String TABLE_CUSTOMERS = "customers";
    private final String TABLE_ITEMS = "items";
    private final String TABLE_PURCHASES = "purchases";

    private final String SCHEMA_CUSTOMERS = String.join(".", SCHEMA_NAME, TABLE_CUSTOMERS);
    private final String SCHEMA_ITEMS = String.join(".", SCHEMA_NAME, TABLE_ITEMS);
    private final String SCHEMA_PURCHASES = String.join(".", SCHEMA_NAME, TABLE_PURCHASES);

    private final String ENTRY_ID = "ID";

    private final String ENTRY_PERSONAL_NAME = "person_name";
    private final String ENTRY_SURNAME = "surname";

    private final String ENTRY_ITEM_NAME = "item_name";
    private final String ENTRY_ITEM_PRICE = "item_price";

    private final String ENTRY_PURCHASE_CUSTOMER_ID = "customer_id";
    private final String ENTRY_PURCHASE_ITEM_ID = "item_id";
    private final String ENTRY_PURCHASE_DATE = "purchase_date";

    Main() {
    }

    private ResultSet GetData(String schemaTable, String[] conditions) {
        Connection conn = connect();
        try {
            SelectQueryBuilder queryBuilder = new SelectQueryBuilder();
            String query = queryBuilder.Select().From(schemaTable).Where(conditions).GetQuery();
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            System.out.println(query);
            return preparedStatement.executeQuery();
        } catch (Exception ex) {
            System.out.println(ex);
            return null;
        }
    }

    public static void main(String[] args) {
        // System.out.println(System.getProperty("user.dir"));
        Main main = new Main();

        for (int i = 0; i < args.length; i++) {
            // System.out.println(args[i]);
            switch (i) {
                case 1:
                    main.OpenInput(args[1]);
                    break;
            }
        }
    }

    private void PrintResultSet(ResultSet resultSet) {
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

    private void ParseJSON(Iterator<JSONObject> criteriaUnit) {
        while (criteriaUnit.hasNext()) {
            JSONObject currentCriteria = criteriaUnit.next();
            Set<String> keySet = currentCriteria.keySet();
            // System.out.print("New criteria!\n");
            keySet.forEach(criteriaType -> {
                Object keyvalue = currentCriteria.get(criteriaType);
                // System.out.println("key: " + criteriaType + " value: " + keyvalue);
                switch (criteriaType) {
                    case "lastName":
                        String[] conditions = new String[] { ENTRY_SURNAME, "=", "'" + (String) keyvalue + "'" };
                        ResultSet result = this.GetData(SCHEMA_CUSTOMERS, conditions);
                        this.PrintResultSet(result);
                        break;
                    case "productName":
                    case "minTimes":
                        break;
                    case "minExpenses":
                    case "maxExpenses":

                        break;
                    case "badCustomers":

                        break;
                }
            });
        }

    }

    public void OpenInput(String inputPath) {
        JSONParser parser = new JSONParser();

        try {
            Object obj = parser.parse(new FileReader(inputPath));

            JSONObject jsonObject = (JSONObject) obj;

            JSONArray criterias = (JSONArray) jsonObject.get("criterias");

            Iterator<JSONObject> criteriaUnit = criterias.iterator();
            this.ParseJSON(criteriaUnit);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return conn;
    }
}