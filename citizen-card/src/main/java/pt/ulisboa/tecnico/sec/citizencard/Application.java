package pt.ulisboa.tecnico.sec.citizencard;

public class Application {
    public static void main(String[] args) throws Exception {
        CitizenCard cc = CitizenCard.getInstance();
        cc.sign();
    }
}
