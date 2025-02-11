const postCssModulesValues = require("postcss-modules-values")
const path = require('path')
const autoprefixer = require('autoprefixer')

module.exports = {
  entry: './src/index.tsx',
  module: {
    rules: [{
        test: /\.css$/,
      use: [{
            loader: 'style-loader',
          },
          {
            loader: 'css-loader',
            options: {
              importLoaders: 1,
              modules : {
                localIdentName: '[name]__[local]__[hash:base64:5]',
              },
            }
        }]
      },
      {
        test: /\.tsx?$/,
        use: [
          {
            loader: 'babel-loader',
            options: {
              presets: [
                '@babel/preset-env',
                '@babel/preset-react',
                '@babel/preset-typescript'
              ]
            }
          },
          'ts-loader'
        ],
        exclude: /node_modules/
      },
      {
        test: /\.(png|jpe?g|gif|svg)(\?v=\d+\.\d+\.\d+)?$/,
        loader: 'file-loader'
      },
      {
        test: /\.(woff(2)?|ttf|eot|otf)(\?v=\d+\.\d+\.\d+)?$/,
        use: [{
          loader: 'file-loader',
          options: {
            name: '[name].[ext]',
            outputPath: 'fonts/'
          }
        }]
      },
      {
        test: /\.ico$/,
        loader: 'file-loader'
      }
    ]
  },
  resolve: {
    extensions: ['.tsx', '.ts', '.js', '.json'],
    symlinks: false
  },
  output: {
    filename: 'bundle.js',
    path: path.resolve(__dirname, 'dist'),
    publicPath: '/'
  }
};
