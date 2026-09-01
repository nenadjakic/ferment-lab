package com.github.nenadjakic.fermentlab.scheduler

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = ["com.github.nenadjakic.fermentlab"])
@EntityScan(basePackages = ["com.github.nenadjakic.fermentlab.data.entity"])
@EnableJpaRepositories(basePackages = ["com.github.nenadjakic.fermentlab.data.repository"])
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
