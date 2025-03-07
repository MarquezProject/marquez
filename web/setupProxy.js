
const express = require('express')
const { createProxyMiddleware } = require('http-proxy-middleware')
const path = require('path')
const appMetrics = require('./services/appMetrics')
const { sendLogToKafka } = require('./services/kafkaProducer')
const { getFormattedDateTime } = require('./services/helpers/dateTimeHelper')
const { excludedEmails } = require('./services/helpers/excludedEmails')
const { connectProducer } = require('./services/kafkaProducer')
const { buildLogData } = require('./services/helpers/logFormatter')

const app = express();
const router = express.Router();
const distPath = path.join(__dirname, 'dist')

// Initialize Metrics
const metrics = new appMetrics()

// Middleware to expose /metrics endpoint
app.get('/metrics', async (req, res) => {
  try {
    res.set('Content-Type', metrics.register.contentType)
    res.end(await metrics.getMetrics())
  } catch (ex) {
    res.status(500).end(ex)
  }
});

(async () => {
  try {
    await connectProducer()
  } catch (error) {
    console.error('Error connecting Kafka producer:', error)
    process.exit(1)
  }
})();

const environmentVariable = (variableName) => {
  const value = process.env[variableName]
  if (!value) {
      console.error(`Error: ${variableName} environment variable is not defined.`)
      console.error(`Please set ${variableName} and restart the application.`)
      process.exit(1)
  }
  return value
}

const port = environmentVariable("WEB_PORT")

const apiOptions = {
  target: `http://${environmentVariable("MARQUEZ_HOST")}:${environmentVariable("MARQUEZ_PORT")}/`,
  changeOrigin: true,
}

app.use(express.json());

// Serve static files for specific routes
app.use('/', express.static(distPath))
app.use('/datasets', express.static(distPath))
app.use('/events', express.static(distPath))
app.use('/lineage', express.static(distPath))
app.use('/jobs', express.static(distPath))
app.use('/datasets/column-level', express.static(distPath))

// Proxy API requests
+app.use('/api/v1', createProxyMiddleware(apiOptions))
app.use('/api/v2beta', createProxyMiddleware(apiOptions))

// Healthcheck route
router.get('/healthcheck', (req, res) => {
  res.send('OK')
})

app.use(router)

// **Catch-All Route to Serve index.html for Client-Side Routing**
app.get('*', (req, res) => {
  res.sendFile(path.join(distPath, 'index.html'))
})

app.listen(port, () => {
  console.log(`App listening on port ${port}!`)
})

app.use(express.json())

// Endpoint to log user info and increment counters
app.post('/api/loguserinfo', (req, res) => {
  const {
    email = '',
    name,
    locale,
    zoneinfo
  } = req.body

  // Guard against invalid email values
  if (typeof email !== 'string') {
    return res.status(400).json({ error: 'Invalid email format' })
  }

    // Calculate the encoded email once
    const encodedEmail = Buffer.from(email).toString('base64')

    // Skip processing if the email is excluded
    if (excludedEmails.has(encodedEmail)) {
      return res.sendStatus(200);
    }

    // Create userInfo from request data (without circular references)
    const userInfo = {
      email,
      name,
      locale,
      zoneinfo,
      email_verified: true
    }

  // Build enriched log data using the helper
  const kafkaData = buildLogData(userInfo)

  // Update metrics
  metrics.incrementTotalLogins(email)
  metrics.incrementUniqueLogins(email)

  if (excludedEmails.has(encodedEmail)) {
    return; // skip everything for excluded emails
  }
  // Console log for local debugging
  const logData = {
    accessLog: {
      email: encodedEmail,
      dateTime: getFormattedDateTime()
    }
  };
  console.log(JSON.stringify(logData))

  // Send meta info to Kafka
  sendLogToKafka(kafkaData)

  // Response
  res.sendStatus(200)
});

module.exports = app

