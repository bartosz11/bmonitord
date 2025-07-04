package helpers

import "math/rand/v2"

const charset = "abcdefghijklmnopqrstuvwxyz" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789"

func GenerateRandomAlphanumericString(length int) string {
	str := make([]byte, length)
	for i := range str {
		str[i] = charset[rand.IntN(len(charset))]
	}
	return string(str)
}
