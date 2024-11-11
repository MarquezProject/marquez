# Use the official Node.js 14 image as the base image
FROM node:18

RUN apt update && apt install -y authbind \
        && rm -rf /var/lib/apt/lists/* \
        # && apk add curl \
        && rm -rf /var/cache/apk/*
# Set the working directory
WORKDIR /usr/src/app

# Copy package.json and package-lock.json (if available)
COPY package*.json ./

# Install dependencies
RUN npm install
# Copy the rest of the application code
COPY . .

# Build the TypeScript code
RUN npm run build \
    && touch /etc/authbind/byport/443 \
    && chown root /etc/authbind/byport/443 \
    && chmod 777 /etc/authbind/byport/443 \
    && touch /etc/authbind/byport/80 \
    && chown root /etc/authbind/byport/80 \
    && chmod 777 /etc/authbind/byport/80

# Expose the port the app runs on
EXPOSE 1337

# Define the command to run the app
CMD ["authbind", "npm", "start"]