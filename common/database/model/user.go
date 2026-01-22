package model

type User struct {
	BaseModel
	Username      string         `gorm:"not null;unique" json:"username"`
	Password      string         `gorm:"not null" json:"-"`
	Enabled       bool           `gorm:"not null;default:true" json:"enabled"`
	Admin         bool           `gorm:"not null;default:false" json:"admin"`
	Targets       []Target       `gorm:"constraint:OnDelete:CASCADE;" json:"targets"`
	Notifications []Notification `gorm:"constraint:OnDelete:CASCADE;" json:"notifications"`
	Sessions      []Session      `gorm:"constraint:OnDelete:CASCADE;" json:"sessions"`
	Statuspages   []Statuspage   `gorm:"constraint:OnDelete:CASCADE;" json:"statuspages"`
}
