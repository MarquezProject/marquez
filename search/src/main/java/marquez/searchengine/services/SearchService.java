package marquez.searchengine.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import marquez.service.models.LineageEvent;
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

import com.fasterxml.jackson.annotation.JsonProperty;

public class SearchService {

    private final Directory indexDirectory;
    private final StandardAnalyzer analyzer;
    private static final int MAX_RESULTS = 10;

    public SearchService() {
        this.indexDirectory = new ByteBuffersDirectory();
        this.analyzer = new StandardAnalyzer();
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
            writer.commit();
        }
    }

    private Document buildJobDocument(UUID runUuid, LineageEvent event) {
        Document doc = new Document();
        doc.add(new StringField("run_id", runUuid.toString(), Field.Store.YES));
        doc.add(new TextField("name", event.getJob().getName(), Field.Store.YES));
        doc.add(new TextField("type", event.getJob().isStreamingJob() ? "STREAM" : "BATCH", Field.Store.YES));
        doc.add(new TextField("namespace", event.getJob().getNamespace(), Field.Store.YES));
        doc.add(new TextField("facets", event.getJob().getFacets().toString(), Field.Store.YES));
        return doc;
    }

    private Document buildDatasetDocument(UUID runUuid, LineageEvent.Dataset dataset, LineageEvent event) {
        Document doc = new Document();
        doc.add(new StringField("run_id", runUuid.toString(), Field.Store.YES));
        doc.add(new TextField("name", dataset.getName(), Field.Store.YES));
        doc.add(new TextField("namespace", dataset.getNamespace(), Field.Store.YES));
        doc.add(new TextField("facets", dataset.getFacets().toString(), Field.Store.YES));
        return doc;
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
            return createEmptySearchResult(startTime);
        }

        try (DirectoryReader reader = DirectoryReader.open(indexDirectory)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            MultiFieldQueryParser parser = new MultiFieldQueryParser(fields.toArray(new String[0]), analyzer);
            Query q = parser.parse(query);

            TopDocs topDocs = searcher.search(q, MAX_RESULTS);
            long took = System.currentTimeMillis() - startTime;

            SearchResult result = new SearchResult();
            result.setTook(took);
            result.getHitsMetadata().getTotalHits().setValue(topDocs.totalHits.value);
            //result.setMaxScore(topDocs.getMaxScore());

            StoredFields storedFields = searcher.storedFields();
            SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter();
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

                result.addDocument(highlightedDoc);
            }

            return result;
        }
    }

    private SearchResult createEmptySearchResult(long startTime) {
        long took = System.currentTimeMillis() - startTime;

        SearchResult result = new SearchResult();
        result.setTook(took);
        result.getHitsMetadata().getTotalHits().setValue(0);
        //result.setMaxScore(0.0f);
        result.setTimedOut(false);

        return result;
    }


    public static class SearchResult {
        @JsonProperty("took")
        private long took;
    
        @JsonProperty("timed_out")
        private boolean timedOut = false;
    
        @JsonProperty("_shards")
        private ShardStatistics shards;
    
        @JsonProperty("hits")
        private HitsMetadata hitsMetadata;
    
        @JsonProperty("num_reduce_phases")
        private long numberOfReducePhases;
    
        @JsonProperty("terminated_early")
        private boolean terminatedEarly;
    
        @JsonProperty("suggest")
        private Map<String, Object> suggest = new HashMap<>(); // Initialize as empty map
    
        // Constructor
        public SearchResult() {
            this.shards = new ShardStatistics(1, 1, 0, 0); // Assuming a single shard with no failures
            this.hitsMetadata = new HitsMetadata();
            this.numberOfReducePhases = 0; // Default value
            this.terminatedEarly = false; // Default value
            this.suggest = new HashMap<>(); // Empty suggestion map
        }
    
        // Add document to hits
        public void addDocument(Map<String, String> doc) {
            Map<String, Object> hit = new HashMap<>();
            hit.put("_source", doc);
            hitsMetadata.addHit(hit);
        }
    
        // Getters and Setters for all fields
        public long getTook() {
            return took;
        }
    
        public void setTook(long took) {
            this.took = took;
        }
    
        public boolean isTimedOut() {
            return timedOut;
        }
    
        public void setTimedOut(boolean timedOut) {
            this.timedOut = timedOut;
        }
    
        public ShardStatistics getShards() {
            return shards;
        }
    
        public void setShards(ShardStatistics shards) {
            this.shards = shards;
        }
    
        public HitsMetadata getHitsMetadata() {
            return hitsMetadata;
        }
    
        public void setHitsMetadata(HitsMetadata hitsMetadata) {
            this.hitsMetadata = hitsMetadata;
        }
    
        public long getNumberOfReducePhases() {
            return numberOfReducePhases;
        }
    
        public void setNumberOfReducePhases(long numberOfReducePhases) {
            this.numberOfReducePhases = numberOfReducePhases;
        }
    
        public boolean isTerminatedEarly() {
            return terminatedEarly;
        }
    
        public void setTerminatedEarly(boolean terminatedEarly) {
            this.terminatedEarly = terminatedEarly;
        }
    
        public Map<String, Object> getSuggest() {
            return suggest;
        }
    
        public void setSuggest(Map<String, Object> suggest) {
            this.suggest = suggest;
        }
    
        // ShardStatistics inner class
        public static class ShardStatistics {
            @JsonProperty("total")
            private int total;
    
            @JsonProperty("successful")
            private int successful;
    
            @JsonProperty("skipped")
            private int skipped;
    
            @JsonProperty("failed")
            private int failed;
    
            // Constructor
            public ShardStatistics(int total, int successful, int skipped, int failed) {
                this.total = total;
                this.successful = successful;
                this.skipped = skipped;
                this.failed = failed;
            }
    
            // Getters and Setters
            public int getTotal() {
                return total;
            }
    
            public void setTotal(int total) {
                this.total = total;
            }
    
            public int getSuccessful() {
                return successful;
            }
    
            public void setSuccessful(int successful) {
                this.successful = successful;
            }
    
            public int getSkipped() {
                return skipped;
            }
    
            public void setSkipped(int skipped) {
                this.skipped = skipped;
            }
    
            public int getFailed() {
                return failed;
            }
    
            public void setFailed(int failed) {
                this.failed = failed;
            }
        }
    
        // HitsMetadata inner class
        public static class HitsMetadata {
            @JsonProperty("total")
            private TotalHits totalHits;
    
            @JsonProperty("max_score")
            private Float maxScore;
    
            @JsonProperty("hits")
            private List<Map<String, Object>> hits;
    
            public HitsMetadata() {
                this.totalHits = new TotalHits(0, "eq");
                this.maxScore = null;
                this.hits = new ArrayList<>();
            }
    
            // Getters and Setters
            public TotalHits getTotalHits() {
                return totalHits;
            }
    
            public void setTotalHits(TotalHits totalHits) {
                this.totalHits = totalHits;
            }
    
            public Float getMaxScore() {
                return maxScore;
            }
    
            public void setMaxScore(Float maxScore) {
                this.maxScore = maxScore;
            }
    
            public List<Map<String, Object>> getHits() {
                return hits;
            }
    
            public void setHits(List<Map<String, Object>> hits) {
                this.hits = hits;
            }
    
            // Add a hit to the hits list
            public void addHit(Map<String, Object> hit) {
                this.hits.add(hit);
            }
        }
    
        // TotalHits inner class
        public static class TotalHits {
            @JsonProperty("value")
            private long value;
    
            @JsonProperty("relation")
            private String relation;
    
            public TotalHits(long value, String relation) {
                this.value = value;
                this.relation = relation;
            }
    
            // Getters and Setters
            public long getValue() {
                return value;
            }
    
            public void setValue(long value) {
                this.value = value;
            }
    
            public String getRelation() {
                return relation;
            }
    
            public void setRelation(String relation) {
                this.relation = relation;
            }
        }
    }
 
}
