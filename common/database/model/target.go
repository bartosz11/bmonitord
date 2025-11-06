package model

import (
	"fmt"
	"time"
)

type Target struct {
	BaseModel
	Name        string         `gorm:"not null" json:"name"`
	ChecksUp    uint64         `json:"checksUp"`
	ChecksDown  uint64         `json:"checksDown"`
	MaxRetries  uint           `gorm:"not null" json:"maxRetries"`
	UsedRetries uint           `gorm:"not null;default:0" json:"usedRetries"`
	LastCheck   time.Time      `json:"lastCheck"`
	LastStatus  TargetStatus   `json:"lastStatus"`
	Type        TargetType     `gorm:"not null" json:"type"`
	Paused      bool           `gorm:"default:false" json:"paused"`
	Public      bool           `gorm:"not null;default:false" json:"public"`
	Timeout     uint           `json:"timeout"`
	UserID      uint           `gorm:"not null" json:"userId"`
	Heartbeats  []Heartbeat    `gorm:"constraint:OnDelete:CASCADE;" json:"heartbeats"`
	Alarms      []Alarm        `gorm:"constraint:OnDelete:CASCADE;" json:"alarms"`
	Incidents   []Incident     `gorm:"constraint:OnDelete:CASCADE;" json:"incidents"`
	Checkers    []Checker      `gorm:"many2many:targets_checkers;constraint:OnDelete:CASCADE;" json:"checkers"`
	HTTPInfo    TargetHTTPInfo `gorm:"constraint:OnDelete:CASCADE;" json:"httpInfo"`
	PingInfo    TargetPingInfo `gorm:"constraint:OnDelete:CASCADE;" json:"pingInfo"`
	Agent       Agent          `gorm:"constraint:OnDelete:CASCADE;" json:"agent"`
}

type TargetType uint

const (
	PING TargetType = iota
	HTTP
	AGENT
	targetTypeMax // "sentinel" value
)

type TargetStatus uint

const (
	Up TargetStatus = iota
	Down
	Unknown
)

func StatusToString(status TargetStatus) string {
	switch status {
	case Up:
		return "UP"
	case Down:
		return "DOWN"
	case Unknown:
		return "UNKNOWN"
	}
	return "" // Literally impossible
}

func ValidateTargetType(tt TargetType) error {
	if tt >= targetTypeMax {
		return fmt.Errorf("invalid target type: %d", tt)
	}
	return nil
}

func (t TargetType) IsPush() bool {
	return t == AGENT
}

func (target *Target) GetHost() string {
	switch target.Type {
	case PING:
		return target.PingInfo.Host
	case HTTP:
		return target.HTTPInfo.Host
	case AGENT:
		return "Server Agent"
	default:
		return ""
	}
}
