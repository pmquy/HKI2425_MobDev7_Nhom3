
class Controller {
  constructor() {
    this.model = require('../models/file')
    this.getById = this.getById.bind(this)
  }

  async getById(req, res, next) {
    try {
      const { id } = req.params
      const file = await this.model.findById(id).select('-_system')
      if(!file) throw new Error('File not found')
      res.json(file)
    } catch (error) {
      next(error)
    }
  }
}


module.exports = new Controller();