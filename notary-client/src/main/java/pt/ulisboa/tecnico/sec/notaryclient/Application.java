package pt.ulisboa.tecnico.sec.notaryclient;

public class Application {
    public static void main(String[] args) throws Exception {
        NotaryClient nc = new NotaryClient();
        System.out.print(nc.getStateOfGood("good1").getOwnerID());
//        System.out.print(nc.getStateOfGood("good2").getOwnerID());
    }
}
