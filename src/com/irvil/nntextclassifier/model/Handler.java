package com.irvil.nntextclassifier.model;

import com.irvil.nntextclassifier.dao.jdbc.JDBCHandlerDAO;

public class Handler extends Catalog {
  public Handler(int id, String value) {
    super(id, value, new JDBCHandlerDAO());
  }
}
