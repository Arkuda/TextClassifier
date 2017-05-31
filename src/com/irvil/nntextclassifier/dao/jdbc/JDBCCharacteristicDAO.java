package com.irvil.nntextclassifier.dao.jdbc;

import com.irvil.nntextclassifier.dao.AlreadyExistsException;
import com.irvil.nntextclassifier.dao.CharacteristicDAO;
import com.irvil.nntextclassifier.dao.EmptyRecordException;
import com.irvil.nntextclassifier.dao.jdbc.connectors.JDBCConnector;
import com.irvil.nntextclassifier.model.Characteristic;
import com.irvil.nntextclassifier.model.CharacteristicValue;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
        characteristic.setPossibleValues(getAllPossibleValues(characteristic));

        characteristics.add(characteristic);
      }
    } catch (SQLException e) {
      e.printStackTrace();
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

    if (isCharacteristicExistsInDB(characteristic)) {
      throw new AlreadyExistsException("Characteristic already exists");
    }

    try (Connection con = connector.getConnection()) {
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
          insertPossibleValue(characteristic, possibleValue);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return characteristic;
  }

  private List<CharacteristicValue> getAllPossibleValues(Characteristic characteristic) {
    List<CharacteristicValue> possibleValues = new ArrayList<>();

    try (Connection con = connector.getConnection()) {
      String sqlSelect = "SELECT Id, Value FROM CharacteristicsValues WHERE CharacteristicsNameId = ?";
      PreparedStatement statement = con.prepareStatement(sqlSelect);
      statement.setInt(1, characteristic.getId());
      ResultSet rs = statement.executeQuery();

      while (rs.next()) {
        possibleValues.add(new CharacteristicValue(rs.getInt("Id"), rs.getString("Value")));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return possibleValues;
  }

  private boolean isCharacteristicExistsInDB(Characteristic characteristic) {
    try (Connection con = connector.getConnection()) {
      String sqlSelect = "SELECT Id FROM CharacteristicsNames WHERE Name = ?";
      PreparedStatement statement = con.prepareStatement(sqlSelect);
      statement.setString(1, characteristic.getName());
      ResultSet rs = statement.executeQuery();

      if (rs.next()) {
        return true;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return false;
  }

  private void insertPossibleValue(Characteristic characteristic, CharacteristicValue characteristicValue) {
    if (characteristicValue != null &&
        !characteristicValue.getValue().equals("")) {

      // try to find Value in DB
      int newCharacteristicValueId = searchCharacteristicPossibleValue(characteristic, characteristicValue);

      if (newCharacteristicValueId == -1) { // not found -> insert it
        newCharacteristicValueId = getLastCharacteristicPossibleValueId(characteristic) + 1;

        try (Connection con = connector.getConnection()) {
          String sqlInsert = "INSERT INTO CharacteristicsValues (Id, CharacteristicsNameId, Value) VALUES (?, ?, ?)";
          PreparedStatement statement = con.prepareStatement(sqlInsert);
          statement.setInt(1, newCharacteristicValueId);
          statement.setInt(2, characteristic.getId());
          statement.setString(3, characteristicValue.getValue());
          statement.executeUpdate();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }

      // set inserted row Id to CharacteristicValue
      characteristicValue.setId(newCharacteristicValueId);
    }
  }

  private int searchCharacteristicPossibleValue(Characteristic characteristic, CharacteristicValue characteristicValue) {
    try (Connection con = connector.getConnection()) {
      String sqlSelect = "SELECT Id FROM CharacteristicsValues WHERE CharacteristicsNameId = ? AND Value = ?";

      PreparedStatement statement = con.prepareStatement(sqlSelect);
      statement.setInt(1, characteristic.getId());
      statement.setString(2, characteristicValue.getValue());
      ResultSet rs = statement.executeQuery();

      if (rs.next()) {
        return rs.getInt("Id");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return -1; // not found in Db
  }

  private int getLastCharacteristicPossibleValueId(Characteristic characteristic) {
    try (Connection con = connector.getConnection()) {
      String sqlSelect = "SELECT MAX(Id) AS MaxID FROM CharacteristicsValues WHERE CharacteristicsNameId = ?";
      PreparedStatement statement = con.prepareStatement(sqlSelect);
      statement.setInt(1, characteristic.getId());
      ResultSet rs = statement.executeQuery();

      if (rs.next()) {
        return rs.getInt("MaxID"); // last possible values id
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return 0; // possible values not found in DB
  }
}