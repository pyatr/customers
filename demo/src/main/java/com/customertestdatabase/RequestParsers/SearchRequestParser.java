package com.customertestdatabase.RequestParsers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.customertestdatabase.ErrorPrinter;
import com.customertestdatabase.SQL.Database;
import com.customertestdatabase.SQL.Tables;
import com.customertestdatabase.SQL.QueryObjects.SelectQueryBuilder;
import com.customertestdatabase.SQL.TableEntries.Customers;
import com.customertestdatabase.SQL.TableEntries.Purchases;

public class SearchRequestParser extends AbstractRequestParser {
    public SearchRequestParser(Database database, String outputFilename) {
        super(database, outputFilename);
    }

    public void ParseJSON(JSONObject json) {
        try {
            JSONArray criterias = (JSONArray) json.get("criterias");
            Iterator<JSONObject> criteriaUnit = criterias.iterator();
            JSONArray resultArray = new JSONArray();
            while (criteriaUnit.hasNext()) {
                JSONObject currentCriteria = criteriaUnit.next();
                Set<String> keySet = currentCriteria.keySet();
                keySet.forEach(criteriaType -> {
                    switch (criteriaType) {
                        case "lastName":
                            resultArray.add(this.GetCustomersWithLastName((String) currentCriteria.get(criteriaType)));
                            break;
                        case "productName":
                            String productName = (String) currentCriteria.get("productName");
                            Long minBought = (Long) currentCriteria.get("minTimes");
                            resultArray.add(this.GetMinBought(productName, minBought));
                            break;
                        case "minExpenses":
                            Long minExp = (Long) currentCriteria.get("minExpenses");
                            Long maxExp = (Long) currentCriteria.get("maxExpenses");
                            resultArray.add(this.GetCustomersWithExpensesWithinRange(minExp, maxExp));
                            break;
                        case "badCustomers":
                            Long badCustomers = (Long) currentCriteria.get("badCustomers");
                            resultArray.add(this.GetCustomersThatBoughtLessThan(badCustomers));
                            break;
                    }
                });
            }
            Boolean hasErrors = (resultArray.indexOf(null) != -1);
            if (!hasErrors) {
                JSONObject result = new JSONObject();
                result.put("type", GetOperationName());
                result.put("results", resultArray);
                this.WriteJSON(result, outputFilename);
            }
        } catch (Exception e) {
            ErrorPrinter.Print(e.toString(), outputFilename);
            return;
        }
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

    private JSONObject GetMinBought(String productName, Long minBoughtCount) {
        if (productName == null) {
            ErrorPrinter.Print("No product name specified", outputFilename);
            return null;
        }

        if (minBoughtCount == null) {
            ErrorPrinter.Print("No minBoughtCount specified for product " + productName, outputFilename);
            return null;
        }

        if (minBoughtCount < 1) {
            ErrorPrinter.Print("minBoughtCount (" + minBoughtCount + ") is less than 1", outputFilename);
            return null;
        }

        JSONObject container = new JSONObject();
        JSONObject criteria = new JSONObject();
        JSONArray resultJsonArray = new JSONArray();
        criteria.put("productName", productName);
        criteria.put("minTimes", minBoughtCount);
        container.put("criteria", criteria);

        Integer itemID = GetItemIDByProductName(productName);
        if (itemID == -1) {
            ErrorPrinter.Print("Item " + productName + " does not exist", outputFilename);
            return null;
        }

        Object[] boughtTimesResult = GetCustomersBoughtTimes(itemID, minBoughtCount);
        ArrayList<Integer> validIDs = (ArrayList<Integer>) boughtTimesResult[0];
        ArrayList<Integer> allBoughtTimes = (ArrayList<Integer>) boughtTimesResult[1];
        if (validIDs.size() > 0) {
            ArrayList<String[]> customers = GetCustomersWithIDs(validIDs);

            for (int i = 0; i < customers.size(); i++) {
                HashMap<String, String> purchases = new HashMap<>();
                purchases.put(Customers.NAME, customers.get(i)[0]);
                purchases.put(Customers.SURNAME, customers.get(i)[1]);
                purchases.put("bought_times", allBoughtTimes.get(i).toString());
                JSONObject purchaseObject = new JSONObject(purchases);
                resultJsonArray.add(purchaseObject);
            }
        }
        container.put("results", resultJsonArray);
        return container;
    }

    private JSONObject GetCustomersWithExpensesWithinRange(Long minExpenses, Long maxExpenses) {
        if (minExpenses == null) {
            ErrorPrinter.Print("No min expenses specified", outputFilename);
            return null;
        }

        if (maxExpenses == null) {
            ErrorPrinter.Print("No max expenses specified", outputFilename);
            return null;
        }

        if (minExpenses < 0) {
            ErrorPrinter.Print("Min expenses (" + minExpenses + ") are less than 0", outputFilename);
            return null;
        }

        if (maxExpenses < 1) {
            ErrorPrinter.Print("Max expenses (" + maxExpenses + ") are less than 1", outputFilename);
            return null;
        }

        if (minExpenses > maxExpenses) {
            ErrorPrinter.Print(
                    "Min expenses (" + minExpenses + ") bigger than max expenses (" + maxExpenses + ")",
                    outputFilename);
            return null;
        }
        JSONObject container = new JSONObject();
        JSONObject criteria = new JSONObject();
        JSONArray resultJsonArray = new JSONArray();
        criteria.put("minExpenses", minExpenses);
        criteria.put("maxExpenses", maxExpenses);
        container.put("criteria", criteria);

        HashMap<Integer, Integer> moneySpentByCustomers = GetMoneySpentByCustomers();
        Object[] customers = moneySpentByCustomers.keySet().toArray();
        ArrayList<Integer> activeCustomerIDs = new ArrayList<>();
        ArrayList<Integer> activeCustomerSpentMoney = new ArrayList<>();
        for (int i = 0; i < moneySpentByCustomers.size(); i++) {
            Integer spentByCustomer = moneySpentByCustomers.get(customers[i]);
            if (spentByCustomer >= minExpenses && spentByCustomer <= maxExpenses) {
                activeCustomerIDs.add(Integer.parseInt(customers[i].toString()));
                activeCustomerSpentMoney.add(spentByCustomer);
            }
        }

        ArrayList<String[]> activeCustomerData = GetCustomersWithIDs(activeCustomerIDs);
        for (int i = 0; i < activeCustomerData.size(); i++) {
            HashMap<String, String> purchases = new HashMap<>();
            purchases.put(Customers.NAME, activeCustomerData.get(i)[0]);
            purchases.put(Customers.SURNAME, activeCustomerData.get(i)[1]);
            purchases.put("money_spent", activeCustomerSpentMoney.get(i).toString());
            JSONObject purchaseObject = new JSONObject(purchases);
            resultJsonArray.add(purchaseObject);
        }

        container.put("results", resultJsonArray);

        return container;
    }

    private JSONObject GetCustomersThatBoughtLessThan(Long minBuyCount) {
        if (minBuyCount == null) {
            ErrorPrinter.Print("No minBuyCount specified for customers who bought less than X", outputFilename);
            return null;
        }

        if (minBuyCount < 1) {
            ErrorPrinter.Print("minBuyCount " + minBuyCount + " is less than 1", outputFilename);
            return null;
        }

        JSONObject container = new JSONObject();
        JSONObject criteria = new JSONObject();
        JSONArray resultJsonArray = new JSONArray();
        criteria.put("badCustomers", minBuyCount);
        container.put("criteria", criteria);

        SelectQueryBuilder queryBuilder = new SelectQueryBuilder();
        String countCustomer = "COUNT(" + Purchases.CUSTOMER_ID + ")";
        String query = queryBuilder
                .Select(Purchases.CUSTOMER_ID, countCustomer)
                .From(Tables.PURCHASES)
                .GroupBy(Purchases.CUSTOMER_ID)
                .Having("HAVING", countCustomer, "<=", minBuyCount.toString())
                .GetQuery();

        ResultSet result = database.ExecuteQuery(query);

        ArrayList<Integer> ids = new ArrayList<>();
        ArrayList<Integer> boughtThisMuch = new ArrayList<>();

        try {
            while (result.next()) {
                ids.add(result.getInt(1));
                boughtThisMuch.add(result.getInt(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        ArrayList<String[]> customers = GetCustomersWithIDs(ids);

        for (int i = 0; i < customers.size(); i++) {
            HashMap<String, String> purchases = new HashMap<>();
            purchases.put(Customers.NAME, customers.get(i)[0]);
            purchases.put(Customers.SURNAME, customers.get(i)[1]);
            purchases.put("bought_this_much", boughtThisMuch.get(i).toString());
            JSONObject purchaseObject = new JSONObject(purchases);
            resultJsonArray.add(purchaseObject);
        }

        container.put("results", resultJsonArray);

        return container;
    }

    @Override
    protected String GetOperationName() {
        return "search";
    }

}
