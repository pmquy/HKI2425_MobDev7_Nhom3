const nodemailer = require('nodemailer')

class Mailer {
  #transporter

  async config() {
    this.#transporter = nodemailer.createTransport({
      service: "gmail",
      auth: {
        user: "lokikurri@gmail.com",
        pass: "kzss layx dttj idoo"
      },
      from: "lokikurri@gmail.com"
    })
  }

  async sendMail(data) {
    await this.#transporter.sendMail(data)
  }
}


module.exports = new Mailer()