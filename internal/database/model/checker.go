package model

import "gorm.io/gorm"

type Checker struct {
	gorm.Model
	Name       string `gorm:"not null"`
	Location   string
	Key        string `gorm:"not null"`
	Heartbeats []Heartbeat
	Targets    []Target `gorm:"many2many:targets_checkers;"`
}
