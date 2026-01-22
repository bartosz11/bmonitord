package model

type StatuspageTarget struct {
	BaseModel
	Position          float64          `gorm:"not null" json:"position"`
	StatuspageGroupID *uint            `json:"statuspageGroupId"`
	StatuspageGroup   *StatuspageGroup `json:"statuspageGroup"`
	StatuspageID      uint             `gorm:"not null" json:"statuspageId"`
	Statuspage        Statuspage       `gorm:"foreignKey:StatuspageID" json:"statuspage"`
	TargetID          uint             `gorm:"not null" json:"targetId"`
	Target            Target           `gorm:"foreignKey:TargetID" json:"target"`
}
