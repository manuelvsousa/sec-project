package pt.ulisboa.tecnico.sec.model.exception;

import javassist.NotFoundException;

public class GroupNotFoundException extends RuntimeException {
    public GroupNotFoundException() { super("Group Not Found"); }
}
