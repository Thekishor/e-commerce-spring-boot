package common.events.kafkaEvents;

import java.util.List;

public class OrderEvent {

    private String reference;
    private String orderNumber;
    private Long amount;
    private String paymentMethod;
    private String username;
    private String email;
    private List<PurchaseResponse> purchaseResponseList;

    public OrderEvent() {
        
    }

    public OrderEvent(String reference, String orderNumber, Long amount, String paymentMethod, String username, String email, List<PurchaseResponse> purchaseResponseList) {
        this.reference = reference;
        this.orderNumber = orderNumber;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.username = username;
        this.email = email;
        this.purchaseResponseList = purchaseResponseList;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<PurchaseResponse> getPurchaseResponseList() {
        return purchaseResponseList;
    }

    public void setPurchaseResponseList(List<PurchaseResponse> purchaseResponseList) {
        this.purchaseResponseList = purchaseResponseList;
    }
}
