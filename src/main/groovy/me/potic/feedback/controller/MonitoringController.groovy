package me.potic.feedback.controller

import groovy.util.logging.Slf4j
import me.potic.feedback.service.EventsService
import me.potic.feedback.service.MonitoringService
import me.potic.feedback.service.TableService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletResponse

@RestController
@Slf4j
class MonitoringController {

    @Autowired
    EventsService eventsService

    @Autowired
    MonitoringService monitoringService

    @Autowired
    TableService tableService

    @CrossOrigin
    @GetMapping(path = '/monitor/events')
    void monitorLatestEvents(
            @RequestParam(value = 'count', required = false) Integer count,
            HttpServletResponse response
    ) {
        log.debug "receive GET request for /monitor/events?count=${count}"

        try {
            response.outputStream.withPrintWriter { writer ->
                tableService.table(monitoringService.monitorLatestEvents(count?:100)).forEach({
                    writer.write(it)
                    writer.write('\n')
                })
            }
        } catch (e) {
            log.error "GET request for /monitor/events?count=${count} failed: $e.message", e
            throw new RuntimeException("GET request for /monitor/events?count=${count} failed: $e.message", e)
        }
    }

    @CrossOrigin
    @GetMapping(path = '/monitor/articles')
    void monitorLatestArticles(
            @RequestParam(value = 'count', required = false) Integer count,
            HttpServletResponse response
    ) {
        log.debug "receive GET request for /monitor/articles?count=${count}"

        try {
            response.outputStream.withPrintWriter { writer ->
                tableService.table(monitoringService.monitorLatestArticles(count?:100)).forEach({
                    writer.write(it)
                    writer.write('\n')
                })
            }
        } catch (e) {
            log.error "GET request for /monitor/articles?count=${count} failed: $e.message", e
            throw new RuntimeException("GET request for /monitor/articles?count=${count} failed: $e.message", e)
        }
    }
}
