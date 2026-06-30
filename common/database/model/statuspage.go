package model

type Statuspage struct {
	BaseModel
	Name                   string             `gorm:"not null" json:"name"`
	Slug                   string             `gorm:"not null;unique" json:"slug"`
	Title                  string             `gorm:"not null" json:"title"`
	Description            string             `json:"description"`
	LogoURL                string             `json:"logoURL"`
	TitleSectionOnClickURL string             `json:"titleSectionOnClickURL"`
	FooterContent          string             `json:"footerContent"`
	DisplayCheckmateFooter bool               `gorm:"not null;default:true" json:"displayCheckmateFooter"`
	UserID                 uint               `gorm:"not null" json:"userId"`
	Domains                []StatuspageDomain `gorm:"constraint:OnDelete:CASCADE;" json:"domains"`
	Notices                []StatuspageNotice `gorm:"constraint:OnDelete:CASCADE;" json:"notices"`
	Groups                 []StatuspageGroup  `gorm:"constraint:OnDelete:CASCADE;" json:"groups"`
	Targets                []StatuspageTarget `gorm:"constraint:OnDelete:CASCADE;" json:"targets"`
}
