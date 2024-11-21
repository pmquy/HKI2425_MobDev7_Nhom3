const mongoose = require('mongoose');
const { v2 } = require('cloudinary')

const File = new mongoose.Schema({
  name: String,
  type: String,
  url: String,
  blurUrl: String,
  status: {
    type: String,
    enum: ['processing', 'safe', 'unsafe'],
    default: 'processing'
  },
  description: String,
  _system: {
    need_to_delete: Boolean,
    cloudinary_public_id: String,
    resource_type: String,
  }
}, { timestamps: true, versionKey: false })

File.statics = {
  async findByIdAndDelete (id) {
    const file = await this.findById(id)
    if (file._system.need_to_delete) {
      v2.uploader.destroy(file._system.cloudinary_public_id).catch(console.error)
      return file.deleteOne().catch(console.error)
    }
  },
}

module.exports = mongoose.model('File', File);