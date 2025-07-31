package notification

import (
	"errors"
	"github.com/bartosz11/checkmate/internal/api/helpers"
	"github.com/bartosz11/checkmate/internal/database/model"
	"strings"
)

func ValidateCredentialsForType(notificationType model.NotificationType, credentials string) error {
	// I ignore the Go "error strings shouldn't be capitalized" convention here to
	switch notificationType {
	case model.Discord:
		if strings.HasPrefix(credentials, "https://discord.com/api/webhooks/") {
			return nil
		}
		return errors.New("Discord webhook URL must start with https://discord.com/api/webhooks/")

	case model.Slack:
		if strings.HasPrefix(credentials, "https://hooks.slack.com/") {
			return nil
		}
		return errors.New("Slack webhook URL must start with https://hooks.slack.com/")

	case model.Pushbullet:
		if !helpers.IsBlank(credentials) {
			return nil
		}
		return errors.New("Pushbullet access token must not be empty")

	case model.Email:
		if strings.Contains(credentials, "@") {
			return nil
		}
		return errors.New("Email must contain a valid email address")

	case model.Gotify:
		//Gotify credentials are stored in a URL;token format
		split := strings.Split(credentials, ";")
		if len(split) != 2 {
			return errors.New("Gotify credentials must include a URL and an access token")
		}

		url := split[0]
		if err := validateURL(url, "Gotify"); err != nil {
			return err
		}

		token := split[1]
		if helpers.IsBlank(token) {
			return errors.New("Gotify token must not be empty")
		}

		return nil

	case model.GenericWebhook:
		return validateURL(credentials, "Generic webhook")
	default:
		// "accidentally" this is returned when a user sets type to a nonexistent value, but it's ok and actually correct
		return errors.New("unknown notification type")
	}
}

func NormalizeCredentials(notifiType model.NotificationType, credentials string) string {
	switch notifiType {
	case model.Gotify:
		split := strings.Split(credentials, ";")
		url := split[0]
		return strings.TrimSpace(strings.TrimRight(url, "/")) + ";" + strings.TrimSpace(split[1])
	default:
		return strings.TrimSpace(credentials)
	}
}

func validateURL(url string, notifiType string) error {
	if strings.HasPrefix(url, "http://") || strings.HasPrefix(url, "https://") {
		return nil
	}
	return errors.New(notifiType + " URL must start with http:// or https://")
}
