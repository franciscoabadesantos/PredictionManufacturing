package Resource;

import jade.core.Agent;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import Libraries.IResource;
import Utilities.DFInteraction;
import jade.domain.FIPAException;

import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import jade.proto.AchieveREResponder;




/**
 *
 * @author Ricardo Silva Peres <ricardo.peres@uninova.pt>
 */
public class ResourceAgent extends Agent {

    String id;
    IResource myLib;
    String description;
    String[] associatedSkills;
    String location;
    boolean available = true;
    boolean operating = true;


    @Override
    protected void setup() {
        Object[] args = this.getArguments();
        this.id = (String) args[0];
        this.description = (String) args[1];

        //Load hw lib
        try {
            String className = "Libraries." + (String) args[2];
            Class cls = Class.forName(className);
            Object instance;
            instance = cls.newInstance();
            myLib = (IResource) instance;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(ResourceAgent.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.location = (String) args[3];

        myLib.init(this);
        this.associatedSkills = myLib.getSkills();
        System.out.println("Resource Deployed: " + this.id + " Executes: " + Arrays.toString(associatedSkills));

        // -> TO DO: Register in DF with the corresponding skills as services
        try{
            DFInteraction.RegisterInDF(this, this.id, this.associatedSkills);
        }
        catch(FIPAException ex){
            Logger.getLogger(ResourceAgent.class.getName()).log(Level.SEVERE, null, ex);
        }

        
        // -> TO DO: Add responder behaviour/s
        this.addBehaviour(new responder(this,MessageTemplate.MatchPerformative(ACLMessage.CFP)));
        this.addBehaviour(new RequestResponder(this,MessageTemplate.MatchPerformative(ACLMessage.REQUEST)));

    }

    @Override
    protected void takeDown() {
        super.takeDown(); 
    }
    

    private class responder extends ContractNetResponder{

        public responder(Agent a, MessageTemplate mt){
            super(a, mt);
        }


        @Override
        protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, FailureException, NotUnderstoodException {
            System.out.println(myAgent.getLocalName() + ": Processing CFP message"); 
          //  block(5000);

            ACLMessage reply = cfp.createReply(); 
            if (IsAvailable()) {
            System.out.println(myAgent.getLocalName() + ": Processing propose message"); 

                reply.setPerformative(ACLMessage.PROPOSE);
                reply.setContent("accept"); // Replace this with actual proposal content (e.g., price or time)
            } else {
           System.out.println(myAgent.getLocalName() + ": Processing refuse message"); 

                reply.setPerformative(ACLMessage.REFUSE);
                reply.setContent("refusal");
            }

            return reply;
        }



        @Override
        protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
            System.out.println(myAgent.getLocalName()+": Preparing result of CFP");
          //  block(5000);

            // Enviar INFORM ao ProductAgent com mensagem do nome da localização destino - neste caso ID
            ACLMessage inform = cfp.createReply();
            inform.setPerformative(ACLMessage.INFORM);
            inform.setContent(location);
            setAvailable(false);


            return inform;
        }

    }
    private class RequestResponder extends AchieveREResponder {

        public RequestResponder(Agent a, MessageTemplate mt) {
            super(a, mt);
        }

        @Override
        protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException {
            System.out.println(myAgent.getLocalName()+ ": Processing Request message.");
            ACLMessage Agree_reply = request.createReply();
            
            if (IsOperational()){
                System.out.println( "Request accept");
                Agree_reply.setPerformative(ACLMessage.AGREE);
                Agree_reply.setContent(id);

            }
            else{
                System.out.println( "Request Decline");
                Agree_reply.setPerformative(ACLMessage.REFUSE);
                Agree_reply.setContent(id);
            }
            return Agree_reply;
        }
            
            
        @Override
        protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
            System.out.println(myAgent.getLocalName() + ": Processing REQUEST message: " + request.getContent());

            String skill = request.getContent();
            System.out.println("skill: "+ skill);
            //StartTask();
            myLib.executeSkill(skill);
            //block(1000);

            // make a repay
            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            reply.setContent(id);
            setAvailable(true);
            return reply;
            }  
        }
                
        
        // Logica para  ver se agente está disponivel para realizar uma das suas taks
        public boolean IsAvailable(){
            return ResourceAgent.this.available;
        }
        public void setAvailable(boolean status){
            ResourceAgent.this.available=status;

        }
        
        public boolean IsOperational(){
            return operating;
        }
        public void setOperating(boolean status){
             operating = status;

        }
} 
