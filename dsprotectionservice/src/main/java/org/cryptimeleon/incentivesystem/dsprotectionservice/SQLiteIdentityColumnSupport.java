package org.cryptimeleon.incentivesystem.dsprotectionservice;

import org.hibernate.MappingException;
import org.hibernate.dialect.identity.IdentityColumnSupportImpl;

// TODO: for simplicity: only integer-typed identity columns supported, maybe we need to change this later on

/**
 * Specifies how used SQL implementation (here: SQLite) handles identity columns (@Id columns).
 * In particular:
 */
public class SQLiteIdentityColumnSupport extends IdentityColumnSupportImpl {
    /** whether the dialect supports identity column key generation */
    @Override
    public boolean supportsIdentityColumns(){
        return true;
    }

    /** returns the select command to retrieve the last generated IDENTITY value for a particular table */
    @Override
    public String getIdentitySelectString(String table, String column, int type) throws MappingException {
        return "select last_insert_rowid()";
    }

    /**
     * Yields the syntax used in DDL (data definition language) of the dialect to define a column as identifying for data records in a data table.
     * @param type type of the column
     * @return syntax used in DDL
     */
    @Override
    public String getIdentityColumnString(int type) {
        return "integer";
    }
}
