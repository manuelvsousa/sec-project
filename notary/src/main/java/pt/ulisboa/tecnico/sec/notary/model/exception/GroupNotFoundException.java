package pt.ulisboa.tecnico.sec.notary.model.exception;

public class GroupNotFoundException extends RuntimeException {
    public GroupNotFoundException(String group) {
        super("Group " + group + " Not Found");
    }
}
