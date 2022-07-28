const { defineConfig } = require('@vue/cli-service')

const DS_ENDPOINT_DEV = "http://localhost:8004"

// Configure DS-Protection service endpoint
// Production Server (nginx)
// DS-Prot.: https://incentives.cs.uni-paderborn.de/dsprotection/
// Frontend: https://incentives.cs.uni-paderborn.de
// DevServer
// DS-Prot.: localhost:8004
// Frontend: localhost:XXXX

module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    /*
    * Capsulate backend API endpoint URL behind a custom name.
    * If migrating to a different server for example,
    * the URLs do not need to be rewritten all over the code base but only here.
    */
    proxy: {
      '^/dsprotection': {
        target: DS_ENDPOINT_DEV,
        changeOrigin: true,
        logLevel: 'debug',
        pathRewrite: { '^/dsprotection': '' }
      }
    }
  }
})
