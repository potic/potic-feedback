package me.potic.feedback.domain

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode
@ToString(includeNames = true)
class Model {

    String id

    String name

    String version

    String description

    String trainTimestamp

    boolean isActive
}
