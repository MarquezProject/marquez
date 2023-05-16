# FAQ

### How do I configure a data retention policy?

By default, Marquez does not apply a data retention policy on collected metadata. However, you can adjust the maximum retention days for metadata in Marquez. This allows you to better manage your storage space and comply with your organizational retention policies.

To adjust the retention period, add a `dbRetention` section in your [`marquez.yml`](https://github.com/MarquezProject/marquez/blob/main/marquez.example.yml):

```yaml
# Adjusts retention policy
dbRetention:
  # Apply data retention at a frequency of every '15' minutes
  frequencyMins: 15
  # Maximum retention for metadata
  retentionDays: 7
```

To run an _ad-hoc_ retention policy on your metadata, use the `db-retention` command:

```bash
java -jar marquez-api.jar --retention-days 7 marquez.yml
```
