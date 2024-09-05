package marquez.searchengine.services;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

//import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
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
import marquez.db.OpenLineageDao;
import marquez.service.models.LineageEvent;
import marquez.service.models.LineageEvent.Dataset;

public class SearchService {

    private final OpenLineageDao openLineageDao;
    private final Directory jobIndexDirectory;
    private final Directory datasetIndexDirectory;    
    //private final StandardAnalyzer analyzer;
    private final NGramAnalyzer analyzer;
    private static final int MAX_RESULTS = 10;

    public SearchService(OpenLineageDao openLineageDao) throws IOException {
        this.openLineageDao = openLineageDao;
        this.jobIndexDirectory = new ByteBuffersDirectory();
        this.datasetIndexDirectory = new ByteBuffersDirectory();
        //this.analyzer = new StandardAnalyzer();
        this.analyzer = new NGramAnalyzer(3, 4);
        // init index with DB lineage events
        loadLineageEventsFromDatabase();
    }

    private void loadLineageEventsFromDatabase() throws IOException {
        ZonedDateTime before = ZonedDateTime.now(); // Current time
        ZonedDateTime after = before.minusYears(5); // Fetch events from the past 1 month
        int limit = 1000; // Limit of events to load at a time
        int offset = 0; // Offset for pagination

        List<LineageEvent> lineageEvents;
        do {
            // Fetch a batch of lineage events
            lineageEvents = openLineageDao.getAllLineageEventsDesc(before, after, limit, offset);

            // Index each event into Lucene
            for (LineageEvent event : lineageEvents) {
                indexLineageEvent(event);
            }

            offset += limit; // Increment the offset for the next batch
        } while (!lineageEvents.isEmpty());
    }

    private void indexLineageEvent(@Valid @NotNull LineageEvent event) throws IOException {
        // Convert inputs and outputs to Map<String, Object> and index them
        if (event.getInputs() != null) {
            for (Dataset input : event.getInputs()) {
                Map<String, Object> inputMap = mapDatasetEvent(input, event.getRun().getRunId(), event.getEventType());
                indexDatasetDocument(inputMap);
            }
        }

        if (event.getOutputs() != null) {
            for (Dataset  output : event.getOutputs()) {
                Map<String, Object> outputMap = mapDatasetEvent(output, event.getRun().getRunId(), event.getEventType());
                indexDatasetDocument(outputMap);
            }
        }
        Map<String, Object> jobMap = mapJobEvent(event);
        indexJobDocument(jobMap);
    }

    private Map<String, Object> mapDatasetEvent(Dataset dataset, String run_id, String eventType) {
        Map<String, Object> datasetMap = new HashMap<>();
        datasetMap.put("run_id", run_id);
        datasetMap.put("eventType", eventType);
        datasetMap.put("name", dataset.getName());
        datasetMap.put("namespace", dataset.getNamespace());
        Optional.ofNullable(dataset.getFacets()).ifPresent(facets -> datasetMap.put("facets", facets));
        return datasetMap;
    }

    // Helper method to map job details to Map<String, Object>
    private Map<String, Object> mapJobEvent(LineageEvent event) {
        Map<String, Object> jobMap = new HashMap<>();
        jobMap.put("run_id", event.getRun().getRunId().toString());
        jobMap.put("name", event.getJob().getName());
        jobMap.put("namespace", event.getJob().getNamespace());
        jobMap.put("eventType", event.getEventType());
        Optional.ofNullable(event.getRun().getFacets()).ifPresent(facets -> jobMap.put("facets", facets));
        return jobMap;
    }

