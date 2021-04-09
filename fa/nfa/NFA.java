package fa.nfa;

import com.sun.deploy.util.StringUtils;
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
        DFA dfa = new DFA();

        Queue<Set<NFAState>> nfaSetQueue = new LinkedList<Set<NFAState>>(); // elements are each a set of nfa states which will correspond to a single dfa state
        Set<Set<NFAState>> visitedNFAStates = new HashSet<Set<NFAState>>(); // used to track which elements we have visited

        dfa.addStartState(startState.getDFAName());
        Set<NFAState> startSet = new LinkedHashSet<NFAState>();
        startSet.add(startState);
        nfaSetQueue.add(startSet);

        // do a bfs traversal of the NFA graph and create the appropriate dfa states and transitions as we go
        while (!nfaSetQueue.isEmpty()) {
            Set<NFAState> currentSet = new HashSet<NFAState>();
            currentSet = nfaSetQueue.remove();
            visitedNFAStates.add(currentSet);
            String currentSetName = getDFANameFromSet(currentSet); // this currentSet will be a state in our DFA

            // explore currentSet's children via alphabet - for each transition from currentState
            for (Character c : alphabet) {
                Set<NFAState> transitionStates = new HashSet<NFAState>();
                Set<NFAState> transitionStatesAndEpsilons = new HashSet<NFAState>();
                for (NFAState child : currentSet) {
                    transitionStates.addAll(child.getTo(c)); // this could return an empty set
                    transitionStatesAndEpsilons = getEpsilonDeltasFromSet(transitionStates); // this set will become the new DFA state

                    if (transitionStatesAndEpsilons.isEmpty()) {
                        // checks if child maps to an empty set
                        boolean childMapsToEmptySet = false;
                        boolean isVisitedStateEquivalent = false;
                        for (DFAState dfaS : dfa.getStates()) {
                            String dfaStateName = dfaS.getName();
                            String childDFAName = child.getDFAName();
                            if (dfaStateName.equals(childDFAName)) {
                                childMapsToEmptySet = true;
                            }
                        }

                        // handles case where child does map to empty set, but still don't want to enqueue (tc1, tc2) and ....tc3 where equivalent state exists...????
                        if (childMapsToEmptySet) {
                            // add an empty set to represent NFA states that don't have any transitions
                            String emptyStateName = "[]";
                            dfa.addState(emptyStateName);
                            dfa.addTransition(child.getDFAName(), c, emptyStateName);
                            dfa.addTransition(emptyStateName, c, emptyStateName);
                        }
                        // intentional fallthrough - if transitionStatesAndEpsilons is empty but child does not map to an empty set we want do continue
                    }
                }

                // this should only be done when we have transitions to explore, the case where we don't is handled in the foreach child loop above
                if (!transitionStatesAndEpsilons.isEmpty()) {
                    String newDFAStateName = getDFANameFromSet(transitionStatesAndEpsilons);
                    // newDFAStateName retains its value if an equivalent state does not exist in the dfa, else it is set to the name of the equivalent state
                    newDFAStateName = equivalentStateExists(newDFAStateName, dfa.getStates());


                    if (!visitedNFAStates.contains(transitionStatesAndEpsilons) && !nfaSetQueue.contains(transitionStatesAndEpsilons)) {
                        nfaSetQueue.add(transitionStatesAndEpsilons);
                    }

                    boolean isFinalSet = false;
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

    // TODO: this gets the subset not the set
    private boolean isSameState(String s1, String s2) {
        String[] sName1 = s1.substring(1,s1.length()-1).split(",");
        String[] sName2 = s2.substring(1,s2.length()-1).split(",");
        List<String> sName2List = Arrays.asList(sName2);
        for (String s : sName1) {
            // a single case where s is not present in sName2List means that the names are not equivalent
            if (!sName2List.contains(s)) {
                return false;
            }
        }
        return true;
    }

    /*
    * Returns true when the stateToCheck has an equivalent state already in the dfa
    * */
    private String equivalentStateExists(String checkName, Set<DFAState> dfaStates) {
        for (DFAState dfaState : dfaStates) {
            // states are equivalent when their sets are the same size and one is a subset of the other
            if (isSameState(checkName, dfaState.getName()) && checkName.length() == dfaState.getName().length()) {
                return dfaState.getName();
            }
        }
        return checkName;
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
        // TODO: Do we need this??
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
