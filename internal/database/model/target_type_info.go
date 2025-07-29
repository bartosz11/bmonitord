package model

type TargetPingInfo struct {
	BaseModel
	Host     string `gorm:"not null" json:"host"`
	TargetID uint   `gorm:"not null" json:"targetId"`
}

type TargetHTTPInfo struct {
	BaseModel
	Host            string `gorm:"not null" json:"host"`
	AllowedCodes    string `gorm:"not null" json:"allowedCodes"`
	FollowRedirects bool   `gorm:"not null;default:false" json:"followRedirects"`
	VerifySSLCert   bool   `gorm:"not null;default:false" json:"verifySSLCert"`
	TargetID        uint   `gorm:"not null" json:"targetId"`
}
