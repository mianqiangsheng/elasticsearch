package com.lizhen.elasticsearch.controller;

import org.apache.coyote.Response;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/es")
public class ElasticSearchController {

    @RequestMapping(value = "/getIndex", method = RequestMethod.GET)
    public Response getIndex(){
        Response response = new Response();
        response.setStatus(200);
        response.setMessage("成功");
        return response;
    }
}
