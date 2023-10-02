package com.customertestdatabase.SQL.QueryObjects;

public class DeleteQueryBuilder extends AbstractQueryBuilder
{
    private String delete = "";

    public DeleteQueryBuilder Delete(String tableName)
    {
        this.delete = "DELETE FROM tableName";
        return this;
    }

    @Override
    protected String[] GetRequestParts()
    {
        return new String[]{this.delete, this.where};
    }
}