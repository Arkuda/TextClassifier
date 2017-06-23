package com.irvil.nntextclassifier.ngram;

public class BigramTest extends NGramStrategyTest {
  @Override
  public void setUp() {
    nGramStrategy = new Bigram(new Unigram());
    idealCyrillicText = new String[]{"привет хотела", "хотела бы", "бы сделать", "сделать 235", "235 2", "2 тест", "тест метода", "метода хотел", "хотел сделал"};
    idealLatinText = new String[]{"hello this", "this is", "is method", "method 23", "23 test", "test methods"};
  }
}