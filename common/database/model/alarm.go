package model

import (
	"fmt"
	"strconv"
	"strings"
	"time"

	"github.com/bartosz11/checkmate/common/helpers"
)

type Alarm struct {
	BaseModel
	Name                    string              `gorm:"not null" json:"name"`
	Type                    AlarmType           `gorm:"not null" json:"type"`
	Triggered               bool                `gorm:"default:false" json:"triggered"`
	TriggeredStateChangedAt time.Time           `json:"triggeredStateChangedAt"`
	Suspended               bool                `gorm:"not null;default:false" json:"suspended"`
	Muted                   bool                `gorm:"default:false" json:"muted"`
	System                  bool                `gorm:"not null;default:false" json:"system"`
	MaxRetries              uint                `gorm:"not null;default:0" json:"maxRetries"`
	UsedRetries             uint                `gorm:"not null;default:0" json:"usedRetries"`
	Threshold               float64             `json:"threshold"`
	ThresholdField          AlarmThresholdField `json:"thresholdField"`
	// For example what NIC should the threshold apply to
	ThresholdFieldParams string         `json:"thresholdFieldParams"`
	TargetID             uint           `gorm:"not null" json:"targetId"`
	Notifications        []Notification `gorm:"many2many:alarms_notifications;constraint:OnDelete:CASCADE;" json:"notifications"`
	Incidents            []Incident     `gorm:"constraint:OnDelete:CASCADE;" json:"incidents"`
}

func (alarm *Alarm) CreateIncidentCause() string {
	var builder strings.Builder
	switch alarm.Type {
	case Unavailable:
		builder.WriteString("Health check failed")
	case Threshold:
		meta := AlarmThresholdFieldMetas[alarm.ThresholdField]
		builder.WriteString(meta.FormattedName)
		if len(alarm.ThresholdFieldParams) != 0 {
			builder.WriteString(" on ")
			builder.WriteString(alarm.ThresholdFieldParams)
		}
		builder.WriteString(" exceeded ")
		builder.WriteString(strconv.FormatFloat(alarm.Threshold, 'f', -1, 64))
		builder.WriteString(meta.Unit)
	case alarmTypeMax:
		//	Literally cannot happen
	}
	builder.WriteString("(")
	builder.WriteString(strconv.FormatUint(uint64(alarm.MaxRetries), 10))
	builder.WriteString(" retries exceeded)")
	return builder.String()
}

type AlarmType uint

const (
	Unavailable AlarmType = iota
	Threshold
	alarmTypeMax
)

func ValidateAlarmTypeForCreateUpdate(at AlarmType) error {
	if at == Unavailable {
		return fmt.Errorf("type %d cannot be used to create new alarms or update existing ones to it", at)
	}
	if at >= alarmTypeMax {
		return fmt.Errorf("invalid alarm type: %d", at)
	}
	return nil
}

type AlarmThresholdField uint

const (
	Latency AlarmThresholdField = iota
	CPUFrequency
	CPUUsage
	IOWait
	MemoryUsagePercent
	SwapUsagePercent
	DiskUsagePercent
	NICInbound
	NICOutbound
	thresholdFieldMax
)

func ValidateAlarmThresholdField(tf AlarmThresholdField) error {
	if tf >= thresholdFieldMax {
		return fmt.Errorf("invalid threshold field: %d", tf)
	}
	return nil
}

type AlarmThresholdFieldMeta struct {
	FormattedName string
	Unit          string
	GetValueFunc  func(hb *Heartbeat, alarm *Alarm) float64
}

