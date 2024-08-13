package marquez.searchengine.services;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import marquez.searchengine.SearchConfig;
import marquez.service.models.LineageEvent;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class SearchService {

  private final SearchConfig config;
  private final Directory indexDirectory;
  private final StandardAnalyzer analyzer;

  public SearchService(SearchConfig config) throws IOException {
    this.config = config;
    this.analyzer = new StandardAnalyzer();
    this.indexDirectory = FSDirectory.open(Paths.get(config.getIndexDirectory()));
  }

  public void indexEvent(LineageEvent event) throws IOException {
    try (IndexWriter writer = new IndexWriter(indexDirectory, new IndexWriterConfig(analyzer))) {
      UUID runUuid = UUID.fromString(event.getRun().getRunId());
      if (event.getInputs() != null) {
        for (LineageEvent.Dataset dataset : event.getInputs()) {
          writer.addDocument(buildDatasetDocument(runUuid, dataset, event));
        }
      }
      if (event.getOutputs() != null) {
        for (LineageEvent.Dataset dataset : event.getOutputs()) {
          writer.addDocument(buildDatasetDocument(runUuid, dataset, event));
        }
      }
      writer.addDocument(buildJobDocument(runUuid, event));
    }
  }

  private Document buildJobDocument(UUID runUuid, LineageEvent event) {
    Document doc = new Document();
    doc.add(new StringField("run_id", runUuid.toString(), Field.Store.YES));
    doc.add(new TextField("name", event.getJob().getName(), Field.Store.YES));
    doc.add(
        new TextField(
            "type", event.getJob().isStreamingJob() ? "STREAM" : "BATCH", Field.Store.YES));
    doc.add(new TextField("namespace", event.getJob().getNamespace(), Field.Store.YES));
    doc.add(new TextField("facets", event.getJob().getFacets().toString(), Field.Store.YES));
    return doc;
  }

  private Document buildDatasetDocument(
      UUID runUuid, LineageEvent.Dataset dataset, LineageEvent event) {
    Document doc = new Document();
    doc.add(new StringField("run_id", runUuid.toString(), Field.Store.YES));
    doc.add(new TextField("name", dataset.getName(), Field.Store.YES));
    doc.add(new TextField("namespace", dataset.getNamespace(), Field.Store.YES));
    doc.add(new TextField("facets", dataset.getFacets().toString(), Field.Store.YES));
    return doc;
  }

  public SearchResult searchDatasets(String query) throws Exception {
    return search(query, new String[] {"name", "namespace", "facets"});
  }

  public SearchResult searchJobs(String query) throws Exception {
    return search(query, new String[] {"name", "namespace", "facets"});
  }

  private SearchResult search(String query, String[] fields) throws Exception {
    try (DirectoryReader reader = DirectoryReader.open(indexDirectory)) {
      IndexSearcher searcher = new IndexSearcher(reader);
      MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, analyzer);
      Query q = parser.parse(query);
      TopDocs topDocs = searcher.search(q, 10);

      SearchResult result = new SearchResult();
      for (ScoreDoc sd : topDocs.scoreDocs) {
        Document doc = searcher.doc(sd.doc);
        result.addDocument(doc);
      }
      return result;
    }
  }

  public static class SearchResult {
    private final List<Map<String, String>> results = new ArrayList<>();

    public void addDocument(Document doc) {
      Map<String, String> map = new HashMap<>();
      for (IndexableField field : doc.getFields()) {
        map.put(field.name(), doc.get(field.name()));
      }
      results.add(map);
    }

    public List<Map<String, String>> getResults() {
      return results;
    }
  }
}
