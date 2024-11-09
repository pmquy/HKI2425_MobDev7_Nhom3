const amqplib = require('amqplib');

class RabbitMQ {

  async config() {
    const connection = await amqplib.connect(process.env.RABBITMQ_URI);
    const channel = await connection.createChannel();

    connection.on('close', () => {
      console.error('RabbitMQ connection closed, reconnecting...');
      setTimeout(() => this.config(), 1000)
    })

    connection.on('error', (err) => {
      console.error('RabbitMQ connection error:', err);
      setTimeout(() => this.config(), 1000)
    })

    console.log('RabbitMQ connected');
    this.channel = channel;
  }
}

module.exports = new RabbitMQ();