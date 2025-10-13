# This Dockerfile can theoretically be used to build an image for AIO/orchestrator/checker/API. You can change the
# GO_ENTRYPOINT arg to cmd/{aio.go|checker.go|api.go|orchestrator.go}. It'll still build frontend even if the module
# you're building doesn't serve it, so it might be somewhat slower. If you want more control, you can use the Dockerfile.ci
# instead. You'll have to build frontend manually beforehand if you're building API/AIO though.
ARG GO_ENTRYPOINT="cmd/aio.go"

FROM oven/bun:latest AS build_frontend
COPY ../frontend /home/root
WORKDIR /home/root
RUN bun i
RUN bun run build
FROM golang:1.24-alpine AS build_go
ARG GO_ENTRYPOINT
WORKDIR /home/root
COPY .. .
COPY --from=build_frontend /home/root frontend/
RUN go build -o app $GO_ENTRYPOINT
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