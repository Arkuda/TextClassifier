package com.irvil.nntextclassifier.dao.jdbc;

import com.irvil.nntextclassifier.Config;
import com.irvil.nntextclassifier.dao.CharacteristicDAO;
import com.irvil.nntextclassifier.dao.IncomingCallDAO;
import com.irvil.nntextclassifier.dao.jdbc.connectors.JDBCConnector;
import com.irvil.nntextclassifier.dao.jdbc.connectors.JDBCSQLiteConnector;
import com.irvil.nntextclassifier.model.CharacteristicValue;
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
    Config config = Config.getInstance();
    JDBCConnector jdbcConnector = new JDBCSQLiteConnector(config.getDbPath() + "/test.db");
    incomingCallDAO = new JDBCIncomingCallDAO(jdbcConnector);
    CharacteristicDAO moduleDAO = new JDBCCharacteristicDAO(jdbcConnector);
    CharacteristicDAO handlerDAO = new JDBCCharacteristicDAO(jdbcConnector);

    // clear tables

    JDBCDatabaseUtilities.clearTable(jdbcConnector, "CharacteristicsNames");
    JDBCDatabaseUtilities.clearTable(jdbcConnector, "CharacteristicsValues");
    JDBCDatabaseUtilities.clearTable(jdbcConnector, "IncomingCalls");
    JDBCDatabaseUtilities.clearTable(jdbcConnector, "IncomingCallsCharacteristics");

    // fill Module characteristics
    //

//    moduleDAO.addPossibleValue(, new CharacteristicValue(0, "PM")); // ok
//    moduleDAO.addPossibleValue(, new CharacteristicValue(0, "MM")); // ok
//
//    // fill Handler characteristics
//    //
//
//    handlerDAO.addPossibleValue(, new CharacteristicValue(0, "User 1")); // ok
//    handlerDAO.addPossibleValue(, new CharacteristicValue(0, "User 2")); // ok

    // fill incoming calls

    Map<String, CharacteristicValue> characteristics = new HashMap<>();
    characteristics.put("Module", new CharacteristicValue(0, "PM"));
    characteristics.put("Handler", new CharacteristicValue(0, "User 1"));
    incomingCallDAO.add(new IncomingCall("text text", characteristics)); // ok

    characteristics = new HashMap<>();
    characteristics.put("Module", new CharacteristicValue(0, "MM"));
    characteristics.put("Handler", new CharacteristicValue(0, "User 2"));
    incomingCallDAO.add(new IncomingCall("text1 text1", characteristics)); // ok

    characteristics = new HashMap<>();
    characteristics.put("Module", new CharacteristicValue(0, "MM"));
    characteristics.put("Handler", new CharacteristicValue(0, "User 2"));
    incomingCallDAO.add(new IncomingCall("text1 text1", characteristics)); //ok

    characteristics = new HashMap<>();
    characteristics.put("Module", new CharacteristicValue(0, "BC"));
    characteristics.put("Handler", new CharacteristicValue(0, "User 3"));
    incomingCallDAO.add(new IncomingCall("text2 text2", characteristics)); // error: "User 3" not exists

    characteristics = new HashMap<>();
    characteristics.put("Module", new CharacteristicValue(0, "PM"));
    characteristics.put("Handler", new CharacteristicValue(0, "User 1"));
    characteristics.put("Category", new CharacteristicValue(0, "User 1"));
    incomingCallDAO.add(new IncomingCall("text text", characteristics)); // error: "Category" not exists

    characteristics = new HashMap<>();
    incomingCallDAO.add(new IncomingCall("text text", characteristics)); // error: empty characteristics

    incomingCallDAO.add(new IncomingCall("text3 text3", null)); // error: null characteristics

    incomingCallDAO.add(null); // error: null IncomingCall
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

    // check characteristics
    assertEquals(incomingCalls.get(0).getCharacteristic("Module").getValue(), "PM");
    assertEquals(incomingCalls.get(1).getCharacteristic("Module").getValue(), "MM");
    assertEquals(incomingCalls.get(2).getCharacteristic("Module").getValue(), "MM");

    assertEquals(incomingCalls.get(0).getCharacteristic("Handler").getValue(), "User 1");
    assertEquals(incomingCalls.get(1).getCharacteristic("Handler").getValue(), "User 2");
    assertEquals(incomingCalls.get(2).getCharacteristic("Handler").getValue(), "User 2");
  }
}