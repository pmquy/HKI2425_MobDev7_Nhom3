const JOI = require('joi')
const ChatGroup = require('../models/chatgroup')
const SocketIO = require('../configs/socketio')
const Firebase = require('../configs/firebase')
const RabbitMQ = require('../configs/rabbitmq')
const Joi = require('joi')
const File = require('../models/file')


class Controller {
  constructor() {
    this.model = require('../models/message')
    this.getAll = this.getAll.bind(this)
    this.create = this.create.bind(this)
    this.updateById = this.updateById.bind(this)
    this.deleteById = this.deleteById.bind(this)
    this.getById = this.getById.bind(this)
    this.#initQueue()
  }


  #createSchema = JOI.object({
    message: JOI.string().default('').when('files', { is: Joi.array().min(1), otherwise: Joi.required() }),
    chatgroup: JOI.string().required(),
    files: JOI.array().items(JOI.string()).default([]),
  }).unknown(false).required()

  #MESSAGE_NOTIFICATION = 'MESSAGE_NOTIFICATION'

  #initQueue = async () => {
    await RabbitMQ.channel.assertQueue(this.#MESSAGE_NOTIFICATION, { durable: true })
    await RabbitMQ.channel.consume(this.#MESSAGE_NOTIFICATION, async (msg) => {
      try {
        const { message, sender, users } = JSON.parse(msg.content.toString())
        const file = message.files[0] && await File.findById(message.files[0])
        if (file?.type === 'audio' && !file.description) throw new Error("Processing the file")
        Firebase.sendEachForMulticast({
          title: sender.firstName + ' ' + sender.lastName,
          body: file?.type === 'image' ? 'Đã gửi một hình ảnh' : file?.type === 'audio' ? file.description : file?.type === 'video' ? 'Đã gửi một video' : file ? 'Đã gửi một tệp tin' : message.message,
          imageUrl: file?.type === 'image' ? file.url : undefined
        }, users.map(u => u.user)).catch(e => console.error(e))
        RabbitMQ.channel.ack(msg)
      } catch (error) {
        console.error(error)
        setTimeout(() => RabbitMQ.channel.nack(msg), 2000)
      }
    })
  }

  async create(req, res, next) {
    try {
      const value = await this.#createSchema.validateAsync(req.body)
      const chatgroup = await ChatGroup.findById(value.chatgroup)
      if (!chatgroup) throw new Error('Chatgroup not found')
      if (!chatgroup.hasMember(req.user._id)) throw new Error('You are not a member of this group')
      const result = await this.model.create({ ...value, user: req.user._id })
      res.json(result)
      SocketIO.io.to(`chatgroup-${result.chatgroup}`).emit('new_message', result)
      RabbitMQ.channel.sendToQueue(this.#MESSAGE_NOTIFICATION, Buffer.from(JSON.stringify({ message: result, sender: req.user, users: chatgroup.users })))
    } catch (error) {
      next(error)
    }
  }

  #updateSchema = JOI.object({
    message: JOI.string(),
    files: JOI.array().items(JOI.string())
  }).unknown(false).required()

  async updateById(req, res, next) {
    try {
      const value = await this.#updateSchema.validateAsync(req.body)
      const message = await this.model.findById(req.params.id)
      if (!message) throw new Error('Message not found')
      if (message.user != req.user._id) throw new Error('You are not the owner of this message')
      message.files.forEach(e => File.findByIdAndDelete(e))
      message.set(value)
      await message.save()
      res.json(message)
    } catch (error) {
      next(error)
    }
  }

  async deleteById(req, res, next) {
    try {
      const message = await this.model.findById(req.params.id)
      if (!message) throw new Error('Message not found')
      if (message.user != req.user._id) throw new Error('You are not the owner of this message')
      await message.deleteOne()
      res.json({ message: 'Message deleted' })
    } catch (error) {
      next(error)
    }
  }

  #getAllSchema = JOI.object({
    offset: JOI.number().default(0),
    limit: JOI.number().default(10),
    q: JOI.string().default('{}')
  }).unknown(false).required()

  async getAll(req, res, next) {
    try {
      const { offset, limit, q } = await this.#getAllSchema.validateAsync(req.query)
      const query = { ...JSON.parse(q), user: req.user._id }
      const count = await this.model.countDocuments(query)
      const messages = await this.model.find(query).skip(offset).limit(limit)
      res.json({
        data: messages,
        hasMore: count > offset + limit
      })
    } catch (error) {
      next(error)
    }
  }

  async getById(req, res, next) {
    try {
      const message = await this.model.findById(req.params.id)
      if (!message) throw new Error('Message not found')
      res.json(message)
    } catch (error) {
      next(error)
    }
  }
}

module.exports = new Controller()