import fs from 'fs';
import passport from 'passport';
import { Strategy } from 'passport-saml';
import config from './config';
import logging from './logging';

// Utilizar um Set para armazenar e-mails dos usuários
const savedUsers: Set<string> = new Set();

passport.serializeUser<string>((userEmail, done) => {
    logging.info(userEmail, 'Serialize User');
    done(null);
});

passport.deserializeUser<string>((userEmail, done) => {
    logging.info(userEmail, 'Deserialize User');
    done(null, userEmail);
});

passport.use(
    new Strategy(
        {
            issuer: config.saml.issuer,
            protocol: 'https://',
            path: '/login/callback',
            entryPoint: config.saml.entryPoint,
            cert: fs.readFileSync(config.saml.cert, 'utf-8'),
            logoutUrl: 'https://nubank.okta.com',
            acceptedClockSkewMs: 11100000 // 3 horas e 5 minutos
        },
        (profile: any, done: any) => {
            try {
                const userEmail: string = profile.email;

                if (!savedUsers.has(userEmail)) {
                    savedUsers.add(userEmail);
                }

                return done(null, userEmail);
            } catch (error) {
                logging.error(error, 'SAML Strategy Error');
                return done(error, null);
            }
        }
    )
);