# Build context is the repository root.
FROM node:22-alpine AS build
WORKDIR /workspace

COPY frontend/package.json frontend/package-lock.json* ./
RUN npm install

COPY frontend/ ./
RUN npm run build -- --configuration production

FROM nginx:1.27-alpine AS runtime
COPY infrastructure/frontend/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /workspace/dist/ferment-lab/browser /usr/share/nginx/html

EXPOSE 80
