package api

import (
	"bmonitord/config"
	"github.com/golang-jwt/jwt/v5"
	"github.com/rs/zerolog/log"
	"time"
)

var (
	jwtSecret   []byte
	jwtValidity int
)

func InitJWTHelper(apiCfg *config.APIConfig) {
	jwtSecret = []byte(apiCfg.JWTSecret)
	jwtValidity = apiCfg.JWTValidity
}

func GenerateJWT(userID uint, sessionID uint) (string, error) {
	claims := jwt.MapClaims{
		"userID":    userID,
		"sessionID": sessionID,
		"exp":       time.Now().Add(time.Minute * time.Duration(jwtValidity)),
	}
	token := jwt.NewWithClaims(jwt.SigningMethodHS512, claims)
	return token.SignedString(jwtSecret)
}

// ExtractIDsFromJWT returns respectively user ID, session ID and "ok"
func ExtractIDsFromJWT(token *jwt.Token) (uint, uint, bool) {
	claims, ok := token.Claims.(jwt.MapClaims)
	if !ok {
		return 0, 0, false
	}

	userID, ok1 := getClaimAsUint(claims, "userID")
	sessionID, ok2 := getClaimAsUint(claims, "sessionID")
	return userID, sessionID, ok1 && ok2
}

func ParseToken(tokenStr string) (*jwt.Token, bool) {
	token, err := jwt.Parse(tokenStr, secret, jwt.WithValidMethods([]string{jwt.SigningMethodHS512.Alg()}), jwt.WithExpirationRequired(), jwt.WithLeeway(0))
	if err != nil {
		log.Err(err).Msg("failed to parse token")
		return nil, false
	}
	return token, true
}

func getClaimAsUint(claims jwt.MapClaims, key string) (uint, bool) {
	raw, ok := claims[key]
	if !ok {
		return 0, false
	}

	floatVal, ok := raw.(float64) // encoding/json maps to float64 or smth
	if !ok || floatVal < 0 {
		return 0, false
	}

	return uint(floatVal), true
}

func secret(*jwt.Token) (interface{}, error) {
	return jwtSecret, nil
}
