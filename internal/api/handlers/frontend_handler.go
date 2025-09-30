package handlers

import (
	"embed"
	"net/http"
	"path/filepath"
	"strings"

	"github.com/bartosz11/checkmate/internal/api/helpers"
	"github.com/gin-gonic/gin"
)

// ServeFile serves a specific file from the embedded filesystem
func ServeFile(c *gin.Context, fs embed.FS, path string) {
	content, err := fs.ReadFile(path)
	if err != nil {
		c.Status(http.StatusNotFound)
		return
	}
	// Automatically detect content type based on the file extension
	mime := mimeFromFileExtension(path)
	c.Data(http.StatusOK, mime, content)
}

func mimeFromFileExtension(path string) string {
	ext := strings.ToLower(filepath.Ext(path))
	switch ext {
	case ".html":
		return "text/html"
	case ".png":
		return "image/png"
	case ".svg":
		return "image/svg+xml"
	case ".ico":
		return "image/x-icon"
	case ".css":
		return "text/css"
	case ".js":
		return "application/javascript"
	case ".json":
		return "application/json"
	case ".webmanifest":
		return "application/manifest+json"
	case ".jpg", ".jpeg":
		return "image/jpeg"
	case ".gif":
		return "image/gif"
	case ".woff":
		return "font/woff"
	case ".woff2":
		return "font/woff2"
	case ".txt":
		return "text/plain"
	default:
		return "application/octet-stream"
	}
}

// StaticFileHandler serves the static files, including index.html
func StaticFileHandler(c *gin.Context, embeddedFiles embed.FS) {
	path := c.Request.URL.Path

	filePath := "build" + path

	// Check if the requested file exists in the embedded file system
	_, err := embeddedFiles.Open(filePath)

	if err == nil {
		// If file exists, serve it
		ServeFile(c, embeddedFiles, filePath)
		return
	}

	// Skip API routes, since they are handled by other handlers
	if strings.HasPrefix(path, "/api/") || strings.HasPrefix(path, "/orchestrator/") {
		helpers.NotFound(c) // If no matching API route, return 404
		return
	}

	// Serve index.html for any other routes (SPA fallback)
	ServeFile(c, embeddedFiles, "build/index.html")
}
