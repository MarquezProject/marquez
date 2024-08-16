const { createProxyMiddleware } = require('http-proxy-middleware');

const express = require('express')
const router = express.Router()

const apiOptions = {
  target: `http://${process.env.MARQUEZ_HOST}:${process.env.MARQUEZ_PORT}/`
}
const app = express()
const path = __dirname + '/dist'

const port = process.env.WEB_PORT

app.use('/', express.static(path))
app.use('/datasets', express.static(path))
app.use('/events', express.static(path))
app.use('/lineage/:type/:namespace/:name', express.static(path))
app.use('/datasets/column-level/:namespace/:name', express.static(path))
app.use(createProxyMiddleware('/api/v1', apiOptions))
app.use(createProxyMiddleware('/api/v2beta', apiOptions))

router.get('/healthcheck', function (req, res) {
  res.send('OK')
})

app.use(router)

app.listen(port, function() {
  console.log(`App listening on port ${port}!`)
})
