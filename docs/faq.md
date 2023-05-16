# FAQ

### How do I configure a metadata retention policy?

By default, Marquez does not apply a retention policy on collected metadata. However, you can adjust the maximum retention days for metadata in Marquez. This allows you to better manage your storage space and comply with your organizational retention policies. BBelow, you'll find examples of how to change retention days in YAML and via the CLI:

**`YAML`**

To adjust the retention period, add a `dbRetention` section in your [`marquez.yml`](https://github.com/MarquezProject/marquez/blob/main/marquez.example.yml):

```yaml
# Adjusts retention policy
dbRetention:
  # Apply data retention at a frequency of every '15' minutes
  frequencyMins: 15
  # Maximum retention for metadata
  retentionDays: 7
```

**`CLI`**

To run an _ad-hoc_ retention policy on your metadata, use the [`db-retention`](https://github.com/MarquezProject/marquez/blob/main/api/src/main/java/marquez/cli/DbRetentionCommand.java) command:

```bash
java -jar marquez-api.jar db-retention --retention-days 7 marquez.yml
```
