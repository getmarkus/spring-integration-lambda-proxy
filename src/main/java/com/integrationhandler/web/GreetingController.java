package com.integrationhandler.web;

import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.integrationhandler.domain.GreetingMessage;

@RestController
@RequestMapping("/greeting")
public class GreetingController {

    @RequestMapping(method = RequestMethod.POST, produces="application/json")
    @ServiceActivator(inputChannel="POST-greeting-activator")
    public GreetingMessage reply(@RequestBody GreetingMessage greeting) {
        
        greeting.setMessage("Well...hello there");
        return greeting;
    }
}