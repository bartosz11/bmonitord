package model

type Orchestrator struct {
	BaseModel
	Name   string `gorm:"not null;unique" json:"name"`
	Host   string `gorm:"not null;unique" json:"host"`
	Leader bool   `gorm:"default:false" json:"leader"`
	System bool   `gorm:"not null;default:false" json:"system"`
}
