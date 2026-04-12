package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public record Field(int row, int col, int box, Set<Integer> candidates) {

    public void excludeValueFromRowColAndBox(int value, Field other) {
        if (other.equals(this))
            return;
        if (other.row() == row || other.col() == col || other.box() == box) {
            candidates.remove(value);
            if (candidates.isEmpty()) {throw new IllegalArgumentException("No solution possible");}
        }
    }

    public int countCandidates() {
        return candidates.size();
    }

    public int getSolution(){
        return candidates.size() == 1
                ? candidates.stream().findFirst().get()
                : 0;

    }

    public void setValue(int value){
        candidates.clear();
        candidates.add(value);
    }

    public List<Integer> getCandidates(){
        return new ArrayList<>(candidates);
    }

    public boolean hasCandidate(int value){
        return candidates.contains(value);
    }

}
