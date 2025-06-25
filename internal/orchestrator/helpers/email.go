package helpers

import (
	"bmonitord/config"
	"github.com/rs/zerolog/log"
	"gopkg.in/gomail.v2"
)

var (
	sender    gomail.SendCloser
	connected = false
	from      string
)

func InitEmail(emailCfg *config.EmailConfig) {
	if !validateEmailConfig(emailCfg) {
		return
	}
	dialer := gomail.NewDialer(emailCfg.Host, emailCfg.Port, emailCfg.Username, emailCfg.Password)
	dialer.SSL = emailCfg.SSL
	from = emailCfg.From
	s, err := dialer.Dial()
	if err != nil {
		log.Err(err).Msg("Failed to connect to SMTP server")
		return
	}
	sender = s
	connected = true
}

func SendEmailMessage(msg *gomail.Message) {
	if !connected {
		return // just a quiet exit, we sent a warning if the config is invalid, we also send connection errors to the console
	}
	msg.SetHeader("From", from)
	err := gomail.Send(sender, msg)
	if err != nil {
		log.Err(err).Msg("Failed to send email message")
	}
}

func validateEmailConfig(emailCfg *config.EmailConfig) bool {
	//We only check for 0-values,
	if emailCfg.Host == "" || emailCfg.Username == "" || emailCfg.Password == "" || emailCfg.Port == 0 || emailCfg.From == "" {
		log.Warn().Msg("Email configuration is invalid! Email notifications won't be sent.")
		return false
	}
	return true
}
