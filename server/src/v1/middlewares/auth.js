const jwt = require('jsonwebtoken')
const User = require('../models/user')


const auth = (options = { required: false }) => {

  return async (req, res, next) => {
    try {
      const {accessToken} = req.cookies
      if (!accessToken && options.required) throw new Error("You must be logged in")
      const { _id } = jwt.verify(accessToken, process.env.JWT_SECRET)
      const user = await User.findById(_id)
      if (!user && options.required) throw new Error("You must be logged in")
      req.user = user
      next()
    } catch (error) {
      next(error)
    }
  }
}

module.exports = auth