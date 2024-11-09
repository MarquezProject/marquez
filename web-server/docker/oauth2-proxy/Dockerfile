# Dockerfile for OAuth2 Proxy

FROM quay.io/oauth2-proxy/oauth2-proxy:v7.2.0

# Copy custom configuration if needed
# COPY oauth2_proxy.cfg /etc/oauth2_proxy.cfg

# Optionally, set environment variables directly
# ENV OAUTH2_PROXY_PROVIDER=oidc
# ENV OAUTH2_PROXY_CLIENT_ID=0oa1zfdpg80bKlXcq0h8
# ENV OAUTH2_PROXY_CLIENT_SECRET=your_client_secret
# ... add other environment variables as needed

# Expose the port
EXPOSE 4180

# Start OAuth2 Proxy
CMD ["oauth2-proxy", "/etc/oauth2_proxy.cfg"]