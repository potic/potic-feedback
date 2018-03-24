package me.potic.feedback.service

import groovy.util.logging.Slf4j
import org.springframework.web.bind.annotation.RestController

@RestController
@Slf4j
class TableService {

    List<String> table(List<List> data) {
        Map<Integer, Integer> columnWidth = [:]
        data.forEach({ List dataLine ->
            dataLine.withIndex().collect({ cell, index -> columnWidth.put(index, Math.max(columnWidth.getOrDefault(index, 0), cellToString(cell).length())) })
        })

        data.collect({ List dataLine ->
            dataLine.withIndex().collect({ cell, index -> cellToString(cell).padLeft(columnWidth.get(index) + 3)}).join(' ')
        })
    }

    String cellToString(Object cell) {
        if (cell instanceof Double || cell instanceof Float || cell instanceof BigDecimal) {
            return String.format(Locale.US, '%.4f', cell)
        }

        return cell.toString()
    }
}
