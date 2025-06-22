package model

import "gorm.io/gorm"

type Orchestrator struct {
	gorm.Model
	Name   string `gorm:"not null;unique"`
	Host   string `gorm:"not null;unique"`
	Leader bool   `gorm:"default:false"`
}
