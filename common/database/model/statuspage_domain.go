package model

type StatuspageDomain struct {
	// This object may seem useless but saves me from script-based migrations later when I might expand this
	BaseModel
	Domain       string     `gorm:"not null;unique" json:"domain"`
	StatuspageID uint       `gorm:"not null" json:"statuspageId"`
	Statuspage   Statuspage `gorm:"foreignKey:StatuspageID" json:"statuspage"`
}
