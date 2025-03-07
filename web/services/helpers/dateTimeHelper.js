/**
 * DateTime Helper Module
 *
 * This module provides a function to format the current date and time
 * into a standardized, human-readable string. This formatted date/time
 * is used across the application to timestamp logs and other events.
 *
 * Author: Jonathan Moraes
 * Created: 2025-02-19
 * Reason: To ensure consistent and formatted timestamps in logging and monitoring.
 */

function getFormattedDateTime() {
    const d = new Date();
    const pad = (n, size = 2) => n.toString().padStart(size, '0');
    const year = d.getFullYear();
    const month = pad(d.getMonth() + 1);
    const day = pad(d.getDate());
    const hour = pad(d.getHours());
    const minute = pad(d.getMinutes());
    const second = pad(d.getSeconds());
    const ms = pad(d.getMilliseconds(), 3);
    return `${year}-${month}-${day} ${hour}:${minute}:${second}.${ms}`;
  }
  
  module.exports = { getFormattedDateTime };