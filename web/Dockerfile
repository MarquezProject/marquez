FROM node:18-alpine
WORKDIR /usr/src/app

ARG REACT_APP_ADVANCED_SEARCH

RUN apk update && apk add --virtual bash coreutils
RUN apk add --no-cache git

COPY package*.json ./

ENV REACT_APP_ADVANCED_SEARCH=$REACT_APP_ADVANCED_SEARCH
ENV MARQUEZ_HOST=marquez-api
ENV MARQUEZ_PORT=5000
ENV WEB_PORT=3000

RUN npm install

COPY . .

RUN REACT_APP_ADVANCED_SEARCH=$REACT_APP_ADVANCED_SEARCH npm run build

COPY docker/entrypoint.sh entrypoint.sh

EXPOSE 3000

CMD ["npm", "start"]

ENTRYPOINT ["/usr/src/app/entrypoint.sh"]
