FROM alpine:3.7
WORKDIR /usr/local/bin
RUN apk add --update bash curl jq
COPY data/* ./data/
COPY seed-db.sh seed-db.sh
COPY entrypoint.sh entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
