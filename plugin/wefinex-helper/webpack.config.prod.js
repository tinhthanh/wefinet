const { CheckerPlugin } = require('awesome-typescript-loader');
const { join } = require('path');
const { optimize } = require('webpack');
module.exports = {
  mode: 'production',
  entry: {
    background: join(__dirname, 'src/background.ts'),
    wefinexMain: join(__dirname, 'src/wefinex/wefinex.main.ts'),
  },
  output: {
    path: join(__dirname, '../wefinex-helper/prod/src'),
    filename: '[name].js'
  },
  module: {
    rules: [
      {
        exclude: /node_modules/,
        test: /\.ts?$/,
        use: 'awesome-typescript-loader?{configFileName: "wefinex-helper/tsconfig.json"}'
      }
    ]
  },
  plugins: [
    new CheckerPlugin(),
    new optimize.AggressiveMergingPlugin(),
    new optimize.OccurrenceOrderPlugin()
  ],
  resolve: {
    extensions: ['.ts', '.js']
  }
};
