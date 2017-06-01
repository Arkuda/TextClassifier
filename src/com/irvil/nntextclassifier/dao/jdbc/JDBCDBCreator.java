package com.irvil.nntextclassifier.dao.jdbc;

import com.irvil.nntextclassifier.dao.StorageCreator;
import com.irvil.nntextclassifier.dao.jdbc.connectors.JDBCConnector;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JDBCDBCreator implements StorageCreator {
  private JDBCConnector connector;

  public JDBCDBCreator(JDBCConnector connector) {
    if (connector == null) {
      throw new IllegalArgumentException();
    }

    this.connector = connector;
  }

  @Override
  public void createStorage() {
    List<String> sqlQueries = new ArrayList<>();

    // create database structure
    //

    sqlQueries.add("CREATE TABLE IF NOT EXISTS CharacteristicsNames " +
        "( `Id` INTEGER PRIMARY KEY AUTOINCREMENT, `Name` TEXT UNIQUE )");
    sqlQueries.add("CREATE TABLE IF NOT EXISTS CharacteristicsValues " +
        "( `Id` INTEGER, `CharacteristicsNameId` INTEGER, `Value` TEXT, PRIMARY KEY(`Id`,`CharacteristicsNameId`,`Value`) )");
    sqlQueries.add("CREATE TABLE IF NOT EXISTS IncomingCalls " +
        "( `Id` INTEGER PRIMARY KEY AUTOINCREMENT, `Text` TEXT )");
    sqlQueries.add("CREATE TABLE IF NOT EXISTS IncomingCallsCharacteristics " +
        "( `IncomingCallId` INTEGER, `CharacteristicsNameId` INTEGER, `CharacteristicsValueId` INTEGER, PRIMARY KEY(`IncomingCallId`,`CharacteristicsNameId`,`CharacteristicsValueId`) )");
    sqlQueries.add("CREATE TABLE IF NOT EXISTS Vocabulary " +
        "( `Id` INTEGER PRIMARY KEY AUTOINCREMENT, `Value` TEXT UNIQUE )");

    executeQueries(sqlQueries);
  }

  @Override
  public void clearStorage() {
    List<String> sqlQueries = new ArrayList<>();

    sqlQueries.add("DELETE FROM CharacteristicsNames");
    sqlQueries.add("DELETE FROM CharacteristicsValues");
    sqlQueries.add("DELETE FROM IncomingCalls");
    sqlQueries.add("DELETE FROM IncomingCallsCharacteristics");
    sqlQueries.add("DELETE FROM Vocabulary");

    // reset autoincrement keys
    sqlQueries.add("DELETE FROM sqlite_sequence WHERE name IN " +
        "('CharacteristicsNames', 'CharacteristicsValues', 'IncomingCalls', " +
        "'IncomingCallsCharacteristics', 'Vocabulary')");

    executeQueries(sqlQueries);
  }

  private void executeQueries(List<String> sqlQueries) {
    try (Connection con = connector.getConnection()) {
      Statement statement = con.createStatement();

      for (String sqlQuery : sqlQueries) {
        statement.execute(sqlQuery);
      }
    } catch (SQLException ignored) {
    }
  }
}