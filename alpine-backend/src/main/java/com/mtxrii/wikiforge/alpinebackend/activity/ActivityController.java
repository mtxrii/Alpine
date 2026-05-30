package com.mtxrii.wikiforge.alpinebackend.activity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.mtxrii.wikiforge.alpinebackend.activity.ActivityService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ActivityController {
    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping("/boards/{boardId}/activity")
    List<ActivityResponse> listByBoard(@PathVariable UUID boardId) {
        return activityService.listByBoard(boardId);
    }

    @GetMapping("/tickets/{ticketId}/activity")
    List<ActivityResponse> listByTicket(@PathVariable UUID ticketId) {
        return activityService.listByTicket(ticketId);
    }

    public record ActivityResponse(UUID id, UUID boardId, UUID ticketId, UUID userId,
                                   String userName, String action, String metadata, Instant createdAt) {
    }
}
