FROM oven/bun:latest AS build_frontend
COPY frontend/ /home/root
WORKDIR /home/root
RUN bun i
RUN bun run build
FROM golang:1.24-alpine AS build_go
WORKDIR /home/root
COPY . .
COPY --from=build_frontend /home/root frontend/
RUN go build -o app main.go
FROM alpine:latest AS run
RUN addgroup -S container && adduser -S container -G container
RUN mkdir -p /home/container && chown container:container /home/container
#This is necessary for the TZ env var to apply correctly, I think
RUN apk add --no-cache tzdata
# Set the working directory
WORKDIR /home/container
# Copy the Go application and frontend assets from the previous stages
COPY --from=build_go /home/root/app /bin/app
# Change ownership of the application files
RUN chown container:container /bin/app
# Switch to the new user
USER container
ENTRYPOINT [ "/bin/app" ]