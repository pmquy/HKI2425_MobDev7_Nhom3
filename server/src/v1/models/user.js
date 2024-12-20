const mongoose = require('mongoose')
const File = require('./file')

const User = new mongoose.Schema({
  firstName: String,
  lastName: String,
  email: {
    type: String,
    unique: true,
  },
  phoneNumber: {
    type: String,
    unique: true,
  },
  avatar: String,
  password: String,
}, { timestamps: true, versionKey: false })

User.index({ firstName: "text", lastName: "text" })

User.post('findOneAndDelete', async function (doc) {
  File.findByIdAndDelete(doc.avatar).then(() => { })
})

module.exports = mongoose.model('User', User)