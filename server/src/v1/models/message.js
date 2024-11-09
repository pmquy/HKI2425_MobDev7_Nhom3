const mongoose = require('mongoose')
const File = require('./file')

const Message = new mongoose.Schema({
  chatgroup: String,
  user: String,
  message: String,
  files: [String],
}, { timestamps: true, versionKey: false })

Message.post('deleteOne', { document: true, query: false }, async function (doc) {
  Promise.all(doc.files.map(e => File.findByIdAndDelete(e)))
})

module.exports = mongoose.model('Message', Message)