# Use the official NGINX base image
FROM nginx:1.27.2-alpine

# Install OpenSSL
RUN apk add --no-cache openssl

# Set the working directory
WORKDIR /usr/src/app

# Copy the localhost.ext file into the container
COPY localhost.ext /etc/nginx/certs/localhost.ext

# Generate a private key
RUN openssl genrsa -out localhost.key 2048

# Create a self-signed certificate
RUN openssl req -x509 -new -nodes -key localhost.key -sha256 -days 3650 -out localhost.crt \
    -subj "/C=US/ST=State/L=City/O=Organization/OU=OrgUnit/CN=localhost"

# Generate a Certificate Signing Request (CSR)
RUN openssl req -new -key localhost.key -out localhost.csr \
    -subj "/C=US/ST=State/L=City/O=Organization/OU=OrgUnit/CN=localhost"

# Generate the .pem certificate using the CSR and the extension file
RUN openssl x509 -req -in localhost.csr -CA localhost.crt -CAkey localhost.key -CAcreateserial \
    -out localhost.pem -days 365 -sha256 -extfile /etc/nginx/certs/localhost.ext

# Copy the generated certificates to /etc/nginx/certs/
RUN cp localhost.key /etc/nginx/certs/localhost.key && \
    cp localhost.crt /etc/nginx/certs/localhost.crt && \
    cp localhost.pem /etc/nginx/certs/localhost.pem

# Remove the default NGINX configuration
RUN rm /etc/nginx/conf.d/default.conf

# Copy your custom NGINX configuration
COPY nginx.conf /etc/nginx/nginx.conf

# Expose ports (optional, adjust as needed)
EXPOSE 80 443

# Start NGINX
CMD ["nginx", "-g", "daemon off;"]