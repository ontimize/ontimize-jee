package com.ontimize.jee.common.db.sql;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AccessSQLHandlerTest {

	@Test
	void testNonValidSQLType() {
		int invalidSQLType = -99999;
		AccessSQLHandler accessSQLHandler = new AccessSQLHandler();
		Assertions.assertThrows(AccessSQLException.class, () -> accessSQLHandler.getSQLTypeName(invalidSQLType));
	}

}
