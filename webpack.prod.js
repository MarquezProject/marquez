const merge = require('webpack-merge')
const webpack = require('webpack')
const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin
const webpackShared = require('./webpack.common.js')
const CleanWebpackPlugin = require('clean-webpack-plugin')
const HtmlWebpackPlugin = require('html-webpack-plugin')
const UglifyJsPlugin = require('uglifyjs-webpack-plugin')

const webpackProd = {
  mode: 'production',
  devtool: 'source-map',
  plugins: [
    new CleanWebpackPlugin(),
    new HtmlWebpackPlugin({
      title: 'Telescope Web',
      hash: true,
      minify: true,
      inject: true,
      template: 'src/index.prod.html'
    }),
    new webpack.DefinePlugin({
      __DEVELOPMENT__: JSON.stringify(false),
      __NODE_ENV__: JSON.stringify('production'),
      __API_URL__: JSON.stringify('/api/v1'),
      __TEMP_ACTOR_STR__: JSON.stringify('me'),
      __ROLLBAR__: JSON.stringify(true)
    })
    //new BundleAnalyzerPlugin(),
  ]
}

module.exports = merge.smart(webpackShared, webpackProd)
