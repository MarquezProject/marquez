const config = {
    server: {
        port: 1337
    },
    session: {
        resave: false,
        secret:'supersecretamazingpassword',
        saveUninitialized: true,
        cookie: { 
            secure: true, 
            httpOnly: true,
            sameSite: 'lax' 
        }
    }
};

export default config;