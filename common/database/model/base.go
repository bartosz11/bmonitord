package model

import (
	"gorm.io/gorm"
	"time"
)

// BaseModel is an equivalent of gorm.Model with custom JSON tags in order to lowercase the ID/Created/Modified/Deleted fields
type BaseModel struct {
	ID        uint           `gorm:"primaryKey" json:"id"`
	CreatedAt time.Time      `json:"createdAt"`
	UpdatedAt time.Time      `json:"updatedAt"`
	DeletedAt gorm.DeletedAt `gorm:"index" json:"deletedAt"`
}
