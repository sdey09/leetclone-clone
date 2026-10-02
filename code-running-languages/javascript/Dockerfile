FROM node:20-alpine

# Create non-root user (Alpine syntax)
RUN addgroup -S coderunner && \
    adduser -S coderunner -G coderunner && \
    mkdir -p /workspace && \
    chown -R coderunner:coderunner /workspace

USER coderunner

CMD ["tail", "-f", "/dev/null"]