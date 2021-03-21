const { CheckerPlugin } = require('awesome-typescript-loader');
const { join } = require('path');

module.exports = {
  mode: 'development',
  devtool: 'inline-source-map',
  entry: {
    content: join(__dirname, 'src/content.ts'),
    analysis: join(__dirname, 'src/background.ts'),
    version: join(__dirname, 'src/controller.ts'),
    lib: join(__dirname, 'src/wefinex/wefinex.main.ts'),
  },
  output: {
    path: join(__dirname, '../prod/src'),
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
  plugins: [new CheckerPlugin()],
  resolve: {
    extensions: ['.ts', '.js']
  }
};
