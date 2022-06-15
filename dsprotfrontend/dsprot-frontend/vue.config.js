const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    /*
    * Capsulate backend API endpoint URL behind a custom name.
    * If migrating to a different server for example,
    * the URLs do not need to be rewritten all over the code base but only here.
    */
    proxy: {
      '^/api': {
        target: 'http://localhost:8004',
        changeOrigin: true,
        logLevel: 'debug',
        pathRewrite: {'^/api': '/'}
      }
    }
  }
})
