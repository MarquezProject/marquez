const config = {
    saml: {
        cert: './src/config/saml.pem',
        entryPoint: 'https://nubank.okta.com/app/nubank_marquezui_1/exk1zathd6ug5a1dY0h8/sso/saml',
        issuer: 'http://www.okta.com/exk1zathd6ug5a1dY0h8',
        options: {
            failureRedirect: '/login',
            failureFlash: true
        }
    },
    server: {
        port: 1337
    },
    session: {
        resave: false,
        secret: 'supersecretamazingpassword',
        saveUninitialized: true,
        cookie: { secure: false }
    }
};

export default config;