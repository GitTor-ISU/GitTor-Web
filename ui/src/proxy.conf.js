module.exports = {
  '/api/v3': {
    bypass: function (req, res) {
      res.statusCode = 403;
      res.end('Access to /api/v3/ is forbidden.');
      return true;
    },
  },
  '/api/actuator': {
    bypass: function (req, res) {
      res.statusCode = 403;
      res.end('Access to /api/actuator/ is forbidden.');
      return true;
    },
  },
  '/api': {
    'target': 'http://localhost:8080',
    'pathRewrite': { '^/api': '' },
    'secure': false,
  },
};
