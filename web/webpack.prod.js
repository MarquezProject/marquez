const { merge } = require('webpack-merge')
const webpack = require('webpack')
const webpackShared = require('./webpack.common.js')
const { CleanWebpackPlugin } = require('clean-webpack-plugin')
const HtmlWebpackPlugin = require('html-webpack-plugin')
const CopyPlugin = require("copy-webpack-plugin");
const path = require("path");

// look for elkjs package folder
const elkjsRoot = path.dirname(require.resolve('elkjs/package.json'));

const webpackProd = {
  mode: 'production',
  devtool: 'source-map',
  plugins: [
    new CleanWebpackPlugin(),
    new HtmlWebpackPlugin({
      title: 'Telescope Web',
      hash: true,
      minify: true,
      inject: false,
      template: 'src/index.prod.html',
      favicon: 'src/img/favicon.png'
    }),
    new webpack.DefinePlugin({
      __DEVELOPMENT__: JSON.stringify(false),
      __NODE_ENV__: JSON.stringify('production'),
      __API_URL__: JSON.stringify('/api/v1'),
      __TEMP_ACTOR_STR__: JSON.stringify('me'),
      __ROLLBAR__: JSON.stringify(true),
      __FEEDBACK_FORM_URL__: JSON.stringify('https://forms.gle/f3tTSrZ8wPj3sHTA7'),
      __API_DOCS_URL__: JSON.stringify('https://marquezproject.github.io/marquez/openapi.html')
    }),
    new CopyPlugin({
      patterns: [
        { from: path.join(elkjsRoot, 'lib/elk-worker.min.js'), to: 'elk-worker.min.js' },
      ],
    }),
  ]
}

module.exports = merge(webpackShared, webpackProd)
