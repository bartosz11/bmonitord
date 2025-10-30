package model

import (
	"bytes"
	"compress/gzip"
	"database/sql/driver"
	"encoding/json"
	"fmt"
	"io"
)

type HeartbeatPayload struct {
	// All TargetTypes are allowed to submit further details
	Type TargetType `json:"type"`
	// Each Type of payload can have their own versioning
	Version uint           `json:"version"`
	Data    map[string]any `json:"data,omitempty"`
}

// Value allows storing HeartbeatPayload as gzipped JSON using PostgreSQL's bytea type
func (payload *HeartbeatPayload) Value() (driver.Value, error) {
	if payload == nil {
		return nil, nil
	}

	jsonBytes, err := json.Marshal(payload)
	if err != nil {
		return nil, err
	}

	var buf bytes.Buffer
	gz := gzip.NewWriter(&buf)
	if _, err = gz.Write(jsonBytes); err != nil {
		return nil, err
	}
	if err = gz.Close(); err != nil {
		return nil, err
	}

	return buf.Bytes(), nil
}

// Scan allows reading HeartbeatPayloads from the database
func (payload *HeartbeatPayload) Scan(src any) error {
	if src == nil {
		*payload = HeartbeatPayload{}
		return nil
	}

	data, ok := src.([]byte)
	if !ok {
		return fmt.Errorf("HeartbeatPayload: expected []byte, got %T", src)
	}

	gr, err := gzip.NewReader(bytes.NewReader(data))
	if err != nil {
		return err
	}
	defer gr.Close()

	decompressed, err := io.ReadAll(gr)
	if err != nil {
		return err
	}

	return json.Unmarshal(decompressed, payload)
}
