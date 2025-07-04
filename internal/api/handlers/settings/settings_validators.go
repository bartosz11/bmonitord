package settings

var modifiableSettings = []string{
	"registration-enabled",
}

func isModifiable(key string) bool {
	for _, setting := range modifiableSettings {
		if key == setting {
			return true
		}
	}
	return false
}

func validateSetting(request ChangeSettingRequest) bool {
	switch request.Key {
	case "registration-enabled": // There's probably a better way for doing this instead of switch-cases
		value := *request.Value
		if value == "true" || value == "false" {
			return true
		}
		break
	}
	return false
}
