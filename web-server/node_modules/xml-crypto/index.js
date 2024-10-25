var select = require('xpath').select

module.exports = require('./lib/signed-xml')
module.exports.xpath = function(node, xpath) {
  return select(xpath, node)
}