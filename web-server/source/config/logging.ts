const DEFAULT_NAMESPACE = 'Server';

const info = (message: any, namespace?: string) => {
    if (typeof message === 'string') {
        console.log(`[${getDate()}] [${namespace || DEFAULT_NAMESPACE}] [INFO] ${message}`);
    } else {
        console.log(`[${getDate()}] [${namespace || DEFAULT_NAMESPACE}] [INFO]`, message);
    }
};

const warn = (message: any, namespace?: string) => {
    if (typeof message === 'string') {
        console.log(`[${getDate()}] [${namespace || DEFAULT_NAMESPACE}] [WARN] ${message}`);
    } else {
        console.log(`[${getDate()}] [${namespace || DEFAULT_NAMESPACE}] [WARN]`, message);
    }
};

const error = (message: any, namespace?: string) => {
    if (typeof message === 'string') {
        console.log(`[${getDate()}] [${namespace || DEFAULT_NAMESPACE}] [ERROR] ${message}`);
    } else {
        console.log(`[${getDate()}] [${namespace || DEFAULT_NAMESPACE}] [ERROR]`, message);
    }
};

const getDate = () => {
    return new Date().toISOString();
};

export default { info, warn, error };