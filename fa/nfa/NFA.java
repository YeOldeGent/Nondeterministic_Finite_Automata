package fa.nfa;

import fa.State;
import fa.dfa.DFA;
import fa.dfa.DFAState;
import sun.misc.Queue;

import java.util.LinkedHashSet;
import java.util.Set;

public class NFA implements NFAInterface {

    //set of states should be modeled by a class implementing the java.util.Set interface
    private LinkedHashSet<NFAState> nfaStates;
    private LinkedHashSet<NFAState> finalStates;
    private LinkedHashSet<Character> alphabet;
    private NFAState startState;
    private final char epsilon = 'e';

    public NFA() {
        nfaStates = new LinkedHashSet<NFAState>();
        finalStates = new LinkedHashSet<NFAState>();
        alphabet = new LinkedHashSet<Character>();
        startState = null;
    }

    @Override
    public DFA getDFA() {

        // this will be hard supposedly
        //NFAState r = checkIfExists("r");
        //Set<NFAState> eClosureStates = this.eClosure(startState);

        // Requires traversing NFA. To do this you must implement the breadth-first search (BFS)
        //algorithm ( a loop iterating over a queue; an element of a queue is a set of NFA states).

        // by hand steps
        // 1) Create a transition table and epsilon closure for each state
        // 2) Take power set of our states to get all possible DFA states
        // 3) If any state is present in the 1st transition, include it as a power set transition

        // final DFA has no e transitions


        // make a dfa object
        // add start state - should be the starting state of nfa
        // add final states - this will be any dfa state whose name contains a nfa final state, check name
        // add other states - will be combos of nfa states - all original nfa states will map to a set
        // ^^ will require adding new states
        // add transitions from

        // we will need to check the new state we create and add transitions for them too

        DFA dfa = new DFA();
        dfa.addStartState(startState.getName());
        Queue<NFAState> stateQueue = new Queue<NFAState>();

        // add all NFA states to a queue
//        for (NFAState currentNFAState : nfaStates) {
//            stateQueue.enqueue(currentNFAState);
//        }
        stateQueue.enqueue(startState);


        // do a bfs traversal of the NFA graph and create the appropriate dfa states and transitions as we go
        // assumption is that startState is first in
        while (!stateQueue.isEmpty()) {
            NFAState currentState = null;
            try {currentState = stateQueue.dequeue(); } catch (InterruptedException e) { e.printStackTrace(); }
            // option 1: cycle abc  // option 2:  expose all chars at once???
            for (Character c : alphabet) {
                Set<NFAState> transitionStates = currentState.getTo(c);
                Set<NFAState> transitionStatesAndEpsilons = getEpsilonDeltasFromSet(transitionStates); // this set will become the new DFA state
                String newDFAStateName = getDFANameFromSet(transitionStatesAndEpsilons);

                // check if this state is a final state
                if (dfaStateIsFinalState(newDFAStateName)) {
                    addFinalState(newDFAStateName); // will add it to DFA states
                }
                // TODO: error from empty set, we likely need to ignore empty sets and not add them to the dfa
                dfa.addState(newDFAStateName);

                // transition
                dfa.addTransition(currentState.getName(), c, newDFAStateName);
            }
        }

        return dfa;
    }

    /** Checks if DFAState should be a final state based on our NFA Final States*/
    private boolean dfaStateIsFinalState(String dfaName) {
        for (NFAState nfaS : finalStates) {
            if (dfaName.contains(nfaS.getName())) {
                return true;
            }
        }
        return false;
    }


    private String getDFANameFromSet(Set<NFAState> set) {
        String newDFAStateName = "{";
        for (NFAState s : set) {
            newDFAStateName += s.getName() + ",";
        }
        newDFAStateName = newDFAStateName.substring(0,newDFAStateName.length()-2); // remove the extra comma
        newDFAStateName += "}";

        return newDFAStateName;
    }

    /** returns the set of states accessible from the given set of states
     * via epsilon transitions
     * @param nfaStateSet - the set of states to check for additional state which may be reachable via e transitions
     * @return reachableStates - the set of states that are reachable via e transitions
     */
    private Set<NFAState> getEpsilonDeltasFromSet(Set<NFAState> nfaStateSet) {
        Set<NFAState> reachableStates = nfaStateSet;
        for (NFAState state : nfaStateSet) {
            Set<NFAState> eStates = this.eClosure(state);
            // TODO: this that states are returning
            for (NFAState reachedState : eStates) {
                if (!reachableStates.contains(reachedState)) {
                    reachableStates.add(reachedState);
                }
            }
        }
        return reachableStates;
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



        return eClosure(s,new LinkedHashSet<>());
    }

    private Set<NFAState> eClosure(NFAState currentState, Set<NFAState> visited) {

        visited.add(currentState); // we are visiting current state
        Set<NFAState> epsilonStates = currentState.getEpsilonStates(); // gets all the states reachable by an epsilon transition

        // base case
        // no epsilon transitions lead us to unvisited states
        if (epsilonStates == null) {
            return visited; // return empty set when currentState has no e transitions
        }

        //general case
        // current state and explore its one of its epsilons
        for (NFAState eState : epsilonStates) {
            if (!visited.contains(eState)) {
                visited = eClosure(eState, visited);
            }
        }

        // ending condition: all nodes were explored
        return visited; // we think we will want to return the nodes which we visited
    }

    @Override
    public void addStartState(String name) {
       startState = addStateHelper(name);
    }

    @Override
    public void addState(String name) {
        addStateHelper(name);
    }

    /** Helper to add the state object  */
    private NFAState addStateHelper(String name) {
        NFAState s = checkIfExists(name);
        if(s == null){
            s = new NFAState(name);
            nfaStates.add(s);
        } else {
            System.out.println("WARNING: A state with name " + name + " already exists in the DFA");
        }

        return s;
    }

    @Override
    public void addFinalState(String name) {
        NFAState finalState = addStateHelper(name);
        finalState.setFinal(); // TODO: Check to make sure this modifies the state that was previously added by the addStateHelper
        finalStates.add(finalState);
    }

    @Override
    public void addTransition(String fromState, char onSymb, String toState) {
        if (!alphabet.contains(onSymb) && onSymb != epsilon) { alphabet.add(onSymb); }
        NFAState originState  = checkIfExists(fromState);
        NFAState destinationState = checkIfExists(toState);
        if (originState == null || destinationState == null) {
            System.out.println("The fromState or toState does not exist in the NFA");
            System.exit(-1);
        }

        originState.addTransition(onSymb, destinationState);
    }

    @Override
    public Set<? extends State> getStates() {
        return nfaStates;
    }

    @Override
    public Set<? extends State> getFinalStates() {
        return finalStates;
    }

    @Override
    public State getStartState() {
        return startState;
    }

    @Override
    public Set<Character> getABC() {
        return alphabet;
    }

    private NFAState checkIfExists(String name){
        NFAState ret = null;
        for(NFAState s : nfaStates){
            if(s.getName().equals(name)){
                ret = s;
                break;
            }
        }
        return ret;
    }
}
