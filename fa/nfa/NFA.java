package fa.nfa;

import fa.State;
import fa.dfa.DFA;
import fa.dfa.DFAState;
//import sun.misc.Queue;
//import java.util.PriorityQueue;
import java.util.*;

/**
 * Questions:
 * just look at
 */

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



        ///----------------------------------New PLan--------------------------------------
        // key: a DFA is a set of NFA states

        // queue of sets of NFAStates
        // Queue<Set<NFAState>> nfaSetQueue = new LinkedList<Set<NFAState>>();

        // 1st set of NFAStates will jsut be startState
        // make a new set with jsut the starState
        // enqueue that

        // dequeue startState {a} and mark visited
        // look at its its transitions
        //{a} -0->{a}     {a} -1->{a,b}
        // here we need to add a new set of nFAState to queue {a,b}
        // enqueue {a,b}

        // dequeue {a,b}     - since a and b are NFAState type we have a's transitions and b's transitions
        // process transitions
        // {a,b} --0-> {a}              {a,b} --1-> {a,b}           ?? how do we represent these transitions - in dfa states


//-----------------------------------------------------------------------------------------------
        DFA dfa = new DFA();

        Queue<Set<NFAState>> nfaSetQueue = new LinkedList<Set<NFAState>>(); // elements are each a set of nfa states which will correspond to a single dfa state
        Set<Set<NFAState>> visitedNFAStates = new LinkedHashSet<Set<NFAState>>(); // used to track which elements we have visited

        dfa.addStartState(startState.getDFAName());
        Set<NFAState> startSet = new LinkedHashSet<NFAState>();
        startSet.add(startState);
        nfaSetQueue.add(startSet);
        //visitedNFAStates.add(startSet);

        // do a bfs traversal of the NFA graph and create the appropriate dfa states and transitions as we go
        // assumption is that startState is first in
        while (!nfaSetQueue.isEmpty()) {
            Set<NFAState> currentSet = null;
            currentSet = nfaSetQueue.remove();
            visitedNFAStates.add(currentSet);
            String currentSetName = getDFANameFromSet(currentSet); // this currentSet will be a state in our DFA
            // if any NFAState is a final state this set of NFAStates (aka is our DFAState) should be treated as a final state
            // TODO: might be able to remove this
            boolean isFinalSet = false;
            for (NFAState s : currentSet) {
                if (s.isFinal()) {
                    dfa.addFinalState(currentSetName);
                    isFinalSet = true;
                    break;
                }
            }

            if (!isFinalSet) {
                dfa.addState(currentSetName);
            }

            // explore currentSet's children via alphabet - for each transition from currentState
            for (NFAState child : currentSet) {
                for (Character c : alphabet) {
                    Set<NFAState> transitionStates = child.getTo(c); // this could return an empty set
                    Set<NFAState> transitionStatesAndEpsilons = getEpsilonDeltasFromSet(transitionStates); // this set will become the new DFA state
                    if (transitionStatesAndEpsilons.isEmpty()) {continue;}
                    String newDFAStateName = getDFANameFromSet(transitionStatesAndEpsilons);

                    if (!visitedNFAStates.contains(transitionStatesAndEpsilons)) {
                        nfaSetQueue.add(transitionStatesAndEpsilons);
                    }

                    isFinalSet = false;
                    for (NFAState s : transitionStatesAndEpsilons) {
                        if (s.isFinal()) {
                            dfa.addFinalState(newDFAStateName);
                            isFinalSet = true;
                            break;
                        }
                    }

                    if (!isFinalSet) {
                        dfa.addState(newDFAStateName);
                    }

                    dfa.addTransition(currentSetName, c, newDFAStateName);
                }
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
        StringBuilder newDFAStateName = new StringBuilder("[");
        for (NFAState s : set) {
            newDFAStateName.append(s.getName()).append(",");
        }
        if (newDFAStateName.length() > 1) {
            newDFAStateName = new StringBuilder(newDFAStateName.substring(0, newDFAStateName.length() - 1)); // remove the extra comma
        }
        newDFAStateName.append("]");

        return newDFAStateName.toString();
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
            // TODO: check that states are returning the propper states. ie that
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
