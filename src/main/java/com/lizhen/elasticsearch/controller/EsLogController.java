package com.lizhen.elasticsearch.controller;

import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/es/send")
@Api(value = "/es/send", tags = "发送日志")
public class EsLogController {

    private static final Logger LOGGER = LoggerFactory.getLogger(EsLogController.class);

    @RequestMapping(value = "/trace", method = RequestMethod.POST)
    public ResponseEntity<String> trace() {
        LOGGER.trace("This is a trace message" + " at " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return ResponseEntity.status(HttpStatus.OK).body("success");
    }

    @RequestMapping(value = "/error", method = RequestMethod.POST)
    public ResponseEntity<String> error() {
        LOGGER.error("This is a error message" + " at " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return ResponseEntity.status(HttpStatus.OK).body("success");
    }

    @RequestMapping(value = "/debug", method = RequestMethod.POST)
    public ResponseEntity<String> debug() {
        LOGGER.debug("This is a debug message" + " at " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return ResponseEntity.status(HttpStatus.OK).body("success");
    }

    @RequestMapping(value = "/info", method = RequestMethod.POST)
    public ResponseEntity<String> info() {
        LOGGER.info("This is a info message" + " at " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return ResponseEntity.status(HttpStatus.OK).body("success");
    }

    @RequestMapping(value = "/warn", method = RequestMethod.POST)
    public ResponseEntity<String> warn() {
        LOGGER.warn("This is a warn message" + " at " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return ResponseEntity.status(HttpStatus.OK).body("success");
    }

    @RequestMapping(value = "/throwException", method = RequestMethod.POST)
    public ResponseEntity<String> throwException() {
        int i = 1 / 0;
        return ResponseEntity.status(HttpStatus.OK).body("success");
    }

    @RequestMapping(value = "/catchException", method = RequestMethod.POST)
    public ResponseEntity<String> catchException() {
        try {
            throw new ClassNotFoundException();
        }catch (ClassNotFoundException e){
            LOGGER.error("This is a catchException message {}",e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body("success");
    }

}
