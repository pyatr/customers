package com.customertestdatabase.RequestParsers;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.customertestdatabase.ErrorPrinter;
import com.customertestdatabase.SQL.Database;
import com.customertestdatabase.SQL.Tables;
import com.customertestdatabase.SQL.QueryObjects.SelectQueryBuilder;
import com.customertestdatabase.SQL.TableEntries.Customers;
import com.customertestdatabase.SQL.TableEntries.Items;
import com.customertestdatabase.SQL.TableEntries.Purchases;

public abstract class AbstractRequestParser {
    protected Database database;
    protected String outputFilename;

    public AbstractRequestParser(Database database, String outputFilename) {
        this.database = database;
        this.outputFilename = outputFilename;
    }

    protected Object[] GetCustomersBoughtTimes(Integer itemID, Long minBoughtCount) {
        SelectQueryBuilder queryBuilder = new SelectQueryBuilder();
        String customersThatBoughtQuery = queryBuilder
                .Select(Purchases.CUSTOMER_ID, "COUNT(" + Purchases.CUSTOMER_ID + ")")
                .From(Tables.PURCHASES)
                .GroupBy(Purchases.CUSTOMER_ID)
                .Where(Purchases.ITEM_ID, "=", "'" + itemID + "'")
                .GetQuery();
        ResultSet result = database.ExecuteQuery(customersThatBoughtQuery);

        ArrayList<Integer> validIDs = new ArrayList<>(0);
        ArrayList<Integer> allBoughtTimes = new ArrayList<>(0);
        try {
            while (result.next()) {
                Integer boughtTimes = result.getInt(2);
                if (boughtTimes >= minBoughtCount) {
                    validIDs.add(result.getInt(Purchases.CUSTOMER_ID));
                    allBoughtTimes.add(boughtTimes);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Object[] { validIDs, allBoughtTimes };
    }

    protected Integer GetItemIDByProductName(String productName) {
        SelectQueryBuilder queryBuilder = new SelectQueryBuilder();
        String itemIDQuery = queryBuilder
                .Select(Items.ID)
                .From(Tables.ITEMS)
                .Where(Items.ITEM_NAME, "=", "'" + productName + "'")
                .GetQuery();

        ResultSet result = database.ExecuteQuery(itemIDQuery);
        Integer itemID = -1;
        try {
            while (result.next()) {
                itemID = result.getInt(1);
            }
            return itemID;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    protected ArrayList<String[]> GetCustomersWithIDs(ArrayList<Integer> ids) {
        ArrayList<String> inConditions = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            inConditions.add(ids.get(i).toString());
        }
        ArrayList<String[]> customers = new ArrayList<String[]>();
        if (ids.size() > 0) {
            SelectQueryBuilder customersSurnamesQueryBuilder = new SelectQueryBuilder();
            String customersSurnamesQuery = customersSurnamesQueryBuilder
                    .Select(Customers.NAME, Customers.SURNAME)
                    .From(Tables.CUSTOMERS)
                    .In(inConditions.toArray(new String[0]))
                    .Where(Customers.ID)
                    .GetQuery();
            ResultSet result = database.ExecuteQuery(customersSurnamesQuery);

            try {
                while (result.next()) {
                    customers.add(
                            new String[] { result.getString(Customers.NAME), result.getString(Customers.SURNAME) });
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return customers;
    }

    protected ArrayList<Integer> GetAllCustomers() {
        ArrayList<Integer> customerIDs = new ArrayList<>();
        SelectQueryBuilder queryBuilder = new SelectQueryBuilder();
        String query = queryBuilder
                .Select(Customers.ID)
                .From(Tables.CUSTOMERS)
                .GetQuery();
        ResultSet result = database.ExecuteQuery(query);
        try {
            while (result.next()) {
                customerIDs.add(result.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customerIDs;
    }

    protected ArrayList<Integer> GetCustomerPurchasedItems(Integer customerID) {
        ArrayList<Integer> itemIDs = new ArrayList<>();
        SelectQueryBuilder queryBuilder = new SelectQueryBuilder();
        String query = queryBuilder
                .Select(Purchases.ITEM_ID)
                .From(Tables.PURCHASES)
                .Where(Purchases.CUSTOMER_ID, "=", customerID.toString())
                .GetQuery();
        ResultSet result = database.ExecuteQuery(query);
        try {
            while (result.next()) {
                itemIDs.add(result.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return itemIDs;
    }

    protected Integer GetItemPrice(Integer itemID) {
        Integer price = 0;
        SelectQueryBuilder queryBuilder = new SelectQueryBuilder();
        String query = queryBuilder
                .Select(Items.ITEM_PRICE)
                .From(Tables.ITEMS)
                .Where(Items.ID, "=", itemID.toString())
                .GetQuery();
        ResultSet result = database.ExecuteQuery(query);
        try {
            while (result.next()) {
                price = result.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return price;
    }

    protected String GetItemName(Integer itemID) {
        String name = "";
        SelectQueryBuilder queryBuilder = new SelectQueryBuilder();
        String query = queryBuilder
                .Select(Items.ITEM_NAME)
                .From(Tables.ITEMS)
                .Where(Items.ID, "=", itemID.toString())
                .GetQuery();
        ResultSet result = database.ExecuteQuery(query);
        try {
            while (result.next()) {
                name = result.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return name;
    }

    protected HashMap<Integer, Integer> GetMoneySpentByCustomers() {
        HashMap<Integer, Integer> customerPurchasedAmount = new HashMap<>();
        HashMap<Integer, Integer> itemPrices = new HashMap<>();
        ArrayList<Integer> customerIDs = GetAllCustomers();
        customerIDs.forEach(customerID -> {
            Integer purchasedAmount = 0;
            ArrayList<Integer> purchasedItems = GetCustomerPurchasedItems(customerID);
            for (int i = 0; i < purchasedItems.size(); i++) {
                Integer currentItemID = purchasedItems.get(i);
                if (itemPrices.containsKey(currentItemID)) {
                    purchasedAmount += itemPrices.get(currentItemID);
                } else {
                    Integer currentItemPrice = GetItemPrice(currentItemID);
                    itemPrices.put(currentItemID, currentItemPrice);
                    purchasedAmount += currentItemPrice;
                }
            }
            customerPurchasedAmount.put(customerID, purchasedAmount);
        });
        return customerPurchasedAmount;
    }

    public abstract void ParseJSON(JSONObject json);

    protected abstract String GetOperationName();

    protected void WriteJSON(JSONObject json, String outputFilename) {
        Writer fstream = null;
        try {
            fstream = new OutputStreamWriter(new FileOutputStream(outputFilename), StandardCharsets.UTF_8);
            fstream.write(json.toJSONString());
            fstream.close();
        } catch (IOException e) {
            e.printStackTrace();
            ErrorPrinter.Print(e.toString(), outputFilename);
        }
    }
}
