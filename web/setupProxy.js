
const express = require('express')
const { createProxyMiddleware } = require('http-proxy-middleware')
const path = require('path')
const appMetrics = require('./services/appMetrics')

const app = express();
const router = express.Router();
const distPath = path.join(__dirname, 'dist')

// Initialize Metrics
const metrics = new appMetrics();

// Middleware to expose /metrics endpoint
app.get('/metrics', async (req, res) => {
  try {
    res.set('Content-Type', metrics.register.contentType)
    res.end(await metrics.getMetrics());
  } catch (ex) {
    res.status(500).end(ex);
  }
});

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

// Helper function to format datetime as "YYYY-MM-DD HH:mm:SS.sss"
function getFormattedDateTime() {
  const d = new Date();
  const pad = (n, size = 2) => n.toString().padStart(size, '0')
  const year = d.getFullYear()
  const month = pad(d.getMonth() + 1)
  const day = pad(d.getDate())
  const hour = pad(d.getHours())
  const minute = pad(d.getMinutes())
  const second = pad(d.getSeconds())
  // JavaScript Date only provides milliseconds (0-999), so we pad to 3 digits
  const ms = pad(d.getMilliseconds(), 3)
  return `${year}-${month}-${day} ${hour}:${minute}:${second}.${ms}`
}

// Endpoint to log user info and increment counters
app.post('/api/loguserinfo', (req, res) => {
  const { email = '' } = req.body;

  if (typeof email !== 'string') {
    return res.status(400).json({ error: 'Invalid email format' })
  }

  const encodedEmail = Buffer.from(email).toString('base64')

  // Increment total logins counter
  metrics.incrementTotalLogins(email)

  // Check if the user is logging in for the first time in the last 8 hours
  metrics.incrementUniqueLogins(email)

  const logData = {
    accessLog: {
      email: encodedEmail,
      dateTime: getFormattedDateTime(),
    },
  };
  console.log(JSON.stringify(logData));
  res.sendStatus(200);
});

// Export the app for testing or further integration
module.exports = app;

