package com.customertestdatabase.RequestParsers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.customertestdatabase.ErrorPrinter;
import com.customertestdatabase.SQL.Database;
import com.customertestdatabase.SQL.Tables;
import com.customertestdatabase.SQL.QueryObjects.SelectQueryBuilder;
import com.customertestdatabase.SQL.TableEntries.Purchases;

public class StatRequestParser extends AbstractRequestParser {

    public StatRequestParser(Database database, String outputFilename) {
        super(database, outputFilename);
    }

    public void ParseJSON(JSONObject json) {
        try {
            JSONObject result = new JSONObject();
            String startDate = (String) json.get("startDate");
            String endDate = (String) json.get("endDate");

            if (startDate == null) {
                ErrorPrinter.Print("No start date", outputFilename);
                return;
            }

            if (endDate == null) {
                ErrorPrinter.Print("No end date", outputFilename);
                return;
            }

            LocalDate d1 = LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate d2 = LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE);
            Duration diff = Duration.between(d1.atStartOfDay(), d2.atStartOfDay());
            long diffDays = diff.toDays();
            if (diffDays <= 0) {
                ErrorPrinter.Print("startDate is bigger than endDate (" + startDate + ">" + endDate + ")",
                        outputFilename);
                return;
            }
            result.put("type", GetOperationName());
            result.put("totalDays", diffDays);

            SelectQueryBuilder queryBuilder = new SelectQueryBuilder();
            String query = queryBuilder
                    .Select()
                    .From(Tables.PURCHASES)
                    .OrderBy(Purchases.CUSTOMER_ID)
                    .Where(Purchases.PURCHASE_DATE, ">=", "'" + startDate + "'::date",
                            "AND", Purchases.PURCHASE_DATE, "<=", "'" + endDate + "'::date")
                    .GetQuery();

            ResultSet queryResult = database.ExecuteQuery(query);
            // Customer ID
            // and IDs of every item type with how much money was spent on that item
            HashMap<Integer, HashMap<Integer, Integer>> customers = new HashMap<>();
            HashMap<Integer, Integer> totalCustomerExpenses = new HashMap<>();

            try {
                while (queryResult.next()) {
                    Integer customerID = queryResult.getInt(Purchases.CUSTOMER_ID);
                    Integer itemID = queryResult.getInt(Purchases.ITEM_ID);
                    Integer itemPrice = GetItemPrice(itemID);

                    if (!customers.containsKey(customerID)) {
                        HashMap<Integer, Integer> newItem = new HashMap<>();
                        newItem.put(itemID, itemPrice);
                        customers.put(customerID, newItem);
                    } else {
                        HashMap<Integer, Integer> customerPurchases = customers.get(customerID);
                        if (!customerPurchases.containsKey(itemID)) {
                            customerPurchases.put(itemID, itemPrice);
                        } else {
                            Integer moneySpentOnItem = customerPurchases.get(itemID);
                            moneySpentOnItem += itemPrice;
                            customerPurchases.replace(itemID, moneySpentOnItem);
                        }
                    }

                    if (!totalCustomerExpenses.containsKey(customerID)) {
                        totalCustomerExpenses.put(customerID, itemPrice);
                    } else {
                        Integer totalSpentByThisCustomer = totalCustomerExpenses.get(customerID);
                        totalSpentByThisCustomer += itemPrice;
                        totalCustomerExpenses.replace(customerID, totalSpentByThisCustomer);
                    }
                }
            } catch (SQLException e) {
                ErrorPrinter.Print(e.toString(), outputFilename);
            }

            if (customers.size() == 0) {
                result.put("customers", new JSONArray());
                result.put("totalExpenses", 0);
                result.put("avgExpenses", 0);
                this.WriteJSON(result, outputFilename);
                return;
            }

            Integer totalExpenses = 0;
            JSONArray customersArray = new JSONArray();
            Integer[] customersIDs = customers.keySet().toArray(new Integer[0]);
            ArrayList<String[]> customerData = GetCustomersWithIDs(new ArrayList<Integer>(Arrays.asList(customersIDs)));
            for (int i = 0; i < customersIDs.length; i++) {
                JSONObject currentCustomer = new JSONObject();
                String customerFullName = String.join(" ", customerData.get(i)[0], customerData.get(i)[1]);
                JSONArray currentCustomerPurchasesJSON = new JSONArray();

                Integer customerID = customersIDs[i];
                HashMap<Integer, Integer> currentCustomerPurchases = customers.get(customerID);

                currentCustomerPurchases.forEach((purchaseID, purchaseTotalCost) -> {
                    JSONObject purchase = new JSONObject();
                    purchase.put("name", GetItemName(purchaseID));
                    purchase.put("expenses", purchaseTotalCost);
                    currentCustomerPurchasesJSON.add(purchase);
                });

                currentCustomer.put("purchases", currentCustomerPurchasesJSON);
                currentCustomer.put("name", customerFullName);
                currentCustomer.put("totalExpenses", totalCustomerExpenses.get(customerID));
                totalExpenses += totalCustomerExpenses.get(customerID);
                customersArray.add(currentCustomer);
            }
            result.put("customers", customersArray);
            result.put("totalExpenses", totalExpenses);
            result.put("avgExpenses", totalExpenses / customersIDs.length);

            this.WriteJSON(result, outputFilename);
        } catch (Exception e) {
            ErrorPrinter.Print(e.toString(), outputFilename);
        }
    }

    @Override
    protected String GetOperationName() {
        return "stat";
    }

}
