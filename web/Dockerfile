FROM node:18-alpine
WORKDIR /usr/src/app
RUN apk update && apk add --virtual bash coreutils
RUN apk add --no-cache git
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build
COPY docker/entrypoint.sh entrypoint.sh
EXPOSE 3000
ENTRYPOINT ["/usr/src/app/entrypoint.sh"]
