package model

import (
	"fmt"
	"time"
)

type StatuspageNotice struct {
	BaseModel
	Title            string                   `json:"title"`
	StartedAt        *time.Time               `json:"startedAt"`
	ResolvedAt       *time.Time               `json:"resolvedAt"`
	ScheduledStartAt *time.Time               `json:"scheduledStartAt"`
	ScheduledEndAt   *time.Time               `json:"scheduledEndAt"`
	Status           StatuspageNoticeStatus   `gorm:"not null" json:"status"`
	Type             StatuspageNoticeType     `gorm:"not null" json:"type"`
	Severity         StatuspageNoticeSeverity `gorm:"not null" json:"severity"`
	StatuspageID     uint                     `gorm:"not null" json:"statuspageId"`
	Statuspage       Statuspage               `gorm:"foreignKey:StatuspageID" json:"statuspage"`
	Updates          []StatuspageNoticeUpdate `gorm:"constraint:OnDelete:CASCADE;foreignKey:NoticeID" json:"updates"`
	Targets          []Target                 `gorm:"many2many:statuspages_notices_targets;constraint:OnDelete:CASCADE;" json:"targets"`
}

type StatuspageNoticeUpdate struct {
	BaseModel
	Message  string                 `gorm:"not null" json:"message"`
	Status   StatuspageNoticeStatus `gorm:"not null" json:"status"`
	Date     time.Time              `gorm:"not null" json:"date"`
	NoticeID uint                   `gorm:"not null" json:"noticeId"`
	Notice   StatuspageNotice       `gorm:"foreignKey:NoticeID" json:"notice"`
}

type StatuspageNoticeType uint

const (
	Issue StatuspageNoticeType = iota
	Maintenance
	Announcement
	statuspageNoticeTypeMax
)

func ValidateStatuspageNoticeType(nt StatuspageNoticeType) error {
	if nt >= statuspageNoticeTypeMax {
		return fmt.Errorf("invalid statuspage notice type: %d", nt)
	}
	return nil
}

type StatuspageNoticeSeverity uint

const (
	None StatuspageNoticeSeverity = iota
	Degraded
	PartialOutage
	MajorOutage
	statuspageNoticeSeverityMax
)

func ValidateStatuspageNoticeSeverity(ns StatuspageNoticeSeverity) error {
	if ns >= statuspageNoticeSeverityMax {
		return fmt.Errorf("invalid statuspage notice severity: %d", ns)
	}
	return nil
}

type StatuspageNoticeStatus uint

const (
	Scheduled StatuspageNoticeStatus = iota
	Started
	Investigating
	Identified
	Monitoring
	Resolved
	Completed
	statuspageNoticeStatusMax
)

func ValidateStatuspageNoticeStatus(s StatuspageNoticeStatus) error {
	if s >= statuspageNoticeStatusMax {
		return fmt.Errorf("invalid statuspage notice status: %d", s)
	}
	return nil
}
