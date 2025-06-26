package api

import (
	"bmonitord/config"
	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"time"
)

func StartAPI(db *gorm.DB, router *gin.Engine, production bool, apiConfig *config.APIConfig) {
	apiGroup := router.Group("/api")
	if !production {
		apiGroup.Use(cors.New(cors.Config{
			AllowOrigins:     []string{"http://localhost:5173"},
			AllowMethods:     []string{"PUT", "PATCH", "GET", "OPTIONS", "HEAD", "POST", "DELETE"},
			AllowHeaders:     []string{"Origin"},
			ExposeHeaders:    []string{"Content-Length"},
			AllowCredentials: true,
			MaxAge:           12 * time.Hour,
		}))
	}

	InitJWTHelper(apiConfig)

	apiGroup.Use(AuthMiddleware(db))

}
