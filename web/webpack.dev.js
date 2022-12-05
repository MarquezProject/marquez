const merge = require('webpack-merge')
const webpack = require('webpack')
const webpackShared = require('./webpack.common.js')

const webpackDev = {
  mode: 'development',
  devServer: {
    static: {
      directory: __dirname + '/src',
      staticOptions: {},
      publicPath: "/",
      serveIndex: true,
      watch: true,
    },
    port: 1337,
    devMiddleware: {
      publicPath: '/'
    },
    historyApiFallback: {
      index: './index.html',
      disableDotRule: true
    },
    proxy: {
      '/api': {
        target: `http://${process.env.MARQUEZ_HOST || 'localhost'}:${process.env.MARQUEZ_PORT || 8080}/`,
        secure: false,
        logLevel: 'debug',
        headers: {
          'X-Bifrost-Authentication': 'developer'
        }
      }
    }
  },
  // Enable sourcemaps for debugging webpack"s output.
  devtool: 'eval-cheap-module-source-map',
  plugins: [
    new webpack.DefinePlugin({
      __DEVELOPMENT__: JSON.stringify(true),
      __API_URL__: JSON.stringify('/api/v1'),
      __NODE_ENV__: JSON.stringify('development'),
      __TEMP_ACTOR_STR__: JSON.stringify('me'),
      __FEEDBACK_FORM_URL__: JSON.stringify('https://forms.gle/f3tTSrZ8wPj3sHTA7'),
      __API_DOCS_URL__: JSON.stringify('https://marquezproject.github.io/marquez/openapi.html')
    })
  ]
}

module.exports = merge.smart(webpackShared, webpackDev)
