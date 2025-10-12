package helpers

type GenericErrorResponse struct {
	Code  int    `json:"code" example:"400"`
	Error string `json:"error" example:"parsing id failed"`
}

type GenericDeleteSuccessResponse struct {
	Code int `json:"code" example:"204"`
}
