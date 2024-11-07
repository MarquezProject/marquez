import https from 'https';
import fs from 'fs';
import express from 'express';
import session from 'express-session';
import passport from 'passport';
import logging from './config/logging';
import config from './config/config';
import './config/passport';
import cors from 'cors';

const router = express();

/** Server Handling */
// Read SSL certificate and key
const sslOptions = {
    key: fs.readFileSync(config.server.ssl.key),
    cert: fs.readFileSync(config.server.ssl.cert)
};

/** Create HTTPS server with SSL options */
const httpsServer = https.createServer(sslOptions, router);

/** Log the request */
router.use((req, res, next) => {
    logging.info(`METHOD: [${req.method}] - URL: [${req.url}] - IP: [${req.socket.remoteAddress}]`);

    res.on('finish', () => {
        logging.info(`METHOD: [${req.method}] - URL: [${req.url}] - STATUS: [${res.statusCode}] - IP: [${req.socket.remoteAddress}]`);
    });

    next();
});

/** Rules of our API */
const corsOptions = {
    allowedOrigins: ['*'],
    origin: (origin: string | undefined, callback: (err: Error | null, allow?: boolean) => void) => {
        if (corsOptions.allowedOrigins.indexOf('*') !== -1) {
            callback(null, true);
        } else {
            callback(new Error('Not allowed by CORS'));
        }
    },
    credentials: true,
    methods: 'GET,HEAD,PUT,PATCH,POST,DELETE',
    allowedHeaders: 'Origin, X-Requested-With, Content-Type, Accept, Authorization'
};

/** Parse the body of the request / Passport */
router.use(session(config.session));
router.use(passport.initialize());
router.use(passport.session());
router.use(express.urlencoded({ extended: false }));
router.use(express.json()); 
router.use(cors(corsOptions));

/** Passport & SAML Routes */
router.get('/login', passport.authenticate('saml', config.saml.options), (req, res, next) => {
    return res.redirect('https://acec0e16141394e81af6a6eb748d23e7-b55d2dceb7142392.elb.us-west-2.amazonaws.com');
});

router.post('/login/callback', passport.authenticate('saml', config.saml.options), (req, res, next) => {
    return res.redirect('https://acec0e16141394e81af6a6eb748d23e7-b55d2dceb7142392.elb.us-west-2.amazonaws.com');
});

router.get('/whoami', (req, res, next) => {
    if (!req.isAuthenticated()) {
        logging.info('User not authenticated');

        return res.status(401).json({
            message: 'Unauthorized'
        });
    } else {
        logging.info('User authenticated');
        logging.info(req.user);

        return res.status(200).json({ user: req.user });
    }
});

/** Health Check */
router.get('/healthcheck', (req, res, next) => {
    return res.status(200).json({ message: 'Server is up and running!' });
});

/** Error handling */
router.use((req, res, next) => {
    const error = new Error('Not found');

    res.status(404).json({
        message: error.message
    });
});

httpsServer.listen(config.server.port, () => logging.info(`Server is running on port ${config.server.port} with HTTPS`));