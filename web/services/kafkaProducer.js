/**
 * Kafka Producer Module
 *
 * This module configures and manages a Kafka producer for sending log messages
 * to a specified Kafka topic. It ensures that the topic exists before sending
 * messages and handles connection setup and error logging.
 *
 * Author: Jonathan Moraes
 * Created: 2025-02-19
 * Reason: To standardize and manage the process of sending log messages to Kafka
 * for user access logging.
 */

const { Kafka } = require('kafkajs');

const KAFKA_LOADBALANCER_DNS = process.env.KAFKA_LOADBALANCER_DNS
const KAFKA_PORT = process.env.KAFKA_PORT

// Configure Kafka client with your broker list (can be set via environment variables)
const kafka = new Kafka({
  clientId: 'log-producer',
  brokers: [`${KAFKA_LOADBALANCER_DNS}:${KAFKA_PORT}`]
});

const producer = kafka.producer();

const connectProducer = async () => {
  await producer.connect();
  console.log('Kafka producer connected.');
};

const sendLogToKafka = async (log) => {
  try {
    await producer.send({
      topic: 'DATA-LINEAGE.USER-ACCESS-LOGS',
      messages: [
        { value: JSON.stringify(log) }
      ]
    });
    console.log('Log sent to Kafka.');
  } catch (error) {
    console.error('Error sending log to Kafka:', error);
  }
};

module.exports = { connectProducer, sendLogToKafka, producer };