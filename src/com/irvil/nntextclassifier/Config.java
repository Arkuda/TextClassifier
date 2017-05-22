package com.irvil.nntextclassifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
  private static Config instance;
  private Properties properties = new Properties();

  private Config() {
    // read config file
    try (InputStream inputStream = new FileInputStream(new File("./config/config.ini"))) {
      properties.load(inputStream);
    } catch (IOException ignored) {

    }
  }

  public static Config getInstance() {
    // create only one object - Singleton pattern
    if (instance == null) {
      instance = new Config();
    }

    return instance;
  }

  public boolean isLoaded() {
    return properties.size() > 0;
  }

  public String getDbPath() {
    return properties.getProperty("db_path");
  }

  public String getDaoType() {
    return properties.getProperty("dao_type");
  }

  public String getDBMSType() {
    return properties.getProperty("dbms_type");
  }

  public String getSQLiteDbFileName() {
    return properties.getProperty("sqlite_db_filename");
  }
}