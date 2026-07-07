FROM node:22-alpine

WORKDIR /app

COPY match-movie/package*.json ./

RUN npm install

COPY match-movie/. .

EXPOSE 4200

CMD ["npx", "ng", "serve", "--host", "0.0.0.0"]