package helpers

import (
	"errors"
	"fmt"
	"strconv"
	"strings"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// I really like how Spring handles pagination, so I tried my best to recreate it

// Pageable holds page/size/sort settings
type Pageable struct {
	Page int
	Size int
	Sort []SortOrder
}

// SortOrder represents one sorting expression
type SortOrder struct {
	Field     string
	Direction string // asc or desc
}

// Page is the paginated result
type Page[T any] struct {
	Content       []T `json:"content"`
	Page          int `json:"page"`
	Size          int `json:"size"`
	TotalElements int `json:"totalElements"`
	TotalPages    int `json:"totalPages"`
}

func ParsePageable(c *gin.Context, allowedSortFields []string) (Pageable, error) {
	page, _ := strconv.Atoi(c.DefaultQuery("page", "0"))
	size, _ := strconv.Atoi(c.DefaultQuery("size", "20"))

	if page < 0 {
		page = 0
	}
	if size <= 0 {
		size = 20
	}
	if size > 200 {
		size = 200 // hard limit of 200, paging exists for a reason I guess
	}

	sortParams := c.QueryArray("sort")
	var sortOrders []SortOrder

	allowed := map[string]bool{}
	for _, f := range allowedSortFields {
		allowed[f] = true
	}

	for _, s := range sortParams {
		parts := strings.Split(s, ",")
		if len(parts) != 2 {
			return Pageable{}, errors.New("invalid sort syntax")
		}

		field := parts[0]
		dir := strings.ToLower(parts[1])
		if dir != "asc" && dir != "desc" {
			return Pageable{}, errors.New("invalid sort direction")
		}

		if !allowed[field] {
			return Pageable{}, fmt.Errorf("sorting by '%s' is not allowed", field)
		}

		sortOrders = append(sortOrders, SortOrder{
			Field:     field,
			Direction: dir,
		})
	}

	return Pageable{
		Page: page,
		Size: size,
		Sort: sortOrders,
	}, nil
}

func ApplyPageable[T any](db *gorm.DB, pageable Pageable) (Page[T], error) {
	var total int64
	// count total rows
	if err := db.Count(&total).Error; err != nil {
		return Page[T]{}, err
	}

	// apply the sorting (yes it's safe, check the parsing function)
	for _, s := range pageable.Sort {
		db = db.Order(fmt.Sprintf("%s %s", s.Field, s.Direction))
	}

	// apply the paging
	offset := pageable.Page * pageable.Size
	db = db.Offset(offset).Limit(pageable.Size)

	// fetch
	var dest []T
	if err := db.Find(&dest).Error; err != nil {
		return Page[T]{}, err
	}

	return Page[T]{
		Content:       dest,
		Page:          pageable.Page,
		Size:          pageable.Size,
		TotalElements: int(total),
		// rounding up trick
		TotalPages: int((total + int64(pageable.Size) - 1) / int64(pageable.Size)),
	}, nil
}
