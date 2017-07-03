package com.irvil.textclassifier.dao.jdbc;

import com.irvil.textclassifier.dao.AlreadyExistsException;
import com.irvil.textclassifier.dao.CharacteristicDAO;
import com.irvil.textclassifier.dao.EmptyRecordException;
import com.irvil.textclassifier.dao.jdbc.connectors.JDBCConnector;
import com.irvil.textclassifier.model.Characteristic;
import com.irvil.textclassifier.model.CharacteristicValue;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class JDBCCharacteristicDAO implements CharacteristicDAO {
  private JDBCConnector connector;

  public JDBCCharacteristicDAO(JDBCConnector connector) {
    if (connector == null) {
      throw new IllegalArgumentException();
    }

    this.connector = connector;
  }

  @Override
  public List<Characteristic> getAllCharacteristics() {
    List<Characteristic> characteristics = new ArrayList<>();

    try (Connection con = connector.getConnection()) {
      String sqlSelect = "SELECT Id, Name FROM CharacteristicsNames";
      PreparedStatement statement = con.prepareStatement(sqlSelect);
      ResultSet rs = statement.executeQuery();

      while (rs.next()) {
        Characteristic characteristic = new Characteristic(rs.getInt("Id"), rs.getString("Name"));

        // get all possible values
        characteristic.setPossibleValues(getAllPossibleValues(con, characteristic));

        characteristics.add(characteristic);
      }
    } catch (SQLException ignored) {
    }

    return characteristics;
  }

  @Override
  public Characteristic addCharacteristic(Characteristic characteristic) throws AlreadyExistsException, EmptyRecordException {
    if (characteristic == null ||
        characteristic.getName().equals("") ||
        characteristic.getPossibleValues() == null ||
        characteristic.getPossibleValues().size() == 0) {
      throw new EmptyRecordException("Characteristic and/or Possible values are null or empty");
    }

    try (Connection con = connector.getConnection()) {
      if (isCharacteristicExistsInDB(con, characteristic)) {
        throw new AlreadyExistsException("Characteristic already exists");
      }

      String sqlInsert = "INSERT INTO CharacteristicsNames (Name) VALUES (?)";
      PreparedStatement statement = con.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS);
      statement.setString(1, characteristic.getName());
      statement.executeUpdate();

      // get inserted row Id
      ResultSet generatedKeys = statement.getGeneratedKeys();

      if (generatedKeys.next()) {
        // set inserted row Id to Characteristic
        characteristic.setId(generatedKeys.getInt(1));

        // insert possible values
        //

        for (CharacteristicValue possibleValue : characteristic.getPossibleValues()) {
          insertPossibleValue(con, characteristic, possibleValue);
        }
      }
    } catch (SQLException ignored) {
    }

    return characteristic;
  }

  private Set<CharacteristicValue> getAllPossibleValues(Connection con, Characteristic characteristic) throws SQLException {
    Set<CharacteristicValue> possibleValues = new LinkedHashSet<>();

    String sqlSelect = "SELECT Id, Value FROM CharacteristicsValues WHERE CharacteristicsNameId = ?";
    PreparedStatement statement = con.prepareStatement(sqlSelect);
    statement.setInt(1, characteristic.getId());
    ResultSet rs = statement.executeQuery();

    while (rs.next()) {
      possibleValues.add(new CharacteristicValue(rs.getInt("Id"), rs.getString("Value")));
    }

    return possibleValues;
  }

  private boolean isCharacteristicExistsInDB(Connection con, Characteristic characteristic) throws SQLException {
    String sqlSelect = "SELECT Id FROM CharacteristicsNames WHERE Name = ?";
    PreparedStatement statement = con.prepareStatement(sqlSelect);
    statement.setString(1, characteristic.getName());
    ResultSet rs = statement.executeQuery();

    return rs.next();
  }

  private void insertPossibleValue(Connection con, Characteristic characteristic, CharacteristicValue characteristicValue) throws SQLException {
    if (characteristicValue != null &&
        !characteristicValue.getValue().equals("")) {

      // try to find Value in DB
      int newCharacteristicValueId = searchCharacteristicPossibleValue(con, characteristic, characteristicValue);

      if (newCharacteristicValueId == -1) { // not found -> insert it
        newCharacteristicValueId = getLastCharacteristicPossibleValueId(con, characteristic) + 1;

        String sqlInsert = "INSERT INTO CharacteristicsValues (Id, CharacteristicsNameId, Value) VALUES (?, ?, ?)";
        PreparedStatement statement = con.prepareStatement(sqlInsert);
        statement.setInt(1, newCharacteristicValueId);
        statement.setInt(2, characteristic.getId());
        statement.setString(3, characteristicValue.getValue());
        statement.executeUpdate();
      }

      // set inserted row Id to CharacteristicValue
      characteristicValue.setId(newCharacteristicValueId);
    }
  }

  private int searchCharacteristicPossibleValue(Connection con, Characteristic characteristic, CharacteristicValue characteristicValue) throws SQLException {
    String sqlSelect = "SELECT Id FROM CharacteristicsValues WHERE CharacteristicsNameId = ? AND Value = ?";

    PreparedStatement statement = con.prepareStatement(sqlSelect);
    statement.setInt(1, characteristic.getId());
    statement.setString(2, characteristicValue.getValue());
    ResultSet rs = statement.executeQuery();

    if (rs.next()) {
      return rs.getInt("Id");
    }

    return -1; // not found in Db
  }

  private int getLastCharacteristicPossibleValueId(Connection con, Characteristic characteristic) throws SQLException {
    String sqlSelect = "SELECT MAX(Id) AS MaxID FROM CharacteristicsValues WHERE CharacteristicsNameId = ?";
    PreparedStatement statement = con.prepareStatement(sqlSelect);
    statement.setInt(1, characteristic.getId());
    ResultSet rs = statement.executeQuery();

    if (rs.next()) {
      return rs.getInt("MaxID"); // last possible values id
    }

    return 0; // possible values not found in DB
  }
}