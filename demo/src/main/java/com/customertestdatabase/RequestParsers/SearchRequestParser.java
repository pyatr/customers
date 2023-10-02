package com.customertestdatabase.RequestParsers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.customertestdatabase.SQL.Database;
import com.customertestdatabase.SQL.Tables;
import com.customertestdatabase.SQL.QueryObjects.SelectQueryBuilder;
import com.customertestdatabase.SQL.TableEntries.Customers;
import com.customertestdatabase.SQL.TableEntries.Items;
import com.customertestdatabase.SQL.TableEntries.Purchases;

public class SearchRequestParser extends AbstractRequestParser {
    public SearchRequestParser(Database database, String outputFilename) {
        super(database, outputFilename);
    }

    public void ParseJSON(Iterator<JSONObject> criteriaUnit) {
        JSONArray resultArray = new JSONArray();
        while (criteriaUnit.hasNext()) {
            JSONObject currentCriteria = criteriaUnit.next();
            Set<String> keySet = currentCriteria.keySet();
            keySet.forEach(criteriaType -> {
                Object criteriaValue = currentCriteria.get(criteriaType);
                switch (criteriaType) {
                    case "lastName":
                        resultArray.add(this.GetCustomersWithLastName((String) criteriaValue));
                        break;
                    case "productName":
                        String productName = (String) currentCriteria.get("productName");
                        Long minBought = (Long) currentCriteria.get("minTimes");
                        resultArray.add(this.GetMinBought(productName, minBought));
                        break;
                    case "minExpenses":
                        // case "maxExpenses":

                        break;
                    case "badCustomers":

                        break;
                }
            });
        }
        JSONObject result = new JSONObject();
        result.put("type", GetOperationName());
        result.put("results", resultArray);
        this.WriteJSON(result, outputFilename);
    }

    private JSONObject GetCustomersWithLastName(String surname) {

        SelectQueryBuilder queryBuilder = new SelectQueryBuilder();
        String query = queryBuilder
                .Select(Customers.NAME, Customers.SURNAME)
                .From(Tables.CUSTOMERS)
                .Where(Customers.SURNAME, "=", "'" + surname + "'")
                .GetQuery();
        ResultSet result = database.ExecuteQuery(query);

        JSONObject container = new JSONObject();
        JSONObject criteria = new JSONObject();
        criteria.put("lastName", surname);
        try {
            JSONArray resultJsonArray = new JSONArray();
            while (result.next()) {
                HashMap<String, String> customersInResultMap = new HashMap<String, String>();
                customersInResultMap.put("lastName", result.getString(Customers.NAME));
                customersInResultMap.put("firstName", result.getString(Customers.SURNAME));
                JSONObject customer = new JSONObject(customersInResultMap);
                resultJsonArray.add(customer);
            }
            container.put("criteria", criteria);
            container.put("results", resultJsonArray);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return container;
    }

    private Integer GetItemIDByProductName(String productName) {
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

    private Object[] GetCustomersBoughtTimes(Integer itemID, Long minBoughtCount) {
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

    private JSONObject GetMinBought(String productName, Long minBoughtCount) {
        JSONObject container = new JSONObject();
        JSONObject criteria = new JSONObject();
        JSONArray resultJsonArray = new JSONArray();
        criteria.put("productName", productName);
        criteria.put("minTimes", minBoughtCount);
        container.put("criteria", criteria);

        Integer itemID = GetItemIDByProductName(productName);
        if (itemID == -1) {
            System.out.println("That item does not exist");
            return container;
        }

        Object[] boughtTimesResult = GetCustomersBoughtTimes(itemID, minBoughtCount);
        ArrayList<Integer> validIDs = (ArrayList<Integer>) boughtTimesResult[0];
        ArrayList<Integer> allBoughtTimes = (ArrayList<Integer>) boughtTimesResult[1];
        if (validIDs.size() == 0) {
            System.out.println("Nobody bought that much");
            return container;
        }

        ArrayList<String> orConditions = new ArrayList<>();
        for (int i = 1; i < validIDs.size(); i++) {
            orConditions.add("OR");
            orConditions.add(Customers.ID);
            orConditions.add("=");
            orConditions.add("'" + validIDs.get(i).toString() + "'");
        }

        SelectQueryBuilder customersSurnamesQueryBuilder = new SelectQueryBuilder();
        String customersSurnamesQuery = customersSurnamesQueryBuilder
                .Select(Customers.NAME, Customers.SURNAME)
                .From(Tables.CUSTOMERS)
                .Where(Customers.ID, "=", "'" + validIDs.get(0).toString() + "'")
                .Or(orConditions.toArray(new String[0]))
                .GetQuery();
        ResultSet result = database.ExecuteQuery(customersSurnamesQuery);

        try {
            int i = 0;
            while (result.next()) {
                HashMap<String, String> purchases = new HashMap<String, String>();
                purchases.put(Customers.NAME, result.getString(Customers.NAME));
                purchases.put(Customers.SURNAME, result.getString(Customers.SURNAME));
                purchases.put("bought_times", allBoughtTimes.get(i).toString());
                JSONObject purchaseObject = new JSONObject(purchases);
                resultJsonArray.add(purchaseObject);
                i++;
            }
            container.put("results", resultJsonArray);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return container;
    }

    @Override
    protected String GetOperationName() {
        return "search";
    }

}
