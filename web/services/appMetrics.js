/**
 * appMetrics Module
 *
 * This module is responsible for collecting and exposing various Prometheus 
 * metrics related to the application's performance and user activity. It 
 * tracks total and unique user logins and application uptime. Additionally, 
 * it integrates with Redis for ensuring that emails on the excluded list 
 * are not processed.
 *
 * Author: Jonathan Moraes
 * Created: 2025-02-19
 * Reason: To monitor application performance and user activity while protecting 
 * sensitive internal data.
 */

const client = require('prom-client')
const crypto = require('crypto')
const { excludedEmails } = require('./helpers/excludedEmails')
const { redisWriteClient, redisReadClient } = require('./redisClient')

class AppMetrics {
  constructor() {
    // Create a Registry to hold all metrics
    this.register = new client.Registry()

    // Define Prometheus Counters
    this.uniqueUserLoginCounter = new client.Counter({
      name: 'unique_user_login_total',
      help: 'Total number of unique user logins',
    })

    this.totalUserLoginCounter = new client.Counter({
      name: 'total_user_logins',
      help: 'Total number of user logins',
    })

    // Define Prometheus Gauge for application uptime
    this.appUptimeGauge = new client.Gauge({
      name: 'app_uptime_seconds',
      help: 'Uptime of the application in seconds',
    })

    // Register the counters and gauges
    this.register.registerMetric(this.uniqueUserLoginCounter)
    this.register.registerMetric(this.totalUserLoginCounter)
    this.register.registerMetric(this.appUptimeGauge)

    // Collect default metrics like CPU and memory usage
    client.collectDefaultMetrics({ register: this.register })

    // Record the application start time
    this.startTime = Date.now()

    // Update the uptime gauge periodically
    this.updateUptime()
  }

  async getMetrics() {
    return await this.register.metrics()
  }

  incrementTotalLogins(email) {
    if (typeof email !== 'string') {
      console.error('Invalid email provided to incrementTotalLogins:', email)
      return;
    }
    const encodedEmail = this.encodeEmail(email)
    if (excludedEmails.has(encodedEmail)) {
      return
    }
    this.totalUserLoginCounter.inc()
  }
  
  encodeEmail(email) {
    if (!email) {
      console.error('No email provided to encodeEmail.')
      return ''
    }
    return Buffer.from(email).toString('base64')
  }

  async incrementUniqueLogins(email) {
    const encodedEmail = this.encodeEmail(email)
    const key = `unique_user:${encodedEmail}`
    const currentTime = Date.now()
    const sevenDays = 7 * 24 * 60 * 60 * 1000
    
    if (excludedEmails.has(encodedEmail)) {
      return
    }

    try {
      const storedTime = await redisReadClient.get(key)
      if (!storedTime || (currentTime - parseInt(storedTime)) > sevenDays) {
        await redisWriteClient.set(key, currentTime, { EX: 7 * 24 * 60 * 60 })
        this.uniqueUserLoginCounter.inc()
      }
    } catch (err) {
      console.error('Error in incrementUniqueLogins:', err)
    }
  }

  hashEmail(email) {
    return crypto.createHash('sha256').update(email).digest('hex')
  }

  updateUptime() {
    setInterval(() => {
      const uptimeSeconds = (Date.now() - this.startTime) / 1000
      this.appUptimeGauge.set(uptimeSeconds)
    }, 1000)
  }
}

module.exports = AppMetrics