package marquez.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

public class ElasticConfig {
    public static final boolean ENABLED = false;
    public static final String SCHEME = "http";
    public static final String HOST = "search";
    public static final int PORT = 9200;
    public static final String USERNAME = "elastic";
    public static final String PASSWORD = "elastic";

    @Getter @JsonProperty
    private boolean enabled = ENABLED;

    @Getter @JsonProperty
    private String scheme = SCHEME;

    @Getter @JsonProperty
    private String host = HOST;

    @Getter @JsonProperty
    private int port = PORT;

    @Getter @JsonProperty
    private String username = USERNAME;

    @Getter @JsonProperty
    private String password = PASSWORD;

}
