package Libraries;

import jade.core.Agent;

/**
 *
 * @author Ricardo Silva Peres <ricardo.peres@uninova.pt>
 */
public interface IResource {
    public void init(Agent myAgent); 
    public String[] getSkills();
    public boolean executeSkill(String skillID);
}