    private boolean documentAlreadyExists(Map<String, Object> document, Directory indexDirectory) throws IOException {
        // Check if the index is empty before performing any search
        if (isIndexEmpty(indexDirectory)) {
            return false; // No document exists if the index is empty
        }
    
        try (DirectoryReader reader = DirectoryReader.open(indexDirectory)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            MultiFieldQueryParser parser = new MultiFieldQueryParser(new String[]{"name", "namespace"}, analyzer);
            String name = (String) document.get("name");
            String namespace = (String) document.get("namespace");
            Query query = parser.parse("name:\"" + name + "\" AND namespace:\"" + namespace + "\"");
            TopDocs topDocs = searcher.search(query, 1);
    
            if (topDocs.totalHits.value > 0) {
                StoredFields storedFields = searcher.storedFields();
                Document existingDoc = storedFields.document(topDocs.scoreDocs[0].doc);
                // Compare other fields to determine if the document needs an update
                for (Map.Entry<String, Object> entry : document.entrySet()) {
                    String fieldName = entry.getKey();
                    String fieldValue = entry.getValue() != null ? entry.getValue().toString() : null;
                    // If the stored field is different from the new field value, return true (needs update)
                    if (fieldValue != null && !fieldValue.equals(existingDoc.get(fieldName))) {
                        return false; // Document exists but needs an update
                        //TODO: handle that case in a better way
                    }
                }
                return true; // Document exists and does not need an update
            }
    
            return false; // Document does not exist
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Failed to search for document", e);
        }
    }
    

    // Method to index a job document
    //TODO: don't index a Map, use the Dataset object directly
    public IndexResponse indexJobDocument(Map<String, Object> document) throws IOException {
        // Check if the document already exists
        if (documentAlreadyExists(document, jobIndexDirectory)) {
            // Document exists and needs an update; first delete the old document
            return createIndexResponse("jobs", document.get("name").toString(), false);
        }

        try (IndexWriter writer = new IndexWriter(jobIndexDirectory, new IndexWriterConfig(analyzer))) {
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
    //TODO: don't index a Map, use the Dataset object directly
    public IndexResponse indexDatasetDocument(Map<String, Object> document) throws IOException {
        // Check if the document exists
        if (documentAlreadyExists(document, datasetIndexDirectory)) {
            // Document exists and needs an update; first delete the old document
            return createIndexResponse("datasets", document.get("name").toString(), false);
        }

        try (IndexWriter writer = new IndexWriter(datasetIndexDirectory, new IndexWriterConfig(analyzer))) {
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

    private boolean isIndexEmpty(Directory indexDirectory) throws IOException {
        try (DirectoryReader reader = DirectoryReader.open(indexDirectory)) {
            return reader.numDocs() == 0;
        } catch (IndexNotFoundException e) {
            return true;
        }
    }

    public SearchResult searchDatasets(String query, List<String> fields) throws Exception {
        return search(query, fields, datasetIndexDirectory);
    }

    public SearchResult searchJobs(String query, List<String> fields) throws Exception {
        return search(query, fields, jobIndexDirectory);
    }

    private SearchResult search(String query, List<String> fields, Directory indexDirectory) throws Exception {
        long startTime = System.currentTimeMillis();

        if (isIndexEmpty(indexDirectory)) {
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

            StoredFields storedFields = searcher.storedFields();
            SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter("<em>", "</em>");
            Highlighter highlighter = new Highlighter(htmlFormatter, new QueryScorer(q));

            for (int i = 0; i < topDocs.scoreDocs.length; i++) {
                ScoreDoc sd = topDocs.scoreDocs[i];
                Document doc = storedFields.document(sd.doc);
                Map<String, String> allDoc = new HashMap<>();
                Map<String, List<String>> highlight = new HashMap<>();
                
                for (IndexableField field : doc.getFields()) {
                    allDoc.put(field.name(), field.stringValue());
                }

                for (String field : fields) {
                    String text = doc.get(field);
                    if (text != null) {
                        String highlightedText = highlighter.getBestFragment(analyzer, field, text);
                        if (highlightedText != null) {
                            List<String> highlightList = new ArrayList<>();
                            highlightList.add(highlightedText);
                            highlight.put(field, highlightList);
                        }
                    }
                }

                result.addDocument(indexDirectory == jobIndexDirectory ? "jobs" : "datasets", allDoc, highlight, i);
            }

            return result;
        }
    }

    private SearchResult createEmptySearchResult(long startTime) {
        long took = System.currentTimeMillis() - startTime;

        SearchResult result = new SearchResult();
        result.setTook(took);
        result.getHitsMetadata().getTotalHits().setValue(0);
        result.setTimedOut(false);

        return result;
    }
}
