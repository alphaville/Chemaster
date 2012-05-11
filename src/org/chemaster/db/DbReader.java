/*
 *
 * ToxOtis
 *
 * ToxOtis is the Greek word for Sagittarius, that actually means ‘archer’. ToxOtis
 * is a Java interface to the predictive toxicology services of OpenTox. ToxOtis is
 * being developed to help both those who need a painless way to consume OpenTox
 * services and for ambitious service providers that don’t want to spend half of
 * their time in RDF parsing and creation.
 *
 * Copyright (C) 2009-2010 Pantelis Sopasakis & Charalampos Chomenides
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact:
 * Pantelis Sopasakis
 * chvng@mail.ntua.gr
 * Address: Iroon Politechniou St. 9, Zografou, Athens Greece
 * tel. +30 210 7723236
 *
 */


package org.chemaster.db;

import org.chemaster.db.exception.DbException;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenides
 */
public abstract class DbReader<T> extends DbOperation {

    protected String[] tableColumns;
    protected String table;
    protected String innerJoin;
    protected String where;
    protected int page = 0;
    protected int pageSize = 0;

    public String getInnerJoin() {
        return innerJoin;
    }

    public void setInnerJoin(String innerJoin) {
        this.innerJoin = innerJoin;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    protected void setTableColumns(String... columns) {
        int length = columns.length;
        tableColumns = new String[length];
        int index = 0;
        for (String col : columns) {
            tableColumns[index] = col;
            index++;
        }
    }

    public void setWhere(String where) {
        this.where = where;
    }

    @Override
    public String getSqlTemplate() {
        /*
         * Instructions:
         *  1. Columns
         *  2. (Main) Table name
         *  3. Inner Join
         *  4. Where
         *  5. Limit
         */
        return "SELECT %s FROM %s %s %s %s";
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    protected String getSql() {
        StringBuilder tableColumnsString = new StringBuilder();
        for (int i = 0; i < tableColumns.length; i++) {
            tableColumnsString.append(tableColumns[i]);
            if (i != tableColumns.length - 1) {
                tableColumnsString.append(",");
            }
        }

        StringBuilder innerJoinClause = new StringBuilder("");
        if (innerJoin != null) {
            innerJoinClause.append("INNER JOIN ");
            innerJoinClause.append(innerJoin);
            innerJoinClause.append(" ");
        }

        StringBuilder whereClause = new StringBuilder("");
        if (where != null) {
            whereClause.append("WHERE ");
            whereClause.append(where);
            whereClause.append(" ");
        }

        StringBuilder limitClause = new StringBuilder("");
        if (pageSize != 0) {
            limitClause.append("LIMIT ");
            if (page != 0) {
                limitClause.append(page * pageSize - 1);
                limitClause.append(",");
                limitClause.append(pageSize);
            } else {
                limitClause.append(pageSize);
            }
        }

        return String.format(getSqlTemplate(), tableColumnsString.toString(), table, innerJoinClause, whereClause, limitClause);
    }

    public abstract IDbIterator<T> list() throws DbException;

    
}
