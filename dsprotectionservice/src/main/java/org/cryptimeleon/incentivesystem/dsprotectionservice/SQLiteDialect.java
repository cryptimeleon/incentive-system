package org.cryptimeleon.incentivesystem.dsprotectionservice;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.identity.IdentityColumnSupport;

import java.sql.Types;

/**
 * Implements Hibernate compatibility with SQLite. This means its objects tell Hibernate which SQL features are implemented by SQLite.
 */
public final class SQLiteDialect extends Dialect {
    public SQLiteDialect(){
        registerColumnType(Types.BIT, "integer"); // register a data type with its code (integer constant) and name
        registerColumnType(Types.TINYINT, "tinyint");
        registerColumnType(Types.SMALLINT, "integer");
        registerColumnType(Types.INTEGER, "integer");
        // TODO: add other type registrations
    }

    /**
     * @return object specifying how this SQL dialect handles identifying columns
     */
    @Override
    public IdentityColumnSupport getIdentityColumnSupport(){
        return new SQLiteIdentityColumnSupport();
    }

    /** @return whether the dialect supports ALTER TABLE syntax */
    @Override
    public boolean hasAlterTable() {
        return false;
    }

    /** @return whether we need to drop constraints before we can drop tables in this dialect */
    @Override
    public boolean dropConstraints() {
        return false;
    }

    /**
     * Hibernate JavaDocs do not provide any explanation here, my educated guess would be that this returns the syntax for removing the foreign key property from a
     * column of a table.
     */
    @Override
    public String getDropForeignKeyString() {
        return "";
    }

    /**
     * Returns syntax used to add a foreign key constraint to a table.
     * @param constraintName name of the constraint to be added
     * @param foreignKey names of the columns that make up the foreign key
     * @param refTable the table referenced by the foreign key
     * @param primaryKey the columns forming the primary key of the referenced table
     * @param referencesPrimaryKey whether it is clear which columns the constraint refers to
     *                             (if false, then the constraint should be explicit about this)
     * @return
     */
    @Override
    public String getAddForeignKeyConstraintString(String constraintName, String[] foreignKey, String refTable, String[] primaryKey, boolean referencesPrimaryKey) {
        return "";
    }

    /**
     * Returns syntax used to add a primary key constraint to a table.
     * @param name name of the constraint
     * @return
     */
    @Override
    public String getAddPrimaryKeyConstraintString(String name) {
        return "";
    }
}