package com.github.nenadjakic.fermentlab.scheduler

import com.github.nenadjakic.fermentlab.service.AlertEvaluationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Manual trigger for the scheduled jobs, so the rules can be exercised without waiting for the cron.
 */
@RestController
@RequestMapping("/scheduler")
class SchedulerController(
    private val alertEvaluationService: AlertEvaluationService
) {

    /**
     * Runs the alert rules now.
     *
     * @return how many alerts were raised or refreshed.
     */
    @PostMapping("/evaluate-alerts")
    fun evaluateAlerts(): ResponseEntity<Map<String, Int>> =
        ResponseEntity.ok(mapOf("raised" to alertEvaluationService.evaluate()))
}
