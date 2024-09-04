package marquez.searchengine.models;

import java.util.Map;
import java.util.List;

public class SearchRequest {
    private Highlight highlight;
    private Query query;

    public static class Highlight {
        private Map<String, Map<String, String>> fields;

        // Getters and setters
        public Map<String, Map<String, String>> getFields() {
            return fields;
        }

        public void setFields(Map<String, Map<String, String>> fields) {
            this.fields = fields;
        }
    }

    public static class Query {
        private MultiMatch multi_match;

        public static class MultiMatch {
            private List<String> fields;
            private String operator;
            private String query;
            private String type;

            // Getters and setters
            public List<String> getFields() {
                return fields;
            }

            public void setFields(List<String> fields) {
                this.fields = fields;
            }

            public String getOperator() {
                return operator;
            }

            public void setOperator(String operator) {
                this.operator = operator;
            }

            public String getQuery() {
                return query;
            }

            public void setQuery(String query) {
                this.query = query;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }
        }

        // Getters and setters
        public MultiMatch getMulti_match() {
            return multi_match;
        }

        public void setMulti_match(MultiMatch multi_match) {
            this.multi_match = multi_match;
        }
    }

    // Getters and setters for SearchRequest
    public Highlight getHighlight() {
        return highlight;
    }

    public void setHighlight(Highlight highlight) {
        this.highlight = highlight;
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }
}
