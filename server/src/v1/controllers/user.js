const JOI = require('joi')
const BCRYPT = require('bcrypt')
const JWT = require('jsonwebtoken')
const Redis = require('../configs/redis')
const Mailer = require('../configs/mailer')
const File = require('../models/file')
const Friend = require('../models/friend')

const TOKEN_SECRET = process.env.JWT_SECRET
const TOKEN_EXPIRY = '30d'
const COOKIE_OPTIONS = { httpOnly: true, sameSite: 'none', secure: true, maxAge: 1000 * 60 * 60 * 24 * 30 }

class Controller {
  constructor() {
    this.model = require('../models/user')
    this.create = this.create.bind(this)
    this.login = this.login.bind(this)
    this.update = this.update.bind(this)
    this.delete = this.delete.bind(this)
    this.auth = this.auth.bind(this)
    this.logout = this.logout.bind(this)
    this.getById = this.getById.bind(this)
    this.getAll = this.getAll.bind(this)
    this.otp = this.otp.bind(this)
  }

  #createSchema = JOI.object({
    firstName: JOI.string().required(),
    lastName: JOI.string().required(),
    phoneNumber: JOI.string().required(),
    email: JOI.string().email().required(),
    avatar: JOI.string().default("67400340428abd4234ad1160"),
    password: JOI.string().min(5).max(12).required()
  }).unknown(false).required().custom((value, helpers) =>
    BCRYPT.hash(value.password, 10)
      .then(hashed => {
        value.password = hashed
        return value
      })
  )

  #generateToken(data) {
    return JWT.sign(data, TOKEN_SECRET, { expiresIn: TOKEN_EXPIRY })
  }

  async create(req, res, next) {
    try {
      const value = await this.#createSchema.validateAsync(req.body)
      if (await this.model.findOne({ email: value.email })) throw new Error('Email already exists')
      const otp = Math.round(Math.random() * 100000)
      res.json({ message: 'OTP sent to your email' })
      Redis.client.set(`register-${value.email}`, JSON.stringify({ otp, user: value }), { EX: 60 * 5 },)
      Mailer.sendMail({
        to: value.email,
        subject: 'OTP for registration',
        text: `Your OTP is ${otp}. The OTP will expire in 5 minutes`
      })
    } catch (error) {
      req.body.avatar && File.findByIdAndDelete(req.body.avatar).then(() => { }).catch(console.error)
      next(error)
    }
  }

  #otpSchema = JOI.object({
    email: JOI.string().email().required(),
    otp: JOI.string().required()
  }).unknown(false).required()

  async otp(req, res, next) {
    try {
      const value = await this.#otpSchema.validateAsync(req.body)
      const data = await Redis.client.get(`register-${value.email}`)
      if (!data) throw new Error('OTP expired')
      const { otp, user } = JSON.parse(data)
      if (otp != value.otp) throw new Error('Invalid OTP')
      await this.model.create(user)
      res.json({ message: 'User created' })
    } catch (error) {
      next(error)
    }
  }

  #loginSchema = JOI.object({
    email: JOI.string().email().required(),
    password: JOI.string().required()
  }).unknown(false).required()

  async login(req, res, next) {
    try {
      const value = await this.#loginSchema.validateAsync(req.body)
      const user = await this.model.findOne({ email: value.email }).select('-_system')
      if (!user) throw new Error('Invalid email or password')
      const match = await BCRYPT.compare(value.password, user.password)
      if (!match) throw new Error('Invalid email or password')
      res.cookie('accessToken', this.#generateToken({ _id: user._id }), COOKIE_OPTIONS)
      res.json(user)
      Redis.client.json.set(`user:${user._id}`, '$', user, "NX")
        .then(() => Redis.client.json.set(`user:${user._id}`, '$._system', {
          online: true,
          token: req.query.token,
          socketId: req.query.socketId
        }))
    } catch (error) {
      next(error)
    }
  }

  #updateSchema = JOI.object({
    firstName: JOI.string(),
    lastName: JOI.string(),
    password: JOI.string().min(5).max(12),
    avatar: JOI.string(),
    phoneNumber: JOI.string()
  }).unknown(false).required().custom((value, helpers) => {
    if (value.password)
      return BCRYPT.hash(value.password, 10)
        .then(hashed => {
          value.password = hashed
          return value
        })
    else return value
  }
  )

  async update(req, res, next) {
    try {
      if (!req.user) throw new Error('You must be logged in')
      const value = await this.#updateSchema.validateAsync(req.body)
      const user = await this.model.findByIdAndUpdate(req.user._id, value, { new: true })
      res.json(user)
    } catch (error) {
      next(error)
    }
  }

  async delete(req, res, next) {
    try {
      if (!req.user) throw new Error('You must be logged in')
      await this.model.findByIdAndDelete(req.user._id)
      res.json({ message: 'User deleted' })
    } catch (error) {
      next(error)
    }
  }

  async auth(req, res, next) {
    try {
      if (!req.user) throw new Error('You must be logged in')
      res.json(req.user)
      Redis.client.json.set(`user:${req.user._id}`, '$', req.user, "NX")
        .then(() => Redis.client.json.set(`user:${req.user._id}`, '$._system', {
          online: true,
          token: req.query.token,
          socketId: req.query.socketId
        }))
        .catch(e => console.error(e))
    } catch (error) {
      next(error)
    }
  }

  async logout(req, res, next) {
    try {
      res.clearCookie('accessToken', COOKIE_OPTIONS)
      res.json({ message: 'You have been logged out' })
    } catch (error) {
      next(error)
    }
  }

  async getById(req, res, next) {
    try {
      const user = await this.model.findById(req.params.id).select('-password -_system')
      if (!user) throw new Error('User not found')
      let friendStatus = 'suggest'
      const from = await Friend.findOne({ from: req.user._id, to: req.params.id })
      const to = await Friend.findOne({ from: req.params.id, to: req.user._id })
      if (from?.status === 'accepted' || to?.status === 'accepted') friendStatus = 'friend'
      else if (from?.status === 'pending') friendStatus = 'send'
      else if (to?.status === 'pending') friendStatus = 'request'
      res.json({
        ...user.toObject(),
        friendStatus
      })
    } catch (error) {
      next(error)
    }
  }

  #getAllSchema = JOI.object({
    limit: JOI.number().default(10),
    offset: JOI.number().default(0),
    q: JOI.string().default('{}')
  }).unknown(false).required()

  async getAll(req, res, next) {
    try {
      const { limit, offset, q } = await this.#getAllSchema.validateAsync(req.query)
      const query = JSON.parse(q)
      if (query.name) {
        query.$text = { $search: query.name }
        delete query.name
      }
      const count = await this.model.countDocuments(query)
      const users = await this.model.find(query).select('-password -_system').limit(limit).skip(offset)
      res.json({
        data: users,
        hasMore: count > offset + limit
      })
    } catch (error) {
      next(error)
    }
  }
}


module.exports = new Controller()