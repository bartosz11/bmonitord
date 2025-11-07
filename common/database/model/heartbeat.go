package model

import (
	"time"
)

type Heartbeat struct {
	BaseModel
	// Latency is a "generic" field, payload can provide further information in non-push targets (http, ping etc.)
	Latency   uint64       `json:"latency"`
	Timestamp time.Time    `gorm:"not null" json:"timestamp"`
	Status    TargetStatus `gorm:"not null" json:"status"`
	TargetID  uint         `gorm:"not null" json:"targetId"`
	Target    Target       `gorm:"foreignKey:TargetID" json:"target"`
	CheckerID *uint        `json:"checkerId"`
	Checker   *Checker     `gorm:"foreignKey:CheckerID" json:"checker"`
	// Payload is stored as gzipped json in a bytea column, but sent in JSON normally
	Payload *HeartbeatPayload `gorm:"type:bytea" json:"payload"`
}
