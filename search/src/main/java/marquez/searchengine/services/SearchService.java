package marquez.searchengine.services;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import marquez.searchengine.models.IndexResponse;
import marquez.searchengine.models.SearchResult;

public class SearchService {

    private final Directory indexDirectory;
    private final StandardAnalyzer analyzer;
    private static final int MAX_RESULTS = 10;

    public SearchService() {
        this.indexDirectory = new ByteBuffersDirectory();
        this.analyzer = new StandardAnalyzer();
    }

    // Method to index a job document
    public IndexResponse indexJobDocument(Map<String, Object> document) throws IOException {
        try (IndexWriter writer = new IndexWriter(indexDirectory, new IndexWriterConfig(analyzer))) {
            Document doc = new Document();

            doc.add(new StringField("run_id", (String) document.get("run_id"), Field.Store.YES));
            doc.add(new TextField("name", (String) document.get("name"), Field.Store.YES));
            doc.add(new TextField("namespace", (String) document.get("namespace"), Field.Store.YES));
            doc.add(new TextField("eventType", (String) document.get("eventType"), Field.Store.YES));

            if (document.containsKey("facets")) {
                doc.add(new TextField("facets", document.get("facets").toString(), Field.Store.YES));
            }
            if (document.containsKey("runFacets")) {
                doc.add(new TextField("runFacets", document.get("runFacets").toString(), Field.Store.YES));
            }

            writer.addDocument(doc);
            writer.commit();
            return createIndexResponse("jobs", document.get("name").toString(), true);
        }
    }

    // Method to index a dataset document
    public IndexResponse indexDatasetDocument(Map<String, Object> document) throws IOException {
        try (IndexWriter writer = new IndexWriter(indexDirectory, new IndexWriterConfig(analyzer))) {
            Document doc = new Document();

            doc.add(new StringField("run_id", (String) document.get("run_id"), Field.Store.YES));
            doc.add(new TextField("name", (String) document.get("name"), Field.Store.YES));
            doc.add(new TextField("namespace", (String) document.get("namespace"), Field.Store.YES));
            doc.add(new TextField("eventType", (String) document.get("eventType"), Field.Store.YES));

            if (document.containsKey("facets")) {
                doc.add(new TextField("facets", document.get("facets").toString(), Field.Store.YES));
            }
            if (document.containsKey("inputFacets")) {
                doc.add(new TextField("inputFacets", document.get("inputFacets").toString(), Field.Store.YES));
            }
            if (document.containsKey("outputFacets")) {
                doc.add(new TextField("outputFacets", document.get("outputFacets").toString(), Field.Store.YES));
            }

            //System.out.println("Indexing document: " + doc);
            writer.addDocument(doc);
            writer.commit();

            return createIndexResponse("datasets", document.get("name").toString(), true);
        }
    }

    private IndexResponse createIndexResponse(String index, String id, boolean created) {
        long version = 1L; // Simulated version number
        String result = created ? "created" : "updated";

        IndexResponse.ShardInfo shardInfo = new IndexResponse.ShardInfo(1, 1, 0); // 1 shard, 1 successful, 0 failed

        long seqNo = 1L; // Simulated sequence number
        long primaryTerm = 1L; // Simulated primary term

        return new IndexResponse(index, id, version, result, shardInfo, seqNo, primaryTerm);
    }

    private boolean isIndexEmpty() throws IOException {
        try (DirectoryReader reader = DirectoryReader.open(indexDirectory)) {
            return reader.numDocs() == 0;
        } catch (IndexNotFoundException e) {
            return true;
        }
    }

    public SearchResult searchDatasets(String query, List<String> fields) throws Exception {
        return search(query, fields);
    }

    public SearchResult searchJobs(String query, List<String> fields) throws Exception {
        return search(query, fields);
    }

    private SearchResult search(String query, List<String> fields) throws Exception {
        long startTime = System.currentTimeMillis();

        if (isIndexEmpty()) {
            System.out.println("Index is empty");
            return createEmptySearchResult(startTime);
        }

        try (DirectoryReader reader = DirectoryReader.open(indexDirectory)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            MultiFieldQueryParser parser = new MultiFieldQueryParser(fields.toArray(new String[0]), analyzer);
            Query q = parser.parse(query);
            //System.out.println("Executing query: " + q.toString());

            TopDocs topDocs = searcher.search(q, MAX_RESULTS);
            //System.out.println("Total hits: " + topDocs.totalHits);
            long took = System.currentTimeMillis() - startTime;

            SearchResult result = new SearchResult();
            result.setTook(took);
            result.getHitsMetadata().getTotalHits().setValue(topDocs.totalHits.value);
            // result.setMaxScore(topDocs.getMaxScore());

            StoredFields storedFields = searcher.storedFields();
            SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter("","");
            Highlighter highlighter = new Highlighter(htmlFormatter, new QueryScorer(q));

            for (ScoreDoc sd : topDocs.scoreDocs) {
                Document doc = storedFields.document(sd.doc);
                Map<String, String> highlightedDoc = new HashMap<>();

                for (String field : fields) {
                    String text = doc.get(field);
                    if (text != null) {
                        String highlightedText = highlighter.getBestFragment(analyzer, field, text);
                        highlightedDoc.put(field, highlightedText != null ? highlightedText : text);
                    } else {
                        highlightedDoc.put(field, doc.get(field));
                    }
                }

                result.addDocument("index",highlightedDoc);
            }

            return result;
        }
    }

    private SearchResult createEmptySearchResult(long startTime) {
        long took = System.currentTimeMillis() - startTime;

        SearchResult result = new SearchResult();
        result.setTook(took);
        result.getHitsMetadata().getTotalHits().setValue(0);
        // result.setMaxScore(0.0f);
        result.setTimedOut(false);

        return result;
    }
}