var AlarmThresholdFieldMetas = map[AlarmThresholdField]AlarmThresholdFieldMeta{
	Latency: {
		FormattedName: "latency",
		Unit:          " ms",
		GetValueFunc: func(hb *Heartbeat, _ *Alarm) float64 {
			return float64(hb.Latency)
		},
	},
	CPUFrequency: {
		FormattedName: "CPU frequency",
		Unit:          " MHz",
		GetValueFunc: func(hb *Heartbeat, _ *Alarm) float64 {
			cpuInfo, ok := hb.Payload.Data["cpu"].(map[string]any)
			if !ok {
				return 0
			}
			freq, ok := helpers.ToFloat64(cpuInfo["frequency"])
			if !ok {
				return 0
			}
			return freq
		},
	},
	CPUUsage: {
		FormattedName: "CPU usage",
		Unit:          " %",
		GetValueFunc: func(hb *Heartbeat, _ *Alarm) float64 {
			cpuInfo, ok := hb.Payload.Data["cpu"].(map[string]any)
			if !ok {
				return 0
			}
			freq, ok := helpers.ToFloat64(cpuInfo["usage"])
			if !ok {
				return 0
			}
			return freq
		},
	},
	IOWait: {
		FormattedName: "IOWait",
		Unit:          " %",
		GetValueFunc: func(hb *Heartbeat, _ *Alarm) float64 {
			cpuInfo, ok := hb.Payload.Data["cpu"].(map[string]any)
			if !ok {
				return 0
			}
			freq, ok := helpers.ToFloat64(cpuInfo["iowait"])
			if !ok {
				return 0
			}
			return freq
		},
	},
	MemoryUsagePercent: {
		FormattedName: "memory usage",
		Unit:          " %",
		GetValueFunc: func(hb *Heartbeat, _ *Alarm) float64 {
			memInfo, ok := hb.Payload.Data["memory"].(map[string]any)
			if !ok {
				return 0
			}
			return calculateUsage(&memInfo)
		},
	},
	SwapUsagePercent: {
		FormattedName: "swap usage",
		Unit:          " %",
		GetValueFunc: func(hb *Heartbeat, _ *Alarm) float64 {
			memInfo, ok := hb.Payload.Data["swap"].(map[string]any)
			if !ok {
				return 0
			}
			return calculateUsage(&memInfo)
		},
	},
	DiskUsagePercent: {
		FormattedName: "disk usage",
		Unit:          " %",
		GetValueFunc: func(hb *Heartbeat, alarm *Alarm) float64 {
			disksInfo, ok := hb.Payload.Data["disks"].([]map[string]any)
			if !ok {
				return 0
			}

			usedSum := float64(0)
			totalSum := float64(0)
			for _, diskInfo := range disksInfo {
				mountpoint, ok := diskInfo["mount"].(string)
				if !ok {
					continue // I guess
				}

				// Calculate the usage of this disk only if it's the "wanted" one
				if mountpoint == alarm.ThresholdFieldParams {
					return calculateUsage(&diskInfo)
				}

				// Otherwise the "desired" mountpoint can be blank so we can just keep blindly aggregating
				usedFloat, ok1 := helpers.ToFloat64(diskInfo["used"])
				totalFloat, ok2 := helpers.ToFloat64(diskInfo["total"])
				if !ok1 || !ok2 {
					continue
				}
				usedSum += usedFloat
				totalSum += totalFloat
			}

			return (usedSum / totalSum) * 100
		},
	},
	NICInbound: {
		FormattedName: "NIC inbound traffic",
		Unit:          " bytes",
		GetValueFunc: func(hb *Heartbeat, alarm *Alarm) float64 {
			nicsInfo, ok := hb.Payload.Data["network"].([]map[string]any)
			if !ok {
				return 0
			}
			return getNICTraffic(nicsInfo, alarm, "rx")
		},
	},
	NICOutbound: {
		FormattedName: "NIC outbound traffic",
		Unit:          " bytes",
		GetValueFunc: func(hb *Heartbeat, alarm *Alarm) float64 {
			nicsInfo, ok := hb.Payload.Data["network"].([]map[string]any)
			if !ok {
				return 0
			}
			return getNICTraffic(nicsInfo, alarm, "tx")
		},
	},
}

func calculateUsage(info *map[string]any) float64 {
	used := (*info)["used"]
	total := (*info)["total"]
	if used == nil || total == nil {
		return 0
	}

	usedFloat, ok1 := helpers.ToFloat64(used)
	totalFloat, ok2 := helpers.ToFloat64(total)
	if !ok1 || !ok2 {
		return 0
	}

	return (usedFloat / totalFloat) * 100
}

func getNICTraffic(nicsInfo []map[string]any, alarm *Alarm, field string) float64 {
	trafficSum := float64(0)
	for _, nicInfo := range nicsInfo {
		iface, ok := nicInfo["iface"].(string)
		if !ok {
			continue
		}

		trafficFloat, ok := helpers.ToFloat64(nicInfo[field])
		if !ok {
			continue
		}

		// Return the bytes if that's the "wanted" NIC
		if iface == alarm.ThresholdFieldParams {
			return trafficFloat
		}

		// Otherwise the "desired" iface can be blank so we can just keep blindly aggregating
		trafficSum += trafficFloat
	}

	return trafficSum
}
