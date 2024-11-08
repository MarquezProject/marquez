# Dockerfile for NGINX

FROM nginx:latest

# Remove the default NGINX configuration
RUN rm /etc/nginx/conf.d/default.conf

# Copy your custom NGINX configuration
COPY nginx.conf /etc/nginx/nginx.conf

# Copy SSL certificates
COPY ../certs/certs /etc/nginx/certs

# Create directory for logs
RUN mkdir -p /var/log/nginx

# Expose ports
EXPOSE 80 443

# Start NGINX when the container launches
CMD ["nginx", "-g", "daemon off;"]