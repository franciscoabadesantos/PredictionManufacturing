package Libraries;

import jade.core.Agent;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ricardo Silva Peres <ricardo.peres@uninova.pt>
 */
public class TestLibrary implements IResource {

    private Agent myAgent;

    @Override
    public void init(Agent myAgent) {
        this.myAgent = myAgent;
        System.out.println("Test library has been successfully initialized for agent: " + myAgent.getLocalName());
    }

    @Override
    public boolean executeSkill(String skillID) {
        try {
            switch (skillID) {
                case Utilities.Constants.SK_GLUE_TYPE_A: {
                    Thread.sleep(2000);
                    return true;
                }
                case Utilities.Constants.SK_GLUE_TYPE_B: {
                    Thread.sleep(3000);
                    return true;
                }
                case Utilities.Constants.SK_GLUE_TYPE_C: {
                    Thread.sleep(4000);
                    return true;
                }                
                case Utilities.Constants.SK_PICK_UP:
                    Thread.sleep(1000);
                    return true;
                case Utilities.Constants.SK_DROP:
                    Thread.sleep(1000);
                    return true;
                case Utilities.Constants.SK_QUALITY_CHECK:
                    Thread.sleep(2000);
                    return true;
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(TestLibrary.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public String[] getSkills() {
        String[] skills;
        switch (myAgent.getLocalName()) {
            case "GlueStation1":
                skills = new String[2];
                skills[0] = Utilities.Constants.SK_GLUE_TYPE_A;
                skills[1] = Utilities.Constants.SK_GLUE_TYPE_B;
                return skills;
            case "GlueStation2":
                skills = new String[2];
                skills[0] = Utilities.Constants.SK_GLUE_TYPE_A;
                skills[1] = Utilities.Constants.SK_GLUE_TYPE_C;
                return skills;
            case "GlueStation3":
                skills = new String[2];
                skills[0] = Utilities.Constants.SK_GLUE_TYPE_B;
                skills[1] = Utilities.Constants.SK_GLUE_TYPE_C;
                return skills;
            case "GlueStation4":
                skills = new String[3];
                skills[0] = Utilities.Constants.SK_GLUE_TYPE_A;
                skills[1] = Utilities.Constants.SK_GLUE_TYPE_B;
                skills[2] = Utilities.Constants.SK_GLUE_TYPE_C;
                return skills;
            case "Operator":
                skills = new String[2];
                skills[0] = Utilities.Constants.SK_PICK_UP;
                skills[1] = Utilities.Constants.SK_DROP;
                return skills;
        }
        return null;
    }
}
