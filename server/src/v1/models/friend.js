const mongoose = require('mongoose')

const Friend = new mongoose.Schema({
  from: String,
  to: String,
  status: {
    type: String,
    enum: ['pending', 'accepted'],
    default: 'pending'
  },
}, { timestamps: true, versionKey: false })

Friend.index({ from: 1, to: 1 }, { unique: true })

module.exports = mongoose.model('Friend', Friend)