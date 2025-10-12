package main

import (
	"github.com/bartosz11/checkmate/checker"
	"github.com/bartosz11/checkmate/common"
	"github.com/bartosz11/checkmate/common/config"
)

// main is the entrypoint of the standalone checker binary/image
func main() {
	cfg := config.LoadConfig()

	common.SetupLogging(&cfg)

	checker.StartChecker(&cfg.CheckerConfig)

	// We need to block the main thread to prevent the process from exiting, probably not the most optimal way
	select {}
}
