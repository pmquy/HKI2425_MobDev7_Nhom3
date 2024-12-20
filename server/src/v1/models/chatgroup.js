const mongoose = require('mongoose');
const Message = require('./message')

const ChatGroup = new mongoose.Schema({
  name: String,
  users: [new mongoose.Schema({
    user: String,
    role: { type: String, default: 'member', enum: ['member', 'admin'] }
  }, { _id: false, timestamps: true })],
  avatar: String,
  _system: {
    lastMessageTimeStamp: { type: Date, default: Date.now, index: true }
  }
}, { timestamps: true, versionKey: false })


ChatGroup.methods = {
  hasMember(user) {
    return this.users.some(e => e.user == user)
  },

  hasAdmin(user) {
    return this.users.some(e => e.user == user && e.role === 'admin')
  },

  getMembers() {
    return this.users
  }
}

ChatGroup.statics = {
  async isMember(id, user) {
    const members = await this.getMembers(id)
    return members.some(e => e.user == user)
  },
  async isAdmin(id, user) {
    const members = await this.getMembers(id)
    return members.some(e => e.user == user && e.role === 'admin')
  },
  async getMembers(id) {
    const chatgroup = await this.findById(id)
    return chatgroup.users
  }
}

ChatGroup.post("deleteOne", { document: true, query: false }, async function (doc) {
  Message.find({ chatgroup: doc._id }).then(messages => Promise.all(messages.map(e => e.deleteOne()))).catch(console.error)
})


module.exports = mongoose.model('ChatGroup', ChatGroup)

