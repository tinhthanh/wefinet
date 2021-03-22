const { CheckerPlugin } = require('awesome-typescript-loader');
const { join } = require('path');
const { optimize } = require('webpack');
module.exports = {
  mode: 'production',
  entry: {
    content: join(__dirname, 'src/content.ts'),
    analysis: join(__dirname, 'src/background.ts'),
    version: join(__dirname, 'src/controller.ts'),
    lib: join(__dirname, 'src/wefinex/wefinex.main.ts')
  },
  output: {
    path: join(__dirname, '../chrome/prod/src'),
    filename: '[name].js'
  },
  module: {
    rules: [
      {
        exclude: /node_modules/,
        test: /\.ts?$/,
        use: 'awesome-typescript-loader?{configFileName: "chrome/tsconfig.json"}'
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
