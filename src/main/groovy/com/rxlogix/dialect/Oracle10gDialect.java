package com.rxlogix.dialect;

import org.hibernate.sql.ANSIJoinFragment;
import org.hibernate.sql.JoinFragment;

import java.sql.Types;

/**
 * Attempts to fix the issue w/ Oracle mapping long strings types to LONG type instead of CLOB
 */
class Oracle10gDialect extends org.hibernate.dialect.Oracle9iDialect {

    public Oracle10gDialect() {
    }

    protected void registerCharacterTypeMappings() {
        super.registerCharacterTypeMappings();
        this.registerColumnType(12, "clob");
        this.registerColumnType(-1, "clob");
    }

    public JoinFragment createOuterJoinFragment() {
        return new ANSIJoinFragment();
    }
}
