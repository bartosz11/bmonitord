package model

import (
	"fmt"
	"gorm.io/gorm"
	"time"
)

type Target struct {
	gorm.Model
	Name        string `gorm:"not null"`
	ChecksUp    uint64
	ChecksDown  uint64
	MaxRetries  uint `gorm:"not null"`
	UsedRetries uint `gorm:"not null;default:0"`
	LastCheck   time.Time
	LastStatus  TargetStatus
	Type        TargetType `gorm:"not null"`
	Paused      bool       `gorm:"default:false"`
	Timeout     uint
	UserID      uint           `gorm:"not null"`
	Heartbeats  []Heartbeat    `gorm:"constraint:OnDelete:CASCADE;"`
	Alarms      []Alarm        `gorm:"constraint:OnDelete:CASCADE;"`
	Incidents   []Incident     `gorm:"constraint:OnDelete:CASCADE;"`
	Checkers    []Checker      `gorm:"many2many:targets_checkers;constraint:OnDelete:CASCADE;"`
	HTTPInfo    TargetHTTPInfo `gorm:"constraint:OnDelete:CASCADE;"`
	PingInfo    TargetPingInfo `gorm:"constraint:OnDelete:CASCADE;"`
}

type TargetType uint

const (
	PING TargetType = iota
	HTTP
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
