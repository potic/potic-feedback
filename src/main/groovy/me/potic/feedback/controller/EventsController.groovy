package me.potic.feedback.controller

import groovy.util.logging.Slf4j
import me.potic.feedback.domain.ArticleEvent
import me.potic.feedback.service.EventsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@Slf4j
class EventsController {

    @Autowired
    EventsService eventsService

    @CrossOrigin
    @PostMapping(path = '/event')
    @ResponseBody ResponseEntity<Void> storeNewEvent(@RequestBody ArticleEvent articleEvent) {
        log.info "receive POST request for /event; body=${articleEvent}"

        try {
            eventsService.store(articleEvent)
            return new ResponseEntity<>(HttpStatus.OK)
        } catch (e) {
            log.error "POST request for /event; body=${articleEvent} failed: $e.message", e
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}
