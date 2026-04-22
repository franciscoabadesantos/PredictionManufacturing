package Libraries;

import jade.core.Agent;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ricardo Silva Peres <ricardo.peres@uninova.pt>
 */
public class TestTransportLibrary implements ITransport {

    private Agent myAgent;

    @Override
    public void init(Agent a) {
        this.myAgent = a;
        System.out.println("Test library has been successfully initialized for agent: " + this.myAgent.getLocalName());
    }
    

    @Override
    public boolean executeMove(String origin, String destination, String productID) {
        System.out.println("Performing transportation from " + origin + " to " + destination + ".");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            Logger.getLogger(TestTransportLibrary.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    @Override
    public String[] getSkills() {
        String[] skills = new String[1];
        skills[0] = Utilities.Constants.SK_MOVE;
        return skills;
    }

}
