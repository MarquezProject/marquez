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
        secret: process.env.SESSION_SECRET || 'supersecretamazingpassword',
        saveUninitialized: true,
        cookie: { secure: false }
    }
};

export const corsOptions = {
    origin: process.env.CORS_ORIGIN || '*',
    allowedOrigins: (process.env.CORS_ALLOWED_ORIGINS || '').split(','),
    methods: process.env.CORS_METHODS || 'GET,HEAD,PUT,PATCH,POST,DELETE',
    allowedHeaders: process.env.CORS_ALLOWED_HEADERS || 'Origin, X-Requested-With, Content-Type, Accept, Authorization',
    credentials: process.env.CORS_CREDENTIALS === 'true'
};

export default config;
