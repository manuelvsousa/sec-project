package pt.ulisboa.tecnico.sec.notaryclient;

public class Application {
    public static void main(String[] args) throws Exception {
        NotaryClient nc = new NotaryClient();
        //System.out.print(nc.getStateOfGood("good12").getOwnerID());

        //nc.intentionToSell("good1","user1");
        nc.transferGood("good1", "user2", "user1");
    }
}
