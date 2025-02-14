const client = require('prom-client');
const crypto = require('crypto');

// Import Redis write client from your redisClient.js file
const { redisWriteClient, redisReadClient } = require('./redisClient');

// Centralize the excluded emails list
const excludedEmails = new Set([
  'bWF0ZXVzLmNhcmRvc29AbnViYW5rLmNvbS5icg==',
  'bHVpcy55YW1hZGFAbnViYW5rLmNvbS5icg==',
  'cmFmYWVsLmJyYWdlcm9sbGlAbnViYW5rLmNvbS5icg==',
  'a2Fpby5iZW5pY2lvQG51YmFuay5jb20uYnI=',
  'bWljaGFlbC5zYW50YUBudWJhbmsuY29tLmJy',
  'cGVkcm8uYXJhdWpvMUBudWJhbmsuY29tLmJy',
  'amhvbmF0YXMucm9zZW5kb0BudWJhbmsuY29tLmJy',
  'dml2aWFuLm1pcmFuZGFAbnViYW5rLmNvbS5icg==',
  'YnJ1bmEucGVyaW5AbnViYW5rLmNvbS5icg=='
]);

class appMetrics {
  constructor() {
    // Create a Registry to hold all metrics
    this.register = new client.Registry();

    // Define Prometheus Counters
    this.uniqueUserLoginCounter = new client.Counter({
      name: 'unique_user_login_total',
      help: 'Total number of unique user logins',
    });

    this.totalUserLoginCounter = new client.Counter({
      name: 'total_user_logins',
      help: 'Total number of user logins',
    });

    // Define Prometheus Gauge for application uptime
    this.appUptimeGauge = new client.Gauge({
      name: 'app_uptime_seconds',
      help: 'Uptime of the application in seconds',
    });

    // Define Prometheus Gauge for user activity in the last 72 hours
    this.userActivityGauge = new client.Gauge({
      name: 'user_activity_last_72_hours',
      help: 'Indicates whether there have been users in the last 72 hours (1) or not (0)',
    });

    // Register the counters and gauges
    this.register.registerMetric(this.uniqueUserLoginCounter);
    this.register.registerMetric(this.totalUserLoginCounter);
    this.register.registerMetric(this.appUptimeGauge);
    this.register.registerMetric(this.userActivityGauge);

    // (Optional) Collect default metrics like CPU and memory usage
    client.collectDefaultMetrics({ register: this.register });

    // Record the application start time
    this.startTime = Date.now();

    // Update the uptime gauge periodically
    this.updateUptime();

    // Update the user activity gauge periodically
    this.updateUserActivity();
  }

  /**
   * Returns the Prometheus metrics as a string
   */
  async getMetrics() {
    return await this.register.metrics();
  }

  /**
   * Increments the total logins counter if the user is not excluded.
   * @param {string} email - The user's email
   */
  incrementTotalLogins(email) {
    const encodedEmail = this.encodeEmail(email);
    if (excludedEmails.has(encodedEmail)) {
      return; // Do not increment if user is in the excluded list
    }
    this.totalUserLoginCounter.inc();
  }

  /**
   * Increments the unique user logins counter if the user hasn't logged in within the retention period.
   * Stores/updates in Redis with a TTL of 7 days.
   * @param {string} email - The user's email
   */
  async incrementUniqueLogins(email) {
    const encodedEmail = this.encodeEmail(email);
    const key = `unique_user:${encodedEmail}`;
    const currentTime = Date.now();
    const eightHours = 8 * 60 * 60 * 1000;

    try {
      const storedTime = await redisReadClient.get(key);
      if (!storedTime || (currentTime - parseInt(storedTime)) > eightHours) {
        // Set the key with expiration (7 days)
        await redisWriteClient.set(key, currentTime, { EX: 7 * 24 * 60 * 60 });
        this.uniqueUserLoginCounter.inc();
      }
    } catch (err) {
      console.error('Error in incrementUniqueLogins:', err);
    }
  }

  /**
   * Encodes the email using Base64 encoding
   * @param {string} email
   * @returns {string}
   */
  encodeEmail(email) {
    return Buffer.from(email).toString('base64');
  }

  /**
   * Hashes the email using SHA-256 for enhanced security
   * @param {string} email
   * @returns {string}
   */
  hashEmail(email) {
    return crypto.createHash('sha256').update(email).digest('hex');
  }

  /**
   * Updates the uptime gauge every second
   */
  updateUptime() {
    setInterval(() => {
      const uptimeSeconds = (Date.now() - this.startTime) / 1000;
      this.appUptimeGauge.set(uptimeSeconds);
    }, 1000); // Update every second
  }

  /**
   * Updates the user activity gauge every minute.
   * Examines Redis keys starting with "unique_user:" and counts keys where the stored timestamp is within 72 hours.
   * Excludes specific base64-encoded emails from the count.
   */
  async updateUserActivity() {
    setInterval(async () => {
      try {
        const keys = await redisReadClient.keys('unique_user:*');
        let activeUsers = 0;
        const currentTime = Date.now();
        const seventyTwoHours = 72 * 60 * 60 * 1000;

        if (keys && keys.length) {
          // Get values for all keys
          const pipeline = redisReadClient.multi();
          keys.forEach((key) => {
            pipeline.get(key);
          });
          const results = await pipeline.exec();

          keys.forEach((key, index) => {
            const storedTime = results[index] ? parseInt(results[index]) : 0;
            const encodedEmail = key.replace('unique_user:', '');
            // Only count if within 72h and not in excluded list
            if ((currentTime - storedTime) <= seventyTwoHours && !excludedEmails.has(encodedEmail)) {
              activeUsers++;
            }
          });
        }
        this.userActivityGauge.set(activeUsers > 0 ? 1 : 0);
      } catch (err) {
        console.error('Error updating user activity gauge:', err);
      }
    }, 60 * 1000); // Update every minute
  }
}

module.exports = appMetrics;