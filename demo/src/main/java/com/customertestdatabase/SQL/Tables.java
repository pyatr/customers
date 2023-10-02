package com.customertestdatabase.SQL;

public class Tables {
    private static final String SCHEMA_NAME = "customersschema";

    private static final String TABLE_CUSTOMERS = "customers";
    private static final String TABLE_ITEMS = "items";
    private static final String TABLE_PURCHASES = "purchases";

    public static final String CUSTOMERS = String.join(".", SCHEMA_NAME, TABLE_CUSTOMERS);
    public static final String ITEMS = String.join(".", SCHEMA_NAME, TABLE_ITEMS);
    public static final String PURCHASES = String.join(".", SCHEMA_NAME, TABLE_PURCHASES);
}
