package me.potic.feedback.service

import spock.lang.Specification

class TableServiceTest extends Specification {

    def "List<String> table(List<List> data)"() {
        setup:
        def data = [ ['row1', 'row2'], [123, 'Hello'], [2828282828, 'World!'] ]
        TableService tableService = new TableService()

        when:
        def actual = tableService.table(data)

        then:
        actual == [ '         row1      row2',
                    '          123     Hello',
                    '   2828282828    World!' ]
    }

    def "List<String> table(List<List> data) - with double data"() {
        setup:
        def data = [ ['row1', 'row2'], [123, 'Hello'], [1.0/3, 'World!'] ]
        TableService tableService = new TableService()

        when:
        def actual = tableService.table(data)

        then:
        actual == [ '     row1      row2',
                    '      123     Hello',
                    '   0.3333    World!' ]
    }
}
