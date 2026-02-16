package com.shopper.mail.zeptomail;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Request body for Zeptomail API POST /v1.1/email
 * https://www.zoho.com/zeptomail/help/api/email-sending.html
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ZeptomailRequest {

    private From from;
    private List<ToEntry> to;
    private String subject;
    private String htmlbody;
    private String textbody;

    @Data
    @Builder
    public static class From {
        private String address;
        private String name;
    }

    @Data
    @Builder
    public static class ToEntry {
        private EmailAddress email_address;
    }

    @Data
    @Builder
    public static class EmailAddress {
        private String address;
        private String name;
    }
}
