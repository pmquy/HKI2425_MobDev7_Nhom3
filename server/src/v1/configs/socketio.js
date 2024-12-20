const { Server } = require('socket.io')
const JWT = require('jsonwebtoken')
const Redis = require('./redis')
const ChatGroup = require('../models/chatgroup')
const cookieParser = require('cookie-parser')

class SocketIO {
  config(server) {
    const io = new Server(server, {
      cors: {
        origin: process.env.CLIENT.split(','),
      }
    })

    io.on('connection', socket => {
      
      socket.on('join', (topic, id) => {
        try {
          console.log('join', topic, id)
          // const cookie = cookieParser.JSONCookies(cookieParser.JSONCookie(socket.handshake.headers.cookie))
          // console.log(cookie)
          // const { _id } = JWT.verify(cookie?.accessToken, process.env.JWT_SECRET)
          // console.log('join', topic, id, _id)
          switch (topic) {
            case 'chatgroup':
              // if (ChatGroup.isMember(id, _id)) 
                socket.join(topic + '-' + id)
              break
            case 'call':
              // if (ChatGroup.isMember(id, _id))
                 socket.join(topic + '-' + id)
              break
          }
        } catch (error) {
          console.log(error)
        }
      })

      socket.on('call', (id, data) => {
        console.log('call', id, data)
        if (socket.rooms.has('call-' + id))
          socket.to(data.to ? data.to : 'call-' + id).emit('call', data)
      })

      socket.on("leave", (topic, id) => {
          socket.leave(topic + '-' + id)
      })

      socket.on('disconnect', async () => {
        try {
          const cookie = cookieParser.JSONCookies(cookieParser.JSONCookie(socket.handshake.headers.cookie))
          const { _id } = JWT.verify(cookie?.accessToken, process.env.JWT_SECRET)
          Redis.client.json.set(`user:${_id}`, '$._system.online', false, "XX")
        } catch (error) {
          console.log(error)
        }
      })

    })

    console.log('Socket.io is running')
    this.io = io
  }
}

module.exports = new SocketIO()