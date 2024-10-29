const config = {
    saml: {
        cert: './src/config/saml.pem',
        entryPoint: 'https://nubank.okta.com/app/nubank_marquezui_1/exk1zathd6ug5a1dY0h8/sso/saml',
        issuer: `http://${process.env.MARQUEZ_WEB_AUTH_SERVER_HOST}:${process.env.MARQUEZ_WEB_AUTH_SERVER_PORT}`,
        options: {
            failureRedirect: '/login',
            failureFlash: true
        }
    },
    server: {
        port: `${process.env.MARQUEZ_WEB_AUTH_SERVER_PORT}`
    },
    session: {
        resave: false,
        secret: 'supersecretamazingpassword',
        saveUninitialized: true
    }
};

export default config;