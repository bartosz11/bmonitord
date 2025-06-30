package model

import "gorm.io/gorm"

type Alarm struct {
	gorm.Model
	Name           string    `gorm:"not null"`
	Type           AlarmType `gorm:"not null"`
	Active         bool      `gorm:"default:false"`
	Muted          bool      `gorm:"default:false"`
	Threshold      float64
	ThresholdField AlarmThresholdField
	TargetID       uint           `gorm:"not null"`
	Notifications  []Notification `gorm:"many2many:alarms_notifications;constraint:OnDelete:CASCADE;"`
}

type AlarmType uint

const (
	Unavailable AlarmType = iota
	Threshold
)

type AlarmThresholdField uint

const (
	Latency AlarmThresholdField = iota
)

type AlarmThresholdFieldMeta struct {
	FormattedName string
	Unit          string
	GetValueFunc  func(hb *Heartbeat) float64
}

var AlarmThresholdFieldMetas = map[AlarmThresholdField]AlarmThresholdFieldMeta{
	Latency: {
		FormattedName: "latency",
		Unit:          " ms",
		GetValueFunc: func(hb *Heartbeat) float64 {
			return float64(hb.Latency)
		},
	},
}
