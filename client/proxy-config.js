const proxy_config = [
    {
        context: [ '/api/**' ],
        target: 'https://star-trains-production.up.railway.app',
        secure: false
    }
]

module.exports = proxy_config