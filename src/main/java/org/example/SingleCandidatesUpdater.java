package org.example;

public class SingleCandidatesUpdater extends Strategy {


    public SingleCandidatesUpdater() {
        nextStrategy = null;
    }

    @Override
    public boolean isApplicable() {
        return false;
    }

    @Override
    public void execute() {

    }
}
