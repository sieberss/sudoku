package org.example;

import java.util.List;

public abstract class Strategy {

    Strategy nextStrategy;

    public abstract boolean isApplicable();
    public abstract void execute();

    public void check() {
        if (isApplicable())
            execute();
        else nextStrategy.check();
    }

}
