const config = {
    saml: {
        cert: './source/config/saml.pem',
        entryPoint: 'https://nubank.okta.com/app/nubank_marquezui_1/exk1zathd6ug5a1dY0h8/sso/saml',
        issuer: 'http://localhost:3000',
        options: {
            failureRedirect: '/login',
            failureFlash: true
        }
    },
    server: {
        port: 3000
    },
    session: {
        resave: false,
        secret: 'supersecretamazingpassword',
        saveUninitialized: true
    }
};

export default config;