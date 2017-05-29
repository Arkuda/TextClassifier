package com.irvil.nntextclassifier.dao.jdbc;

import com.irvil.nntextclassifier.dao.IncomingCallDAO;
import com.irvil.nntextclassifier.model.Characteristic;
import com.irvil.nntextclassifier.model.IncomingCall;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class JDBCIncomingCallDAOTest {
  private IncomingCallDAO incomingCallDAO;

  @Before
  public void initializeTable() throws Exception {
    incomingCallDAO = new JDBCIncomingCallDAO(new JDBCSQLiteTestConnector());

    String tableName = "IncomingCalls";
    JDBCDatabaseUtilities.cleanTable(tableName);

    Map<String, Characteristic> characteristics = new HashMap<>();
    characteristics.put("Module", new Characteristic(0, "PM"));
    characteristics.put("Handler", new Characteristic(0, "User 1"));
    JDBCDatabaseUtilities.insertIncomingCall(new IncomingCall("text text", characteristics));

    for (int i = 0; i < 2; i++) {
      characteristics = new HashMap<>();
      characteristics.put("Module", new Characteristic(0, "MM"));
      characteristics.put("Handler", new Characteristic(0, "User 2"));
      JDBCDatabaseUtilities.insertIncomingCall(new IncomingCall("text1 text1", characteristics));
    }

    tableName = "Handlers";
    JDBCDatabaseUtilities.cleanTable(tableName);
    JDBCDatabaseUtilities.insertToCatalog(tableName, new Characteristic(1, "User 1"));
    JDBCDatabaseUtilities.insertToCatalog(tableName, new Characteristic(2, "User 2"));

    tableName = "Modules";
    JDBCDatabaseUtilities.cleanTable(tableName);
    JDBCDatabaseUtilities.insertToCatalog(tableName, new Characteristic(1, "PM"));
    JDBCDatabaseUtilities.insertToCatalog(tableName, new Characteristic(2, "MM"));
  }

  @Test
  public void getAll() throws Exception {
    List<IncomingCall> incomingCalls = incomingCallDAO.getAll();

    // check size
    assertEquals(incomingCalls.size(), 3);

    // check text
    assertEquals(incomingCalls.get(0).getText(), "text text");
    assertEquals(incomingCalls.get(1).getText(), "text1 text1");
    assertEquals(incomingCalls.get(2).getText(), "text1 text1");

    // check modules
    assertEquals(incomingCalls.get(0).getCharacteristic("Module").getValue(), "PM");
    assertEquals(incomingCalls.get(1).getCharacteristic("Module").getValue(), "MM");
    assertEquals(incomingCalls.get(2).getCharacteristic("Module").getValue(), "MM");

    // check handlers
    assertEquals(incomingCalls.get(0).getCharacteristic("Handler").getValue(), "User 1");
    assertEquals(incomingCalls.get(1).getCharacteristic("Handler").getValue(), "User 2");
    assertEquals(incomingCalls.get(2).getCharacteristic("Handler").getValue(), "User 2");
  }

  @Test
  public void getUniqueModules() throws Exception {
    List<Characteristic> modules = incomingCallDAO.getUniqueValueOfCharacteristic("Module");

    // check size
    assertEquals(modules.size(), 2);

    // check modules
    assertEquals(modules.get(0).getValue(), "PM");
    assertEquals(modules.get(1).getValue(), "MM");
  }

  @Test
  public void getUniqueHandlers() throws Exception {
    List<Characteristic> handlers = incomingCallDAO.getUniqueValueOfCharacteristic("Handler");

    // check size
    assertEquals(handlers.size(), 2);

    // check modules
    assertEquals(handlers.get(0).getValue(), "User 1");
    assertEquals(handlers.get(1).getValue(), "User 2");
  }
}