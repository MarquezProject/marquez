FROM node:22-alpine
WORKDIR /usr/src/app

ARG REACT_APP_ADVANCED_SEARCH

RUN apk update && apk add --virtual bash coreutils
RUN apk add --no-cache git
COPY package*.json ./

ENV REACT_APP_ADVANCED_SEARCH=$REACT_APP_ADVANCED_SEARCH

RUN npm install
COPY . .
RUN REACT_APP_ADVANCED_SEARCH=$REACT_APP_ADVANCED_SEARCH npm run build
COPY docker/entrypoint.sh entrypoint.sh
EXPOSE 3000
ENTRYPOINT ["/usr/src/app/entrypoint.sh"]
