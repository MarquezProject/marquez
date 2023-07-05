# FAQ

### How do I configure a retention policy for metadata?

By default, Marquez does not apply a retention policy on collected metadata. However, you can adjust the maximum retention days for metadata in Marquez. This allows you to better manage your storage space and comply with your organizational retention policies. Below, you'll find examples of how to change retention days in `YAML` and via the [CLI](https://github.com/MarquezProject/marquez/tree/main/api/src/main/java/marquez/cli):

**`YAML`**

To adjust the retention period, add a **`dbRetention`** section in your [`marquez.yml`](https://github.com/MarquezProject/marquez/blob/main/marquez.example.yml):

```yaml
# Adjusts retention policy
dbRetention:
  # Apply retention policy at a frequency of every '15' minutes
  frequencyMins: 15
  # Maximum number of rows deleted per batch
  numberOfRowsPerBatch: 1000
  # Maximum retention days
  retentionDays: 7
```

**`CLI`**

To run a  _one-off_ _ad-hoc_ retention policy on your metadata, use the [`db-retention`](https://github.com/MarquezProject/marquez/blob/main/api/src/main/java/marquez/cli/DbRetentionCommand.java) command:

```bash
java -jar marquez-api.jar db-retention \
  --number-of-rows-per-batch 1000 \
  --retention-days 7 \
  marquez.yml
```

Use the `--dry-run` flag to output an estimate of metadata deleted by the retention policy:

```bash
java -jar marquez-api.jar db-retention \
  --number-of-rows-per-batch 1000 \
  --retention-days 7 \
  --dry-run \
  marquez.yml
```
