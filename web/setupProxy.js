const proxy = require('http-proxy-middleware')
const express = require('express')
const router = express.Router()

const apiOptions = {
  target: `http://${process.env.MARQUEZ_HOST}:${process.env.MARQUEZ_PORT}/`
}
const app = express()
const path = __dirname + '/dist'

app.use('/', express.static(path))
app.use('/datasets', express.static(path))
app.use('/lineage/:type/:namespace/:name', express.static(path))
app.use(proxy('/api/v1', apiOptions))

router.get('/healthcheck', function (req, res) {
  res.send('OK')
})

app.use(router)

app.listen(3000, function() {
  console.log('App listening on port 3000!')
})
