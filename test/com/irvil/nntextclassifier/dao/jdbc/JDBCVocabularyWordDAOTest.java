package com.irvil.nntextclassifier.dao.jdbc;

import com.irvil.nntextclassifier.dao.VocabularyWordDAOTest;
import com.irvil.nntextclassifier.dao.jdbc.connectors.JDBCConnector;
import com.irvil.nntextclassifier.dao.jdbc.connectors.JDBCSQLiteConnector;

public class JDBCVocabularyWordDAOTest extends VocabularyWordDAOTest {
  @Override
  public void initializeDAO() {
    JDBCConnector jdbcConnector = new JDBCSQLiteConnector("./db/test.db");
    storageCreator = new JDBCDBCreator(jdbcConnector);
    characteristicDAO = new JDBCCharacteristicDAO(jdbcConnector);
    incomingCallDAO = new JDBCIncomingCallDAO(jdbcConnector);
    vocabularyWordDAO = new JDBCVocabularyWordDAO(jdbcConnector);
  }
}