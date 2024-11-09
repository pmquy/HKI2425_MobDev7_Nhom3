const handleErrors = (err, req, res, next) => {
  console.log(err)
  res.status(err.status || 500).json({ message: err.message || 'Something went wrong' })
}

module.exports = handleErrors