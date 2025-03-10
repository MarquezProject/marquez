/**
 * appMetrics Module
 *
 * This module is responsible for collecting and exposing various Prometheus 
 * metrics related to the application's performance and user activity. It 
 * tracks total and unique user logins, application uptime, and user activity 
 * over a specified duration. Additionally, it integrates with Redis for 
 * storing login timestamps and Kafka for logging unique user events, all while 
 * ensuring that emails on the excluded list are not processed.
 *
 * Author: Jonathan Moraes
 * Created: 2025-02-19
 * Reason: To monitor application performance and user activity while protecting 
 * sensitive internal data.
 */

const client = require('prom-client')
const crypto = require('crypto')
const { buildLogData } = require('./helpers/logFormatter')

// Centralize the excluded emails list
const { excludedEmails } = require('./helpers/excludedEmails')

// Import Kafka producer functions from your kafkaProducer.js file
const { sendLogToKafka } = require('./kafkaProducer')

// Import Redis write client from your redisClient.js file
const { redisWriteClient, redisReadClient } = require('./redisClient')

class AppMetrics {
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
      name: 'user_access_activity_gauge',
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
    if (typeof email !== 'string') {
      console.error('Invalid email provided to incrementTotalLogins:', email);
      return; // Early exit if email is not a string.
    }
    const encodedEmail = this.encodeEmail(email);
    if (excludedEmails.has(encodedEmail)) {
      return; // Do not increment if user is in the excluded list
    }
    this.totalUserLoginCounter.inc();
  }
  
  encodeEmail(email) {
    // Optionally check here:
    if (!email) {
      console.error('No email provided to encodeEmail.');
      return '';
    }
    return Buffer.from(email).toString('base64');
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
    const sevenDays = 7 * 24 * 60 * 60 * 1000; // 7 days in milliseconds
    
    if (excludedEmails.has(encodedEmail)) {
      return; // skip everything for excluded emails
    }

    try {
      const storedTime = await redisReadClient.get(key);
      if (!storedTime || (currentTime - parseInt(storedTime)) > sevenDays) {
        // Set the key with expiration (7 days)
        await redisWriteClient.set(key, currentTime, { EX: 7 * 24 * 60 * 60 });
        this.uniqueUserLoginCounter.inc();

        const userInfo = { email}; 
        const logData = buildLogData(userInfo);
        sendLogToKafka(logData);
      }
    } catch (err) {
      console.error('Error in incrementUniqueLogins:', err);
    }
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
   * Checks if there is any user logged in at the moment.
   * Excludes specific base64-encoded emails from the count.
   */
  async updateUserActivity() {
    setInterval(async () => {
      try {
        const keys = await redisReadClient.keys('unique_user:*');
        let userActivityDetected = false;

        if (keys && keys.length) {
          // Get values for all keys
          const pipeline = redisReadClient.multi();
          keys.forEach((key) => {
            pipeline.get(key);
          });
          await pipeline.exec();

          for (const key of keys) {
            const encodedEmail = key.replace('unique_user:', '');
            // Check if the email is not in the excluded list
            if (!excludedEmails.has(encodedEmail)) {
              userActivityDetected = true;
              break; // No need to check further if activity is detected
            }
          }
        }
        this.userActivityGauge.set(userActivityDetected ? 1 : 0);
      } catch (err) {
        console.error('Error updating user activity gauge:', err);
      }
    }, 60 * 1000); // Update every minute
  }
}
module.exports = AppMetrics;