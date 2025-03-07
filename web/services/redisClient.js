/**
 * Redis Client Module
 *
 * This module configures and manages Redis clients for both read and write operations.
 * It ensures that the clients are connected and handles connection setup and error logging.
 *
 * Author: Jonathan Moraes
 * Created: 2025-02-19
 * Reason: To standardize and manage the process of connecting to Redis for read and write operations.
 */

const redis = require('redis');

// Function to create a Redis client
const createRedisClient = (host, port, role) => {
  const client = redis.createClient({
    url: `redis://${host}:${port}`,
  });

  client.on('error', (err) => {
    console.error(`Redis ${role} client error:`, err);
  });

  client.on('connect', () => {
    console.log(`Connected to Redis ${role} client`);
  });

  return client;
};

// Create Redis write and read clients
const redisWriteClient = createRedisClient(
  process.env.REDIS_WRITE_HOST,
  process.env.REDIS_PORT,
  'write'
);

const redisReadClient = createRedisClient(
  process.env.REDIS_READ_HOST,
  process.env.REDIS_PORT,
  'read'
);

// Connect the clients once
(async () => {
  try {
    await redisWriteClient.connect();
    await redisReadClient.connect();
    console.log('Redis clients connected successfully.');
  } catch (err) {
    console.error('Error connecting to Redis:', err);
    process.exit(1);
  }
})();

module.exports = {
  redisWriteClient,
  redisReadClient,
};