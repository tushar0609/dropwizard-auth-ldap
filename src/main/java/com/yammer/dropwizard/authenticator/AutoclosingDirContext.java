package com.yammer.dropwizard.authenticator;

import java.util.Hashtable;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;

public class AutoclosingDirContext extends InitialDirContext implements AutoCloseable {

    protected AutoclosingDirContext(boolean lazy) throws NamingException {
        super(lazy);
    }

    public AutoclosingDirContext() throws NamingException {
        super();
    }

    public AutoclosingDirContext(Hashtable<?, ?> environment) throws NamingException {
        super(environment);
    }

    @Override
    public void close() throws NamingException {
        super.close();
    }
}
