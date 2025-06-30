package helpers

import (
	"github.com/gin-gonic/gin"
	"net/http"
)

func NotFound(c *gin.Context) {
	c.JSON(http.StatusNotFound, gin.H{
		"error": "not found",
	})
}

func BadRequest(c *gin.Context) {
	c.JSON(http.StatusBadRequest, gin.H{
		"error": "bad request",
	})
}

func AccountDisabled(c *gin.Context) {
	c.AbortWithStatusJSON(http.StatusForbidden, gin.H{
		"error": "account disabled",
	})
}

func UsernameAlreadyTaken(c *gin.Context) {
	c.JSON(http.StatusConflict, gin.H{
		"error": "username already taken",
	})
}

func WeakPassword(c *gin.Context) {
	c.JSON(http.StatusBadRequest, gin.H{
		"error": "invalid password: password must contain at least 1 uppercase letter, 1 lowercase letter, 1 number and must consist of at least 8 characters.",
	})
}

func PasswordHashingFailed(c *gin.Context) {
	c.JSON(http.StatusInternalServerError, gin.H{
		"error": "failed to hash password",
	})
}

func DBInteractionFailed(c *gin.Context) {
	c.JSON(http.StatusInternalServerError, gin.H{
		"error": "db interaction failed",
	})
}
