package com.ontimize.jee.common.db.sql;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AccessSQLHandlerTest {

	@Test
	void getSQLTypeName_Fail_IfSQLTypeNotExist() {
		int invalidSQLType = -99999;
		AccessSQLHandler accessSQLHandler = new AccessSQLHandler();
		Assertions.assertThrows(AccessSQLException.class, () -> accessSQLHandler.getSQLTypeName(invalidSQLType));
	}

	//	@Test
	//	void getSQLTypeName_Success_IfSQLTypeExist() {
	//		int validSQLType = ;
	//		AccessSQLHandler accessSQLHandler = new AccessSQLHandler();
	//		Assertions.asser;
	//	}

}
