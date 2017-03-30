package com.irvil.nntextclassifier.model;

import com.irvil.nntextclassifier.dao.DAOFactory;

public class Category extends Catalog {
  public Category(int id, String value) {
    super(id, value, DAOFactory.categoryDAO("jdbc"));
  }
}