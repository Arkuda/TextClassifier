package com.irvil.nntextclassifier;

import com.irvil.nntextclassifier.dao.IncomingCallDAO;
import com.irvil.nntextclassifier.dao.StorageCreator;
import com.irvil.nntextclassifier.dao.jdbc.*;
import com.irvil.nntextclassifier.model.*;
import com.irvil.nntextclassifier.ngram.NGramStrategy;
import com.irvil.nntextclassifier.ngram.Unigram;
import com.irvil.nntextclassifier.recognizer.CategoryRecognizer;
import com.irvil.nntextclassifier.recognizer.HandlerRecognizer;
import com.irvil.nntextclassifier.recognizer.ModuleRecognizer;
import com.irvil.nntextclassifier.recognizer.Recognizer;
import org.encog.Encog;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class FirstStart {
  private void createStorage() {
    StorageCreator sc = new JDBCDBCreator();
    sc.createStorage();

    System.out.println("Storage created");
  }

  private void fillVocabulary(NGramStrategy nGram) {
    List<IncomingCall> incomingCalls = new JDBCIncomingCallDAO().getAll();
    Set<String> vocabulary = getVocabulary(nGram, incomingCalls);

    for (String word : vocabulary) {
      new JDBCVocabularyWordDAO().add(new VocabularyWord(0, word));
    }

    System.out.println("Vocabulary filled");
  }

  private Set<String> getVocabulary(NGramStrategy nGram, List<IncomingCall> incomingCalls) {
    Set<String> vocabulary = new LinkedHashSet<>();

    for (IncomingCall ic : incomingCalls) {
      vocabulary.addAll(nGram.getNGram(ic.getText()));
    }

    return vocabulary;
  }

  private void fillReferenceData() {
    IncomingCallDAO icDAO = new JDBCIncomingCallDAO();

    List<Module> modules = icDAO.getAllModules();
    List<Category> categories = icDAO.getAllCategories();
    List<Handler> handlers = icDAO.getAllHandlers();

    modules.forEach((module) -> new JDBCModuleDAO().add(module));
    categories.forEach((category) -> new JDBCCategoryDAO().add(category));
    handlers.forEach((handler) -> new JDBCHandlerDAO().add(handler));

    System.out.println("Modules, Categories, Handlers filled");
  }

  private void trainRecognizer(Recognizer recognizer) {
    recognizer.train();
    recognizer.saveTrainedNetwork(new File("./db/" + recognizer.toString() + "TrainedNetwork"));
  }

  private void createDbFolder(String path) {
    new File(path).mkdir();
  }

  public static void main(String[] args) throws IOException {
    FirstStart fs = new FirstStart();
    fs.createDbFolder("./db");
    fs.createStorage();

    System.out.println("Fill IncomingCalls and press Enter");
    System.in.read();

    fs.fillVocabulary(new Unigram());
    fs.fillReferenceData();

    fs.trainRecognizer(new ModuleRecognizer());
    fs.trainRecognizer(new CategoryRecognizer());
    fs.trainRecognizer(new HandlerRecognizer());
    Encog.getInstance().shutdown();
  }
}