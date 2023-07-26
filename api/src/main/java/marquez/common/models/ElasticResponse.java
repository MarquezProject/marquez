package marquez.common.models;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;
@Value
@Builder
@AllArgsConstructor
public class ElasticResponse {

    List<ObjectNode> datasets;

    List<ObjectNode> jobs;
}
