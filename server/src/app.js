const express = require('express')
const app = express()
const server = require('http').createServer(app)

app.use(express.json())
app.use(express.urlencoded({ extended: true }))
app.use(require('cookie-parser')())
app.use(require('morgan')('dev'))
app.use(require('cors')({
  credentials: true,
  origin: process.env.CLIENT.split(',')
}))

Promise.all([
  require('./v1/configs/firebase').config(),
  require('./v1/configs/redis').config(),
  require('./v1/configs/mongodb').config(),
  require('./v1/configs/socketio').config(server),
  require('./v1/configs/cloudinary').config(),
  require('./v1/configs/rabbitmq').config(),
  require('./v1/configs/mailer').config()
])
  .then(() => app.use('/api/v1', require('./v1/routers')))

module.exports = server