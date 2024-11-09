import http from 'http';
import express from 'express';
import logging from './config/logging';
import config from './config/config';

const app = express();
app.set('trust proxy', true); 

const httpServer = http.createServer(app);

/** Log the request */
app.use((req, res, next) => {
    logging.info(`METHOD: [${req.method}] - URL: [${req.url}] - IP: [${req.socket.remoteAddress}]`);

    res.on('finish', () => {
        logging.info(`METHOD: [${req.method}] - URL: [${req.url}] - STATUS: [${res.statusCode}] - IP: [${req.socket.remoteAddress}]`);
    });

    next();
});

/** Middleware to parse authenticated user from headers */
app.use((req, res, next) => {
    const userEmail = req.headers['x-auth-request-user'] as string;

    if (userEmail) {
        req.user = { email: userEmail };
    }

    next();
});

/** Protected Route Example */
app.get('/whoami', (req, res) => {
    if (!req.user) {
        logging.info('User not authenticated');
        return res.status(401).json({ message: 'Unauthorized' });
    } else {
        logging.info('User authenticated');
        logging.info(req.user);
        return res.status(200).json({ user: req.user });
    }
});

/** Health Check */
app.get('/proxy/healthcheck/', (req, res) => {
    return res.status(200).json({ message: 'Server is up and running!' });
});

/** Additional API Routes */
app.get('/proxy/', (req, res) => {
    if (!req.user) {
        return res.status(401).json({ message: 'Unauthorized' });
    }
    // Your protected API logic here
    res.json({ data: 'Protected data' });
});

/** Error Handling */
app.use((req, res) => {
    const error = new Error('Not found');
    res.status(404).json({ message: error.message });
});

/** Handle Graceful Shutdown */
process.on('SIGTERM', () => {
    logging.info('SIGTERM signal received: closing http server');
    httpServer.close(() => {
        logging.info('http server closed');
        process.exit(0);
    });
});

httpServer.listen(config.server.port, '0.0.0.0', () => 
    logging.info(`Server is running on port ${config.server.port}`));