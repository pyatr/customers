package com.customertestdatabase.RequestParsers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.customertestdatabase.SQL.Database;
import com.customertestdatabase.SQL.Tables;
import com.customertestdatabase.SQL.QueryObjects.SelectQueryBuilder;
import com.customertestdatabase.SQL.TableEntries.Customers;
import com.customertestdatabase.SQL.TableEntries.Purchases;

public class StatRequestParser extends AbstractRequestParser {

    public StatRequestParser(Database database, String outputFilename) {
        super(database, outputFilename);
    }

    public void ParseJSON(JSONObject json) {
        String startDate = (String) json.get("startDate");
        String endDate = (String) json.get("endDate");

        System.out.println(startDate);
        System.out.println(endDate);

        SelectQueryBuilder queryBuilder = new SelectQueryBuilder();
        String query = queryBuilder
                .Select()
                .From(Tables.PURCHASES)
                .Where(Purchases.PURCHASE_DATE, ">=", "'" + startDate + "'::date",
                        "AND", Purchases.PURCHASE_DATE, "<=", "'" + endDate + "'::date")
                .GetQuery();

        ResultSet queryResult = database.ExecuteQuery(query);

        try {
            while (queryResult.next()) {
                System.out.println(queryResult.getString(Purchases.CUSTOMER_ID));
                System.out.println(queryResult.getString(Purchases.ITEM_ID));
                System.out.println(queryResult.getString(Purchases.PURCHASE_DATE));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JSONObject result = new JSONObject();
        result.put("type", GetOperationName());
        // result.put("results", resultArray);
        this.WriteJSON(result, outputFilename);
    }

    @Override
    protected String GetOperationName() {
        return "stat";
    }

}
