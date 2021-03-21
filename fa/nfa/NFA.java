package fa.nfa;

import fa.State;
import fa.dfa.DFA;

import java.util.Set;

public class NFA implements NFAInterface {

    //set of states should be modeled by a class implementing the java.util.Set interface
    @Override
    public DFA getDFA() {

        // this will be hard supposedly

        // Requires traversing NFA. To do this you must implement the breadth-first search (BFS)
        //algorithm ( a loop iterating over a queue; an element of a queue is a set of NFA states).

        return null;
    }

    @Override
    public Set<NFAState> getToState(NFAState from, char onSymb) {
        return null;
    }

    @Override
    public Set<NFAState> eClosure(NFAState s) {

        // TODO: Do this before getDFA()
//        computes the set of NFA states that can be reached from the argument state s by going
//        only along ε transitions, including s itself. You must implement it using the depth-first
//        search algorithm (DFS) using a recursion, i.e., eClosure should invoke itself or another
//        helper method, e.g., private Set<NFAState> eClosure(NFAState s, Set<NFASate> visited)
//        that invokes itself

//
//        A------B ----- D
//                \
//                 \
//                  C

        // base case
        // no epsilon transitions lead us to unvisited states
        // return an empty set

        //general case
        // current state and explore its one of its epsilons



        return null;
    }

    private Set<NFAState> eClosure(NFAState s, Set<NFAState> visited) {

    }

    @Override
    public void addStartState(String name) {

    }

    @Override
    public void addState(String name) {

    }

    @Override
    public void addFinalState(String name) {

    }

    @Override
    public void addTransition(String fromState, char onSymb, String toState) {

    }

    @Override
    public Set<? extends State> getStates() {
        return null;
    }

    @Override
    public Set<? extends State> getFinalStates() {
        return null;
    }

    @Override
    public State getStartState() {
        return null;
    }

    @Override
    public Set<Character> getABC() {
        return null;
    }
}
