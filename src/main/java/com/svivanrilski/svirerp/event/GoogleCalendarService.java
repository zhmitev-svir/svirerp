package com.svivanrilski.svirerp.event;

import com.svivanrilski.svirerp.common.GoogleOAuthTokenService;
import com.svivanrilski.svirerp.settings.AppSettingService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One-way push of a CalendarEvent to the official/internal Google Calendars.
 * The ERP is the system of record; this never pulls changes back from
 * Google. Every public method is best-effort — failures are recorded onto
 * the entity's sync columns (see the migration adding them) and never
 * thrown, so a Google outage never blocks saving/editing/deleting an event
 * locally. No Google API client library is used, consistent with the rest
 * of this app's Google integrations (see EmailService) — plain REST calls
 * against the Calendar API v3.
 */
@Service
@RequiredArgsConstructor
public class GoogleCalendarService {

    private static final Logger log = LoggerFactory.getLogger(GoogleCalendarService.class);

    private static final String EVENTS_BASE = "https://www.googleapis.com/calendar/v3/calendars";
    private static final String CLIENT_ID_KEY = "calendar.oauth.client-id";
    private static final String CLIENT_SECRET_KEY = "calendar.oauth.client-secret";
    private static final String REFRESH_TOKEN_KEY = "calendar.oauth.refresh-token";

    private final AppSettingService settingService;
    private final GoogleOAuthTokenService tokenService;
    private final RestClient restClient = RestClient.create();

    /** Pushes to whichever of official/internal are checked; unpublishes whichever were unchecked. Never throws. */
    public void syncToGoogle(CalendarEvent event) {
        syncCalendar(event, true);
        syncCalendar(event, false);
    }

    /** Best-effort delete of any Google-side copies before the local row is removed. Never throws. */
    public void deleteFromGoogle(CalendarEvent event) {
        deleteOne(event.getGoogleOfficialEventId(), "calendar.official.id");
        deleteOne(event.getGoogleInternalEventId(), "calendar.internal.id");
    }

    private void syncCalendar(CalendarEvent event, boolean official) {
        boolean publish = Boolean.TRUE.equals(official ? event.getPublishToOfficial() : event.getPublishToInternal());
        String existingId = official ? event.getGoogleOfficialEventId() : event.getGoogleInternalEventId();
        String calendarIdKey = official ? "calendar.official.id" : "calendar.internal.id";
        String label = official ? "official" : "internal";

        try {
            if (!publish) {
                if (existingId != null) {
                    String calendarId = settingService.getDecryptedValue(calendarIdKey).orElse(null);
                    if (calendarId != null) {
                        deleteEventFromCalendar(calendarId, existingId);
                    }
                }
                setResult(event, official, null, null);
                return;
            }

            String calendarId = settingService.getDecryptedValue(calendarIdKey)
                    .orElseThrow(() -> new IllegalStateException(
                            (official ? "Official" : "Internal") + " calendar ID is not configured"));

            String accessToken = tokenService.getAccessToken(CLIENT_ID_KEY, CLIENT_SECRET_KEY, REFRESH_TOKEN_KEY);
            Map<String, Object> body = buildEventBody(event);

            Map<?, ?> response = existingId == null
                    ? restClient.post()
                        .uri(EVENTS_BASE + "/{calendarId}/events", calendarId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .body(body)
                        .retrieve()
                        .body(Map.class)
                    : restClient.patch()
                        .uri(EVENTS_BASE + "/{calendarId}/events/{eventId}", calendarId, existingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .body(body)
                        .retrieve()
                        .body(Map.class);

            setResult(event, official, (String) response.get("id"), null);
        } catch (Exception e) {
            log.warn("Google Calendar sync failed for event {} ({}): {}", event.getId(), label, e.getMessage());
            setResult(event, official, existingId, e.getMessage());
        }
    }

    private void deleteOne(String eventId, String calendarIdKey) {
        if (eventId == null) return;
        try {
            String calendarId = settingService.getDecryptedValue(calendarIdKey).orElse(null);
            if (calendarId == null) return;
            deleteEventFromCalendar(calendarId, eventId);
        } catch (Exception e) {
            log.warn("Failed to delete Google Calendar event {}: {}", eventId, e.getMessage());
        }
    }

    private void deleteEventFromCalendar(String calendarId, String eventId) {
        String accessToken = tokenService.getAccessToken(CLIENT_ID_KEY, CLIENT_SECRET_KEY, REFRESH_TOKEN_KEY);
        restClient.delete()
                .uri(EVENTS_BASE + "/{calendarId}/events/{eventId}", calendarId, eventId)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .toBodilessEntity();
    }

    private Map<String, Object> buildEventBody(CalendarEvent event) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("summary", event.getTitle());
        if (event.getDescription() != null) body.put("description", event.getDescription());
        if (event.getLocation() != null) body.put("location", event.getLocation());

        if (Boolean.TRUE.equals(event.getIsAllDay())) {
            LocalDate startDate = event.getStartDatetime().toLocalDate();
            // Google Calendar's all-day end.date is exclusive (the day after the last day).
            LocalDate endDate = event.getEndDatetime() != null
                    ? event.getEndDatetime().toLocalDate().plusDays(1)
                    : startDate.plusDays(1);
            body.put("start", Map.of("date", startDate.toString()));
            body.put("end", Map.of("date", endDate.toString()));
        } else {
            OffsetDateTime end = event.getEndDatetime() != null ? event.getEndDatetime() : event.getStartDatetime();
            body.put("start", Map.of("dateTime", event.getStartDatetime().toString()));
            body.put("end", Map.of("dateTime", end.toString()));
        }
        return body;
    }

    private void setResult(CalendarEvent event, boolean official, String eventId, String error) {
        if (official) {
            event.setGoogleOfficialEventId(eventId);
            event.setGoogleOfficialSyncError(error);
        } else {
            event.setGoogleInternalEventId(eventId);
            event.setGoogleInternalSyncError(error);
        }
    }
}
