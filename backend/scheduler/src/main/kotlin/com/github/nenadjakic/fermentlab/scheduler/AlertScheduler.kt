package com.github.nenadjakic.fermentlab.scheduler

import com.github.nenadjakic.fermentlab.service.AlertEvaluationService
import com.github.nenadjakic.fermentlab.service.AlertService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * Runs the alert rules on a schedule.
 *
 * The evaluation itself is idempotent, so a missed run simply catches up on the next tick and a
 * duplicated run does not duplicate alerts.
 */
@Component
class AlertScheduler(
    private val alertEvaluationService: AlertEvaluationService,
    private val alertService: AlertService,

    /**
     * How long an acknowledged alert is kept before it is purged.
     */
    @Value("\${fermentlab.scheduler.alert-retention-days:30}")
    private val alertRetentionDays: Long
) {

    private val logger = LoggerFactory.getLogger(AlertScheduler::class.java)

    /**
     * Evaluates the alert rules. Cron comes from `fermentlab.scheduler.cron`, every 30 minutes by
     * default.
     */
    @Scheduled(cron = "\${fermentlab.scheduler.cron}")
    fun evaluateAlerts() {
        logger.debug("Starting scheduled alert evaluation.")
        alertEvaluationService.evaluate()
    }

    /**
     * Drops acknowledged alerts that have aged out, daily at 03:15.
     */
    @Scheduled(cron = "0 15 3 * * *")
    fun purgeAcknowledgedAlerts() {
        val purged = alertService.purgeAcknowledged(alertRetentionDays)
        if (purged > 0) {
            logger.info("Purged {} acknowledged alert(s) older than {} days.", purged, alertRetentionDays)
        }
    }
}
