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
      // All files with a '.ts' or '.tsx' extension will be handled by 'ts-loader'.
      {
        test: /\.tsx?$/,
        loader: "ts-loader"
      },
      {
        test: /\.ico$/,
        loader: 'file-loader'
      },
      {
        test: /\.svg$/,
        use: ['@svgr/webpack'], // Use the @svgr/webpack loader
        issuer: {
          and: [/\.(js|ts|jsx|tsx)$/], // Only apply this rule to JS/TS files
        },
        type: 'javascript/auto', // Prevent Webpack's asset module from interfering
      },
      // Fallback rule for SVGs imported elsewhere (e.g., in CSS)
      {
        test: /\.svg$/,
        type: 'asset/resource',
        issuer: {
          and: [/\.(css|sass|scss|less)$/], // Only apply this rule to style files
        },
      },
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
