const redis = require('redis')

class Redis {
  config() {
    this.client = redis.createClient({url : process.env.REDIS_URI})
    this.client.on('error', e => {
      console.log(e)
    })
    this.client.connect()
      .then(() => console.log('Connected to Redis'))
      .catch(err => console.log(err.message))
  }
}

module.exports = new Redis()