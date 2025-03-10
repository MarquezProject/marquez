/**
 * Log Formatter Module
 *
 * This module provides a function to build enriched log data from a userInfo payload.
 * It extracts and formats information such as timestamp, pod name, username, locale,
 * email, zone information, and email verification status.
 *
 * Author: Jonathan Moraes
 * Created: 2025-02-19
 * Reason: To standardize the format of log data sent to Kafka for user access logging.
 */

const { getFormattedDateTime } = require('./dateTimeHelper')

function buildLogData(userInfo) {
    const timestamp = getFormattedDateTime();
    const podName = process.env.POD_NAME || "unknown-pod";
    
    return {
      timestamp,
      podName,
      username: userInfo.name,
      locale: userInfo.locale,
      email: userInfo.email,
      zoneinfo: userInfo.zoneinfo,
      email_verified: userInfo.email_verified
    };
  }
  
  module.exports = { buildLogData };