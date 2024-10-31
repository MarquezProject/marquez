const config = {
    saml: {
        cert: './src/config/saml.pem',
        entryPoint: process.env.SAML_ENTRY_POINT || 'https://nubank.okta.com/app/nubank_marquezui_1/exk1zathd6ug5a1dY0h8/sso/saml',
        issuer: process.env.SAML_ISSUER || 'http://www.okta.com/exk1zathd6ug5a1dY0h8',
        options: {
            failureRedirect: '/login',
            failureFlash: true
        }
    },
    server: {
        port: process.env.AUTH_SERVER_PORT ?? 1337
    },
    session: {
        resave: false,
        secret: 'supersecretamazingpassword',
        saveUninitialized: true,
        cookie: { secure: false }
    }
};

export default config;