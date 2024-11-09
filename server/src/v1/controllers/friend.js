const JOI = require('joi');


class Controller {

  constructor() {
    this.model = require('../models/friend')
    this.request = this.request.bind(this)
    this.accept = this.accept.bind(this)
    this.revoke = this.revoke.bind(this)
    this.decline = this.decline.bind(this)
    this.getAll = this.getAll.bind(this)
    this.disfriend = this.disfriend.bind(this)
  }


  #requestSchema = JOI.object({
    to: JOI.string().required(),
  }).unknown(false).required()

  #acceptSchema = JOI.object({
    from: JOI.string().required(),
  }).unknown(false).required()

  #getAllSchema = JOI.object({
    q: JOI.string().default('{}'),
    page: JOI.number().default(0),
    limit: JOI.number().default(10)
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
      await this.model.findOneAndDelete({ from: value.from, to: req.user._id })
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


  async getAll(req, res, next) {
    try {
      const { page, limit, q } = await this.#getAllSchema.validateAsync(req.query)
      const query = { ...JSON.parse(q), $or: [{ from: req.user._id }, { to: req.user._id }] }
      const count = await this.model.countDocuments(query)
      const friends = await this.model.find(query).skip(page * limit).limit(limit)
      res.json({
        data: friends,
        hasMore: count > (page + 1) * limit
      })
    } catch (error) {
      next(error)
    }
  }
}


module.exports = new Controller();