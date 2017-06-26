package com.irvil.textclassifier.dao.jdbc;

import com.irvil.textclassifier.dao.ClassifiableTextDAOTest;
import com.irvil.textclassifier.dao.jdbc.connectors.JDBCConnector;
import com.irvil.textclassifier.dao.jdbc.connectors.JDBCSQLiteConnector;

public class JDBCClassifiableTextDAOTest extends ClassifiableTextDAOTest {
  @Override
  public void initializeDAO() {
    JDBCConnector jdbcConnector = new JDBCSQLiteConnector("./test_db/test.db");
    storageCreator = new JDBCDBCreator(jdbcConnector);
    characteristicDAO = new JDBCCharacteristicDAO(jdbcConnector);
    classifiableTextDAO = new JDBCClassifiableTextDAO(jdbcConnector);
    vocabularyWordDAO = new JDBCVocabularyWordDAO(jdbcConnector);
  }
}