package com.ontimize.jee.common.db.sql;

import java.sql.Types;

public class AccessSQLHandler extends AbstractSQLHandler {

	/**
	 * Returns a string with the SQL type for Access's database.
	 * @param sqlType Int with the SQL Type
	 * @return a <code>String</code>
	 */
	@Override
	public String getSQLTypeName(int sqlType) throws AccessSQLException {
		switch (sqlType) {

		case Types.BIGINT: // -5
			return "long";

		case Types.BLOB:
			return "OLE Object";

		case Types.DATALINK:
			return "hyperlink";

		case Types.DATE: // 91
			return "date";

		case Types.DOUBLE: // 8
			return "double"; // money too

		case Types.FLOAT: // 6
			return "single";

		case Types.INTEGER: // 4
			return "integer"; // oid too

		case Types.LONGVARBINARY: // -4
			return "bytea";

		case Types.LONGVARCHAR: // -1
			return "memo";

		case Types.SMALLINT: // 5
			return "byte";

		case Types.TIME: // 92
			return "time"; // timetz too

		case Types.TINYINT: // -6
			return "byte";

		case Types.VARCHAR: // 12
			return "text"; // text too

		default:
			throw new AccessSQLException("Invalid type");
		}
	}

}
