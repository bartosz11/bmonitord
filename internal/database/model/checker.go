package model

import "gorm.io/gorm"

type Checker struct {
	gorm.Model
	Name       string `gorm:"not null"`
	Location   string
	Key        string      `gorm:"not null;unique" json:"Key,omitempty"`
	Heartbeats []Heartbeat `gorm:"constraint:OnDelete:CASCADE;"`
	Targets    []Target    `gorm:"many2many:targets_checkers;constraint:OnDelete:CASCADE;"`
}
