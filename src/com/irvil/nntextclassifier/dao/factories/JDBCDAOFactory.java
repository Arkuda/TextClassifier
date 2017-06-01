package com.irvil.nntextclassifier.dao.factories;

import com.irvil.nntextclassifier.dao.CharacteristicDAO;
import com.irvil.nntextclassifier.dao.IncomingCallDAO;
import com.irvil.nntextclassifier.dao.StorageCreator;
import com.irvil.nntextclassifier.dao.VocabularyWordDAO;
import com.irvil.nntextclassifier.dao.jdbc.JDBCCharacteristicDAO;
import com.irvil.nntextclassifier.dao.jdbc.JDBCDBCreator;
import com.irvil.nntextclassifier.dao.jdbc.JDBCIncomingCallDAO;
import com.irvil.nntextclassifier.dao.jdbc.JDBCVocabularyWordDAO;
import com.irvil.nntextclassifier.dao.jdbc.connectors.JDBCConnector;

public class JDBCDAOFactory implements DAOFactory {
  private JDBCConnector connector;

  public JDBCDAOFactory(JDBCConnector connector) {
    if (connector == null) {
      throw new IllegalArgumentException();
    }

    this.connector = connector;
  }

  @Override
  public IncomingCallDAO incomingCallDAO() {
    return new JDBCIncomingCallDAO(connector);
  }

  @Override
  public CharacteristicDAO characteristicDAO() {
    return new JDBCCharacteristicDAO(connector);
  }

  @Override
  public VocabularyWordDAO vocabularyWordDAO() {
    return new JDBCVocabularyWordDAO(connector);
  }

  @Override
  public StorageCreator storageCreator() {
    return new JDBCDBCreator(connector);
  }
}