
const { createProxyMiddleware } = require('http-proxy-middleware')

const express = require('express')
const router = express.Router()
const path = require('path')

const environmentVariable = (variableName) => {
  const value = process.env[variableName]
  if (!value) {
      console.error(`Error: ${variableName} environment variable is not defined.`)
      console.error(`Please set ${variableName} and restart the application.`)
      process.exit(1)
  }
  return value
}

const apiOptions = {
  target: `http://${environmentVariable("MARQUEZ_HOST")}:${environmentVariable("MARQUEZ_PORT")}/`,
  changeOrigin: true,
}

const app = express()

const distPath = path.join(__dirname, 'dist')

const port = environmentVariable("WEB_PORT")

// Serve static files for specific routes
app.use('/', express.static(distPath))
app.use('/datasets', express.static(distPath))
app.use('/events', express.static(distPath))
app.use('/lineage', express.static(distPath))
app.use('/datasets/column-level', express.static(distPath))

// Proxy API requests
app.use('/api/v1', createProxyMiddleware(apiOptions))
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