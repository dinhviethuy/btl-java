package com.fullnestjob.modules.mail.controller;

import com.fullnestjob.common.response.Message;
import com.fullnestjob.modules.mail.service.SharedMailService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/mail")
public class MailController {

    private final SharedMailService sharedMailService;

    public MailController(SharedMailService sharedMailService) {
        this.sharedMailService = sharedMailService;
    }

    @GetMapping
    public void sendMail() {
        sharedMailService.sendToAllSubscribers();
    }

    @PostMapping("/send-mail")
    @Message("Send mail to user")
    public Object sendMailToUser() {
        return sharedMailService.sendEmailTestForActiveUser();
    }
}


