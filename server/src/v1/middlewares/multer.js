const multer = require('multer')
const File = require('../models/file')
const { v2 } = require('cloudinary')
const fs = require('fs')
const RabbitMQ = require('../configs/rabbitmq')
const SocketIO = require('../configs/socketio')
const FOLDER_NAME = 'mobile-app'
const FILE_CREATING = 'FILE_CREATING'
const AUDIO_PROCESSING = 'AUDIO_PROCESSING'
const IMAGE_PROCESSING = 'IMAGE_PROCESSING';

(async function () {
  const fetch = (await import('node-fetch')).default
  await RabbitMQ.channel.assertQueue(FILE_CREATING, { durable: true })
  await RabbitMQ.channel.assertQueue(IMAGE_PROCESSING, { durable: true })
  await RabbitMQ.channel.assertQueue(AUDIO_PROCESSING, { durable: true })

  RabbitMQ.channel.consume(IMAGE_PROCESSING, async message => {
    try {
      const { id, _id } = JSON.parse(message.content.toString())
      const res = await fetch(`https://api.edenai.run/v2/workflow/542532d4-ff34-4168-91db-feb80fcd9a35/execution/${id}`, {
        headers: {
          Authorization: "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiNjBlMjg4OGUtZmQ2NS00ZGU3LWFkNGUtMzRlYWVkZDkyOGRmIiwidHlwZSI6ImFwaV90b2tlbiJ9.l0nFCh_L0YSELnE5KjTAcy8dPu1HQX0xsIrXWTBZ6c0"
        }
      }).then(res => res.json())
      const isSafe = !res.content.results.image__explicit_content.results.some(e => e.items?.length)
      console.log(SocketIO.io.emit("file_update", _id, { status: isSafe ? "safe" : "unsafe" }))
      await File.findByIdAndUpdate(_id, { status: isSafe ? "safe" : "unsafe" })
      RabbitMQ.channel.ack(message)
    } catch (error) {
      console.error(error)
      RabbitMQ.channel.nack(message)
    }
  })

  RabbitMQ.channel.consume(AUDIO_PROCESSING, async message => {
    try {
      const { _id, path } = JSON.parse(message.content.toString())
      const res1 = await fetch('https://api.deepgram.com/v1/listen?model=nova-2&language=vi', {
        method: 'POST',
        headers: {
          Authorization: 'Token 4e47248c0ed7de0dc51d7ccf1df988b2876cdcfc',
        },
        body: fs.readFileSync(path)
      }).then(res => res.json());

      let description = res1.results.channels[0].alternatives[0].transcript
      

      if(description.length > 30) {
        description = await fetch('https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=AIzaSyBQm4dj0r3MaXTbhzHoZ6jLQVvbE448Pzc', {
          headers: {
            'Content-Type': 'application/json',
          },
          method: 'POST',
          body: JSON.stringify({
            contents: [{ parts: [{ text: `Tóm tắt đoạn văn ${description} ngắn nhất có thể bằng ngôn ngữ gốc` }] }]
          })
        })
          .then(res => res.json())
          .then(res => res.candidates[0].content.parts[0].text)
      }

      SocketIO.io.emit("file_update", _id, { description })
      await File.findByIdAndUpdate(_id, { description })
      fs.unlinkSync(path, () => { })
      RabbitMQ.channel.ack(message)
    } catch (error) {
      console.error(error)
      RabbitMQ.channel.nack(message)
    }
  })

  RabbitMQ.channel.consume(FILE_CREATING, async message => {
    try {
      const { _id, path, mimetype } = JSON.parse(message.content.toString())
      switch (mimetype) {
        case 'image': {
          const { url, public_id } = await v2.uploader.upload(path, { folder: FOLDER_NAME, resource_type: "image" })
          SocketIO.io.emit("file_update", _id, { url, blurUrl: v2.url(public_id, { transformation: { effect: "blur:2000" } }) })
          const { id } = await fetch("https://api.edenai.run/v2/workflow/542532d4-ff34-4168-91db-feb80fcd9a35/execution/", {
            method: "POST",
            headers: {
              Authorization: "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiNjBlMjg4OGUtZmQ2NS00ZGU3LWFkNGUtMzRlYWVkZDkyOGRmIiwidHlwZSI6ImFwaV90b2tlbiJ9.l0nFCh_L0YSELnE5KjTAcy8dPu1HQX0xsIrXWTBZ6c0",
              'Content-Type': 'application/json'
            },
            body: JSON.stringify({ file: url })
          }).then(res => res.json())
          if (!id) throw new Error("VIP")
          RabbitMQ.channel.sendToQueue(IMAGE_PROCESSING, Buffer.from(JSON.stringify({ id, _id })))
          await File.findByIdAndUpdate(_id, { url, blurUrl: v2.url(public_id, { transformation: { effect: "blur:2000" } }), _system: { need_to_delete: true, cloudinary_public_id: public_id } })
          fs.unlink(path, () => { })
          break
        }
        case 'audio': {
          const { url, public_id } = await v2.uploader.upload(path, { folder: FOLDER_NAME, resource_type: "video" })
          SocketIO.io.emit("file_update", _id, { url, status: "safe" })
          RabbitMQ.channel.sendToQueue(AUDIO_PROCESSING, Buffer.from(JSON.stringify({ _id, path })))
          await File.findByIdAndUpdate(_id, { url, status: "safe", _system: { need_to_delete: true, cloudinary_public_id: public_id } })
          break
        }
        default: {
          const { url, public_id } = await v2.uploader.upload(path, { folder: FOLDER_NAME, resource_type: "auto" })
          SocketIO.io.emit("file_update", _id, { url, status: "safe" })
          await File.findByIdAndUpdate(_id, { url, status: "safe", _system: { need_to_delete: true, cloudinary_public_id: public_id } })
          fs.unlink(path, () => { })
        }
      }
      RabbitMQ.channel.ack(message)
    } catch (error) {
      console.error(error)
      RabbitMQ.channel.nack(message)
    }
  })

})()


const upload = multer({
  storage:
    multer.diskStorage({
      destination: (req, file, cb) => {
        cb(null, "uploads")
      },
      filename: (req, file, cb) => {
        file.mimetype = file.mimetype.split('/')[0]
        cb(null, file.originalname)
      }
    }),
  limits: 10 * 1024,
})

async function getFile(f) {
  const file = await File.create({
    type: f.mimetype,
    name: f.originalname,
  })
  RabbitMQ.channel.sendToQueue(FILE_CREATING, Buffer.from(JSON.stringify({ _id: file._id, path: `./uploads/${f.filename}`, mimetype: f.mimetype })))
  return file._id.toString()
}

const single = (fieldName) => {
  return [
    upload.single(fieldName),
    async (req, res, next) => {
      try {
        if (req.file) req.body[fieldName] = await getFile(req.file)
        next()
      } catch (error) {
        next(error)
      }
    }
  ]
}

const array = (fieldName, maxCount) => {
  return [
    upload.array(fieldName, maxCount),
    async (req, res, next) => {
      try {
        req.body[fieldName] = req.body[fieldName] || []
        if (req.files) req.body[fieldName].push(...await Promise.all(req.files.map(getFile)))
        next()
      } catch (error) {
        next(error)
      }
    }
  ]
}

module.exports = { single, array }