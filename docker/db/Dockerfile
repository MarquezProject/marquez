FROM alpine:3.7
RUN apk add --update bash curl jq
COPY data/* ./data/
COPY wait-for-marquez.sh wait-for-marquez.sh
COPY seed-db.sh seed-db.sh
COPY entrypoint.sh entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
