package com.svivanrilski.svirerp.settings;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Restricted to ROLE_ADMIN (local admin only) in SecurityConfig — Google-
 * authenticated users never get that role, so they're excluded automatically.
 */
@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private static final String SECRET_TYPE = "SECRET";

    private final AppSettingService service;

    @GetMapping
    public List<SettingResponse> list() {
        return service.list().stream().map(SettingResponse::from).toList();
    }

    @PutMapping("/{key}")
    public SettingResponse update(@PathVariable String key, @RequestBody UpdateSettingRequest request) {
        AppSetting updated = service.updateValue(key, request.value());
        return SettingResponse.from(updated);
    }

    public record UpdateSettingRequest(String value) {
    }

    /** value is always null for SECRET settings — hasValue tells the UI whether one is configured. */
    public record SettingResponse(String key, String value, String valueType, String description, boolean hasValue) {
        static SettingResponse from(AppSetting setting) {
            boolean isSecret = SECRET_TYPE.equals(setting.getValueType());
            boolean hasValue = setting.getValue() != null && !setting.getValue().isBlank();
            return new SettingResponse(
                    setting.getKey(),
                    isSecret ? null : setting.getValue(),
                    setting.getValueType(),
                    setting.getDescription(),
                    hasValue);
        }
    }
}
