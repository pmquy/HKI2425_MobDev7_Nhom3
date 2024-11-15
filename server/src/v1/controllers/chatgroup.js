const JOI = require('joi')
const Message = require('../models/message')
const Joi = require('joi')

class Controller {

  constructor() {
    this.model = require('../models/chatgroup')
    this.create = this.create.bind(this)
    this.addMember = this.addMember.bind(this)
    this.removeMember = this.removeMember.bind(this)
    this.updateMember = this.updateMember.bind(this)
    this.getMember = this.getMember.bind(this)
    this.update = this.update.bind(this)
    this.getAll = this.getAll.bind(this)
    this.getMessage = this.getMessage.bind(this)
    this.getById = this.getById.bind(this)
    this.deleteById = this.deleteById.bind(this)
  }


  async getById(req, res, next) {
    try {
      const group = await this.model.findById(req.params.id)
      if (!group) throw new Error('Group not found')
      if (!group.hasMember(req.user._id)) throw new Error('Unauthorized')
      res.json(group)
    } catch (error) {
      next(error)
    }
  }


  #createSchema = JOI.object({
    name: JOI.string().required(),
    users: JOI.array().items(JOI.object({
      user: JOI.string().required(),
      role: JOI.string().valid('member', 'admin').default('member')
    })).required().min(1),
    avatar: JOI.string().default("6724e8b3290173e98590e225")
  }).unknown(false).required()


  async create(req, res, next) {
    try {
      const value = await this.#createSchema.validateAsync(req.body)
      value.users.push({ user: req.user._id, role: 'admin' })
      const group = await this.model.create(value)
      res.json(group)
    } catch (error) {
      next(error)
    }
  }

  #updateSchema = JOI.object({
    name: JOI.string(),
    avatar: JOI.string()
  }).unknown(false).required()

  async update(req, res, next) {
    try {
      const value = await this.#updateSchema.validateAsync(req.body)
      const group = await this.model.findById(req.params.id)
      if (!group) throw new Error('Group not found')
      if (!group.hasMember(req.user._id)) throw new Error('Unauthorized')
      group.set(value)
      await group.save()
      return res.json(group)
    } catch (error) {
      next(error)
    }
  }

  #getAllSchema = JOI.object({
    page: JOI.number().default(0),
    limit: JOI.number().default(10),
    q: JOI.string().default('{}')
  }).unknown(false).required()

  async getAll(req, res, next) {
    try {
      const { page, limit, q } = await this.#getAllSchema.validateAsync(req.query)
      const query = { ...JSON.parse(q), users: { $elemMatch: { user: req.user._id } } }
      const count = await this.model.countDocuments(query)
      const groups = await this.model.find(query).skip(page * limit).limit(limit).select('-users')
      res.json({
        data: groups,
        hasMore: count > (page + 1) * limit
      })
    } catch (error) {
      next(error)
    }
  }

  #addMemberSchema = JOI.array().items(
    JOI.object({
      user: JOI.string().required(),
      role: JOI.string().valid('member', 'admin').default('member')
    }).unknown(false)
  ).min(1).required()

  async addMember(req, res, next) {
    try {
      const value = await this.#addMemberSchema.validateAsync(req.body)
      const group = await this.model.findById(req.params.id)
      if (!group) throw new Error('Group not found')
      if (!group.hasAdmin(req.user._id)) throw new Error('Unauthorized')
      group.users.push(...value)
      await group.save()
      res.json(group)
    } catch (error) {
      next(error)
    }
  }

  #removeMemberSchema = JOI.array().items(Joi.string()).required()

  async removeMember(req, res, next) {
    try {
      const value = await this.#removeMemberSchema.validateAsync(req.body)
      const group = await this.model.findById(req.params.id)
      if (!group) throw new Error('Group not found')
      if (!group.hasAdmin(req.user._id)) throw new Error('Unauthorized')
      group.users = group.users.filter(e => value.includes(e.user))
      await group.save()
      res.json(group.users)
    } catch (error) {
      next(error)
    }
  }

  #updateMemberSchema = JOI.array().items(
    JOI.object({
      user: JOI.string().required(),
      role: JOI.string().valid('member', 'admin').required()
    }).unknown(false)
  ).min(1).required()

  async updateMember(req, res, next) {
    try {
      const value = await this.#updateMemberSchema.validateAsync(req.body)
      const group = await this.model.findById(req.params.id)
      if (!group) throw new Error('Group not found')
      if (!group.hasAdmin(req.user._id)) throw new Error('Unauthorized')
      value.forEach(e => {
        const member = group.users.find(v => v.user === e.user)
        if (!member) throw new Error('Member not found')
        member.role = value.role
      })
      await group.save()
      res.json(group.users)
    } catch (error) {
      next(error)
    }
  }

  async getMember(req, res, next) {
    try {
      const group = await this.model.findById(req.params.id)
      if (!group) throw new Error('Group not found')
      if (!group.hasMember(req.user._id)) throw new Error('Unauthorized')
      res.json(group.users)
    } catch (error) {
      next(error)
    }
  }

  #getMessagesSchema = JOI.object({
    page: JOI.number().default(0),
    limit: JOI.number().default(10),
    q: JOI.string().default('{}')
  }).unknown(false).required()

  async getMessage(req, res, next) {
    try {
      const { page, limit, q } = await this.#getMessagesSchema.validateAsync(req.query)
      if (!await this.model.isMember(req.params.id, req.user._id)) throw new Error('You are not a member of this group')
      const query = { ...JSON.parse(q), chatgroup: req.params.id }
      const count = await Message.countDocuments(query)
      const messages = (await Message.find(query).skip(page * limit).limit(limit).sort({ createdAt: -1 })).reverse()
      res.json({
        data: messages,
        hasMore: count > (page + 1) * limit
      })
    } catch (error) {
      next(error)
    }
  }

  async deleteById(req, res, next) {
    try {
      const group = await this.model.findById(req.params.id)
      if (!group) throw new Error('Group not found')
      if (!group.hasAdmin(req.user._id)) throw new Error('Unauthorized')
      await group.deleteOne()
      res.json({ message: 'Group deleted successfully' })
    } catch (error) {
      next(error)
    }
  }
}


module.exports = new Controller()