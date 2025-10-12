package model

type Setting struct {
	Key   string  `gorm:"primaryKey" json:"key"`
	Value *string `json:"value"`
}
