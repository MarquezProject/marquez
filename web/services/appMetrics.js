const client = require('prom-client');
const crypto = require('crypto');

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

    // Register the counters and gauge
    this.register.registerMetric(this.uniqueUserLoginCounter);
    this.register.registerMetric(this.totalUserLoginCounter);
    this.register.registerMetric(this.appUptimeGauge);

    // (Optional) Collect default metrics like CPU and memory usage
    client.collectDefaultMetrics({ register: this.register });

    // In-memory storage for unique users with timestamps
    this.uniqueUsers = new Map();

    // Record the application start time
    this.startTime = Date.now();

    // Update the uptime gauge periodically
    this.updateUptime();

    // Start the cleanup interval
    this.startCleanup();
  }

  /**
   * Returns the Prometheus metrics as a string
   */
  async getMetrics() {
    return await this.register.metrics();
  }

  /**
   * Increments the total logins counter
   */
  incrementTotalLogins() {
    this.totalUserLoginCounter.inc();
  }

  /**
   * Increments the unique user logins counter if the user hasn't logged in within the retention period
   * @param {string} email - The user's email
   */
  incrementUniqueLogins(email) {
    const encodedEmail = this.encodeEmail(email);
    const currentTime = Date.now();
    const eightHours = 8 * 60 * 60 * 1000;

    // Check if the user is logging in for the first time in the last 8 hours
    if (
      !this.uniqueUsers.has(encodedEmail) ||
      currentTime - this.uniqueUsers.get(encodedEmail) > eightHours
    ) {
      this.uniqueUsers.set(encodedEmail, currentTime);
      this.uniqueUserLoginCounter.inc();
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
   * Starts an interval to clean up old unique user entries
   */
  startCleanup() {
    const cleanupInterval = 60 * 60 * 1000; // Every hour
    const eightHours = 8 * 60 * 60 * 1000;

    setInterval(() => {
      const currentTime = Date.now();
      for (const [email, timestamp] of this.uniqueUsers.entries()) {
        if ((currentTime - timestamp) > eightHours) {
          this.uniqueUsers.delete(email);
        }
      }
    }, cleanupInterval);
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
}

module.exports = appMetrics;