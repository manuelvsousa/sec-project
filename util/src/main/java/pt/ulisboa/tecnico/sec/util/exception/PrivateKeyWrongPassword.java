package pt.ulisboa.tecnico.sec.util.exception;

public class PrivateKeyWrongPassword extends RuntimeException {
    public PrivateKeyWrongPassword() {
        super("Private Key Wrong Password");
    }
}
