package ch.epfl.sweng.erpa.operations.helpers;

import ch.epfl.sweng.erpa.operations.annotations.Service;

@SuppressWarnings("unused")
public class TestDependencyClass {
    @Service public TestServiceInterface getInterface() {
        return null;
    }

    public Object method() {
        return null;
    }

    public interface TestServiceInterface {
        Object getObject();
    }
}
