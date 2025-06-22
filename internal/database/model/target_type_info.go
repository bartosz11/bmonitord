package model

import "gorm.io/gorm"

type TargetPingInfo struct {
	gorm.Model
	Host     string `gorm:"not null"`
	TargetID uint   `gorm:"not null"`
}

type TargetHTTPInfo struct {
	gorm.Model
	Host            string `gorm:"not null"`
	AllowedCodes    string `gorm:"not null"`
	FollowRedirects bool   `gorm:"not null;default:false"`
	VerifySSLCert   bool   `gorm:"not null;default:false"`
	TargetID        uint   `gorm:"not null"`
}
