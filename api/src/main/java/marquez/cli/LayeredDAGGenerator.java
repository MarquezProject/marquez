package marquez.cli;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;

/**
 * This class generates DAGs in a "layered" structure. First, it splits graph nodes into randomly
 * sized layers of monotonically growing node numbers. That means that node N will always be in
 * lower layer or the same as every node with number greater than N. Next, it creates jobs for each
 * node element that are in layers higher than 1. Job consists of one output node and up to
 * maxJobInputCount input nodes. The input nodes are chosen by randomly selecting numbers in layers
 * lower than current layer.
 */
@AllArgsConstructor
public class LayeredDAGGenerator {
  private final int nodeCount;
  private final int levelCount;
  private final int firstLevelSize;
  private final int maxJobInputCount;
  private final Random random = new Random();

  public List<JobTemplate> generateGraph() {
    List<JobTemplate> jobs = new ArrayList<>();

    List<List<Integer>> tiers = new ArrayList<>();
    if (firstLevelSize > 0) {
      tiers.add(IntStream.rangeClosed(1, firstLevelSize).boxed().toList());
      tiers.addAll(
          splitListIntoRandomChunks(
              IntStream.rangeClosed(firstLevelSize + 1, nodeCount)
                  .boxed()
                  .collect(Collectors.toList()),
              levelCount - 1));
    } else {
      tiers =
          splitListIntoRandomChunks(
              IntStream.rangeClosed(1, nodeCount).boxed().collect(Collectors.toList()), levelCount);
    }

    int jobCount = 0;
    int previousLevelsSize = tiers.get(0).size();
    for (int i = 1; i < levelCount; i++) {
      System.out.format("\nLevel %d: ", i);
      List<Integer> levelDatasets = tiers.get(i);
      for (Integer levelDataset : levelDatasets) {
        System.out.printf("%s ", levelDataset);
        Set<Integer> inputDatasets = new TreeSet<>();

        int inputCount = Math.max(2, (int) (random.nextGaussian() * maxJobInputCount));
        for (int k = 0; k < inputCount; k++) {
          inputDatasets.add(random.nextInt(1, previousLevelsSize + 1));
        }
        jobs.add(
            new JobTemplate(
                inputDatasets.stream().toList(),
                Collections.singletonList(levelDataset),
                String.format("job%d", jobCount),
                Optional.empty()));
        jobCount++;
      }
      previousLevelsSize += tiers.get(i).size();
    }
    return jobs;
  }

  private List<List<Integer>> splitListIntoRandomChunks(List<Integer> nodes, int chunks) {
    if (chunks == 0) {
      return Collections.emptyList();
    } else if (chunks == 1) {
      return List.of(nodes);
    }

    List<List<Integer>> splits = new ArrayList<>();
    double avg = (double) nodes.size() / (double) chunks;
    double rand = (1.0 + random.nextGaussian() / 3) * avg;
    int index = Math.min(Math.max((int) rand, 1), nodes.size() - chunks);
    splits.add(nodes.subList(0, index));
    splits.addAll(splitListIntoRandomChunks(nodes.subList(index, nodes.size()), chunks - 1));
    return splits;
  }
}
