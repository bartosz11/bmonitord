package helpers

import (
	"github.com/alexedwards/argon2id"
	"github.com/rs/zerolog/log"
)

var hashLimiter chan struct{}

var Argon2Params = &argon2id.Params{
	Memory:      64 * 1024,
	Iterations:  3,
	Parallelism: 1,
	SaltLength:  16,
	KeyLength:   32,
}

func InitHashLimiter(maxConcurrent int) {
	if maxConcurrent <= 0 {
		log.Fatal().Msg("hash pool size must be > 0")
	}
	hashLimiter = make(chan struct{}, maxConcurrent)
}

// acquireHashPoolSlot adds an element to the hashLimiter buffer in order to block a slot and returns a function that can be called to release the slot
func acquireHashPoolSlot() (releaseSlot func()) {
	hashLimiter <- struct{}{}
	return func() { <-hashLimiter }
}

// HashPassword hashes the given password with Argon2id and blocks a hashing pool slot
func HashPassword(password string) (hash string, err error) {
	releaseSlot := acquireHashPoolSlot()
	defer releaseSlot()

	return argon2id.CreateHash(password, Argon2Params)
}

// CheckPassword compares given password with the given password inside the hashing pool. The first returned boolean is whether the password matches the hash, the second indicates if the password should be re-hashed or not. Parameters from the hash are used to compute the password for the matching check. If the params in the hash differ from the ones defined in Argon2Params, 2nd boolean returns true, otherwise false.
func CheckPassword(password string, hash string) (match bool, needsRehash bool, err error) {
	releaseSlot := acquireHashPoolSlot()
	defer releaseSlot()

	match, params, err := argon2id.CheckHash(password, hash)
	if err != nil {
		return false, false, err
	}

	if !match {
		// Make sure we don't rehash the password that is not valid effectively locking out the user
		return false, false, nil
	}

	// if any of the hash's params is lower than it's corresponding value in Argon2Params, rehash
	needsRehash = params.Memory < Argon2Params.Memory || params.Iterations < Argon2Params.Iterations || params.Parallelism < Argon2Params.Parallelism || params.KeyLength < Argon2Params.KeyLength || params.SaltLength < Argon2Params.SaltLength
	return true, needsRehash, nil
}
