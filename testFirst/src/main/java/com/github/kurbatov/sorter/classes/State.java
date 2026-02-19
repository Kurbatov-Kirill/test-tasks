package com.github.kurbatov.sorter.classes;

import java.util.ArrayList;
import java.util.List;

public class State {
    private List<Tube> currentState = new ArrayList<>();
    /*===========================================*/
    public State() {

    }

    public State(State other) {
        this.currentState = new ArrayList<>(other.currentState.size());

        for (Tube tube : other.currentState) {
            this.currentState.add(new Tube(tube));
        }
    }
    /*===========================================*/
    public List<Tube> getCurrentState() {
        return currentState;
    }
}
