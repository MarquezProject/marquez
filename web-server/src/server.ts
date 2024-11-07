import http from 'http';
import express from 'express';
import session from 'express-session';
import passport from 'passport';
import logging from './config/logging';
import config from './config/config';
import './config/passport';

const router = express();
router.set('trust proxy', true);

const httpServer = http.createServer(router);

/** Log the request */
router.use((req, res, next) => {
    logging.info(`METHOD: [${req.method}] - URL: [${req.url}] - IP: [${req.socket.remoteAddress}]`);

    res.on('finish', () => {
        logging.info(`METHOD: [${req.method}] - URL: [${req.url}] - STATUS: [${res.statusCode}] - IP: [${req.socket.remoteAddress}]`);
    });

    next();
});

/** Parse the body of the request / Passport */
router.use(session(config.session));
router.use(passport.initialize());
router.use(passport.session());
router.use(express.urlencoded({ extended: false }));
router.use(express.json()); 

/** Passport & SAML Routes */
router.get('/login', passport.authenticate('saml', config.saml.options), (req, res, next) => {
    return res.redirect('http://acec0e16141394e81af6a6eb748d23e7-b55d2dceb7142392.elb.us-west-2.amazonaws.com');
});

router.post('/login/callback', passport.authenticate('saml', config.saml.options), (req, res, next) => {
    return res.redirect('http://acec0e16141394e81af6a6eb748d23e7-b55d2dceb7142392.elb.us-west-2.amazonaws.com');
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

/** Handle Graceful Shutdown */
process.on('SIGTERM', () => {
    logging.info('SIGTERM signal received: closing http server');
    httpServer.close(() => {
        logging.info('http server closed');
        process.exit(0);
    });
});

httpServer.listen(config.server.port, () => logging.info(`Server is running on port ${config.server.port} with http`));