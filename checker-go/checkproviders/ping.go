package checkproviders

import (
	"context"
	"os/exec"
	"runtime"
	"strconv"
	"time"

	"github.com/bartosz11/checkmate/internal/database/model"
)

func RunPingCheck(target model.Target) model.Heartbeat {
	// target.Timeout, target.PingInfo
	hb := model.Heartbeat{
		Status:   model.Down,
		TargetID: target.ID,
	}

	latency, success := pingHost(target.PingInfo.Host, int(target.Timeout))
	if success {
		hb.Status = model.Up
		hb.Latency = uint64(latency.Milliseconds())
	}

	hb.Timestamp = time.Now()
	return hb
}

func pingHost(host string, timeout int) (*time.Duration, bool) {
	var args []string

	switch runtime.GOOS {
	case "windows":
		// -n count, -w timeout per packet in ms - we can use this since it's just a single packet anyway
		args = []string{"-n", "1", "-w", strconv.Itoa(timeout * 1000), host}
	case "linux":
		// -c count, -w total timeout in seconds
		args = []string{"-c", "1", "-w", strconv.Itoa(timeout), host}
	default: // macOS and others where we're not sure about timeout support
		// -c count
		args = []string{"-c", "1", host}
	}

	// We're also using a context to make the whole thing have a timeout even if the OS doesn't support it
	ctx, cancel := context.WithTimeout(context.Background(), time.Duration(timeout)*time.Second)
	defer cancel()

	cmd := exec.CommandContext(ctx, "ping", args...)
	start := time.Now()
	err := cmd.Run()
	duration := time.Since(start)

	// timeout or some other error
	if err != nil {
		return nil, false
	}

	return &duration, true
}
