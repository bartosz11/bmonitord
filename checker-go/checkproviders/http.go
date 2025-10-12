package checkproviders

import (
	"crypto/tls"
	"net/http"
	"strconv"
	"strings"
	"time"

	"github.com/bartosz11/checkmate/internal/database/model"
)

func RunHTTPCheck(target model.Target) model.Heartbeat {
	hb := model.Heartbeat{
		TargetID: target.ID,
		Status:   model.Down,
	}

	tr := &http.Transport{
		TLSClientConfig: &tls.Config{
			InsecureSkipVerify: !target.HTTPInfo.VerifySSLCert,
		},
	}
	client := &http.Client{Transport: tr, Timeout: time.Second * time.Duration(target.Timeout)}
	// otherwise the HTTP client follows the first 10 redirects
	if !target.HTTPInfo.FollowRedirects {
		client.CheckRedirect = dontFollowRedirects
	}

	start := time.Now()
	resp, err := client.Get(target.HTTPInfo.Host)
	duration := time.Since(start)

	// We got a response
	if err != nil && resp != nil {
		defer resp.Body.Close()
		hb.Latency = uint64(duration.Milliseconds())

		codes := strings.Split(target.HTTPInfo.AllowedCodes, " ")
		for _, code := range codes {
			if code == strconv.Itoa(resp.StatusCode) {
				// if the returned code is "allowed" it's up, in every other case it's considered as down
				hb.Status = model.Up
			}
		}
	}

	hb.Timestamp = time.Now()
	return hb
}

func dontFollowRedirects(_ *http.Request, _ []*http.Request) error {
	return http.ErrUseLastResponse
}
