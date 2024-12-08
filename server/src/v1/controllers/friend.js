const JOI = require('joi');
const User = require('../models/user')

class Controller {

  constructor() {
    this.model = require('../models/friend')
    this.request = this.request.bind(this)
    this.accept = this.accept.bind(this)
    this.revoke = this.revoke.bind(this)
    this.decline = this.decline.bind(this)
    this.getAll = this.getAll.bind(this)
    this.disfriend = this.disfriend.bind(this)
    this.getSuggestions = this.getSuggestions.bind(this)
  }

  #requestSchema = JOI.object({
    to: JOI.string().required(),
  }).unknown(false).required()

  
  async request(req, res, next) {
    try {
      const value = await this.#requestSchema.validateAsync(req.body)
      await this.model.create({ from: req.user._id, to: value.to, status: 'pending' })
      res.json({ message: 'Friend request sent' })
    } catch (error) {
      next(error)
    }
  }

  #acceptSchema = JOI.object({
    from: JOI.string().required(),
  }).unknown(false).required()
  
  async accept(req, res, next) {
    try {
      const value = await this.#acceptSchema.validateAsync(req.body)
      await this.model.findOneAndUpdate({ from: value.from, to: req.user._id }, { status: 'accepted' })
      res.json({ message: 'Friend request accepted' })
    } catch (error) {
      next(error)
    }
  }
  
  async revoke(req, res, next) {
    try {
      const value = await this.#requestSchema.validateAsync(req.body)
      await this.model.findOneAndDelete({ to: value.to, from: req.user._id })
      res.json({ message: 'Friend request revoked' })
    } catch (error) {
      next(error)
    }
  }
  
  async decline(req, res, next) {
    try {
      const value = await this.#acceptSchema.validateAsync(req.body)
      await this.model.findOneAndDelete({ from: value.from, to: req.user._id })
      res.json({ message: 'Friend request declined' })
    } catch (error) {
      next(error)
    }
  }
  
  async disfriend(req, res, next) {
    try {
      const value = await this.#acceptSchema.validateAsync(req.body)
      await this.model.findOneAndDelete({ from: value.from, to: req.user._id })
      await this.model.findOneAndDelete({ from: req.user._id, to: value.from })
      res.json({ message: 'Friend removed' })
    }
    catch (error) {
      next(error)
    }
  }
  
  #getAllSchema = JOI.object({
    q: JOI.string().default('{}'),
    offset: JOI.number().default(0),
    limit: JOI.number().default(10)
  }).unknown(false).required()

  async getAll(req, res, next) {
    try {
      const { offset, limit, q } = await this.#getAllSchema.validateAsync(req.query)
      const query = { ...JSON.parse(q), $or: [{ from: req.user._id }, { to: req.user._id }] }
      const count = await this.model.countDocuments(query)
      const friends = await this.model.find(query).skip(offset).limit(limit)
      res.json({
        data: friends,
        hasMore: count > offset + limit
      })
    } catch (error) {
      next(error)
    }
  }

  #getSuggestionsSchema = JOI.object({
    q: JOI.string().default('{}'),
    offset: JOI.number().default(0),
    limit: JOI.number().default(10)
  }).unknown(false).required()

  async getSuggestions(req, res, next) {
    try {
      const { offset, limit, q } = await this.#getSuggestionsSchema.validateAsync(req.query)
      const friends = await this.model.find({ $or: [{ from: req.user._id }, { to: req.user._id }] })
      const ids = friends.map(f => f.from == req.user._id ? f.to : f.from)
      ids.push(req.user._id)
      const query = { ...JSON.parse(q), _id: { $nin: ids } }
      const count = await this.model.countDocuments(query)
      const users = await User.find(query).skip(offset).limit(limit).select('_id')
      res.json({
        data: users.map(u => u._id),
        hasMore: count > offset + limit
      })
    } catch (error) {
      next(error)
    }
  }
}


module.exports = new Controller();