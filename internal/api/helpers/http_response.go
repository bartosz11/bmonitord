package helpers

import "github.com/gin-gonic/gin"

type HTTPResponse struct {
	Code int         `json:"code"`
	Data interface{} `json:"data"`
	//This also facilitates string errors, which are most of our errors
	Error interface{} `json:"error"`
}

func (r *HTTPResponse) WriteAsJSON(c *gin.Context) {
	c.JSON(r.Code, r)
}

func (r *HTTPResponse) AbortWithJSON(c *gin.Context) {
	c.AbortWithStatusJSON(r.Code, r)
}
