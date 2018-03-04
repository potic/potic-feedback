package me.potic.feedback.controller

import groovy.util.logging.Slf4j
import me.potic.feedback.domain.ArticleEvent
import me.potic.feedback.service.FeedbackService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@Slf4j
class EventsController {

    @Autowired
    FeedbackService feedbackService

    @CrossOrigin
    @PostMapping(path = '/event')
    @ResponseBody ResponseEntity<Void> event(@RequestBody ArticleEvent articleEvent) {
        log.info "receive POST request for /event; body=${articleEvent}"

        try {
            feedbackService.store(articleEvent)
            return new ResponseEntity<>(HttpStatus.OK)
        } catch (e) {
            log.error "POST request for /event; body=${articleEvent} failed: $e.message", e
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}
