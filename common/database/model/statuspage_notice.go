package model

import "time"

type StatuspageNotice struct {
	BaseModel
	Title          string
	StartedAt      *time.Time               `json:"startedAt"`
	ResolvedAt     *time.Time               `json:"resolvedAt"`
	ScheduledFor   *time.Time               `json:"scheduledFor"`
	ScheduledUntil *time.Time               `json:"scheduledUntil"`
	Status         StatuspageNoticeStatus   `gorm:"not null" json:"status"`
	Type           StatuspageNoticeType     `gorm:"not null" json:"type"`
	Severity       StatuspageNoticeSeverity `gorm:"not null" json:"severity"`
	StatuspageID   uint                     `gorm:"not null" json:"statuspageId"`
	Statuspage     Statuspage               `gorm:"foreignKey:StatuspageID" json:"statuspage"`
	Updates        []StatuspageNoticeUpdate `gorm:"constraint:OnDelete:CASCADE;foreignKey:NoticeID" json:"updates"`
	Incidents      []Incident               `gorm:"many2many:statuspages_notices_incidents;constraint:OnDelete:CASCADE;" json:"incidents"`
}

type StatuspageNoticeUpdate struct {
	BaseModel
	Message  string                 `gorm:"not null" json:"message"`
	Status   StatuspageNoticeStatus `gorm:"not null" json:"status"`
	NoticeID uint                   `gorm:"not null" json:"noticeId"`
	Notice   StatuspageNotice       `gorm:"foreignKey:NoticeID" json:"notice"`
}

type StatuspageNoticeType uint

const (
	Issue StatuspageNoticeStatus = iota
	Maintenance
	Announcement
)

type StatuspageNoticeSeverity uint

const (
	None StatuspageNoticeSeverity = iota
	Degraded
	PartialOutage
	MajorOutage
)

type StatuspageNoticeStatus uint

const (
	Scheduled StatuspageNoticeStatus = iota
	Started
	Investigating
	Identified
	Monitoring
	Resolved
	Completed
)
