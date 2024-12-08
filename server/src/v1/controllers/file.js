const JOI = require('joi')

class Controller {
  constructor() {
    this.model = require('../models/file')
    this.getById = this.getById.bind(this)
    this.getResources = this.getResources.bind(this)
    // const array = []
    // for (let i = 0; i < 50; i++) {
    //   array.push({
    //     name: `Gif${i + 1}.gif`,
    //     type: 'image',
    //     url: `https://res.cloudinary.com/duv57fvep/image/upload/v1732025108/gif-${i + 1}.gif`,
    //     blurUrl: `https://res.cloudinary.com/duv57fvep/image/upload/v1732025108/gif-${i + 1}.gif`,
    //     status: 'safe',
    //     _system: {
    //       need_to_delete: false,
    //       cloudinary_public_id: `gif-${i + 1}`,
    //       resource_type: 'gif',
    //     }
    //   })
    // }
    // this.model.create(array).then(console.log).catch(console.error)
  }


  async getById(req, res, next) {
    try {
      const { id } = req.params
      const file = await this.model.findById(id).select('-_system')
      if (!file) throw new Error('File not found')
      res.json(file)
    } catch (error) {
      next(error)
    }
  }


  #getResourcesSchema = JOI.object({
    offset: JOI.number().default(0),
    limit: JOI.number().default(10),
    type: JOI.string().required()
  }).required()

  async getResources(req, res, next) {
    try {
      const { offset, limit, type } = await this.#getResourcesSchema.validateAsync(req.query)
      const count = await this.model.countDocuments({ "_system.resource_type": type })
      const files = await this.model.find({ "_system.resource_type": type }).select('-_system').skip(offset).limit(limit)
      res.json({
        hasMore: count > offset + limit,
        data: files,
      })
    } catch (error) {
      next(error)
    }
  }
}


module.exports = new Controller();