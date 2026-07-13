package com.mesh_suite.controller.notify;

import com.mesh_suite.service.notify.NotificationScheduler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scheduler")
public class SchedulerController {

    private final NotificationScheduler scheduler;

    public SchedulerController(NotificationScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @PostMapping("/trigger")
    public ResponseEntity<Void> triggerScheduler() {
        scheduler.triggerScheduledNotifications();
        return ResponseEntity.ok().build();
    }
}
