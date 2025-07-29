package model

import (
	"fmt"
)

type Alarm struct {
	BaseModel
	Name           string              `gorm:"not null" json:"name"`
	Type           AlarmType           `gorm:"not null" json:"type"`
	Active         bool                `gorm:"default:false" json:"active"`
	Muted          bool                `gorm:"default:false" json:"muted"`
	Threshold      float64             `json:"threshold"`
	ThresholdField AlarmThresholdField `json:"thresholdField"`
	TargetID       uint                `gorm:"not null" json:"targetId"`
	Notifications  []Notification      `gorm:"many2many:alarms_notifications;constraint:OnDelete:CASCADE;" json:"notifications"`
}

type AlarmType uint

const (
	Unavailable AlarmType = iota
	Threshold
	alarmTypeMax
)

func ValidateAlarmType(at AlarmType) error {
	if at >= alarmTypeMax {
		return fmt.Errorf("invalid alarm type: %d", at)
	}
	return nil
}

type AlarmThresholdField uint

const (
	Latency AlarmThresholdField = iota
	thresholdFieldMax
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

func ValidateAlarmThresholdField(tf AlarmThresholdField) error {
	if tf >= thresholdFieldMax {
		return fmt.Errorf("invalid threshold field: %d", tf)
	}
	return nil
}
