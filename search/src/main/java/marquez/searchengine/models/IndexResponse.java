package marquez.searchengine.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IndexResponse {

    @JsonProperty("_index")
    private final String index;

    @JsonProperty("_id")
    private final String id;

    @JsonProperty("_version")
    private final long version;

    @JsonProperty("result")
    private final String result;

    @JsonProperty("_shards")
    private final ShardInfo shardInfo;

    @JsonProperty("_seq_no")
    private final long seqNo;

    @JsonProperty("_primary_term")
    private final long primaryTerm;

    // Constructor to initialize all final fields
    public IndexResponse(String index, String id, long version, String result, ShardInfo shardInfo, long seqNo, long primaryTerm) {
        this.index = index;
        this.id = id;
        this.version = version;
        this.result = result;
        this.shardInfo = shardInfo;
        this.seqNo = seqNo;
        this.primaryTerm = primaryTerm;
    }

    // Getters
    public String getIndex() {
        return index;
    }

    public String getId() {
        return id;
    }

    public long getVersion() {
        return version;
    }

    public String getResult() {
        return result;
    }

    public ShardInfo getShardInfo() {
        return shardInfo;
    }

    public long getSeqNo() {
        return seqNo;
    }

    public long getPrimaryTerm() {
        return primaryTerm;
    }

    // ShardInfo inner class
    public static class ShardInfo {
        @JsonProperty("total")
        private final int total;

        @JsonProperty("successful")
        private final int successful;

        @JsonProperty("failed")
        private final int failed;

        public ShardInfo(int total, int successful, int failed) {
            this.total = total;
            this.successful = successful;
            this.failed = failed;
        }

        // Getters for ShardInfo
        public int getTotal() {
            return total;
        }

        public int getSuccessful() {
            return successful;
        }

        public int getFailed() {
            return failed;
        }
    }
}
