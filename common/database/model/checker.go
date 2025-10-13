package model

type Checker struct {
	BaseModel
	Name       string      `gorm:"not null" json:"name"`
	Location   string      `json:"location"`
	Key        string      `gorm:"not null;unique" json:"key,omitempty"`
	System     bool        `gorm:"not null;default:false" json:"system"`
	Heartbeats []Heartbeat `gorm:"constraint:OnDelete:CASCADE;" json:"heartbeats"`
	Targets    []Target    `gorm:"many2many:targets_checkers;constraint:OnDelete:CASCADE;" json:"targets"`
}
