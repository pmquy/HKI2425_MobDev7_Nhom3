const nodemailer = require('nodemailer')

class Mailer {
  #transporter

  async config() {
    this.#transporter = nodemailer.createTransport({
      service: "gmail",
      auth: {
        user: process.env.EMAIL_USER,
        pass: process.env.EMAIL_PASS
      },
      from: process.env.EMAIL_USER
    })
  }

  async sendMail(data) {
    await this.#transporter.sendMail(data)
  }
}


module.exports = new Mailer()