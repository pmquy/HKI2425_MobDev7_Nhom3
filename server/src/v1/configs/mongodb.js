const mongoose = require('mongoose')

class MongoDB {

  async config() {
    try {
      await mongoose.connect(process.env.MONGODB_URI)
      console.log('Connected to MongoDB')
    } catch (err) {
      console.log(err)
    }
  }

}

module.exports = new MongoDB()