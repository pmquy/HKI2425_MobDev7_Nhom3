const router = require('express').Router()
const auth = require('../middlewares/auth')

router.get('/ping', (req, res) => res.json({ message: 'pong' }))
router.use('/user', require('./user'))
router.use('/file', require('./file'))
router.use('/chatgroup', auth({ required: true }), require('./chatgroup'))
router.use('/friend', auth({ required: true }), require('./friend'))
router.use('/message', auth({ required: true }), require('./message'))
router.use(require('../middlewares/error'))

module.exports = router