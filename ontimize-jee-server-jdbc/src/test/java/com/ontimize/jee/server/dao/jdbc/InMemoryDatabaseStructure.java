package com.ontimize.jee.server.dao.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Enrique Alvarez Pereira <enrique.alvarez@imatia.com>
 */
public class InMemoryDatabaseStructure {

    public void databaseStructureCreation(Statement dbStatement, Connection dbConnection) {
        try {
            dbStatement.execute("CREATE TABLE employees (employeeid INT NOT NULL, name VARCHAR(50) NOT NULL,"
                    + "email VARCHAR(50) NOT NULL, PRIMARY KEY (employeeid))");

            dbStatement.execute("CREATE TABLE accounts (accountid INT NOT NULL, name VARCHAR(50) NOT NULL,"
                    + "balance DOUBLE NOT NULL, PRIMARY KEY (accountid))");

            dbStatement.execute("CREATE TABLE employeeaccounts (eaccountsid INT NOT NULL, employeeid INT NOT NULL,"
                    + "accountid INT, name VARCHAR(50), PRIMARY KEY (eaccountsid), "
                    + "CONSTRAINT FK_EMPLOYEE FOREIGN KEY(employeeid) REFERENCES employees(employeeid),"
                    + "CONSTRAINT FK_ACCOUNT FOREIGN KEY(accountid) REFERENCES accounts(accountid))");
            dbConnection.commit();

            dbStatement.executeUpdate("INSERT INTO employees VALUES (1001,'Vinod', 'vinod@imatia.com')");
            dbStatement.executeUpdate("INSERT INTO employees VALUES (1002,'Dhwani', 'dhwani@imatia.com')");
            dbStatement.executeUpdate("INSERT INTO employees VALUES (1003,'Asmi', 'asmi@imatia.com')");
            dbStatement.executeUpdate("INSERT INTO employees VALUES (1004,'Caroline', 'caroline@imatia.com')");
            dbStatement.executeUpdate("INSERT INTO employees VALUES (1005,'Cris', 'cris@imatia.com')");
            dbStatement.executeUpdate("INSERT INTO employees VALUES (1006,'Mark', 'mark@imatia.com')");
            dbStatement.executeUpdate("INSERT INTO employees VALUES (1007,'Greco','greco@imatia.com')");
            dbStatement.executeUpdate("INSERT INTO employees VALUES (1008,'Sara', 'sara@imatia.com')");
            dbStatement.executeUpdate("INSERT INTO employees VALUES (1009,'Unai', 'unai@imatia.com')");
            dbStatement.executeUpdate("INSERT INTO employees VALUES (1010,'Kike', 'kike@imatia.com')");
            dbStatement.executeUpdate("INSERT INTO employees VALUES (1011,'Pitt', 'pitt@imatia.com')");
            dbStatement.executeUpdate("INSERT INTO employees VALUES (1012,'Aaron', 'aaron@imatia.com')");
            dbStatement.executeUpdate("INSERT INTO employees VALUES (1013,'Maria', 'maria@imatia.com')");
            dbStatement.executeUpdate("INSERT INTO employees VALUES (1014,'Keery', 'kerry@imatia.com')");
            dbStatement.executeUpdate("INSERT INTO employees VALUES (1015,'Tolra', 'tolra@imatia.com')");
            dbStatement.executeUpdate("INSERT INTO employees VALUES (1016,'Elva', 'elva@imatia.com')");
            dbStatement.executeUpdate("INSERT INTO employees VALUES (1017,'Ander', 'ander@imatia.com')");
            dbStatement.executeUpdate("INSERT INTO employees VALUES (1018,'Ruben', 'ruben@imatia.com')");
            dbStatement.executeUpdate("INSERT INTO employees VALUES (1019,'Lenna', 'lenna@imatia.com')");
            dbStatement.executeUpdate("INSERT INTO employees VALUES (1020,'Solomeo', 'solomeo@imatia.com')");
            dbStatement.executeUpdate("INSERT INTO employees VALUES (1021,'Monica', 'monica@imatia.com')");
            dbStatement.executeUpdate("INSERT INTO employees VALUES (1022,'Maru', 'maru@imatia.com')");
            dbStatement.executeUpdate("INSERT INTO employees VALUES (1023,'Elsa', 'elsa@imatia.com')");
            dbStatement.executeUpdate("INSERT INTO employees VALUES (1024,'Alfonso', 'alfonso@imatia.com')");
            dbStatement.executeUpdate("INSERT INTO employees VALUES (1025,'Elver', 'elver@imatia.com')");

            dbStatement.executeUpdate("INSERT INTO accounts VALUES (101,'Abanca', 1890.23)");
            dbStatement.executeUpdate("INSERT INTO accounts VALUES (102,'Caixabank', 567889.34)");
            dbStatement.executeUpdate("INSERT INTO accounts VALUES (103,'Kutxa', 4532.10)");
            dbStatement.executeUpdate("INSERT INTO accounts VALUES (104,'Kutxa', 4456645.77)");
            dbStatement.executeUpdate("INSERT INTO accounts VALUES (105,'Santander', 18765.56)");

            dbStatement.executeUpdate("INSERT INTO employeeaccounts VALUES (1, 1001, 101, 'T1')");
            dbStatement.executeUpdate("INSERT INTO employeeaccounts VALUES (2, 1002, 102, 'T2')");
            dbStatement.executeUpdate("INSERT INTO employeeaccounts VALUES (3, 1003, 103, 'T3')");
            dbStatement.executeUpdate("INSERT INTO employeeaccounts VALUES (4, 1004, 104, 'T4')");
            dbStatement.executeUpdate("INSERT INTO employeeaccounts VALUES (5, 1005, 105, 'T5')");
            dbStatement.executeUpdate("INSERT INTO employeeaccounts VALUES (6, 1006, null, 'T6')");


            dbConnection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void databaseDelete(Statement dbStatement, Connection dbConnection) {
        try {
            dbStatement.executeUpdate("DROP TABLE employeeaccounts");
            dbStatement.executeUpdate("DROP TABLE employees");
            dbStatement.executeUpdate("DROP TABLE accounts");
            dbConnection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
