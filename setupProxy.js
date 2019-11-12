const proxy = require('http-proxy-middleware')
const express = require('express')

var apiOptions = {
  target: `http://${process.env.MARQUEZ_HOST}:${process.env.MARQUEZ_PORT}/`
}

var app = express()

const path = __dirname + '/dist/'
app.use(express.static(path))
app.use(proxy('/api/v1', apiOptions))

app.listen(3000, function() {
  console.log('App listening on port 3000!')
})
