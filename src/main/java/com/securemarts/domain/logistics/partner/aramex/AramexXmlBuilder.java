package com.securemarts.domain.logistics.partner.aramex;

import org.springframework.util.StringUtils;

import java.math.BigDecimal;

/**
 * Builds Aramex API XML payloads. Adapt to actual Aramex Shipments Preparation API schema from their docs.
 * @see <a href="https://www.aramex.com/content/uploads/109/232/42024/shipments-preparation-api-manual.pdf">Shipments Preparation API Manual</a>
 */
public final class AramexXmlBuilder {

    private AramexXmlBuilder() {}

    public static String buildCreateShipmentRequest(AramexShipmentRequest r) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:arr=\"http://ws.aramex.net/ShippingAPI/v1/\">");
        sb.append("<soap:Body>");
        sb.append("<arr:CreateShipments>");
        sb.append("<arr:ClientInfo>");
        sb.append("<arr:UserName>").append(escape(r.getUsername())).append("</arr:UserName>");
        sb.append("<arr:Password>").append(escape(r.getPassword())).append("</arr:Password>");
        sb.append("<arr:AccountNumber>").append(escape(r.getAccountNumber())).append("</arr:AccountNumber>");
        sb.append("<arr:AccountEntity>").append(escape(r.getAccountEntity())).append("</arr:AccountEntity>");
        sb.append("<arr:AccountCountryCode>").append(escape(r.getAccountCountryCode())).append("</arr:AccountCountryCode>");
        sb.append("</arr:ClientInfo>");
        sb.append("<arr:Transaction><arr:Reference1>").append(escape(r.getReference())).append("</arr:Reference1></arr:Transaction>");
        sb.append("<arr:Shipments>");
        sb.append("<arr:Shipment>");
        sb.append("<arr:Shipper><arr:Reference1/>");
        sb.append("<arr:Address><arr:Line1>").append(escape(r.getPickupAddress())).append("</arr:Line1></arr:Address>");
        sb.append("</arr:Shipper>");
        sb.append("<arr:Consignee><arr:Reference1/>");
        sb.append("<arr:Address><arr:Line1>").append(escape(r.getDeliveryAddress())).append("</arr:Line1></arr:Address>");
        sb.append("</arr:Consignee>");
        sb.append("<arr:Details><arr:ActualWeight><arr:Value>").append(r.getWeightKg() != null ? r.getWeightKg() : "1").append("</arr:Value><arr:Unit>KG</arr:Unit></arr:ActualWeight>");
        sb.append("<arr:Description>").append(escape(r.getParcelDescription())).append("</arr:Description></arr:Details>");
        sb.append("</arr:Shipment>");
        sb.append("</arr:Shipments>");
        sb.append("</arr:CreateShipments>");
        sb.append("</soap:Body></soap:Envelope>");
        return sb.toString();
    }

    public static String buildTrackShipmentRequest(String shipmentNumber, String username, String password,
                                                   String accountNumber, String accountEntity, String accountCountryCode) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:arr=\"http://ws.aramex.net/ShippingAPI/v1/\">");
        sb.append("<soap:Body>");
        sb.append("<arr:TrackShipments>");
        sb.append("<arr:ClientInfo>");
        sb.append("<arr:UserName>").append(escape(username)).append("</arr:UserName>");
        sb.append("<arr:Password>").append(escape(password)).append("</arr:Password>");
        sb.append("<arr:AccountNumber>").append(escape(accountNumber)).append("</arr:AccountNumber>");
        sb.append("<arr:AccountEntity>").append(escape(accountEntity)).append("</arr:AccountEntity>");
        sb.append("<arr:AccountCountryCode>").append(escape(accountCountryCode)).append("</arr:AccountCountryCode>");
        sb.append("</arr:ClientInfo>");
        sb.append("<arr:Shipments><arr:string>").append(escape(shipmentNumber)).append("</arr:string></arr:Shipments>");
        sb.append("</arr:TrackShipments>");
        sb.append("</soap:Body></soap:Envelope>");
        return sb.toString();
    }

    public static String buildCancelShipmentRequest(String shipmentNumber, String username, String password,
                                                    String accountNumber, String accountEntity, String accountCountryCode) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:arr=\"http://ws.aramex.net/ShippingAPI/v1/\">");
        sb.append("<soap:Body>");
        sb.append("<arr:CancelShipment>");
        sb.append("<arr:ClientInfo>");
        sb.append("<arr:UserName>").append(escape(username)).append("</arr:UserName>");
        sb.append("<arr:Password>").append(escape(password)).append("</arr:Password>");
        sb.append("<arr:AccountNumber>").append(escape(accountNumber)).append("</arr:AccountNumber>");
        sb.append("<arr:AccountEntity>").append(escape(accountEntity)).append("</arr:AccountEntity>");
        sb.append("<arr:AccountCountryCode>").append(escape(accountCountryCode)).append("</arr:AccountCountryCode>");
        sb.append("</arr:ClientInfo>");
        sb.append("<arr:ShipmentNumber>").append(escape(shipmentNumber)).append("</arr:ShipmentNumber>");
        sb.append("</arr:CancelShipment>");
        sb.append("</soap:Body></soap:Envelope>");
        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
