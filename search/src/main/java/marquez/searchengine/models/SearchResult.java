package marquez.searchengine.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SearchResult {
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
    public void addDocument(String index, Map<String, String> doc) {
        Map<String, Object> hit = new HashMap<>();
        hit.put("_index", index);  // Include the index name in the hit
        hit.put("_source", doc);
        hitsMetadata.addHit(index, hit);
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
        public void addHit(String index, Map<String, Object> doc) {
            Map<String, Object> hit = new HashMap<>();
            hit.put("_index", index); 
            hit.putAll(doc);
            hit.put("_id", "id");
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
