package marquez.searchengine.services;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.ngram.NGramTokenizer;

public class NGramAnalyzer extends Analyzer {
  private final int minGram;
  private final int maxGram;

  public NGramAnalyzer(int minGram, int maxGram) {
    this.minGram = minGram;
    this.maxGram = maxGram;
  }

  @Override
  protected TokenStreamComponents createComponents(String fieldName) {
    NGramTokenizer tokenizer = new NGramTokenizer(minGram, maxGram); // Define the N-grams range
    TokenStream tokenStream = new LowerCaseFilter(tokenizer); // Optional: make everything lowercase
    return new TokenStreamComponents(tokenizer, tokenStream);
  }
}
