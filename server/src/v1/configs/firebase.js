const admin = require('firebase-admin');
const serviceAccount = require('../../serviceAccountKey.json');
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

  async sendEachForMulticast(notification, ids) {
    const tokens = await this.#getTokens(ids)
    await this.#messaging.sendEachForMulticast({
      notification,
      tokens
    })
  }

  // async sendToTopic(notification, topic) {
  //   topic = '/topics/' + topic
  //   console.log(topic)
  //   await this.#messaging.send({
  //     notification,
  //     topic
  //   })
  // }

  // async subscribeToTopic(ids, topic) {
  //   topic = '/topics/' + topic
  //   console.log(topic)
  //   const tokens = await this.#getTokens(ids)
  //   console.log(tokens)
  //   await this.#messaging.subscribeToTopic(tokens, topic)
  //   await Promise.all(ids.map(id => User.findByIdAndUpdate(id, { $addToSet: { "_system.topics": topic } })))
  // }

  // async unsubscribeFromTopic(ids, topic) {
  //   topic = '/topics/' + topic
  //   const tokens = await this.#getTokens(ids)
  //   await this.#messaging.unsubscribeFromTopic(tokens, topic)
  //   await Promise.all(ids.map(id => User.findByIdAndUpdate(id, { $pull: { "_system.topics": topic } })))
  // }
}

module.exports = new Firebase();