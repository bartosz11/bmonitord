package helpers

import (
	"github.com/gin-gonic/gin"
	"net/http"
)

func NotFound(c *gin.Context) {
	resp := HTTPResponse{
		Code:  http.StatusNotFound,
		Error: "not found",
	}
	resp.WriteAsJSON(c)
}

func BadRequestWithSpecificError(c *gin.Context, err interface{}) {
	resp := HTTPResponse{
		Code:  http.StatusBadRequest,
		Error: err,
	}
	resp.WriteAsJSON(c)
}

func AccountDisabled(c *gin.Context) {
	resp := HTTPResponse{
		Code:  http.StatusForbidden,
		Error: "account disabled",
	}
	resp.AbortWithJSON(c)
}

func UsernameAlreadyTaken(c *gin.Context) {
	resp := HTTPResponse{
		Code:  http.StatusConflict,
		Error: "username already taken",
	}
	resp.WriteAsJSON(c)
}

func WeakPassword(c *gin.Context) {
	resp := HTTPResponse{
		Code:  http.StatusBadRequest,
		Error: "invalid password: password must contain at least 1 uppercase letter, 1 lowercase letter, 1 number and must consist of at least 8 characters.",
	}
	resp.WriteAsJSON(c)
}

func PasswordHashingFailed(c *gin.Context) {
	resp := HTTPResponse{
		Code:  http.StatusInternalServerError,
		Error: "failed to hash password",
	}
	resp.WriteAsJSON(c)
}

func DBInteractionFailed(c *gin.Context) {
	resp := HTTPResponse{
		Code:  http.StatusInternalServerError,
		Error: "DB interaction failed",
	}
	resp.WriteAsJSON(c)
}

func ParsingFailed(c *gin.Context, field string) {
	resp := HTTPResponse{
		Code:  http.StatusBadRequest,
		Error: "parsing " + field + " failed",
	}
	resp.WriteAsJSON(c)
}
