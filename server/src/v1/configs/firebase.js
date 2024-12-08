const admin = require('firebase-admin');
const serviceAccount = require('../../../serviceAccountKey.json');
const Redis = require('./redis');

class Firebase {

  #messaging

  async config() {
    admin.initializeApp({
      credential: admin.credential.cert(serviceAccount),
    })
    this.#messaging = admin.messaging()
  }

  async #getTokens(ids) {
    return Promise.all(ids.map(id => Redis.client.json.get(`user:${id}`).then(u => u?._system?.token))).then(tokens => tokens.filter(t => t))
  }

  async sendEachForMulticast(ids, data) {
    const tokens = await this.#getTokens(ids)
    await this.#messaging.sendEachForMulticast({
      tokens,
      data
    })
  }
  
}

module.exports = new Firebase();