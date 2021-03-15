const { CheckerPlugin } = require('awesome-typescript-loader');
const { join } = require('path');

module.exports = {
  mode: 'development',
  devtool: 'inline-source-map',
  entry: {
    content: join(__dirname, 'src/content.ts'),
    background: join(__dirname, 'src/background.ts'),
    controller: join(__dirname, 'src/controller.ts'),
    cookies: join(__dirname, 'src/controllers/cookies.controller.ts'),
    loadscript:  join(__dirname, 'src/controllers/load-script.controller.ts'),
    commonfirebase:  join(__dirname, 'src/controllers/common.firebase.ts'),
    environment:  join(__dirname, 'src/environment.ts'),
    wefinexMain: join(__dirname, 'src/wefinex/wefinex.main.ts'),
    wefinexController: join(__dirname, 'src/wefinex/wefinex.controller.ts')
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
