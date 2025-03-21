/**
 * Excluded Emails List
 *
 * This module provides a Set of Base64 encoded email addresses that should be 
 * excluded from tracking and logging in the application. This is used in both 
 * the metrics and logging modules to prevent any actions on these sensitive emails.
 *
 * Author: Jonathan Moraes
 * Created: 2025-02-19
 * Reason: To protect sensitive internal email addresses and avoid logging/tracking them.
 */

const excludedEmails = new Set([
  'bWF0ZXVzLmNhcmRvc29AbnViYW5rLmNvbS5icg==',
  'bHVpcy55YW1hZGFAbnViYW5rLmNvbS5icg==',
  'cmFmYWVsLmJyYWdlcm9sbGlAbnViYW5rLmNvbS5icg==',
  'a2Fpby5iZW5pY2lvQG51YmFuay5jb20uYnI=',
  'bWljaGFlbC5zYW50YUBudWJhbmsuY29tLmJy',
  'cGVkcm8uYXJhdWpvMUBudWJhbmsuY29tLmJy',
  'amhvbmF0YXMucm9zZW5kb0BudWJhbmsuY29tLmJy',
  'dml2aWFuLm1pcmFuZGFAbnViYW5rLmNvbS5icg==',
  'YnJ1bmEucGVyaW5AbnViYW5rLmNvbS5icg==',
  'am9uYXRoYW4ubW9yYWVzLmdmdEBudWJhbmsuY29tLmJy'
  ]);
  
  module.exports = { excludedEmails }