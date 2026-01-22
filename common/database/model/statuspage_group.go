package model

type StatuspageGroup struct {
	BaseModel
	Name         string             `gorm:"not null" json:"name"`
	Description  string             `json:"description"`
	Position     float64            `gorm:"not null" json:"position"`
	StatuspageID uint               `gorm:"not null" json:"statuspageId"`
	Statuspage   Statuspage         `gorm:"foreignKey:StatuspageID" json:"statuspage"`
	Targets      []StatuspageTarget `gorm:"constraint:OnDelete:CASCADE;" json:"targets"`
}
