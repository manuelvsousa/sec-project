package pt.ulisboa.tecnico.sec.model;

public class Status {
    private User owner;
    private Boolean onSale;

    public Status(User user, Boolean onSale) {
        this.owner = user;
        this.onSale = onSale;
    }

    public User getOwner() {
        return owner;
    }

    public Boolean getOnSale() {
        return onSale;
    }
}
